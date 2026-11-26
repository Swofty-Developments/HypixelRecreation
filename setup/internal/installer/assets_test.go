package installer

import (
	"archive/zip"
	"context"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"testing"
)

func assetsZip(t *testing.T, version string) []byte {
	t.Helper()
	path := filepath.Join(t.TempDir(), "assets.zip")
	file, err := os.Create(path)
	if err != nil {
		t.Fatal(err)
	}
	writer := zip.NewWriter(file)
	entries := map[string]string{
		"limbo.schem":                            "schem",
		"world/hypixel_prototype_lobby.polar":    "world",
		"resourcepacks/skyblockpack/pack.mcmeta": "skyblock " + version,
		"resourcepacks/ravengard/pack.mcmeta":    "ravengard " + version,
	}
	if version != "" {
		entries[worldAssetsVersionFile] = version + "\n"
	}
	for name, body := range entries {
		entry, err := writer.Create(name)
		if err != nil {
			t.Fatal(err)
		}
		if _, err := entry.Write([]byte(body)); err != nil {
			t.Fatal(err)
		}
	}
	if err := writer.Close(); err != nil {
		t.Fatal(err)
	}
	if err := file.Close(); err != nil {
		t.Fatal(err)
	}
	data, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	return data
}

func serve(t *testing.T, body []byte) string {
	t.Helper()
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_, _ = w.Write(body)
	}))
	t.Cleanup(server.Close)
	return server.URL
}

func installOldAssets(t *testing.T, configDir string) {
	t.Helper()
	for name, body := range map[string]string{
		"limbo.schem":                            "schem",
		"world/hypixel_prototype_lobby.polar":    "world",
		"resourcepacks/skyblockpack/pack.mcmeta": "skyblock old",
		"resourcepacks/ravengard/pack.mcmeta":    "ravengard old",
		"resourcepacks/ravengard/stale.json":     "stale",
	} {
		path := filepath.Join(configDir, name)
		if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
			t.Fatal(err)
		}
		if err := os.WriteFile(path, []byte(body), 0o644); err != nil {
			t.Fatal(err)
		}
	}
}

func TestOutdatedAssetsAreReplaced(t *testing.T) {
	configDir := t.TempDir()
	installOldAssets(t, configDir)
	if LimboAssetsCurrent(configDir) {
		t.Fatal("assets without a version file must not count as current")
	}

	url := serve(t, assetsZip(t, WorldAssetsVersion))
	if err := downloadLimboAssetsFrom(context.Background(), url, configDir, nil); err != nil {
		t.Fatal(err)
	}
	if !LimboAssetsCurrent(configDir) {
		t.Fatal("assets should be current after the download")
	}
	if _, err := os.Stat(filepath.Join(configDir, "resourcepacks/ravengard/stale.json")); !os.IsNotExist(err) {
		t.Fatalf("stale pack file survived the update: %v", err)
	}
	data, err := os.ReadFile(filepath.Join(configDir, "resourcepacks/skyblockpack/pack.mcmeta"))
	if err != nil || string(data) != "skyblock "+WorldAssetsVersion {
		t.Fatalf("pack was not replaced: %q %v", data, err)
	}
}

func TestOutdatedZipIsNotExtracted(t *testing.T) {
	configDir := t.TempDir()
	installOldAssets(t, configDir)

	for _, version := range []string{"", "1"} {
		url := serve(t, assetsZip(t, version))
		if err := downloadLimboAssetsFrom(context.Background(), url, configDir, nil); err == nil {
			t.Fatalf("zip with version %q was accepted", version)
		}
	}
	data, err := os.ReadFile(filepath.Join(configDir, "resourcepacks/ravengard/stale.json"))
	if err != nil || string(data) != "stale" {
		t.Fatalf("existing assets were touched by a rejected zip: %q %v", data, err)
	}
}
