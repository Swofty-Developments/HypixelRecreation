package tui

import (
	"bufio"
	"io"
	"testing"

	tea "charm.land/bubbletea/v2"
)

func TestLeavingLogsWhileReadingIgnoresTheClosedStream(t *testing.T) {
	r, w := io.Pipe()
	defer w.Close()
	activeLog = &logStream{rc: r, scan: bufio.NewScanner(r)}
	cmd := nextLog(activeLog)
	done := make(chan tea.Msg, 1)
	go func() { done <- cmd() }()

	closeActiveLog()
	msg := <-done

	_, next := Model{}.Update(msg)
	if next != nil {
		t.Fatal("a closed log stream scheduled another read")
	}
	if activeLog != nil {
		t.Fatal("a closed log stream became active again")
	}
}

func TestLogLinesFromTheActiveStreamKeepReading(t *testing.T) {
	r, w := io.Pipe()
	activeLog = &logStream{rc: r, scan: bufio.NewScanner(r)}
	defer closeActiveLog()
	go func() { _, _ = w.Write([]byte("hello\n")) }()

	msg := nextLog(activeLog)()
	if got := msg.(logsMsg).line; got != "hello" {
		t.Fatalf("line = %q", got)
	}
	_, next := Model{}.Update(msg)
	if next == nil {
		t.Fatal("the active stream stopped after one line")
	}
}
