# Server Types Reference

ServerTypes define the different game modes and locations that can be run as separate server instances. There are **28 server types** in total: 14 SkyBlock locations and 14 lobbies/minigame servers.

The full list lives in `net.swofty.commons.ServerType`.

## SkyBlock Server Types

These server types are part of the SkyBlock gamemode and share SkyBlock-specific functionality.

| ServerType                     | Description      | World Required                                          |
|--------------------------------|------------------|---------------------------------------------------------|
| `SKYBLOCK_ISLAND`              | Personal Island  | `hypixel_skyblock_island_template.polar` (template)     |
| `SKYBLOCK_HUB`                 | Hub              | `hypixel_skyblock_hub.polar`                            |
| `SKYBLOCK_SPIDERS_DEN`         | Spider's Den     | `hypixel_skyblock_spiders_den.polar`                    |
| `SKYBLOCK_THE_END`             | The End          | `hypixel_skyblock_the_end.polar`                        |
| `SKYBLOCK_CRIMSON_ISLE`        | Crimson Isle     | `hypixel_skyblock_crimson_isle.polar`                   |
| `SKYBLOCK_DUNGEON_HUB`         | Dungeon Hub      | `hypixel_skyblock_dungeon_hub.polar`                    |
| `SKYBLOCK_THE_FARMING_ISLANDS` | Farming Islands  | `hypixel_skyblock_hub.polar`                            |
| `SKYBLOCK_GOLD_MINE`           | Gold Mine        | `hypixel_skyblock_gold_mine.polar`                      |
| `SKYBLOCK_DEEP_CAVERNS`        | Deep Caverns     | `hypixel_skyblock_deep_caverns.polar`                   |
| `SKYBLOCK_DWARVEN_MINES`       | Dwarven Mines    | `hypixel_skyblock_dwarven_mines.polar`                  |
| `SKYBLOCK_THE_PARK`            | The Park         | `hypixel_skyblock_the_park.polar`                       |
| `SKYBLOCK_GALATEA`             | Galatea          | `hypixel_skyblock_galatea.polar`                        |
| `SKYBLOCK_BACKWATER_BAYOU`     | Backwater Bayou  | `hypixel_skyblock_backwater_bayou.polar`                |
| `SKYBLOCK_JERRYS_WORKSHOP`     | Jerry's Workshop | `hypixel_skyblock_jerrys_workshop.polar`                |

Personal islands are generated from the island template (`hypixel_skyblock_island_template.polar`) and saved per-profile, so every player gets their own copy.

### Starting a SkyBlock Server

```bash
java -jar HypixelCore.jar SKYBLOCK_ISLAND
java -jar HypixelCore.jar SKYBLOCK_HUB
java -jar HypixelCore.jar SKYBLOCK_SPIDERS_DEN
```

## Lobby Server Types

Lobbies act as spawn and matchmaking hubs. They all implement `LobbyTypeLoader`.

| ServerType             | Description               | World Required                          |
|------------------------|---------------------------|-----------------------------------------|
| `MAIN_LOBBY`           | Network main lobby        | `hypixel_main_lobby.polar`              |
| `PROTOTYPE_LOBBY`      | Prototype/testing lobby   | `hypixel_prototype_lobby.polar`         |
| `BEDWARS_LOBBY`        | BedWars lobby server      | `hypixel_bedwars_lobby.polar`           |
| `SKYWARS_LOBBY`        | SkyWars lobby server      | `hypixel_skywars_lobby.polar`           |
| `MURDER_MYSTERY_LOBBY` | Murder Mystery lobby      | `hypixel_murder_mystery_lobby.polar`    |

## Minigame Server Types

Each minigame runs on a dedicated game server and loads its maps as `.polar` files. Game servers also record replays which can be played back on a Replay Viewer server.

### BedWars

| ServerType             | Description                    | Map Location                        |
|------------------------|--------------------------------|-------------------------------------|
| `BEDWARS_GAME`         | Active BedWars game server     | `configuration/bedwars/<map>.polar` |
| `BEDWARS_CONFIGURATOR` | BedWars map configuration tool | Writes to `configuration/bedwars/`  |

Map definitions live in `configuration/bedwars/maps.json`.

### SkyWars

