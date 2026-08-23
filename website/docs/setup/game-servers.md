# Game Servers Setup

Game servers run the actual gameplay using Minestom. Each server runs as a specific ServerType.

## Download Required Files

1. Download `HypixelCore.jar` from the [releases page](https://github.com/Swofty-Developments/HypixelRecreation/releases/tag/latest)
2. Download [`config.yml`](https://github.com/Swofty-Developments/HypixelRecreation/tree/master/configuration)
3. Download [world files](https://files.catbox.moe/flri48.zip) (all server worlds + `limbo.schem`; extract into `configuration/`)
4. Download [`PicoLimbo.jar`](https://github.com/Swofty-Developments/HypixelRecreation/tree/master/configuration) and its config

## Directory Structure

```
gameserver/
├── HypixelCore.jar
├── configuration/
│   ├── config.yml
│   ├── skyblock/
│   │   ├── collections/
│   │   ├── items/
│   │   ├── levels/
│   │   ├── reforges/
│   │   ├── skills/
│   │   ├── Minestom.fairysouls.yml
│   │   ├── Minestom.regions.yml
│   │   ├── Minestom.crystals.yml
│   │   ├── pack_textures/    # Optional
│   │   └── songs/            # Optional
│   ├── bedwars/              # BedWars maps (.polar) + maps.json
│   ├── murdermystery/        # Murder Mystery maps (.polar) + maps.json
│   ├── skywars/              # SkyWars maps (.polar) + map configs (.json)
│   ├── ravengard/            # Ravengard dungeon worlds + data
│   ├── resourcepacks/        # Resource packs (e.g. ravengard-original.zip)
│   └── world/
│       ├── hypixel_main_lobby.polar
│       ├── hypixel_prototype_lobby.polar
│       ├── hypixel_bedwars_lobby.polar
│       ├── hypixel_murder_mystery_lobby.polar
│       ├── hypixel_skywars_lobby.polar
│       ├── hypixel_ravengard_lobby.polar
│       ├── hypixel_ravengard_tutorial.polar
│       └── ... (other worlds)
```

## Setup Steps

### 1. Create Directory Structure

```bash
mkdir -p gameserver/configuration/world
```

### 2. Configure config.yml

Copy the `forwarding.secret` from your Velocity proxy directory and add it to `config.yml`:

```yaml
host-name: 0.0.0.0
mongodb: mongodb://localhost
redis-uri: redis://localhost:6379
velocity-secret: your-forwarding-secret-here
require-auth: false
integrations:
    spark: false
    anticheat: false
    via-version: false
    sentry-dsn: ''
limbo:
    host-name: 127.0.0.1
    port: 65535
```

### 3. Install World Files

Extract the world files zip directly into your `configuration/` folder. The zip already contains the correct folder structure.

### 4. Install Data Files

Download from [configuration/skyblock](https://github.com/Swofty-Developments/HypixelRecreation/tree/master/configuration/skyblock):

- `skills/` folder → `configuration/skyblock/skills/`
- `levels/` folder → `configuration/skyblock/levels/`
- `reforges/` folder → `configuration/skyblock/reforges/`
- `items/` folder → `configuration/skyblock/items/`
- `collections/` folder → `configuration/skyblock/collections/`
- `Minestom.fairysouls.yml`, `Minestom.regions.yml`, `Minestom.crystals.yml` → `configuration/skyblock/`
- `songs/` folder → `configuration/skyblock/songs/` (optional)

### 5. Setup PicoLimbo

PicoLimbo handles the limbo state:

1. Place `PicoLimbo.jar` in a separate directory
2. Edit the `server.toml` which you can find in the repository configuration folder:
   - Set `method="MODERN"`
   - Set `secret="YOUR_SECRET"` (or FORWARDING_SECRET environment variable by default)
3. Move the `limbo.schem` file to the same directory, or set schematic_file to empty: `schematic_file=""`
4. Keep it running in the background

### 6. Start a Game Server

```bash
java -jar HypixelCore.jar SKYBLOCK_ISLAND
```

Replace `SKYBLOCK_ISLAND` with any valid [ServerType](/docs/reference/server-types).

## Starting Multiple Servers

You can run multiple game servers simultaneously. Each ServerType can have multiple instances:

```bash
# Terminal 1 - Island server
java -jar HypixelCore.jar SKYBLOCK_ISLAND

# Terminal 2 - Hub server
java -jar HypixelCore.jar SKYBLOCK_HUB

# Terminal 3 - Another hub for load balancing
java -jar HypixelCore.jar SKYBLOCK_HUB
```

## Admin Setup

Ranks are not stored in MongoDB. They live in Redis on the account document at `hsb:acct:players:<uuid>`, under the `hypixel:rank` field, so editing anything in MongoDB Compass will not promote you.

Join the server once first so your account document exists, then use one of these two routes.

### Installer dashboard

If you deployed with the [Docker installer](/docs/docker/setup), choose **Make player STAFF** on the management dashboard and enter your Minecraft username. It rewrites `hypixel:rank` on your Redis account document for you.

### In-game command

If you already have a staff account, promote others with the rank command (also available as `/setrank`):

```
/rank <player> <rank>
```

The command requires the `STAFF` rank and can also be run from the server console.

## Memory Allocation

For production, allocate appropriate memory:

```bash
java -Xms2G -Xmx2G -jar HypixelCore.jar SKYBLOCK_HUB
```

Recommended per server type:
- Hub: 2-4 GB
- Island: 1-2 GB
- Other locations: 1-2 GB
