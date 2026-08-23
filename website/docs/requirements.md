# Requirements

This project requires substantial resources to run properly. Make sure your system meets these requirements before proceeding.

## System Requirements

| Resource | Minimum | Recommended |
|----------|---------|-------------|
| RAM | 2.5 GB | 4 GB |
| CPU Cores | 6 | 8+ |
| Storage | 15 GB | 25+ GB |
| Java | 25 | 25 |

The RAM figures are for the installer's default selection: the prototype lobby, a hub and an island, plus the five default services.

### RAM Distribution

RAM scales with how much you choose to run. The installer estimates it before installing and shows the result on its summary screen:

- **Base** (MongoDB, Redis, the proxy, PicoLimbo and the pack server): 900 MB minimum, 1.5 GB recommended
- **Each game server**: 300 MB minimum, 450 MB recommended
- **Each service**: 140 MB minimum, 220 MB recommended

Running everything — all 28 server types and all 13 services — comes to roughly 10.9 GB minimum and 16.6 GB recommended.

## Software Requirements

### Required

| Software | Purpose | Download |
|----------|---------|----------|
| Java 25 | Runtime | [Eclipse Adoptium](https://adoptium.net/) |
| MongoDB | Database | [MongoDB Community](https://www.mongodb.com/try/download/community) |
| Redis | Caching & Messaging | [Redis](https://redis.io/download/) or [Memurai](https://www.memurai.com/) (Windows) |

### Optional

| Software | Purpose | Download |
|----------|---------|----------|
| Docker | Containerized deployment | [Docker Desktop](https://www.docker.com/products/docker-desktop) |
| MongoDB Compass | Database GUI | [MongoDB Compass](https://www.mongodb.com/products/compass) |

If you use the Docker deployment, Java, MongoDB and Redis all run in containers, so Docker with Compose v2 is the only thing you need on the host. The [installer](/docs/docker/setup) checks for it and offers to install it for you if it is missing.

## Network Requirements

The following ports are used by default:

| Port   | Service                                                    |
|--------|------------------------------------------------------------|
| 25565  | Velocity Proxy (player connections)                        |
| 27017  | MongoDB                                                    |
| 6379   | Redis                                                      |
| 7270   | Resource Pack Server                                       |
| 8080   | API Service                                                |
| 20000+ | Game servers (the proxy hands out ports upwards from 20000) |
| 65535  | PicoLimbo                                                  |

:::alert warning
Ensure these ports are available and not blocked by your firewall when running locally.
:::