| ServerType               | Description                    | Map Location                          |
|--------------------------|--------------------------------|---------------------------------------|
| `SKYWARS_GAME`           | Active SkyWars game server     | `configuration/skywars/<map>.polar`   |
| `SKYWARS_CONFIGURATOR`   | SkyWars map configuration tool | Writes to `configuration/skywars/`    |

Map definitions live in `configuration/skywars/*.json` (e.g. `ancient_config.json`, `congo_config.json`).

### Murder Mystery

| ServerType                         | Description                    | Map Location                              |
|------------------------------------|--------------------------------|-------------------------------------------|
| `MURDER_MYSTERY_GAME`              | Active Murder Mystery server   | `configuration/murdermystery/<map>.polar` |
| `MURDER_MYSTERY_CONFIGURATOR`      | Murder Mystery map config tool | Writes to `configuration/murdermystery/`  |

Map definitions live in `configuration/murdermystery/maps.json`.

### Ravengard

| ServerType         | Description                    | World Required                                        |
|--------------------|--------------------------------|-------------------------------------------------------|
| `RAVENGARD_LOBBY`  | Ravengard lobby and tutorial   | `hypixel_ravengard_lobby.polar` + `hypixel_ravengard_tutorial.polar` |
| `RAVENGARD_DUNGEON`| Ravengard dungeon runs         | `configuration/ravengard/dungeon_1.polar`             |

Ravengard also loads its own resource pack (`configuration/resourcepacks/ravengard-original.zip`).

### Replays

| ServerType     | Description                    |
|----------------|--------------------------------|
| `REPLAY_VIEWER`| Watches recorded game replays  |

