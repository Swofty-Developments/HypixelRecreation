package tui

import (
	"bufio"
	"context"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"

	"charm.land/bubbles/v2/list"
	"charm.land/bubbles/v2/spinner"
	"charm.land/bubbles/v2/textinput"
	"charm.land/bubbles/v2/viewport"
	tea "charm.land/bubbletea/v2"
	"charm.land/lipgloss/v2"
	"github.com/Swofty-Developments/HypixelRecreation/setup/internal/installer"
)

type screen int

const (
	screenHome screen = iota
	screenBasics
	screenInput
	screenMulti
	screenSummary
	screenDashboard
	screenRun
	screenRestart
	screenLogSelect
	screenLogs
	screenConfirm
	screenDeps
)

type inputField int

const (
	inputDir inputField = iota
	inputBind
	inputAPIPort
	inputAdminUsername
)

type Model struct {
	cfg          installer.Config
	state        installer.State
	hasInstall   bool
	screen       screen
	width        int
	height       int
	err          error
	sourceConfig string

	menu       list.Model
	multi      list.Model
	restart    list.Model
	logSelect  list.Model
	input      textinput.Model
	inputField inputField
	inputBack  screen
	spinner    spinner.Model
	logs       viewport.Model

	multiTitle string
	multiNext  screen
	selected   map[string]bool
	allServers bool

	runTitle   string
	runDone    bool
	confirmMsg string
	onConfirm  func(Model) (tea.Model, tea.Cmd)

	depsReport installer.DependencyReport
	depsBack   screen
}

type item string

func (i item) FilterValue() string { return string(i) }
func (i item) Title() string       { return string(i) }
func (i item) Description() string { return "" }

type checkItem struct {
	name    string
	checked bool
}

func (i checkItem) FilterValue() string { return i.name }
func (i checkItem) Title() string {
	if i.checked {
		return "[x] " + i.name
	}
	return "[ ] " + i.name
}
func (i checkItem) Description() string { return "" }

type statusMsg struct {
	statuses []installer.ContainerStatus
	err      error
}

type eventMsg installer.Event
type depsMsg error
type logsMsg string

type depsReportMsg struct {
	report installer.DependencyReport
	back   screen
}

type depsInstallMsg struct{ err error }

var (
	titleStyle = lipgloss.NewStyle().Bold(true).Foreground(lipgloss.Color("86"))
	mutedStyle = lipgloss.NewStyle().Foreground(lipgloss.Color("245"))
	warnStyle  = lipgloss.NewStyle().Foreground(lipgloss.Color("214"))
	errStyle   = lipgloss.NewStyle().Foreground(lipgloss.Color("196"))
	okStyle    = lipgloss.NewStyle().Foreground(lipgloss.Color("42"))
)

func New(installDir string) Model {
	cfg := installer.DefaultConfig()
	cfg.InstallDir = installDir
	state, ok := installer.ExistingInstall(installDir)
	if ok {
		cfg = installer.StateToConfig(state)
	}

	s := spinner.New()
	s.Spinner = spinner.Dot

	ti := textinput.New()
	ti.Focus()
	ti.CharLimit = 300

	m := Model{
		cfg:          cfg,
		state:        state,
		hasInstall:   ok,
		screen:       screenHome,
		input:        ti,
		spinner:      s,
		logs:         viewport.New(viewport.WithWidth(80), viewport.WithHeight(18)),
		sourceConfig: localConfigurationDir(),
	}
	m.menu = newList("Choose an action", homeItems(ok))
	return m
}

func (m Model) Init() tea.Cmd {
	return tea.Batch(m.spinner.Tick, checkDeps())
}

