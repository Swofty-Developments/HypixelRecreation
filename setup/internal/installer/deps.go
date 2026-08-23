package installer

import (
	"bufio"
	"context"
	"fmt"
	"os"
	"os/exec"
	"os/user"
	"runtime"
	"strconv"
	"strings"
)

const dockerInstallDocs = "https://docs.docker.com/engine/install/"

type Platform struct {
	OS       string
	Distro   string
	Codename string
	Family   string
	WSL      bool
	Systemd  bool
}

func (p Platform) Label() string {
	switch {
	case p.OS == "darwin":
		return "macOS"
	case p.WSL && p.Distro != "":
		return p.Distro + " (WSL)"
	case p.WSL:
		return "Linux (WSL)"
	case p.Distro != "":
		return p.Distro
	default:
		return p.OS
	}
}

type Dependency struct {
	Name     string
	Detail   string
	Commands []string
	DocURL   string
}

type DependencyReport struct {
	Platform Platform
	Missing  []Dependency
	Relogin  bool
	Notes    []string
}

func (r DependencyReport) OK() bool {
	return len(r.Missing) == 0
}

func (r DependencyReport) Script() string {
	var lines []string
	for _, dep := range r.Missing {
		lines = append(lines, dep.Commands...)
	}
	if len(lines) == 0 {
		return ""
	}
	return "set -eu\n" + strings.Join(lines, "\n") + "\n"
}

func (r DependencyReport) Instructions() string {
	var b strings.Builder
	fmt.Fprintf(&b, "Detected platform: %s\n\n", r.Platform.Label())
	if r.OK() {
		b.WriteString("All dependencies are present.\n")
		return b.String()
	}
	for _, dep := range r.Missing {
		fmt.Fprintf(&b, "%s\n  %s\n", dep.Name, dep.Detail)
		if len(dep.Commands) > 0 {
			b.WriteString("\n")
			for _, cmd := range dep.Commands {
				fmt.Fprintf(&b, "    %s\n", cmd)
			}
		}
		if dep.DocURL != "" {
			fmt.Fprintf(&b, "\n  Official instructions: %s\n", dep.DocURL)
		}
		b.WriteString("\n")
	}
	for _, note := range r.Notes {
		fmt.Fprintf(&b, "%s\n\n", note)
	}
	if r.Relogin {
		b.WriteString("Group membership only applies to new logins, so after this finishes\n")
		b.WriteString("close this shell, open a new one and run the installer again.\n")
	}
	return b.String()
}

func DetectPlatform() Platform {
	p := Platform{OS: runtime.GOOS}
	if p.OS != "linux" {
		return p
	}
	p.Distro, p.Codename, p.Family = detectDistro()
	p.WSL = detectWSL()
	p.Systemd = detectSystemd()
	return p
}

func detectDistro() (id, codename, family string) {
	file, err := os.Open("/etc/os-release")
	if err != nil {
		return "", "", ""
	}
	defer file.Close()

	var idLike string
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		key, value, found := strings.Cut(scanner.Text(), "=")
		if !found {
			continue
		}
		value = strings.Trim(value, `"'`)
		switch key {
		case "ID":
			id = value
		case "ID_LIKE":
			idLike = value
		case "VERSION_CODENAME":
			codename = value
		}
	}

	for _, candidate := range append([]string{id}, strings.Fields(idLike)...) {
		switch candidate {
		case "debian", "ubuntu":
			return id, codename, "debian"
		case "fedora", "rhel", "centos":
			return id, codename, "rhel"
		case "arch":
			return id, codename, "arch"
		case "opensuse", "suse", "opensuse-leap", "opensuse-tumbleweed":
			return id, codename, "suse"
		}
	}
	return id, codename, ""
}

func detectWSL() bool {
	if os.Getenv("WSL_DISTRO_NAME") != "" {
		return true
	}
	data, err := os.ReadFile("/proc/version")
	if err != nil {
		return false
	}
	return strings.Contains(strings.ToLower(string(data)), "microsoft")
}

func detectSystemd() bool {
	info, err := os.Stat("/run/systemd/system")
	return err == nil && info.IsDir()
}

