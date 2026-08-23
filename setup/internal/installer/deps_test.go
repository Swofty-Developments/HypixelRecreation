package installer

import "testing"

func TestDockerFailureReasonPrefersTheActualError(t *testing.T) {
	out := []byte("Client:\n Version: 27.3.1\n\n" +
		"ERROR: permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock\n" +
		"errors pretty printing info\n")
	got := dockerFailureReason(out)
	want := "ERROR: permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock"
	if got != want {
		t.Fatalf("got %q, want %q", got, want)
	}
}

func TestDockerFailureReasonFindsAConnectionRefusal(t *testing.T) {
	out := []byte("Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?\n")
	if got := dockerFailureReason(out); got == "" {
		t.Fatal("expected the connection failure to be reported")
	}
}

func TestDockerFailureReasonIgnoresOrdinaryOutput(t *testing.T) {
	if got := dockerFailureReason([]byte("Client:\n Version: 27.3.1\n")); got != "" {
		t.Fatalf("expected no reason, got %q", got)
	}
}

func TestStaleSessionIsReportedSeparatelyFromAMissingGroup(t *testing.T) {
	report := DependencyReport{}
	dep := dockerDaemonDependency(Platform{Systemd: true}, &report, "")
	if !report.Relogin && dep.Name == "docker daemon access" {
		t.Fatal("a group problem must ask the user to log in again")
	}
	if dep.Name == "docker daemon" && report.Relogin {
		t.Fatal("a dead daemon must not be reported as a login problem")
	}
}