func (m Model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	var cmds []tea.Cmd
	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		m.width, m.height = msg.Width, msg.Height
		m.resizeActiveList()
		m.logs.SetWidth(msg.Width - 4)
		m.logs.SetHeight(msg.Height - 8)
	case tea.KeyMsg:
		if msg.String() == "ctrl+c" {
			return m, tea.Quit
		}
		switch m.screen {
		case screenHome:
			return m.updateHome(msg)
		case screenBasics:
			return m.updateBasics(msg)
		case screenInput:
			return m.updateInput(msg)
		case screenMulti:
			return m.updateMulti(msg)
		case screenSummary:
			return m.updateSummary(msg)
		case screenDashboard:
			return m.updateDashboard(msg)
		case screenRun:
			if m.runDone && msg.String() == "enter" {
				return m.showDashboard(), refreshStatus()
			}
		case screenRestart:
			return m.updateRestart(msg)
		case screenLogSelect:
			return m.updateLogSelect(msg)
		case screenDeps:
			return m.updateDeps(msg)
		case screenLogs:
			if msg.String() == "esc" || msg.String() == "q" {
				if activeLogs != nil {
					_ = activeLogs.Close()
					activeLogs = nil
					activeLogScan = nil
				}
				m.screen = screenDashboard
				return m, refreshStatus()
			}
			var cmd tea.Cmd
			m.logs, cmd = m.logs.Update(msg)
			return m, cmd
		case screenConfirm:
			switch msg.String() {
			case "y", "Y", "enter":
				if m.onConfirm != nil {
					return m.onConfirm(m)
				}
				return m, nil
			case "n", "N", "esc":
				m.screen = screenDashboard
				return m, nil
			}
			return m, nil
		default:
			return m, nil
		}
	case depsMsg:
		if msg != nil {
			m.err = msg
		}
	case depsReportMsg:
		if msg.report.OK() {
			m.err = nil
			m.depsReport = msg.report
			m.screen = msg.back
			m.logs.SetContent(msg.report.Instructions())
			return m, nil
		}
		return m.enterDeps(msg.report, msg.back)
	case depsInstallMsg:
		if msg.err != nil {
			m.err = fmt.Errorf("dependency install failed: %w", msg.err)
			return m, nil
		}
		m.err = nil
		if m.depsReport.Relogin {
			m.logs.SetContent(reloginMessage)
			return m, nil
		}
		return m, checkDepsReport(m.depsBack)
	case statusMsg:
		if msg.err != nil {
			m.err = msg.err
		} else {
			m.err = nil
			m.logs.SetContent(statusTable(msg.statuses))
		}
	case eventMsg:
		ev := installer.Event(msg)
		if ev.Line != "" {
			m.logs.SetContent(appendLine(m.logs.View(), ev.Line))
			m.logs.GotoBottom()
		}
		if ev.Err != nil {
			m.runDone = true
			m.err = ev.Err
			m.logs.SetContent(appendLine(m.logs.View(), "ERROR: "+ev.Err.Error()))
			m.logs.GotoBottom()
		}
		if ev.Done {
			m.runDone = true
			activeEvents = nil
			if ev.Err == nil {
				m.logs.SetContent(appendLine(m.logs.View(), "Done. Press Enter to return."))
				m.logs.GotoBottom()
			}
			return m, nil
		}
		return m, nextEvent()
	case logsMsg:
		m.logs.SetContent(appendLine(m.logs.View(), string(msg)))
		m.logs.GotoBottom()
		if string(msg) == "logs ended" {
			return m, nil
		}
		return m, nextLog()
	case spinner.TickMsg:
		var cmd tea.Cmd
		m.spinner, cmd = m.spinner.Update(msg)
		cmds = append(cmds, cmd)
	}

	var cmd tea.Cmd
	switch m.screen {
	case screenHome, screenBasics, screenDashboard:
		m.menu, cmd = m.menu.Update(msg)
	case screenMulti:
		m.multi, cmd = m.multi.Update(msg)
	case screenRestart:
		m.restart, cmd = m.restart.Update(msg)
	case screenLogSelect:
		m.logSelect, cmd = m.logSelect.Update(msg)
	case screenLogs, screenRun:
		m.logs, cmd = m.logs.Update(msg)
	}
	cmds = append(cmds, cmd)
	return m, tea.Batch(cmds...)
}

