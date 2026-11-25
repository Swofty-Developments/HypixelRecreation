package installer

import (
	"archive/zip"
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"
)

// use this so you can replace the assets
const limboAssetsEnv = "SKYBLOCK_WORLD_ASSETS"

func DownloadLimboAssets(ctx context.Context, installDir string, events chan<- Event) error {
	configDir := filepath.Join(installDir, "configuration")
	if LimboAssetsPresent(configDir) {
		return nil
	}

	if localPath := strings.TrimSpace(os.Getenv(limboAssetsEnv)); localPath != "" {
		return importLocalLimboAssets(localPath, configDir)
	}

	var errs []string
	for _, url := range LimboAssetsURLs {
		err := downloadLimboAssetsFrom(ctx, url, configDir, events)
		if err == nil {
			return nil
		}
		emitAssetProgress(events, "Could not get world assets from %s: %v", url, err)
		errs = append(errs, fmt.Sprintf("%s: %v", url, err))
	}
	return fmt.Errorf("world assets download failed (%s)", strings.Join(errs, "; "))
}

func downloadLimboAssetsFrom(ctx context.Context, url, configDir string, events chan<- Event) error {
	reqCtx, cancel := context.WithTimeout(ctx, 15*time.Minute)
	defer cancel()

	req, err := http.NewRequestWithContext(reqCtx, http.MethodGet, url, nil)
	if err != nil {
		return err
	}
	req.Header.Set("User-Agent", "skyblock-installer/"+Version)
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode < 200 || resp.StatusCode > 299 {
		return fmt.Errorf("download returned %s", resp.Status)
	}
	emitAssetProgress(events, "Downloading world assets from %s", url)
	if resp.ContentLength > 0 {
		emitAssetProgress(events, "Remote asset size: %s", formatBytes(resp.ContentLength))
	} else {
		emitAssetProgress(events, "Remote asset size is unknown; showing downloaded bytes")
	}

	tmp, err := os.CreateTemp("", "skyblock-limbo-assets-*.zip")
	if err != nil {
		return err
	}
	tmpPath := tmp.Name()
	defer os.Remove(tmpPath)

	if err := copyWithProgress(reqCtx, cancel, tmp, resp.Body, resp.ContentLength, events); err != nil {
		_ = tmp.Close()
		return err
	}
	if err := tmp.Close(); err != nil {
		return err
	}

	emitAssetProgress(events, "Extracting world assets")
	if err := extractZip(tmpPath, configDir); err != nil {
		return err
	}
	if !LimboAssetsPresent(configDir) {
		return fmt.Errorf("downloaded zip did not contain limbo.schem, world/ and resourcepacks/")
	}
	return nil
}

func LimboAssetsPresent(configDir string) bool {
	if _, err := os.Stat(filepath.Join(configDir, "limbo.schem")); err != nil {
		return false
	}
	for _, dir := range []string{
		"world",
		filepath.Join("resourcepacks", "ravengard"),
		filepath.Join("resourcepacks", "skyblockpack"),
	} {
		if info, err := os.Stat(filepath.Join(configDir, dir)); err != nil || !info.IsDir() {
			return false
		}
	}
	return true
}

func LimboAssetsManualHint() string {
	return fmt.Sprintf("set %s to a local assets zip or an extracted folder containing limbo.schem, world/ and resourcepacks/", limboAssetsEnv)
}

func importLocalLimboAssets(path, configDir string) error {
	info, err := os.Stat(path)
	if err != nil {
		return err
	}
	if info.IsDir() {
		if err := CopyDir(path, configDir); err != nil {
			return err
		}
	} else if err := extractZip(path, configDir); err != nil {
		return err
	}
	if !LimboAssetsPresent(configDir) {
		return fmt.Errorf("local assets did not contain limbo.schem, world/ and resourcepacks/")
	}
	return nil
}

