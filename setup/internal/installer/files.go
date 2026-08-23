package installer

import (
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"
)

func PrepareFiles(cfg Config) error {
	if cfg.InstallDir == "" {
		return fmt.Errorf("install directory is required")
	}
	if err := os.MkdirAll(filepath.Join(cfg.InstallDir, "configuration", "skyblock"), 0o755); err != nil {
		return err
	}
	if err := os.MkdirAll(filepath.Join(cfg.InstallDir, "DockerFiles"), 0o755); err != nil {
		return err
	}
	if err := EnsureForwardingSecret(cfg.InstallDir); err != nil {
		return err
	}
	if err := writeConfigYAML(cfg); err != nil {
		return err
	}
	if err := writeVelocityTOML(cfg); err != nil {
		return err
	}
	if err := writeCompose(cfg); err != nil {
		return err
	}
	return SaveState(cfg)
}

func EnsureForwardingSecret(dir string) error {
	envFile := filepath.Join(dir, ".env")
	secretFile := filepath.Join(dir, "configuration", "forwarding.secret")

	secret, err := readExistingSecret(envFile, secretFile)
	if err != nil {
		return err
	}
	if secret == "" {
		secret, err = randomHex(24)
		if err != nil {
			return err
		}
	}

	var preserved []string
	if data, err := os.ReadFile(envFile); err == nil {
		for _, line := range strings.Split(string(data), "\n") {
			if line != "" && !strings.HasPrefix(line, "FORWARDING_SECRET=") {
				preserved = append(preserved, line)
			}
		}
	}
	preserved = append(preserved, "FORWARDING_SECRET="+secret)
	if err := os.WriteFile(envFile, []byte(strings.Join(preserved, "\n")+"\n"), 0o600); err != nil {
		return err
	}
	return os.WriteFile(secretFile, []byte(secret), 0o600)
}

func readExistingSecret(envFile, secretFile string) (string, error) {
	if data, err := os.ReadFile(envFile); err == nil {
		for _, line := range strings.Split(string(data), "\n") {
			if strings.HasPrefix(line, "FORWARDING_SECRET=") {
				return strings.TrimPrefix(line, "FORWARDING_SECRET="), nil
			}
		}
	} else if !os.IsNotExist(err) {
		return "", err
	}

	if data, err := os.ReadFile(secretFile); err == nil {
		return strings.TrimSpace(string(data)), nil
	} else if !os.IsNotExist(err) {
		return "", err
	}
	return "", nil
}

func randomHex(bytes int) (string, error) {
	buf := make([]byte, bytes)
	if _, err := rand.Read(buf); err != nil {
		return "", err
	}
	return hex.EncodeToString(buf), nil
}

func CopyDir(src, dst string) error {
	return filepath.WalkDir(src, func(path string, entry os.DirEntry, walkErr error) error {
		if walkErr != nil {
			return walkErr
		}
		rel, err := filepath.Rel(src, path)
		if err != nil {
			return err
		}
		if rel == "." {
			return nil
		}
		target := filepath.Join(dst, rel)
		info, err := entry.Info()
		if err != nil {
			return err
		}
		if entry.IsDir() {
			return os.MkdirAll(target, info.Mode())
		}
		return copyFile(path, target, info.Mode())
	})
}

func copyFile(src, dst string, mode os.FileMode) error {
	if err := os.MkdirAll(filepath.Dir(dst), 0o755); err != nil {
		return err
	}
	in, err := os.Open(src)
	if err != nil {
		return err
	}
	defer func(in *os.File) {
		err := in.Close()
		if err != nil {
			fmt.Println(err)
		}
	}(in)

	out, err := os.OpenFile(dst, os.O_CREATE|os.O_TRUNC|os.O_WRONLY, mode)
	if err != nil {
		return err
	}
	defer func(out *os.File) {
		err := out.Close()
		if err != nil {
			fmt.Println(err)
		}
	}(out)

	_, err = io.Copy(out, in)
	return err
}

func writeConfigYAML(cfg Config) error {
	body := `host-name: 0.0.0.0
transfer-timeout: 800
mongodb: mongodb://mongodb
redis-uri: redis://redis:6379
velocity-secret: placeholder
require-auth: false
integrations:
    spark: false
    anticheat: false
    via-version: false
    sentry-dsn: ''
limbo:
    host-name: pico_limbo
    port: 65535
resource-pack:
    public-url: ''
    pack-hash: ''
resource-packs:
    skyblockpack:
        server-url: http://127.0.0.1:7270
    ravengard:
        server-url: http://127.0.0.1:7270
`
	configPath := filepath.Join(cfg.InstallDir, "configuration", "config.yml")
	if err := os.WriteFile(configPath, []byte(body), 0o644); err != nil {
		return err
	}
	return os.WriteFile(filepath.Join(cfg.InstallDir, "configuration", "config.example.yml"), []byte(body), 0o644)
}

func writeVelocityTOML(cfg Config) error {
	path := filepath.Join(cfg.InstallDir, "configuration", "velocity.toml")
	data, err := os.ReadFile(path)
	if err != nil {
		if os.IsNotExist(err) {
			return nil
		}
		return err
	}
	lines := strings.Split(string(data), "\n")
	for i, line := range lines {
		if strings.HasPrefix(line, "bind = ") {
			lines[i] = fmt.Sprintf("bind = %q", cfg.BindIP+":25565")
		}
		if strings.HasPrefix(line, "online-mode = ") {
			lines[i] = fmt.Sprintf("online-mode = %t", cfg.OnlineMode)
		}
	}
	return os.WriteFile(path, []byte(strings.Join(lines, "\n")), 0o644)
}

func writeCompose(cfg Config) error {
	body := ComposeYAML(cfg)
	return os.WriteFile(filepath.Join(cfg.InstallDir, "docker-compose.yml"), []byte(body), 0o644)
}