func packageInstall(p Platform, packages ...string) []string {
	joined := strings.Join(packages, " ")
	switch p.Family {
	case "debian":
		return []string{"sudo apt-get update", "sudo apt-get install -y " + joined}
	case "rhel":
		return []string{"sudo dnf install -y " + joined}
	case "arch":
		return []string{"sudo pacman -Sy --noconfirm " + joined}
	case "suse":
		return []string{"sudo zypper install -y " + joined}
	default:
		return nil
	}
}

func dockerEngineCommands(p Platform) []string {
	switch p.Family {
	case "debian":
		repo := p.Distro
		if repo != "ubuntu" && repo != "debian" {
			repo = "debian"
		}
		codename := p.Codename
		if codename == "" {
			codename = "$(. /etc/os-release && echo \"$VERSION_CODENAME\")"
		}
		return []string{
			"sudo apt-get update",
			"sudo apt-get install -y ca-certificates curl",
			"sudo install -m 0755 -d /etc/apt/keyrings",
			fmt.Sprintf("sudo curl -fsSL https://download.docker.com/linux/%s/gpg -o /etc/apt/keyrings/docker.asc", repo),
			"sudo chmod a+r /etc/apt/keyrings/docker.asc",
			fmt.Sprintf(`echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/%s %s stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null`, repo, codename),
			"sudo apt-get update",
			"sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",
		}
	case "rhel":
		repo := "fedora"
		if p.Distro != "fedora" {
			repo = "centos"
		}
		return []string{
			"sudo dnf -y install dnf-plugins-core",
			fmt.Sprintf("sudo dnf config-manager --add-repo https://download.docker.com/linux/%s/docker-ce.repo", repo),
			"sudo dnf -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",
		}
	case "arch":
		return []string{"sudo pacman -Sy --noconfirm docker docker-compose"}
	case "suse":
		return []string{"sudo zypper install -y docker docker-compose"}
	default:
		return nil
	}
}

func daemonStartCommands(p Platform) []string {
	if p.Systemd {
		return []string{"sudo systemctl enable --now docker"}
	}
	return []string{"sudo service docker start"}
}

func CheckDependenciesReport(ctx context.Context) DependencyReport {
	p := DetectPlatform()
	report := DependencyReport{Platform: p}

	if p.OS == "darwin" {
		return macOSReport(ctx, p)
	}

	for _, tool := range []string{"curl", "git"} {
		if _, err := exec.LookPath(tool); err != nil {
			report.Missing = append(report.Missing, Dependency{
				Name:     tool,
				Detail:   fmt.Sprintf("%s is required and was not found in PATH.", tool),
				Commands: packageInstall(p, tool),
			})
		}
	}

	dockerFound := true
	if _, err := exec.LookPath("docker"); err != nil {
		dockerFound = false
		commands := dockerEngineCommands(p)
		commands = append(commands, daemonStartCommands(p)...)
		commands = append(commands, "sudo usermod -aG docker $USER")
		report.Missing = append(report.Missing, Dependency{
			Name:     "docker",
			Detail:   "Docker Engine is required and was not found in PATH.",
			Commands: commands,
			DocURL:   dockerInstallDocs,
		})
		report.Relogin = true
	}

	if dockerFound {
		if err := exec.CommandContext(ctx, "docker", "compose", "version").Run(); err != nil {
			report.Missing = append(report.Missing, Dependency{
				Name:     "docker compose v2",
				Detail:   "Docker is installed but the Compose v2 plugin is missing.",
				Commands: packageInstall(p, "docker-compose-plugin"),
				DocURL:   "https://docs.docker.com/compose/install/",
			})
		}
		if out, err := exec.CommandContext(ctx, "docker", "info").CombinedOutput(); err != nil {
			report.Missing = append(report.Missing, dockerDaemonDependency(p, &report, dockerFailureReason(out)))
		}
	}

	if p.WSL && !p.Systemd {
		report.Notes = append(report.Notes,
			"WSL without systemd: add the following to /etc/wsl.conf, then run\n"+
				"'wsl --shutdown' from Windows so the daemon starts on boot.\n\n"+
				"    [boot]\n    systemd=true")
	}

	return report
}