func copyWithProgress(ctx context.Context, cancel context.CancelFunc, dst io.Writer, src io.Reader, total int64, events chan<- Event) error {
	buf := make([]byte, 256*1024)
	var downloaded int64
	lastEmit := time.Now().Add(-time.Minute)
	progress := make(chan struct{}, 1)
	done := make(chan struct{})
	defer close(done)

	go func() {
		timer := time.NewTimer(30 * time.Second)
		defer timer.Stop()
		for {
			select {
			case <-timer.C:
				cancel()
				return
			case <-progress:
				if !timer.Stop() {
					select {
					case <-timer.C:
					default:
					}
				}
				timer.Reset(30 * time.Second)
			case <-done:
				return
			}
		}
	}()

	for {
		select {
		case <-ctx.Done():
			return fmt.Errorf("download stalled or timed out after %s: %w", formatBytes(downloaded), ctx.Err())
		default:
		}

		n, readErr := src.Read(buf)
		if n > 0 {
			written, writeErr := dst.Write(buf[:n])
			if writeErr != nil {
				return writeErr
			}
			if written != n {
				return io.ErrShortWrite
			}
			downloaded += int64(n)
			select {
			case progress <- struct{}{}:
			default:
			}
			if shouldEmitProgress(downloaded, total, lastEmit) {
				emitDownloadProgress(events, downloaded, total)
				lastEmit = time.Now()
			}
		}
		if readErr == io.EOF {
			if downloaded == 0 {
				return fmt.Errorf("download returned no data")
			}
			emitDownloadProgress(events, downloaded, total)
			return nil
		}
		if readErr != nil {
			return readErr
		}
	}
}

func shouldEmitProgress(downloaded, total int64, lastEmit time.Time) bool {
	if time.Since(lastEmit) >= 2*time.Second {
		return true
	}
	if total > 0 {
		percent := downloaded * 100 / total
		lastPercent := (downloaded - 256*1024) * 100 / total
		return percent != lastPercent
	}
	return downloaded%(1024*1024) < 256*1024
}

func emitDownloadProgress(events chan<- Event, downloaded, total int64) {
	if total > 0 {
		percent := int(downloaded * 100 / total)
		emitAssetProgress(events, "World assets %s %s/%s %d%%", progressBar(percent), formatBytes(downloaded), formatBytes(total), percent)
		return
	}
	emitAssetProgress(events, "World assets downloaded %s", formatBytes(downloaded))
}

func progressBar(percent int) string {
	const width = 20
	if percent < 0 {
		percent = 0
	}
	if percent > 100 {
		percent = 100
	}
	filled := percent * width / 100
	return "[" + strings.Repeat("#", filled) + strings.Repeat("-", width-filled) + "]"
}

func formatBytes(bytes int64) string {
	const mb = 1024 * 1024
	if bytes >= mb {
		return fmt.Sprintf("%.1f MB", float64(bytes)/mb)
	}
	return fmt.Sprintf("%.0f KB", float64(bytes)/1024)
}

func emitAssetProgress(events chan<- Event, format string, args ...any) {
	if events != nil {
		events <- Event{Line: fmt.Sprintf(format, args...)}
	}
}

func extractZip(zipPath, dst string) error {
	reader, err := zip.OpenReader(zipPath)
	if err != nil {
		return err
	}
	defer reader.Close()

	cleanDst, err := filepath.Abs(dst)
	if err != nil {
		return err
	}
	if err := os.MkdirAll(cleanDst, 0o755); err != nil {
		return err
	}

	for _, file := range reader.File {
		target, err := safeZipTarget(cleanDst, file.Name)
		if err != nil {
			return err
		}
		if file.FileInfo().IsDir() {
			if err := os.MkdirAll(target, file.Mode()); err != nil {
				return err
			}
			continue
		}
		if err := extractZipFile(file, target); err != nil {
			return err
		}
	}
	return nil
}

func safeZipTarget(dst, name string) (string, error) {
	cleanName := filepath.Clean(strings.ReplaceAll(name, "\\", "/"))
	if cleanName == "." || strings.HasPrefix(cleanName, "../") || filepath.IsAbs(cleanName) {
		return "", fmt.Errorf("unsafe zip entry path: %s", name)
	}

	target := filepath.Join(dst, cleanName)
	cleanTarget, err := filepath.Abs(target)
	if err != nil {
		return "", err
	}
	if cleanTarget != dst && !strings.HasPrefix(cleanTarget, dst+string(os.PathSeparator)) {
		return "", fmt.Errorf("unsafe zip entry path: %s", name)
	}
	return cleanTarget, nil
}

func extractZipFile(file *zip.File, target string) error {
	if err := os.MkdirAll(filepath.Dir(target), 0o755); err != nil {
		return err
	}

	in, err := file.Open()
	if err != nil {
		return err
	}
	defer in.Close()

	out, err := os.OpenFile(target, os.O_CREATE|os.O_TRUNC|os.O_WRONLY, file.Mode())
	if err != nil {
		return err
	}
	defer out.Close()

	_, err = io.Copy(out, in)
	return err
}
