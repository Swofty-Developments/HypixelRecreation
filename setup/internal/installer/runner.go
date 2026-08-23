package installer

import (
	"bufio"
	"context"
	"fmt"
	"io"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"
)

type Event struct {
	Line string
	Err  error
	Done bool
}

type Runner struct {
	WorkDir string
	Events  chan Event
}

func NewRunner(workDir string) *Runner {
	return &Runner{WorkDir: workDir, Events: make(chan Event, 256)}
}

func (r *Runner) emit(format string, args ...any) {
	r.Events <- Event{Line: fmt.Sprintf(format, args...)}
}

func (r *Runner) fail(err error) error {
	r.Events <- Event{Err: err, Done: true}
	return err
}

func (r *Runner) done() {
	r.Events <- Event{Done: true}
}

func (r *Runner) Install(ctx context.Context, cfg Config, sourceConfigDir string) {
	defer close(r.Events)
	if err := r.install(ctx, cfg, sourceConfigDir); err != nil {
		r.fail(err)
		return
	}
	r.done()
}

func (r *Runner) install(ctx context.Context, cfg Config, sourceConfigDir string) error {
	if cfg.Reinstall {
		r.emit("Removing previous installation at %s", cfg.InstallDir)
		_ = Compose(ctx, cfg.InstallDir, r.Events, "down", "--rmi", "all", "--volumes")
		if err := os.RemoveAll(cfg.InstallDir); err != nil {
			return err
		}
	}

	r.emit("Creating installation directory")
	if err := os.MkdirAll(cfg.InstallDir, 0o755); err != nil {
		return err
	}

	repoDir, err := r.cloneAssets(ctx, cfg)
	if err != nil {
		return err
	}

	r.emit("Copying configuration files")
	if err := CopyDir(filepath.Join(repoDir, "configuration"), filepath.Join(cfg.InstallDir, "configuration")); err != nil {
		return err
	}
	r.emit("Copying Dockerfiles")
	if err := CopyDir(filepath.Join(repoDir, "DockerFiles"), filepath.Join(cfg.InstallDir, "DockerFiles")); err != nil {
		return err
	}
	if sourceConfigDir != "" {
		if _, err := os.Stat(sourceConfigDir); err == nil {
			if err := CopyDir(sourceConfigDir, filepath.Join(cfg.InstallDir, "configuration")); err != nil {
				return err
			}
		}
	}

	r.emit("Checking for world assets")
	if err := DownloadLimboAssets(ctx, cfg.InstallDir, r.Events); err != nil {
		r.emit("asset import failed: %v", err)
		r.emit("Manual fallback: %s", LimboAssetsManualHint())
	} else {
		r.emit("world assets are ready")
	}

	r.emit("Writing generated Compose and application config")
	if err := PrepareFiles(cfg); err != nil {
		return err
	}

	r.emit("Building Docker images")
	if err := Compose(ctx, cfg.InstallDir, r.Events, "build", "--no-cache"); err != nil {
		return err
	}

	if cfg.NoStart {
		r.emit("Skipping container start")
		return nil
	}

	return r.Start(ctx, cfg.InstallDir)
}

func (r *Runner) cloneAssets(ctx context.Context, cfg Config) (string, error) {
	if cfg.SourceRepo != "" {
		r.emit("Using repository assets from %s", cfg.SourceRepo)
		return cfg.SourceRepo, nil
	}

	installDir := cfg.InstallDir
	repoDir := filepath.Join(installDir, "_repo")
	if _, err := os.Stat(filepath.Join(repoDir, ".git")); err == nil {
		r.emit("Updating sparse checkout")
		if err := runStream(ctx, repoDir, r.Events, "git", "pull", "--depth", "1"); err != nil {
			return "", err
		}
		if err := runStream(ctx, repoDir, r.Events, "git", "sparse-checkout", "set", "configuration", "DockerFiles"); err != nil {
			return "", err
		}
		return repoDir, nil
	}

	r.emit("Cloning repository assets")
	if err := runStream(ctx, installDir, r.Events, "git", "clone", "--depth", "1", "--filter=blob:none", "--sparse", "https://github.com/"+GitHubRepo+".git", "_repo"); err != nil {
		return "", err
	}
	if err := runStream(ctx, repoDir, r.Events, "git", "sparse-checkout", "set", "configuration", "DockerFiles"); err != nil {
		return "", err
	}
	return repoDir, nil
}

func (r *Runner) Start(ctx context.Context, dir string) error {
	r.emit("Starting MongoDB and Redis")
	if err := Compose(ctx, dir, r.Events, "up", "-d", "mongodb", "redis"); err != nil {
		return err
	}
	if err := WaitHealthy(ctx, "hypixel_mongo", 60*time.Second); err != nil {
		r.emit("MongoDB health warning: %v", err)
	}
	if err := WaitHealthy(ctx, "hypixel_redis", 30*time.Second); err != nil {
		r.emit("Redis health warning: %v", err)
	}

	r.emit("Starting proxy")
	if err := Compose(ctx, dir, r.Events, "up", "-d", "proxy"); err != nil {
		return err
	}
	if err := WaitHealthy(ctx, "hypixel_proxy", 90*time.Second); err != nil {
		r.emit("Proxy health warning: %v", err)
	}

	r.emit("Starting remaining services")
	return Compose(ctx, dir, r.Events, "up", "-d")
}

func (r *Runner) Reconfigure(ctx context.Context, cfg Config) error {
	repoDir, err := r.cloneAssets(ctx, cfg)
	if err != nil {
		return err
	}
	r.emit("Refreshing Dockerfiles")
	if err := CopyDir(filepath.Join(repoDir, "DockerFiles"), filepath.Join(cfg.InstallDir, "DockerFiles")); err != nil {
		return err
	}

	r.emit("Writing updated configuration")
	if err := PrepareFiles(cfg); err != nil {
		return err
	}

	r.emit("Applying Docker Compose changes")
	return Compose(ctx, cfg.InstallDir, r.Events, "up", "-d", "--build")
}

func Compose(ctx context.Context, dir string, events chan<- Event, args ...string) error {
	return runStream(ctx, dir, events, "docker", append([]string{"compose"}, args...)...)
}

func runStream(ctx context.Context, dir string, events chan<- Event, name string, args ...string) error {
	cmd := exec.CommandContext(ctx, name, args...)
	cmd.Dir = dir
	cmd.Env = append(os.Environ(), "DOCKER_BUILDKIT=1")

	stdout, err := cmd.StdoutPipe()
	if err != nil {
		return err
	}
	stderr, err := cmd.StderrPipe()
	if err != nil {
		return err
	}
	if err := cmd.Start(); err != nil {
		return err
	}

	done := make(chan struct{}, 2)
	scan := func(r io.Reader) {
		defer func() { done <- struct{}{} }()
		s := bufio.NewScanner(r)
		for s.Scan() {
			events <- Event{Line: s.Text()}
		}
	}
	go scan(stdout)
	go scan(stderr)
	<-done
	<-done

	if err := cmd.Wait(); err != nil {
		return fmt.Errorf("%s %s failed: %w", name, strings.Join(args, " "), err)
	}
	return nil
}

func CheckDependencies(ctx context.Context) error {
	report := CheckDependenciesReport(ctx)
	if report.OK() {
		return nil
	}
	names := make([]string, 0, len(report.Missing))
	for _, dep := range report.Missing {
		names = append(names, dep.Name)
	}
	return fmt.Errorf("missing dependencies: %s", strings.Join(names, ", "))
}