func (m Model) View() tea.View {
	var b strings.Builder
	fmt.Fprintln(&b, titleStyle.Render("Hypixel SkyBlock Manager"))
	fmt.Fprintln(&b, mutedStyle.Render("v"+installer.Version))
	if m.err != nil {
		fmt.Fprintln(&b, errStyle.Render(m.err.Error()))
	}
	fmt.Fprintln(&b)

	switch m.screen {
	case screenBasics:
		fmt.Fprintln(&b, basicsSummary(m.cfg))
		fmt.Fprintln(&b)
		fmt.Fprintln(&b, m.menu.View())
	case screenInput:
		fmt.Fprintln(&b, m.inputPrompt())
		fmt.Fprintln(&b, m.input.View())
		fmt.Fprintln(&b)
		fmt.Fprintln(&b, mutedStyle.Render("Enter to continue, Esc to go back"))
	case screenMulti:
		fmt.Fprintln(&b, m.multiTitle)
		fmt.Fprintln(&b, mutedStyle.Render("Space toggles, Enter continues"))
		fmt.Fprintln(&b, m.multi.View())
	case screenSummary:
		fmt.Fprint(&b, summaryView(m.cfg))
	case screenDashboard:
		fmt.Fprintln(&b, installSummary(m.cfg))
		fmt.Fprintln(&b, m.menu.View())
		fmt.Fprintln(&b)
		fmt.Fprintln(&b, m.logs.View())
	case screenRun:
		status := m.spinner.View() + " " + m.runTitle
		if m.runDone {
			status = okStyle.Render("Finished")
		}
		fmt.Fprintln(&b, status)
		fmt.Fprintln(&b, m.logs.View())
	case screenRestart:
		fmt.Fprintln(&b, "Select a container to restart")
		fmt.Fprintln(&b, m.restart.View())
	case screenLogSelect:
		fmt.Fprintln(&b, "Select a container for logs")
		fmt.Fprintln(&b, m.logSelect.View())
	case screenLogs:
		fmt.Fprintln(&b, "Logs")
		fmt.Fprintln(&b, mutedStyle.Render("Esc/q returns to dashboard"))
		fmt.Fprintln(&b, m.logs.View())
	case screenDeps:
		fmt.Fprintln(&b, warnStyle.Render("Missing dependencies"))
		fmt.Fprintln(&b, mutedStyle.Render("i installs them for you (uses sudo), r re-checks, Esc goes back"))
		fmt.Fprintln(&b)
		fmt.Fprintln(&b, m.logs.View())
	case screenConfirm:
		fmt.Fprintln(&b, warnStyle.Render(m.confirmMsg))
		fmt.Fprintln(&b, "Press y to confirm or n to cancel.")
	default:
		fmt.Fprintln(&b, m.menu.View())
	}
	view := tea.NewView(b.String())
	view.AltScreen = true
	return view
}

func (m Model) updateHome(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	var cmd tea.Cmd
	m.menu, cmd = m.menu.Update(msg)
	if msg.String() != "enter" {
		return m, cmd
	}
	switch selectedTitle(m.menu) {
	case "Install":
		m.startBasics()
	case "Manage existing installation":
		m = m.showDashboard()
		return m, refreshStatus()
	case "Fresh reinstall":
		m.cfg.Reinstall = true
		m.cfg.OnlineMode = true
		m.startBasics()
	case "Check dependencies":
		return m.enterDeps(installer.CheckDependenciesReport(context.Background()), screenHome)
	case "Quit":
		return m, tea.Quit
	}
	return m, cmd
}

func (m Model) updateBasics(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	var cmd tea.Cmd
	m.menu, cmd = m.menu.Update(msg)
	if msg.String() != "enter" {
		return m, cmd
	}
	switch selectedTitle(m.menu) {
	case "Install directory":
		m.startInput(inputDir, m.cfg.InstallDir, screenBasics)
	case "Bind IP":
		m.startInput(inputBind, m.cfg.BindIP, screenBasics)
	case "API service port":
		m.startInput(inputAPIPort, m.cfg.APIPort, screenBasics)
	case "Continue":
		m.configureMulti("Select servers", allServerOptions(), m.cfg.Servers, true)
	case "Exit":
		return m, tea.Quit
	}
	return m, cmd
}

