package installer

import (
	"encoding/json"
	"errors"
	"os"
	"path/filepath"
	"time"
)

type Config struct {
	InstallDir  string   `json:"install_dir"`
	BindIP      string   `json:"bind_ip"`
	OnlineMode  bool     `json:"online_mode"`
	Servers     []string `json:"selected_servers"`
	Services    []string `json:"selected_services"`
	ExposePorts bool     `json:"expose_ports"`
	APIPort     string   `json:"api_port"`
	Reinstall   bool     `json:"-"`
	SourceRepo  string   `json:"-"`
	NoStart     bool     `json:"-"`
}

type State struct {
	Version         string    `json:"version"`
	InstallDir      string    `json:"install_dir"`
	BindIP          string    `json:"bind_ip"`
	OnlineMode      bool      `json:"online_mode"`
	Servers         []string  `json:"selected_servers"`
	Services        []string  `json:"selected_services"`
	ExposePorts     bool      `json:"expose_ports"`
	APIPort         string    `json:"api_port"`
	LastReleaseID   string    `json:"last_release_id"`
	LastUpdateCheck string    `json:"last_update_check"`
	InstalledAt     time.Time `json:"installed_at"`
}

func DefaultInstallDir() string {
	home, err := os.UserHomeDir()
	if err != nil || home == "" {
		return "./hypixel-skyblock"
	}
	return filepath.Join(home, DefaultInstallSub)
}

func DefaultConfig() Config {
	return Config{
		InstallDir:  DefaultInstallDir(),
		BindIP:      "0.0.0.0",
		OnlineMode:  true,
		Servers:     append([]string{}, append(RequiredServers, "SKYBLOCK_HUB", "SKYBLOCK_ISLAND")...),
		Services:    append([]string{}, DefaultServices...),
		ExposePorts: false,
		APIPort:     "8080",
	}
}

func StatePath(dir string) string {
	return filepath.Join(dir, StateFileName)
}

func LoadState(dir string) (State, error) {
	data, err := os.ReadFile(StatePath(dir))
	if err != nil {
		return State{}, err
	}
	var state State
	if err := json.Unmarshal(data, &state); err != nil {
		return State{}, err
	}
	if state.InstallDir == "" {
		state.InstallDir = dir
	}
	return state, nil
}

func ExistingInstall(dir string) (State, bool) {
	state, err := LoadState(dir)
	return state, err == nil
}

func SaveState(cfg Config) error {
	state := State{
		Version:     Version,
		InstallDir:  cfg.InstallDir,
		BindIP:      cfg.BindIP,
		OnlineMode:  cfg.OnlineMode,
		Servers:     Deduplicate(append([]string{}, cfg.Servers...)),
		Services:    Deduplicate(append([]string{}, cfg.Services...)),
		ExposePorts: cfg.ExposePorts,
		APIPort:     cfg.APIPort,
		InstalledAt: time.Now(),
	}
	data, err := json.MarshalIndent(state, "", "  ")
	if err != nil {
		return err
	}
	return os.WriteFile(StatePath(cfg.InstallDir), append(data, '\n'), 0o644)
}

func StateToConfig(state State) Config {
	return Config{
		InstallDir:  state.InstallDir,
		BindIP:      state.BindIP,
		OnlineMode:  state.OnlineMode,
		Servers:     append([]string{}, state.Servers...),
		Services:    append([]string{}, state.Services...),
		ExposePorts: state.ExposePorts,
		APIPort:     state.APIPort,
	}
}

func RequireState(dir string) (State, error) {
	state, err := LoadState(dir)
	if errors.Is(err, os.ErrNotExist) {
		return State{}, errors.New("no installation state found")
	}
	return state, err
}

func Deduplicate(values []string) []string {
	seen := make(map[string]bool, len(values))
	out := make([]string, 0, len(values))
	for _, value := range values {
		if value == "" || seen[value] {
			continue
		}
		seen[value] = true
		out = append(out, value)
	}
	return out
}
