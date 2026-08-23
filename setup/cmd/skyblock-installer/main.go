package main

import (
	"context"
	"flag"
	"fmt"
	"os"

	tea "charm.land/bubbletea/v2"
	"github.com/Swofty-Developments/HypixelRecreation/setup/internal/installer"
	"github.com/Swofty-Developments/HypixelRecreation/setup/internal/tui"
)

const headlessEnv = "HYPIXEL_INSTALLER_HEADLESS"

func main() {
	var opts installer.HeadlessOptions
	headless := os.Getenv(headlessEnv) != ""

	flag.StringVar(&opts.InstallDir, "dir", installer.DefaultInstallDir(), "installation directory")
	flag.BoolVar(&headless, "headless", headless, "install without the interactive interface")
	flag.StringVar(&opts.SourceRepo, "source", "", "use a local repository checkout instead of cloning from GitHub")
	flag.StringVar(&opts.Servers, "servers", "", "comma separated server types to install, or all")
	flag.StringVar(&opts.Services, "services", "", "comma separated services to install, or all")
	flag.StringVar(&opts.BindIP, "bind-ip", "", "address the proxy binds to")
	flag.StringVar(&opts.APIPort, "api-port", "", "port the API service listens on")
	flag.BoolVar(&opts.OnlineMode, "online-mode", true, "require players to be authenticated with Mojang")
	flag.BoolVar(&opts.ExposePorts, "expose-ports", false, "publish the MongoDB and Redis ports on the host")
	flag.BoolVar(&opts.Reinstall, "reinstall", false, "remove a previous installation before installing")
	flag.BoolVar(&opts.NoStart, "no-start", false, "build the images without starting the containers")
	flag.Parse()

	if headless {
		if err := installer.RunHeadless(context.Background(), opts, os.Stdout); err != nil {
			fmt.Fprintf(os.Stderr, "installer failed: %v\n", err)
			os.Exit(1)
		}
		return
	}

	model := tui.New(opts.InstallDir)
	program := tea.NewProgram(model)
	if _, err := program.Run(); err != nil {
		fmt.Fprintf(os.Stderr, "installer failed: %v\n", err)
		os.Exit(1)
	}
}
