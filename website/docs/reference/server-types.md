# Server Types Reference

ServerTypes define the different game modes and locations that can be run as separate server instances.

## SkyBlock Server Types

These server types are part of the SkyBlock gamemode and share SkyBlock-specific functionality.

| ServerType                     | Description      | World Required                           |
|--------------------------------|------------------|------------------------------------------|
| `SKYBLOCK_ISLAND`              | Personal Island  | `hypixel_skyblock_island_template.polar` |
| `SKYBLOCK_HUB`                 | Hub              | `hypixel_skyblock_hub.polar`             |
| `SKYBLOCK_SPIDERS_DEN`         | Spider's Den     | `hypixel_skyblock_spiders_den.polar`     |
| `SKYBLOCK_THE_END`             | The End          | `hypixel_skyblock_the_end.polar`         |
| `SKYBLOCK_CRIMSON_ISLE`        | Crimson isle     | `hypixel_skyblock_crimson_isle.polar`    |
| `SKYBLOCK_DUNGEON_HUB`         | Dungeon hub      | `hypixel_skyblock_dungeon_hub.polar`     |
| `SKYBLOCK_THE_FARMING_ISLANDS` | Farming Islands  | `hypixel_skyblock_hub.polar`             |
| `SKYBLOCK_GOLD_MINE`           | Gold Mine        | `hypixel_skyblock_gold_mine.polar`       |
| `SKYBLOCK_DEEP_CAVERNS`        | Deep Caverns     | `hypixel_skyblock_deep_caverns.polar`    |
| `SKYBLOCK_DWARVEN_MINES`       | Dwarven Mines    | `hypixel_skyblock_dwarven_mines.polar`   |
| `SKYBLOCK_THE_PARK`            | The Park         | `hypixel_skyblock_the_park.polar`        |
| `SKYBLOCK_GALATEA`             | Galatea          | `hypixel_skyblock_galatea.polar`         |
| `SKYBLOCK_BACKWATER_BAYOU`     | Backwater Bayou  | `hypixel_skyblock_backwater_bayou.polar` |
| `SKYBLOCK_JERRYS_WORKSHOP`     | Jerry's Workshop | `hypixel_skyblock_jerrys_workshop.polar` |
| `SKYBLOCK_LOTUS_ATOLL`         | Lotus Atoll      | Not bundled                           |
| `SKYBLOCK_SAFARI`              | Safari           | Not bundled                           |
### Starting a SkyBlock Server

```bash
java -jar HypixelCore.jar SKYBLOCK_ISLAND
java -jar HypixelCore.jar SKYBLOCK_HUB
java -jar HypixelCore.jar SKYBLOCK_SPIDERS_DEN
```

## Non-SkyBlock Server Types

These server types run independently of SkyBlock features.

| ServerType             | Description                    |
|------------------------|--------------------------------|
| `PROTOTYPE_LOBBY`      | Prototype/testing lobby        |
| `BEDWARS_LOBBY`        | BedWars lobby server           |
| `BEDWARS_GAME`         | Active BedWars game server     |
| `BEDWARS_CONFIGURATOR` | BedWars map configuration tool |

### Starting Non-SkyBlock Servers

```bash
java -jar HypixelCore.jar PROTOTYPE_LOBBY
java -jar HypixelCore.jar BEDWARS_LOBBY
java -jar HypixelCore.jar BEDWARS_GAME
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
├── SkyBlockTypeLoader (SkyBlock-specific base)
│   ├── HubLoader
│   ├── IslandLoader
│   ├── SpidersDenLoader
│   └── ...
└── BedWarsTypeLoader
    ├── BedWarsLobbyLoader
    ├── BedWarsGameLoader
    └── BedWarsConfiguratorLoader
```

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

### Required Worlds

| World                                    | Location               | Used By         |
|------------------------------------------|------------------------|-----------------|
| `hypixel_skyblock_hub.polar`             | `configuration/world/` | SKYBLOCK_HUB    |
| `hypixel_skyblock_island_template.polar` | `configuration/world/` | SKYBLOCK_ISLAND |
| `hypixel_prototype_lobby.polar`          | `configuration/world/` | PROTOTYPE_LOBBY |

### Custom Worlds

Other locations require their own world files. These can be created or obtained separately.

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

# Spider's Den
hypixelcore_spiders:
  environment:
    SERVICE_CMD: java -jar HypixelCore.jar SKYBLOCK_SPIDERS_DEN
```