func (m Model) updateDashboard(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	var cmd tea.Cmd
	m.menu, cmd = m.menu.Update(msg)
	if msg.String() != "enter" {
		return m, cmd
	}
	switch selectedTitle(m.menu) {
	case "Refresh status":
		return m, refreshStatus()
	case "Start all":
		return m.startRun("Starting containers", func(r *installer.Runner) error {
			return r.Start(context.Background(), m.cfg.InstallDir)
		})
	case "Stop all":
		return m.startComposeRun("Stopping containers", "stop")
	case "Rebuild and update":
		return m.startComposeRun("Rebuilding images", "build", "--no-cache")
	case "Configure servers/services":
		m.cfg.Reinstall = false
		m.configureMulti("Select servers", allServerOptions(), m.cfg.Servers, true)
	case "Make player STAFF":
		m.startInput(inputAdminUsername, "", screenDashboard)
	case "Restart container":
		return m.startRestart()
	case "View logs":
		return m.startLogs()
	case "Fresh reinstall":
		m.cfg.Reinstall = true
		m.cfg.OnlineMode = true
		m.startBasics()
	case "Uninstall":
		m.confirmMsg = "This removes containers, images, volumes, and the install directory."
		m.screen = screenConfirm
		m.onConfirm = func(mm Model) (tea.Model, tea.Cmd) {
			return mm.startComposeRun("Uninstalling", "down", "--rmi", "all", "--volumes")
		}
	case "Quit":
		return m, tea.Quit
	}
	return m, cmd
}

func (m Model) showDashboard() Model {
	m.refreshInstallState()
	m.screen = screenDashboard
	m.runDone = false
	m.runTitle = ""
	m.menu = newList("Manage installation", dashboardItems())
	m.sizeList(&m.menu)
	return m
}

func (m Model) updateInput(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	switch msg.String() {
	case "esc":
		m.screen = m.inputBack
		return m, nil
	case "enter":
		value := strings.TrimSpace(m.input.Value())
		switch m.inputField {
		case inputDir:
			if value != "" {
				m.cfg.InstallDir = expandHome(value)
			}
			m.screen = m.inputBack
		case inputBind:
			if value != "" {
				m.cfg.BindIP = value
			}
			m.screen = m.inputBack
		case inputAPIPort:
			if value != "" {
				m.cfg.APIPort = value
			}
			m.screen = screenSummary
		case inputAdminUsername:
			return m.startMakeStaff(value)
		}
		return m, nil
	}
	var cmd tea.Cmd
	m.input, cmd = m.input.Update(msg)
	return m, cmd
}

func (m Model) updateMulti(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	switch msg.String() {
	case " ", "space":
		idx := m.multi.Index()
		items := m.multi.Items()
		if idx >= 0 && idx < len(items) {
			ci := items[idx].(checkItem)
			if !isRequired(ci.name, m.allServers) {
				ci.checked = !ci.checked
				items[idx] = ci
				m.selected[ci.name] = ci.checked
				m.multi.SetItems(items)
			}
		}
	case "enter":
		var values []string
		if m.allServers {
			values = append(values, installer.RequiredServers...)
		} else {
			values = append(values, installer.RequiredServices...)
		}
		for name, ok := range m.selected {
			if ok {
				values = append(values, name)
			}
		}
		values = installer.Deduplicate(values)
		if m.allServers {
			m.cfg.Servers = values
			m.configureMulti("Select services", installer.AllServices, m.cfg.Services, false)
		} else {
			m.cfg.Services = values
			m.screen = screenSummary
		}
	case "esc":
		m.startBasics()
	default:
		var cmd tea.Cmd
		m.multi, cmd = m.multi.Update(msg)
		return m, cmd
	}
	return m, nil
}

func (m Model) updateSummary(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	switch msg.String() {
	case "enter":
		if m.hasInstall && !m.cfg.Reinstall {
			return m.startReconfigure()
		}
		return m.startInstall()
	case "o":
		m.cfg.OnlineMode = !m.cfg.OnlineMode
	case "p":
		m.cfg.ExposePorts = !m.cfg.ExposePorts
	case "e":
		m.startInput(inputDir, m.cfg.InstallDir, screenSummary)
	case "esc":
		m.startBasics()
	}
	return m, nil
}

