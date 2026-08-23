package installer

import (
	"context"
	"fmt"
	"io"
	"path/filepath"
	"strings"
)

type HeadlessOptions struct {
	InstallDir  string
	SourceRepo  string
	Servers     string
	Services    string
	BindIP      string
	APIPort     string
	OnlineMode  bool
	ExposePorts bool
	Reinstall   bool
	NoStart     bool
}

func RunHeadless(ctx context.Context, opts HeadlessOptions, out io.Writer) error {
	cfg, err := opts.Config()
	if err != nil {
		return err
	}
	if err := CheckDependencies(ctx); err != nil {
		return err
	}

	fmt.Fprintf(out, "Installing into %s\n", cfg.InstallDir)
	fmt.Fprintf(out, "Servers: %s\n", strings.Join(cfg.Servers, " "))
	fmt.Fprintf(out, "Services: %s\n", strings.Join(cfg.Services, " "))

	runner := NewRunner(cfg.InstallDir)
	go runner.Install(ctx, cfg, "")

	var runErr error
	for event := range runner.Events {
		if event.Line != "" {
			fmt.Fprintln(out, event.Line)
		}
		if event.Err != nil {
			runErr = event.Err
		}
	}
	return runErr
}

func (o HeadlessOptions) Config() (Config, error) {
	cfg := DefaultConfig()
	if o.InstallDir != "" {
		cfg.InstallDir = o.InstallDir
	}
	if o.BindIP != "" {
		cfg.BindIP = o.BindIP
	}
	if o.APIPort != "" {
		cfg.APIPort = o.APIPort
	}
	cfg.OnlineMode = o.OnlineMode
	cfg.ExposePorts = o.ExposePorts
	cfg.Reinstall = o.Reinstall
	cfg.NoStart = o.NoStart

	if o.SourceRepo != "" {
		source, err := filepath.Abs(o.SourceRepo)
		if err != nil {
			return Config{}, err
		}
		cfg.SourceRepo = source
	}

	servers, err := parseSelection(o.Servers, AllServers(), cfg.Servers, RequiredServers)
	if err != nil {
		return Config{}, fmt.Errorf("invalid -servers: %w", err)
	}
	cfg.Servers = servers

	services, err := parseSelection(o.Services, AllServices, cfg.Services, RequiredServices)
	if err != nil {
		return Config{}, fmt.Errorf("invalid -services: %w", err)
	}
	cfg.Services = services

	return cfg, nil
}

func parseSelection(value string, available, fallback, required []string) ([]string, error) {
	switch strings.ToLower(strings.TrimSpace(value)) {
	case "":
		return Deduplicate(append(append([]string{}, required...), fallback...)), nil
	case "all":
		return append([]string{}, available...), nil
	case "none":
		return append([]string{}, required...), nil
	}

	selected := append([]string{}, required...)
	for _, name := range strings.Split(value, ",") {
		name = strings.TrimSpace(name)
		if name == "" {
			continue
		}
		match, ok := canonicalName(name, available)
		if !ok {
			return nil, fmt.Errorf("unknown entry %q", name)
		}
		selected = append(selected, match)
	}
	return Deduplicate(selected), nil
}

func canonicalName(name string, available []string) (string, bool) {
	for _, candidate := range available {
		if strings.EqualFold(candidate, name) {
			return candidate, true
		}
	}
	return "", false
}
