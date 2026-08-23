# Services Setup

Services are independent microservices that handle specific features. They communicate with game servers via Redis.

## Overview

| Service       | JAR Name                  | Purpose                      |
|---------------|---------------------------|------------------------------|
| API           | `ServiceAPI.jar`          | REST API for external access |
| Auction House | `ServiceAuctionHouse.jar` | Manages auction listings     |
| Bazaar        | `ServiceBazaar.jar`       | Market/trading operations    |
| Party         | `ServiceParty.jar`        | Player party management      |
| Item Tracker  | `ServiceItemTracker.jar`  | Tracks items across servers  |
| Dark Auction  | `ServiceDarkAuction.jar`  | Dark auction events          |
| Orchestrator  | `ServiceOrchestrator.jar` | Minigame server orchestration|
| Friend        | `ServiceFriend.jar`       | Friend system and presence   |
| Election      | `ServiceElection.jar`     | SkyBlock elections           |
| Guild         | `ServiceGuild.jar`        | Guild management             |
| Punishment    | `ServicePunishment.jar`   | Bans and mutes               |
| Replay        | `ServiceReplay.jar`       | Game replay storage          |
| Store         | `ServiceStore.jar`        | Stripe purchase fulfillment  |

## Download

Download all service JARs from the [releases page](https://github.com/Swofty-Developments/HypixelRecreation/releases/tag/latest).

## Directory Structure

Services share configuration with game servers:

```
services/
├── ServiceAPI.jar
├── ServiceAuctionHouse.jar
├── ServiceBazaar.jar
├── ServiceParty.jar
├── ServiceItemTracker.jar
├── ServiceDarkAuction.jar
├── ServiceOrchestrator.jar
├── ServiceFriend.jar
├── ServiceElection.jar
├── ServiceGuild.jar
├── ServicePunishment.jar
├── ServiceReplay.jar
├── ServiceStore.jar
└── configuration/
    └── config.yml    # Same as game servers
```

## Starting Services

Each service runs as a separate process:

```bash
java -jar ServiceAPI.jar
java -jar ServiceAuctionHouse.jar
java -jar ServiceBazaar.jar
java -jar ServiceParty.jar
java -jar ServiceItemTracker.jar
java -jar ServiceDarkAuction.jar
java -jar ServiceOrchestrator.jar
java -jar ServiceFriend.jar
java -jar ServiceElection.jar
java -jar ServiceGuild.jar
java -jar ServicePunishment.jar
java -jar ServiceReplay.jar
java -jar ServiceStore.jar
```

:::alert note
Services should be started after MongoDB and Redis are running, but before or alongside game servers.
:::

## Service Details

### API Service

Provides REST endpoints for external applications.

```bash
java -jar ServiceAPI.jar
# Or with custom port:
java -jar ServiceAPI.jar --port=8081
```

**Default Port**: 8080

**Key Endpoints**:
- Authentication via session cookies
- Admin panel at `/panel/authenticated`
- User and profile data access

### Auction House Service

Manages all auction functionality.

```bash
java -jar ServiceAuctionHouse.jar
```

**MongoDB Collections**:
- `active-auctions` - Current listings
- `inactive-auctions` - Completed/expired auctions

**Features**:
- Create, bid, and complete auctions
- Auction caching for performance

### Bazaar Service

Handles the bazaar marketplace.

```bash
java -jar ServiceBazaar.jar
```

**MongoDB Collections**:
- `bazaarOrders` - Buy/sell orders
- `pendingTransactions` - Transactions awaiting processing

**Features**:
- Order management
- Order matching and execution

### Party Service

Manages player parties and groups.

```bash
java -jar ServiceParty.jar
```

**Features**:
- Party creation and management
- Invitation handling
- Cross-server party sync

### Item Tracker Service

Tracks valuable items across servers.

```bash
java -jar ServiceItemTracker.jar
```

**MongoDB Collections**:
- `tracked-items` - Item tracking data

### Dark Auction Service

Manages periodic dark auction events.

```bash
java -jar ServiceDarkAuction.jar
```

**Features**:
- Scheduled based on SkyBlock time
- Auction state management

### Orchestrator Service

Coordinates minigame servers (BedWars, SkyWars, Murder Mystery).

```bash
java -jar ServiceOrchestrator.jar
```

**Features**:
- Server heartbeat monitoring
- Game map assignment
- Player rejoining

### Friend Service

Manages the friend system across the network.

```bash
java -jar ServiceFriend.jar
```

**MongoDB Collections**:
- `friend-data` - Friend lists
- `pending-friend-requests` - Pending requests

**Features**:
- Friend requests and lists
- Presence (online status) tracking

### Election Service

Runs SkyBlock elections (e.g. the mayor election).

```bash
java -jar ServiceElection.jar
```

**MongoDB Collections**:
- `elections` - Election definitions and state
- `election-votes` - Cast votes
- `election-tallies` - Vote tallies

**Features**:
- Election lifecycle and resolution
- Voting with one vote per player

### Guild Service

Manages player guilds.

```bash
java -jar ServiceGuild.jar
```

**MongoDB Collections**:
- `guilds` - Guild definitions
- `player-guilds` - Player-to-guild membership

**Features**:
- Guild creation and membership
- Guild events and settings

### Punishment Service

Handles bans and mutes.

```bash
java -jar ServicePunishment.jar
```

**MongoDB Collections**:
- `punishments` - Punishment records

**Features**:
- Active punishment tracking
- Punish / unpunish players

### Replay Service

Stores and serves recorded game replays for Replay Viewer servers.

```bash
java -jar ServiceReplay.jar
```

**MongoDB Collections**:
- `replays` - Replay metadata
- `replay_data` - Recorded replay data
- `replay_maps` - Uploaded replay maps

**Features**:
- Recording session management
- Streaming replay data to Replay Viewer servers

### Store Service

Fulfills paid Stripe purchases from the web store.

```bash
java -jar ServiceStore.jar
```

**MongoDB Collections**:

- `store_purchases` - Checkout orders and fulfillment state
- `stripe_events` - Stripe webhook idempotency records
- `store-player-entitlements` - Per-player projection of awarded store goods

**Features**:

- Leases paid purchases before fulfillment
- Retries failed or interrupted fulfillment with backoff
- Talks to the Velocity proxy to award ranks, Gold, Gems, boosters, cosmetics, and feature flags
- Recovers purchases paid while the service was offline

See [Store Payments](/docs/setup/store-payments) for Stripe setup.

## Required vs Optional Services

### Required for Core Gameplay
- **Party** - Needed for party features

### Required for Economy
- **Auction House** - For auction functionality
- **Bazaar** - For bazaar functionality

### Recommended Defaults
The installer enables **Party, API, Auction House, Bazaar, and Item Tracker** by default.

### Optional
- **API** - Only needed for external integrations
- **Item Tracker** - For item tracking features
- **Dark Auction** - For dark auction events
- **Orchestrator** - Mainly for BedWars/SkyWars/Murder Mystery
- **Friend** - For the friend system
- **Election** - For SkyBlock elections
- **Guild** - For guilds
- **Punishment** - For bans and mutes
- **Replay** - For game replay storage (needed by Replay Viewer servers)
- **Store** - Needed for paid store delivery

## Memory Allocation

Services are lightweight and can share a single machine:

```bash
java -Xms256M -Xmx512M -jar ServiceAPI.jar
```

Typical usage: 256-512 MB per service.