func (m Model) updateRestart(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	if msg.String() == "esc" {
		m.screen = screenDashboard
		return m, nil
	}
	if msg.String() == "enter" {
		name := selectedTitle(m.restart)
		if name == "" {
			return m, nil
		}
		m.screen = screenRun
		m.runTitle = "Restarting " + name
		m.runDone = false
		m.logs.SetContent("")
		return m, func() tea.Msg {
			err := installer.RestartContainer(context.Background(), name)
			if err != nil {
				return eventMsg{Err: err, Done: true}
			}
			return eventMsg{Line: name + " restarted", Done: true}
		}
	}
	var cmd tea.Cmd
	m.restart, cmd = m.restart.Update(msg)
	return m, cmd
}

func (m Model) updateLogSelect(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	if msg.String() == "esc" {
		m.screen = screenDashboard
		return m, nil
	}
	if msg.String() == "enter" {
		name := selectedTitle(m.logSelect)
		if name == "" {
			return m, nil
		}
		return m.openLogs(name)
	}
	var cmd tea.Cmd
	m.logSelect, cmd = m.logSelect.Update(msg)
	return m, cmd
}

func (m *Model) startInput(field inputField, value string, back screen) {
	m.screen = screenInput
	m.inputField = field
	m.inputBack = back
	m.input.SetValue(value)
	m.input.CursorEnd()
	m.input.Focus()
}

func (m Model) inputPrompt() string {
	switch m.inputField {
	case inputDir:
		return "Install directory"
	case inputBind:
		return "Bind IP for Velocity"
	case inputAPIPort:
		return "API service port"
	case inputAdminUsername:
		return "Minecraft username to promote to STAFF"
	default:
		return "Value"
	}
}

func (m *Model) startBasics() {
	m.screen = screenBasics
	m.menu = newList("Basic settings", []list.Item{
		item("Install directory"),
		item("Bind IP"),
		item("API service port"),
		item(""),
		item("Continue"),
		item("Exit"),
	})
	m.sizeList(&m.menu)
}

func (m *Model) configureMulti(title string, values []string, checked []string, servers bool) {
	m.screen = screenMulti
	m.multiTitle = title
	m.allServers = servers
	m.selected = make(map[string]bool, len(values))
	for _, value := range checked {
		m.selected[value] = true
	}
	items := make([]list.Item, 0, len(values))
	for _, value := range values {
		required := isRequired(value, servers)
		items = append(items, checkItem{name: value, checked: required || m.selected[value]})
	}
	m.multi = newList(title, items)
	m.sizeList(&m.multi)
}

const reloginMessage = "Dependencies installed.\n\n" +
	"Your user was added to the 'docker' group, which Linux only applies at\n" +
	"login. Close this shell, open a new one, and run the installer again."

func (m Model) enterDeps(report installer.DependencyReport, back screen) (tea.Model, tea.Cmd) {
	m.depsReport = report
	m.depsBack = back
	m.screen = screenDeps
	m.err = nil
	m.logs.SetContent(report.Instructions())
	m.logs.GotoTop()
	return m, nil
}

func (m Model) updateDeps(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	switch msg.String() {
	case "esc", "q":
		m.screen = m.depsBack
		m.err = nil
		return m, nil
	case "r":
		return m, checkDepsReport(m.depsBack)
	case "i":
		cmd := installer.BootstrapCommand(m.depsReport)
		if cmd == nil {
			m.err = fmt.Errorf("no automatic install is available for %s; follow the steps above", m.depsReport.Platform.Label())
			return m, nil
		}
		return m, tea.ExecProcess(cmd, func(err error) tea.Msg {
			return depsInstallMsg{err: err}
		})
	}
	var cmd tea.Cmd
	m.logs, cmd = m.logs.Update(msg)
	return m, cmd
}