Replay data is served by the [Replay service](/docs/reference/services#servicereplay); no world file is required.

### Starting Non-SkyBlock Servers

```bash
java -jar HypixelCore.jar MAIN_LOBBY
java -jar HypixelCore.jar BEDWARS_LOBBY
java -jar HypixelCore.jar BEDWARS_GAME
java -jar HypixelCore.jar SKYWARS_GAME
java -jar HypixelCore.jar MURDER_MYSTERY_GAME
java -jar HypixelCore.jar RAVENGARD_LOBBY
java -jar HypixelCore.jar REPLAY_VIEWER
```

## Type Loader Architecture

Each ServerType has a corresponding TypeLoader class that initializes:

- Event handlers
- NPCs and entities
- GUIs and menus
- Region handlers
- Custom mechanics

### Loader Hierarchy

```
HypixelTypeLoader (base)
├── SkyBlockTypeLoader          (all SKYBLOCK_* loaders)
│   ├── TypeHubLoader
│   ├── TypeIslandLoader
│   ├── TypeSpidersDenLoader
│   └── ...
├── LobbyTypeLoader              (MAIN_LOBBY, PROTOTYPE_LOBBY, game lobbies)
│   ├── TypeMainLobbyLoader
│   ├── TypePrototypeLobbyLoader
│   ├── TypeBedWarsLobbyLoader
│   ├── TypeSkyWarsLobbyLoader
│   └── TypeMurderMysteryLobbyLoader
├── RavengardTypeLoader          (RAVENGARD_LOBBY, RAVENGARD_DUNGEON)
├── Game loaders                 (BEDWARS_GAME, SKYWARS_GAME, MURDER_MYSTERY_GAME)
├── Configurator loaders         (BEDWARS/SKYWARS/MURDER_MYSTERY_CONFIGURATOR)
└── TypeReplayViewerLoader       (REPLAY_VIEWER)
```

Shared game logic lives in `type.generic` (base), `type.skyblockgeneric` (SkyBlock shared), `type.lobby` (lobby and matchmaking logic) and `type.ravengardgeneric` (Ravengard shared); `type.game` provides the replay recording framework used by game servers.

## Server Communication

All servers communicate through:

1. **Redis** - Real-time messaging and pub/sub
2. **MongoDB** - Persistent data storage
3. **Velocity Proxy** - Player routing

### How Servers Register

1. Server starts with specified ServerType
2. Connects to Redis and publishes availability
3. Proxy discovers and adds to routing table
4. Players can be routed to the server

## Multiple Instances

You can run multiple instances of any ServerType for load balancing:

```bash
# Terminal 1
java -jar HypixelCore.jar SKYBLOCK_HUB

# Terminal 2
java -jar HypixelCore.jar SKYBLOCK_HUB

# Terminal 3
java -jar HypixelCore.jar SKYBLOCK_HUB
```

The proxy will distribute players across all available instances.

## World Requirements

All `.polar` world files go in `configuration/world/` unless noted otherwise. The complete world pack is available in the [world download zip](https://files.catbox.moe/flri48.zip) referenced by the [Game Servers setup](/docs/setup/game-servers).

| World                                    | Location                      | Used By                                   |
|------------------------------------------|-------------------------------|-------------------------------------------|
| `hypixel_skyblock_hub.polar`             | `configuration/world/`        | SKYBLOCK_HUB, SKYBLOCK_THE_FARMING_ISLANDS|
| `hypixel_skyblock_island_template.polar` | `configuration/world/`        | SKYBLOCK_ISLAND                           |
| `hypixel_skyblock_spiders_den.polar`     | `configuration/world/`        | SKYBLOCK_SPIDERS_DEN                      |
| `hypixel_skyblock_the_end.polar`         | `configuration/world/`        | SKYBLOCK_THE_END                          |
| `hypixel_skyblock_crimson_isle.polar`    | `configuration/world/`        | SKYBLOCK_CRIMSON_ISLE                     |
| `hypixel_skyblock_dungeon_hub.polar`     | `configuration/world/`        | SKYBLOCK_DUNGEON_HUB                      |
| `hypixel_skyblock_gold_mine.polar`       | `configuration/world/`        | SKYBLOCK_GOLD_MINE                        |
| `hypixel_skyblock_deep_caverns.polar`    | `configuration/world/`        | SKYBLOCK_DEEP_CAVERNS                     |
| `hypixel_skyblock_dwarven_mines.polar`   | `configuration/world/`        | SKYBLOCK_DWARVEN_MINES                    |
| `hypixel_skyblock_the_park.polar`        | `configuration/world/`        | SKYBLOCK_THE_PARK                         |
| `hypixel_skyblock_galatea.polar`         | `configuration/world/`        | SKYBLOCK_GALATEA                          |
| `hypixel_skyblock_backwater_bayou.polar` | `configuration/world/`        | SKYBLOCK_BACKWATER_BAYOU                  |
| `hypixel_skyblock_jerrys_workshop.polar` | `configuration/world/`        | SKYBLOCK_JERRYS_WORKSHOP                  |
| `hypixel_main_lobby.polar`               | `configuration/world/`        | MAIN_LOBBY                                |
| `hypixel_prototype_lobby.polar`          | `configuration/world/`        | PROTOTYPE_LOBBY                           |
| `hypixel_bedwars_lobby.polar`            | `configuration/world/`        | BEDWARS_LOBBY, BEDWARS_CONFIGURATOR       |
| `hypixel_skywars_lobby.polar`            | `configuration/world/`        | SKYWARS_LOBBY                             |
| `hypixel_murder_mystery_lobby.polar`     | `configuration/world/`        | MURDER_MYSTERY_LOBBY, MURDER_MYSTERY_CONFIGURATOR |
| `hypixel_ravengard_lobby.polar`          | `configuration/world/`        | RAVENGARD_LOBBY                           |
| `hypixel_ravengard_tutorial.polar`       | `configuration/world/`        | RAVENGARD_LOBBY (tutorial instance)       |
| `dungeon_1.polar`                        | `configuration/ravengard/`    | RAVENGARD_DUNGEON                         |
| `<map>.polar`                            | `configuration/bedwars/`      | BEDWARS_GAME                              |
| `<map>.polar`                            | `configuration/skywars/`      | SKYWARS_GAME                              |
| `<map>.polar`                            | `configuration/murdermystery/`| MURDER_MYSTERY_GAME                       |

## Docker Reference

```yaml
# SkyBlock Island
hypixelcore_island:
  environment:
    SERVICE_CMD: java -jar HypixelCore.jar SKYBLOCK_ISLAND

# SkyBlock Hub
hypixelcore_hub:
  environment:
    SERVICE_CMD: java -jar HypixelCore.jar SKYBLOCK_HUB

# Main Lobby
hypixelcore_main_lobby:
  environment:
    SERVICE_CMD: java -jar HypixelCore.jar MAIN_LOBBY

# BedWars Game
hypixelcore_bedwars_game:
  environment:
    SERVICE_CMD: java -jar HypixelCore.jar BEDWARS_GAME
```

See [Adding Servers with Docker](/docs/docker/adding-servers) for the full compose template.
