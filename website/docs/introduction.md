# Introduction

HypixelRecreation is a Minestom-based recreation of Hypixel's network, including the SkyBlock gamemode and a growing set of minigames (BedWars, SkyWars, Murder Mystery and Ravengard). This project aims to provide a properly abstracted, scalable codebase for running your own server network.

:::alert note
This implementation is under active development and is not yet production-ready. Some portions of the codebase are still being refined.
:::

## Features

- **Multi-Server Architecture** - Game logic distributed across multiple Minestom servers by ServerType
- **Microservices Pattern** - Each major feature runs as an independent service
- **Redis Communication** - Real-time inter-service communication via Redis pub/sub
- **MongoDB Storage** - Persistent data storage for profiles, auctions, and more
- **Velocity Proxy** - Single entry point with load balancing support
- **Java 25** - Uses modern Java features including virtual threads

## Game Modes

| Gamemode     | Server Types                                                                   |
|--------------|--------------------------------------------------------------------------------|
| SkyBlock     | 14 server types (Hub, Island, Spider's Den, The End, Crimson Isle, and more)   |
| BedWars      | Lobby, Game, Configurator                                                      |
| SkyWars      | Lobby, Game, Configurator                                                      |
| Murder Mystery | Lobby, Game, Configurator                                                    |
| Ravengard    | Lobby, Dungeon                                                                |
| Replays      | Replay Viewer                                                                  |
| Lobbies      | Main Lobby, Prototype Lobby                                                    |

See the [Server Types Reference](/docs/reference/server-types) for the full list.

## Project Structure

The project is organized into several module types:

| Module Type          | Purpose                                                       |
|----------------------|---------------------------------------------------------------|
| `commons`            | Shared enums, configs, and protocols                          |
| `service.*`          | Independent microservices (API, Auctions, Bazaar, etc.)       |
| `type.*`             | Server type implementations (Hub, Island, BedWars, etc.)      |
| `loader`             | Main entry point (HypixelCore.jar)                            |
| `velocity.extension` | Velocity proxy plugin (SkyBlockProxy.jar)                     |
| `proxy.api`          | Client API for reaching the proxy and services from a server  |
| `pvp`                | Combat mechanics library (MinestomPvP)                        |
| `anticheat`          | Anti-cheat module                                             |
| `spark`              | Spark (performance monitoring) integration                     |
| `dungeons`           | SkyBlock Catacombs dungeon library                            |
| `packer`             | Resource pack builder and server                              |
| `store`              | Web store frontend (Next.js) that talks to ServiceStore       |
| `clientmod`          | Optional client mod for resource pack/export tooling          |
| `setup`              | Native TUI installer for Docker deployments                   |

## Related Projects

This project is designed to work with [HypixelForums](https://github.com/Swofty-Developments/HypixelForums) for a complete forum and website experience.

## Getting Help

- **Discord**: [discord.swofty.net](https://discord.swofty.net)
- **Javadocs**: [swofty-developments.github.io/HypixelRecreation](https://swofty-developments.github.io/HypixelRecreation/)
- **Video Guide**: [YouTube Setup Tutorial](https://www.youtube.com/watch?v=pxzJbjjQL-M)