func dockerDaemonDependency(p Platform, report *DependencyReport, reason string) Dependency {
	if !sessionInDockerGroup() {
		report.Relogin = true
		if inDockerGroup() {
			return Dependency{
				Name: "docker daemon access",
				Detail: "Your user is in the 'docker' group but this session started before that, " +
					"so it still cannot reach the daemon. Log out and back in, then run the installer again.",
				Commands: []string{"newgrp docker"},
				DocURL:   "https://docs.docker.com/engine/install/linux-postinstall/",
			}
		}
		commands := append(daemonStartCommands(p), "sudo usermod -aG docker $USER")
		return Dependency{
			Name:     "docker daemon access",
			Detail:   "Cannot talk to the Docker daemon, and your user is not in the 'docker' group.",
			Commands: commands,
			DocURL:   "https://docs.docker.com/engine/install/linux-postinstall/",
		}
	}
	detail := "Docker is installed but the daemon is not responding."
	if reason != "" {
		detail = detail + " Docker reported: " + reason
	}
	return Dependency{
		Name:     "docker daemon",
		Detail:   detail,
		Commands: daemonStartCommands(p),
		DocURL:   dockerInstallDocs,
	}
}

func dockerFailureReason(out []byte) string {
	for _, line := range strings.Split(string(out), "\n") {
		line = strings.TrimSpace(line)
		if line == "" {
			continue
		}
		lowered := strings.ToLower(line)
		if strings.Contains(lowered, "permission denied") ||
			strings.Contains(lowered, "cannot connect") ||
			strings.HasPrefix(lowered, "error") {
			if len(line) > 200 {
				line = line[:200]
			}
			return line
		}
	}
	return ""
}

func sessionInDockerGroup() bool {
	if os.Geteuid() == 0 {
		return true
	}
	group, err := user.LookupGroup("docker")
	if err != nil {
		return false
	}
	gid, err := strconv.Atoi(group.Gid)
	if err != nil {
		return false
	}
	groups, err := os.Getgroups()
	if err != nil {
		return false
	}
	for _, id := range groups {
		if id == gid {
			return true
		}
	}
	return false
}

func inDockerGroup() bool {
	current, err := user.Current()
	if err != nil {
		return false
	}
	group, err := user.LookupGroup("docker")
	if err != nil {
		return false
	}
	ids, err := current.GroupIds()
	if err != nil {
		return false
	}
	for _, id := range ids {
		if id == group.Gid {
			return true
		}
	}
	return false
}

func macOSReport(ctx context.Context, p Platform) DependencyReport {
	report := DependencyReport{Platform: p}
	if _, err := exec.LookPath("docker"); err != nil {
		report.Missing = append(report.Missing, Dependency{
			Name:   "Docker Desktop",
			Detail: "Docker Desktop is required on macOS and cannot be installed unattended.",
			Commands: []string{
				"brew install --cask docker",
				"open -a Docker",
			},
			DocURL: "https://docs.docker.com/desktop/install/mac-install/",
		})
		return report
	}
	if err := exec.CommandContext(ctx, "docker", "compose", "version").Run(); err != nil {
		report.Missing = append(report.Missing, Dependency{
			Name:     "docker compose v2",
			Detail:   "Docker is installed but the Compose v2 plugin is missing.",
			Commands: []string{"open -a Docker"},
			DocURL:   "https://docs.docker.com/compose/install/",
		})
	}
	if err := exec.CommandContext(ctx, "docker", "info").Run(); err != nil {
		report.Missing = append(report.Missing, Dependency{
			Name:     "docker daemon",
			Detail:   "Docker Desktop is installed but not running.",
			Commands: []string{"open -a Docker"},
			DocURL:   "https://docs.docker.com/desktop/install/mac-install/",
		})
	}
	return report
}

func BootstrapCommand(report DependencyReport) *exec.Cmd {
	script := report.Script()
	if script == "" {
		return nil
	}
	cmd := exec.Command("sh", "-c", script)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	return cmd
}
