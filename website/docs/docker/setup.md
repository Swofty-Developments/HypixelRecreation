# Docker Setup

## Quick Install (Linux / macOS)

The fastest way to get running. A single command downloads the native installer and launches an interactive TUI that handles everything:

```bash
curl -fsSL skyblock-installer.swofty.net | bash
```

This fetches the prebuilt `skyblock-installer` binary for your platform from the [latest release](https://github.com/Swofty-Developments/HypixelRecreation/releases/latest), caches it under `~/.cache/skyblock-installer`, and runs it. The installer will:

1. Check its dependencies (`docker`, `docker compose` v2, a reachable daemon, `git`, `curl`) and offer to install anything missing
2. Let you pick which server types and services to run
3. Show a summary of your choices with an estimated RAM figure for that selection
4. Generate all configuration and Docker Compose files
5. Build and start everything in the correct order
6. Drop you into a management dashboard

The installer itself is a self-contained Go binary with no runtime dependencies of its own.

### Dependencies

You do not need Docker installed beforehand. If anything is missing, the installer shows a **Missing dependencies** screen listing what is absent and the exact official commands for your distribution, and offers to run them for you:

- `i` runs the commands (they use `sudo`, so you will be prompted for your password)
- `r` re-checks once you have finished
- `Esc` goes back if you would rather run them yourself

You can reach the same screen any time from **Check dependencies** on the home menu.

The commands come straight from the [official Docker install docs](https://docs.docker.com/engine/install/) and are picked per distribution — the apt repository steps on Debian and Ubuntu, the `dnf` repository steps on Fedora and RHEL, `pacman` on Arch, `zypper` on openSUSE. On macOS, Docker Desktop cannot be installed unattended, so the installer prints the `brew install --cask docker` steps instead of running them.

:::alert warning
Installing Docker adds your user to the `docker` group, and Linux only applies group membership at login. The installer will tell you to close your shell, open a new one, and run it again — this is expected and not a failure.
:::

### What You'll See

The installer walks you through:

| Step              | What It Does                                                              |
|-------------------|---------------------------------------------------------------------------|
| Dependencies      | Checks for Docker, Compose v2, a reachable daemon, git and curl, and offers to install what is missing |
| Configuration     | Pick install directory, bind IP, online mode                              |
| Server Selection  | Choose from 14 SkyBlock servers and 13 lobby/minigame servers             |
| Service Selection | Pick which microservices to run (Party is required)        |
| Summary           | Recaps your choices and estimates the RAM the selection needs             |
| Build & Launch    | Builds Docker images, starts containers in order, waits for health checks |

The installer never inspects your machine's RAM, CPU, disk space or Docker version. The figure on the Summary screen is an estimate derived purely from how many servers and services you picked, so treat it as a budget to check against your own hardware rather than a check the installer has already made for you.

### Management Dashboard

To manage an existing install, run the installer again — it detects your installation and offers to open the dashboard:

```bash
curl -fsSL skyblock-installer.swofty.net | bash
```

By default it installs to `~/.hypixel-skyblock`; pass `-dir <path>` to use a different location (`curl -fsSL skyblock-installer.swofty.net | bash -s -- -dir /opt/skyblock`). From the home menu choose **Manage existing installation** to reach the dashboard, which provides:

- **Refresh status** - Live view of every container's health
- **Start all / Stop all** - Control all containers at once
- **Restart container** - Restart an individual container
- **Rebuild and update** - Pull the latest JARs and rebuild images
- **Configure servers/services** - Change which servers and services run
- **Make player STAFF** - Promote a player to staff rank via the database
- **View logs** - Tail logs from any container
- **Fresh reinstall / Uninstall** - Recreate or remove the deployment

## Manual Setup

If you prefer to set things up manually or aren't on Linux, you can use Docker Compose directly.

### Prerequisites

- [Docker](https://docs.docker.com/engine/install/) with Docker Compose v2
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/Swofty-Developments/HypixelRecreation.git
cd HypixelRecreation
```

### 2. Configure

The containers pick up `configuration/config.docker.yml` on their own, so there is nothing to rename. Only set your forwarding secret.

In the top of `docker-compose.yml` change the `change-me` to other.

```yml
x-forwarding-env: &forwarding_env
  FORWARDING_SECRET: ${FORWARDING_SECRET:-change-me}
```

For reference:

```yml
x-forwarding-env: &forwarding_env
  FORWARDING_SECRET: ${FORWARDING_SECRET:-i7sC4xqh}
```

### 3. Build and Run

```bash
docker compose up --build
```

For detached mode:

```bash
docker compose up --build -d
```

### 4. Stop Containers

```bash
docker compose down
```

## What Gets Started

The Docker Compose setup starts:

| Container            | Purpose                                     |
|----------------------|---------------------------------------------|
| MongoDB              | Database                                    |
| Redis                | Caching & messaging                         |
| Velocity Proxy       | Player connections                          |
| PicoLimbo            | Connection queue                            |
| Resource Pack Server | Serves the resource packs on port 7270      |
| Game Servers         | Gameplay instances                          |
| Services             | Microservices (API, Auctions, Bazaar, etc.) |

## Connecting

Once everything is running, connect with your Minecraft client to:

```
localhost:25565
```

Set **Server Resource Packs** to *Enabled* on the server entry, or the HUD, minimap and custom models will not load.

The pack URL is handed to your client rather than resolved inside the container, so it stays a host address. If players connect from another machine, set `resource-packs` in `configuration/config.docker.yml` to your LAN or public IP and make sure port `7270` is reachable:

```yml
resource-packs:
    skyblockpack:
        server-url: http://192.0.2.10:7270
    ravengard:
        server-url: http://192.0.2.10:7270
```

## Logs and Debugging

View logs for all containers:

```bash
docker compose logs -f
```

View logs for a specific container:

```bash
docker compose logs -f hypixel_proxy
docker compose logs -f hypixelcore_skyblock_hub
```

## Data Persistence

Docker volumes persist data between restarts:

- MongoDB data (player profiles, auctions, etc.)
- Configuration files
- World saves