func (m Model) startInstall() (tea.Model, tea.Cmd) {
	if report := installer.CheckDependenciesReport(context.Background()); !report.OK() {
		return m.enterDeps(report, m.screen)
	}
	m.screen = screenRun
	m.runTitle = "Installing"
	m.runDone = false
	m.logs.SetContent("")
	runner := installer.NewRunner(m.cfg.InstallDir)
	activeEvents = runner.Events
	go runner.Install(context.Background(), m.cfg, m.sourceConfig)
	return m, tea.Batch(m.spinner.Tick, nextEvent())
}

func (m Model) startReconfigure() (tea.Model, tea.Cmd) {
	if report := installer.CheckDependenciesReport(context.Background()); !report.OK() {
		return m.enterDeps(report, m.screen)
	}
	return m.startRun("Applying configuration", func(r *installer.Runner) error {
		return r.Reconfigure(context.Background(), m.cfg)
	})
}

func (m Model) startMakeStaff(username string) (tea.Model, tea.Cmd) {
	username = strings.TrimSpace(username)
	if username == "" {
		m.err = fmt.Errorf("username is required")
		m.screen = screenDashboard
		return m, nil
	}
	m.screen = screenRun
	m.runTitle = "Promoting " + username
	m.runDone = false
	m.logs.SetContent("")
	return m, func() tea.Msg {
		err := installer.MakeStaff(context.Background(), username)
		if err != nil {
			return eventMsg{Err: err, Done: true}
		}
		return eventMsg{Line: username + " is now STAFF", Done: true}
	}
}

func (m Model) startComposeRun(title string, args ...string) (tea.Model, tea.Cmd) {
	m.screen = screenRun
	m.runTitle = title
	m.runDone = false
	m.logs.SetContent("")
	events := make(chan installer.Event, 128)
	activeEvents = events
	go func() {
		defer close(events)
		err := installer.Compose(context.Background(), m.cfg.InstallDir, events, args...)
		if selectedTitle(m.menu) == "Uninstall" && err == nil {
			_ = os.RemoveAll(m.cfg.InstallDir)
		}
		events <- installer.Event{Err: err, Done: true}
	}()
	return m, tea.Batch(m.spinner.Tick, nextEvent())
}

func (m Model) startRun(title string, fn func(*installer.Runner) error) (tea.Model, tea.Cmd) {
	m.screen = screenRun
	m.runTitle = title
	m.runDone = false
	m.logs.SetContent("")
	runner := installer.NewRunner(m.cfg.InstallDir)
	activeEvents = runner.Events
	go func() {
		defer close(runner.Events)
		err := fn(runner)
		runner.Events <- installer.Event{Err: err, Done: true}
	}()
	return m, tea.Batch(m.spinner.Tick, nextEvent())
}

func (m Model) startRestart() (tea.Model, tea.Cmd) {
	statuses, err := installer.ListProjectContainers(context.Background())
	if err != nil {
		m.err = err
		return m, nil
	}
	items := make([]list.Item, 0, len(statuses))
	for _, status := range statuses {
		items = append(items, item(status.Name))
	}
	m.restart = newList("Restart container", items)
	m.sizeList(&m.restart)
	m.screen = screenRestart
	return m, nil
}

func (m Model) startLogs() (tea.Model, tea.Cmd) {
	statuses, err := installer.ListProjectContainers(context.Background())
	if err != nil {
		m.err = err
		return m, nil
	}
	if len(statuses) == 0 {
		m.err = fmt.Errorf("no containers found")
		return m, nil
	}
	items := make([]list.Item, 0, len(statuses))
	for _, status := range statuses {
		items = append(items, item(status.Name))
	}
	m.logSelect = newList("View logs", items)
	m.sizeList(&m.logSelect)
	m.screen = screenLogSelect
	return m, nil
}

func (m Model) openLogs(name string) (tea.Model, tea.Cmd) {
	activeLogs = nil
	activeLogScan = nil
	rc, err := installer.ContainerLogs(context.Background(), name, "80")
	if err != nil {
		m.err = err
		return m, nil
	}
	activeLogs = rc
	activeLogScan = bufio.NewScanner(rc)
	m.screen = screenLogs
	m.logs.SetContent("Logs for " + name)
	return m, nextLog()
}

