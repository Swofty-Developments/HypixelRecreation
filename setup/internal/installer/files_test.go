package installer

import (
	"os"
	"path/filepath"
	"testing"
)

func TestPrepareFilesKeepsEditedConfig(t *testing.T) {
	cfg := Config{InstallDir: t.TempDir()}
	if err := PrepareFiles(cfg); err != nil {
		t.Fatal(err)
	}
	configPath := filepath.Join(cfg.InstallDir, "configuration", "config.yml")
	edited := []byte("integrations:\n    via-version: true\n")
	if err := os.WriteFile(configPath, edited, 0o644); err != nil {
		t.Fatal(err)
	}
	if err := PrepareFiles(cfg); err != nil {
		t.Fatal(err)
	}
	data, err := os.ReadFile(configPath)
	if err != nil || string(data) != string(edited) {
		t.Fatalf("config.yml was overwritten: %q %v", data, err)
	}
	if _, err := os.Stat(filepath.Join(cfg.InstallDir, "configuration", "config.example.yml")); err != nil {
		t.Fatal(err)
	}
}
