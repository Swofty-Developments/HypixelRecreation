package installer

import (
	"context"
	"fmt"
	"io"
	"strings"
	"time"

	"github.com/moby/moby/client"
)

type ContainerStatus struct {
	ID       string
	Name     string
	State    string
	Health   string
	Restarts int
	Started  time.Time
}

func DockerClient() (*client.Client, error) {
	return client.New(client.FromEnv)
}

func ListProjectContainers(ctx context.Context) ([]ContainerStatus, error) {
	cli, err := DockerClient()
	if err != nil {
		return nil, err
	}
	defer cli.Close()

	list, err := cli.ContainerList(ctx, client.ContainerListOptions{All: true})
	if err != nil {
		return nil, err
	}
	statuses := make([]ContainerStatus, 0, len(list.Items))
	for _, c := range list.Items {
		name := strings.TrimPrefix(first(c.Names), "/")
		if !isProjectContainer(name) {
			continue
		}
		inspect, err := cli.ContainerInspect(ctx, c.ID, client.ContainerInspectOptions{})
		if err != nil {
			continue
		}
		container := inspect.Container
		state := "unknown"
		health := "n/a"
		if container.State != nil && container.State.Health != nil {
			health = string(container.State.Health.Status)
		}
		if container.State != nil {
			state = string(container.State.Status)
		}
		started := time.Time{}
		if container.State != nil && container.State.StartedAt != "" {
			started, _ = time.Parse(time.RFC3339Nano, container.State.StartedAt)
		}
		statuses = append(statuses, ContainerStatus{
			ID:       c.ID,
			Name:     name,
			State:    state,
			Health:   health,
			Restarts: container.RestartCount,
			Started:  started,
		})
	}
	return statuses, nil
}

func RestartContainer(ctx context.Context, name string) error {
	cli, err := DockerClient()
	if err != nil {
		return err
	}
	defer cli.Close()
	timeout := 20
	_, err = cli.ContainerRestart(ctx, name, client.ContainerRestartOptions{Timeout: &timeout})
	return err
}

func ContainerLogs(ctx context.Context, name string, tail string) (io.ReadCloser, error) {
	cli, err := DockerClient()
	if err != nil {
		return nil, err
	}
	return cli.ContainerLogs(ctx, name, client.ContainerLogsOptions{
		ShowStdout: true,
		ShowStderr: true,
		Follow:     true,
		Tail:       tail,
	})
}

func WaitHealthy(ctx context.Context, name string, timeout time.Duration) error {
	deadline, cancel := context.WithTimeout(ctx, timeout)
	defer cancel()

	cli, err := DockerClient()
	if err != nil {
		return err
	}
	defer cli.Close()

	ticker := time.NewTicker(2 * time.Second)
	defer ticker.Stop()

	for {
		inspect, err := cli.ContainerInspect(deadline, name, client.ContainerInspectOptions{})
		container := inspect.Container
		if err == nil && container.State != nil {
			if container.State.Health != nil {
				switch container.State.Health.Status {
				case "healthy":
					return nil
				case "unhealthy":
					return fmt.Errorf("%s is unhealthy", name)
				}
			}
			if container.State.Status == "exited" {
				return fmt.Errorf("%s exited", name)
			}
		}

		select {
		case <-deadline.Done():
			return fmt.Errorf("%s did not become healthy within %s", name, timeout)
		case <-ticker.C:
		}
	}
}

const (
	accountPrefix  = "hsb:acct"
	nameIndexKey   = "hsb:name2uuid"
	rankField      = "hypixel:rank"
	staffRankValue = `"STAFF"`
)

const makeStaffScript = `local uuid = redis.call('hget', KEYS[1], ARGV[1])
if not uuid then return 'no-player' end
local document_key = ARGV[2] .. ':players:' .. uuid
local raw = redis.call('get', document_key)
if not raw then return 'no-player' end
local decoded, document = pcall(cjson.decode, raw)
if not decoded or type(document) ~= 'table' then return 'bad-document' end
for _, value in pairs(document) do
  if type(value) ~= 'string' then return 'bad-document' end
end
document[ARGV[3]] = ARGV[4]
redis.call('set', document_key, cjson.encode(document))
redis.call('incr', ARGV[2] .. ':version:players:' .. uuid)
redis.call('sadd', ARGV[2] .. ':index:players', uuid)
return 'ok'`

const makeStaffShell = `status=$(redis-cli EVAL "$1" 1 "$2" "$3" "$4" "$5" "$6") || exit 1
case "$status" in
  ok) exit 0 ;;
  no-player) exit 44 ;;
  bad-document) exit 45 ;;
  *) echo "$status" >&2; exit 1 ;;
esac`

func MakeStaff(ctx context.Context, username string) error {
	username = strings.TrimSpace(username)
	if username == "" {
		return fmt.Errorf("username is required")
	}

	cli, err := DockerClient()
	if err != nil {
		return err
	}
	defer cli.Close()

	created, err := cli.ExecCreate(ctx, "hypixel_redis", client.ExecCreateOptions{
		AttachStdout: false,
		AttachStderr: false,
		Cmd: []string{"sh", "-c", makeStaffShell, "make-staff",
			makeStaffScript, nameIndexKey, strings.ToLower(username),
			accountPrefix, rankField, staffRankValue},
	})
	if err != nil {
		return err
	}
	if _, err := cli.ExecStart(ctx, created.ID, client.ExecStartOptions{}); err != nil {
		return err
	}

	for {
		inspect, err := cli.ExecInspect(ctx, created.ID, client.ExecInspectOptions{})
		if err != nil {
			return err
		}
		if !inspect.Running {
			switch inspect.ExitCode {
			case 0:
				return nil
			case 44:
				return fmt.Errorf("player %q was not found; they need to join once first", username)
			case 45:
				return fmt.Errorf("the stored account for %q is not in the expected format; promote them with an in-game command instead", username)
			default:
				return fmt.Errorf("redis-cli exited with code %d", inspect.ExitCode)
			}
		}

		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-time.After(250 * time.Millisecond):
		}
	}
}

func first(values []string) string {
	if len(values) == 0 {
		return ""
	}
	return values[0]
}

func isProjectContainer(name string) bool {
	return strings.HasPrefix(name, "hypixel_") ||
		strings.HasPrefix(name, "hypixelcore_") ||
		strings.HasPrefix(name, "service_") ||
		name == "pico_limbo" ||
		name == "game_server_builder"
}