func (m *Model) refreshInstallState() {
	state, ok := installer.ExistingInstall(m.cfg.InstallDir)
	m.hasInstall = ok
	if ok {
		m.state = state
		m.cfg = installer.StateToConfig(state)
	}
}

func newList(title string, items []list.Item) list.Model {
	delegate := list.NewDefaultDelegate()
	delegate.ShowDescription = false
	delegate.SetHeight(1)
	delegate.SetSpacing(0)

	l := list.New(items, delegate, 78, 18)
	l.Title = title
	l.SetShowStatusBar(false)
	l.SetFilteringEnabled(false)
	l.SetShowHelp(true)
	return l
}

func (m Model) sizeList(l *list.Model) {
	if m.width <= 0 || m.height <= 0 {
		return
	}
	width := max(20, m.width-4)
	height := max(6, m.height-8)
	l.SetSize(width, height)
}

func (m *Model) resizeActiveList() {
	switch m.screen {
	case screenHome, screenBasics, screenDashboard:
		m.sizeList(&m.menu)
	case screenMulti:
		m.sizeList(&m.multi)
	case screenRestart:
		m.sizeList(&m.restart)
	case screenLogSelect:
		m.sizeList(&m.logSelect)
	}
}

func homeItems(hasInstall bool) []list.Item {
	if hasInstall {
		return []list.Item{item("Manage existing installation"), item("Fresh reinstall"), item("Check dependencies"), item("Quit")}
	}
	return []list.Item{item("Install"), item("Check dependencies"), item("Quit")}
}

func dashboardItems() []list.Item {
	return []list.Item{
		item("Refresh status"),
		item("Start all"),
		item("Stop all"),
		item("Rebuild and update"),
		item("Configure servers/services"),
		item("Make player STAFF"),
		item("Restart container"),
		item("View logs"),
		item("Fresh reinstall"),
		item("Uninstall"),
		item("Quit"),
	}
}

func allServerOptions() []string {
	return installer.AllServers()
}

func selectedTitle(l list.Model) string {
	if selected, ok := l.SelectedItem().(interface{ Title() string }); ok {
		return strings.TrimPrefix(strings.TrimPrefix(selected.Title(), "[x] "), "[ ] ")
	}
	return ""
}

func refreshStatus() tea.Cmd {
	return func() tea.Msg {
		statuses, err := installer.ListProjectContainers(context.Background())
		return statusMsg{statuses: statuses, err: err}
	}
}

func checkDeps() tea.Cmd {
	return func() tea.Msg {
		return depsMsg(installer.CheckDependencies(context.Background()))
	}
}

func checkDepsReport(back screen) tea.Cmd {
	return func() tea.Msg {
		return depsReportMsg{
			report: installer.CheckDependenciesReport(context.Background()),
			back:   back,
		}
	}
}

var activeEvents <-chan installer.Event

func nextEvent() tea.Cmd {
	return func() tea.Msg {
		if activeEvents == nil {
			return nil
		}
		ev, ok := <-activeEvents
		if !ok {
			activeEvents = nil
			return eventMsg{Done: true}
		}
		return eventMsg(ev)
	}
}

var activeLogs io.ReadCloser
var activeLogScan *bufio.Scanner

func nextLog() tea.Cmd {
	return func() tea.Msg {
		if activeLogs == nil || activeLogScan == nil {
			return nil
		}
		if !activeLogScan.Scan() {
			_ = activeLogs.Close()
			activeLogs = nil
			activeLogScan = nil
			return logsMsg("logs ended")
		}
		return logsMsg(activeLogScan.Text())
	}
}

func statusTable(statuses []installer.ContainerStatus) string {
	if len(statuses) == 0 {
		return mutedStyle.Render("No project containers found.")
	}
	var b strings.Builder
	fmt.Fprintf(&b, "%-34s %-12s %-12s %-8s\n", "Container", "State", "Health", "Restarts")
	for _, status := range statuses {
		health := status.Health
		if health == "" {
			health = "n/a"
		}
		fmt.Fprintf(&b, "%-34s %-12s %-12s %-8d\n", status.Name, status.State, health, status.Restarts)
	}
	return b.String()
}

func summaryView(cfg installer.Config) string {
	estimate := installer.EstimateForConfig(cfg)
	var b strings.Builder
	fmt.Fprintln(&b, titleStyle.Render("Ready to install"))
	fmt.Fprintln(&b)
	fmt.Fprintln(&b, installSummary(cfg))
	fmt.Fprintln(&b)
	fmt.Fprintf(&b, "%-16s %s minimum, %s recommended\n", "Estimated RAM", mb(estimate.MinimumMB), mb(estimate.RecommendedMB))
	fmt.Fprintln(&b)
	fmt.Fprintln(&b, mutedStyle.Render("Enter installs, o toggles online mode, p toggles Mongo/Redis ports, e edits directory, Esc returns."))
	return b.String()
}

func basicsSummary(cfg installer.Config) string {
	var b strings.Builder
	fmt.Fprintln(&b, titleStyle.Render("Basic settings"))
	fmt.Fprintf(&b, "%-16s %s\n", "Directory", cfg.InstallDir)
	fmt.Fprintf(&b, "%-16s %s\n", "Bind IP", cfg.BindIP)
	fmt.Fprintf(&b, "%-16s %s\n", "API service port", cfg.APIPort)
	fmt.Fprintf(&b, "%-16s %s\n", "Online mode", enabledLabel(cfg.OnlineMode))
	return b.String()
}

func installSummary(cfg installer.Config) string {
	var b strings.Builder
	fmt.Fprintf(&b, "%-16s %s\n", "Directory", cfg.InstallDir)
	fmt.Fprintf(&b, "%-16s %s\n", "Bind IP", cfg.BindIP)
	fmt.Fprintf(&b, "%-16s %s\n", "Connect", okStyle.Render(connectAddress(cfg)))
	fmt.Fprintf(&b, "%-16s %s\n", "Online mode", enabledLabel(cfg.OnlineMode))
	fmt.Fprintf(&b, "%-16s %d selected\n", "Servers", len(cfg.Servers))
	fmt.Fprintf(&b, "%-16s %d selected\n", "Services", len(cfg.Services))
	fmt.Fprintf(&b, "%-16s %s\n", "DB ports", enabledLabel(cfg.ExposePorts))
	return b.String()
}

// connectAddress is the address players use to join the proxy. A wildcard bind
// (0.0.0.0/::) is not itself connectable, so surface localhost in that case.
func connectAddress(cfg installer.Config) string {
	host := cfg.BindIP
	switch host {
	case "", "0.0.0.0", "::", "[::]":
		host = "localhost"
	}
	return host + ":25565"
}

func enabledLabel(value bool) string {
	if value {
		return okStyle.Render("enabled")
	}
	return mutedStyle.Render("disabled")
}

func mb(value int) string {
	return fmt.Sprintf("%.1f GB", float64(value)/1024)
}

func appendLine(current, line string) string {
	if strings.TrimSpace(current) == "" {
		return line
	}
	return current + "\n" + line
}

func isRequired(name string, servers bool) bool {
	required := installer.RequiredServices
	if servers {
		required = installer.RequiredServers
	}
	return contains(required, name)
}

func contains(values []string, needle string) bool {
	for _, value := range values {
		if value == needle {
			return true
		}
	}
	return false
}

func expandHome(path string) string {
	if path == "~" {
		home, _ := os.UserHomeDir()
		return home
	}
	if strings.HasPrefix(path, "~/") {
		home, _ := os.UserHomeDir()
		return filepath.Join(home, strings.TrimPrefix(path, "~/"))
	}
	return path
}

func localConfigurationDir() string {
	cwd, err := os.Getwd()
	if err != nil {
		return ""
	}
	candidates := []string{
		filepath.Join(cwd, "configuration"),
		filepath.Join(cwd, "..", "configuration"),
	}
	for _, candidate := range candidates {
		if info, err := os.Stat(candidate); err == nil && info.IsDir() {
			return candidate
		}
	}
	return ""
}
