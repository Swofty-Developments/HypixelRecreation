# Services Reference

Services are microservices that handle specific features independently of game servers. There are **13 services**; every service also depends on `service.generic`, the shared service base library (MongoDB connection, Redis manager, service initializer) — it is not a runnable service itself.

The full list lives in `net.swofty.commons.ServiceType`.

## Architecture Overview

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Game Server   │────▶│      Redis      │◀────│   Game Server   │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
         ┌───────────┬───────────┼───────────┬───────────┐
         ▼           ▼           ▼           ▼           ▼
┌─────────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
│ AuctionHouse│ │ Bazaar  │ │  Party  │ │ Friend  │ │  Guild  │
└─────────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘
         ┌───────────┬───────────┬───────────┬───────────┐
         ▼           ▼           ▼           ▼           ▼
┌─────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐
│ Election│ │Punishment│ │  Replay  │ │   API    │ │  Store  │
└─────────┘ └──────────┘ └──────────┘ └──────────┘ └─────────┘
         ┌───────────┬───────────┐
         ▼           ▼
┌─────────────┐ ┌─────────────┐
│ItemTracker  │ │DarkAuction  │
└─────────────┘ └─────────────┘
         ┌───────────┐
         ▼
┌─────────────┐
│ Orchestrator│
└─────────────┘
         ┌─────────────────────────────────────────────────┐
         │                           MongoDB                │
         └─────────────────────────────────────────────────┘
```

## Service List

### ServiceAPI

**JAR**: `ServiceAPI.jar`
**Port**: 8080 (configurable via `--port=`)
**ServiceType**: `API`

REST API service for external integrations and the web forums.

```bash
java -jar ServiceAPI.jar
java -jar ServiceAPI.jar --port=8081  # Custom port
```

**Features**:
- HTTP endpoints under `/api/...` using Spark Framework
- Session-cookie authentication with an admin panel at `/panel/authenticated`
- API key management
- User and profile database access

**MongoDB Collections**:
- `api-key` - API keys
- `api-admin` - Admin sessions
- `api-request-counts` - Per-key request counts used for rate limiting

Accounts and profiles are not MongoDB collections; they live in Redis under the `hsb:acct` and `hsb:prof` prefixes.

---

### ServiceAuctionHouse

**JAR**: `ServiceAuctionHouse.jar`
**ServiceType**: `AUCTION_HOUSE`

Manages auction house listings and transactions.

```bash
java -jar ServiceAuctionHouse.jar
```

**Features**:
- Create, bid, and complete auctions
- Active/inactive auction tracking
- Auction caching for performance

**MongoDB Collections**:
- `active-auctions` - Current listings
- `inactive-auctions` - Completed/expired auctions

---

### ServiceBazaar

**JAR**: `ServiceBazaar.jar`
**ServiceType**: `BAZAAR`

Handles the bazaar marketplace system.

```bash
java -jar ServiceBazaar.jar
```

**Features**:
- Buy and sell order management
- Order matching and execution
- Pending transaction queue

**MongoDB Collections**:
- `bazaarOrders` - Buy/sell orders
- `pendingTransactions` - Transactions awaiting processing

---

### ServiceParty

**JAR**: `ServiceParty.jar`
**ServiceType**: `PARTY`

Manages player parties across servers. This service is required for any deployment.

```bash
java -jar ServiceParty.jar
```

**Features**:
- Party creation and disbanding
- Invitation system
- Cross-server party synchronization
- Party chat routing

**Endpoints**:
- `GetPartyEndpoint` - Retrieve party info
- `IsPlayerInPartyEndpoint` - Check membership
- `PartyActionEndpoint` - Party actions (create, invite, join, leave, etc.)

Parties are cached in memory (`PartyCache`) and synchronized through Redis.

---

### ServiceItemTracker

**JAR**: `ServiceItemTracker.jar`
**ServiceType**: `ITEM_TRACKER`

Tracks valuable items across the server network.

```bash
java -jar ServiceItemTracker.jar
```

**Features**:
- Item location tracking
- Item history logging
- Cross-server item queries

**MongoDB Collections**:
- `tracked-items` - Item tracking data

---

### ServiceDarkAuction

**JAR**: `ServiceDarkAuction.jar`
**ServiceType**: `DARK_AUCTION`

Manages periodic dark auction events.

```bash
java -jar ServiceDarkAuction.jar
```

**Features**:
- Scheduled events based on SkyBlock time
- Auction state management
- Prize pool handling (item and book pools)

**Components**:
- `DarkAuctionScheduler` - Event timing
- `DarkAuctionState` - State management
- `loot/` - Prize pools

**Endpoints**:
- `EndpointGetAuctionState` - Current auction state
- `EndpointPlaceBid` - Place a bid
- `EndpointPlayerLeftAuction` - Player left event
- `EndpointTriggerAuction` - Force-trigger an auction

---

### ServiceOrchestrator

**JAR**: `ServiceOrchestrator.jar`
**ServiceType**: `ORCHESTRATOR`

Orchestrates minigame server management (BedWars, SkyWars, Murder Mystery).

```bash
java -jar ServiceOrchestrator.jar
```

**Features**:
- Server health monitoring via heartbeats
- Game map management
- Player game assignment
- Rejoin handling

**Endpoints**:
- `GameHeartbeatEndpoint` - Server status
- `GetMapsEndpoint` - Available maps
- `GetServerForMapEndpoint` - Server assignment
- `RejoinGameEndpoint` - Rejoin requests
- `GameChooseEndpoint` - Game selection
- `ListGamesEndpoint` - Active games per server
- `GetGameCountsEndpoint` - Game counts

---

### ServiceFriend

**JAR**: `ServiceFriend.jar`
**ServiceType**: `FRIEND`

Manages the friend system across the network.

```bash
java -jar ServiceFriend.jar
```

**Features**:
- Friend lists and pending friend requests
- Presence (online status) tracking across servers
- Friend request caching with expiration

**MongoDB Collections**:
- `friend-data` - Friend lists
- `pending-friend-requests` - Pending requests

**Endpoints**:
- `GetFriendDataEndpoint` - Friend list for a player
- `GetPendingRequestsEndpoint` - Pending requests
- `AreFriendsEndpoint` - Friendship check
- `FriendEventToServiceEndpoint` - Friend events (request, accept, remove)
- `GetPresenceEndpoint` / `UpdatePresenceEndpoint` - Presence queries and updates

---

### ServiceElection

**JAR**: `ServiceElection.jar`
**ServiceType**: `ELECTION`

Runs SkyBlock elections (e.g. the annual mayor election).

```bash
java -jar ServiceElection.jar
```

**Features**:
- Election lifecycle management
- Voting with one vote per player
- Candidate lists and vote tallies
- Election resolution

**MongoDB Collections**:
- `elections` - Election definitions and state
- `election-votes` - Cast votes
- `election-tallies` - Vote tallies

**Endpoints**:
- `StartElectionEndpoint` - Start an election
- `GetElectionDataEndpoint` - Election state and data
- `GetCandidatesEndpoint` - Candidate list
- `CastVoteEndpoint` - Cast a vote
- `GetPlayerVoteEndpoint` - Player's vote status
- `ResolveElectionEndpoint` - Resolve/finish an election

---

### ServiceGuild

**JAR**: `ServiceGuild.jar`
**ServiceType**: `GUILD`

Manages player guilds.

```bash
java -jar ServiceGuild.jar
```

**Features**:
- Guild creation, membership and management
- Guild settings and events
- Cross-server guild synchronization

**MongoDB Collections**:
- `guilds` - Guild definitions
- `player-guilds` - Player-to-guild membership

**Endpoints**:
- `GetGuildEndpoint` - Guild info
- `IsPlayerInGuildEndpoint` - Membership check
- `GuildEventToServiceEndpoint` - Guild events

---

### ServicePunishment

**JAR**: `ServicePunishment.jar`
**ServiceType**: `PUNISHMENT`

Handles bans and mutes across the network.

```bash
java -jar ServicePunishment.jar
```

**Features**:
- Active punishment tracking
- Punish / unpunish players
- Punishment events pushed to the proxy and game servers

**MongoDB Collections**:
- `punishments` - Punishment records

**Endpoints**:
- `PunishPlayerEndpoint` - Punish a player
- `UnpunishPlayerEndpoint` - Remove a punishment
- `GetActivePunishmentEndpoint` - Active punishment for a player

---

### ServiceReplay

**JAR**: `ServiceReplay.jar`
**ServiceType**: `REPLAY`

Stores and serves recorded game replays.

```bash
java -jar ServiceReplay.jar
```

**Features**:
- Recording session management (with cleanup task)
- Replay metadata, data and map storage
- Streaming replay data to Replay Viewer servers

**MongoDB Collections**:
- `replays` - Replay metadata
- `replay_data` - Recorded replay data
- `replay_maps` - Uploaded replay maps

**Endpoints**:
- `ReplayStartEndpoint` / `ReplayEndEndpoint` - Session lifecycle
- `ReplayDataBatchEndpoint` - Recorded data batches
- `ReplayLoadEndpoint` / `ReplayListEndpoint` / `ReplayChooseEndpoint` - Loading and browsing
- `ReplayMapUploadEndpoint` / `ReplayMapLoadEndpoint` - Map storage

---

### ServiceStore

**JAR**: `ServiceStore.jar`
**ServiceType**: `STORE`

Fulfills paid web-store purchases.

```bash
java -jar ServiceStore.jar
```

**Features**:

- Durable Stripe purchase recovery
- MongoDB fulfillment leases
- Idempotent proxy entitlement application
- Rank, Gold, Gems, booster, cosmetic, and feature-flag awards

**MongoDB Collections**:

- `store_purchases` - Checkout and fulfillment state
- `stripe_events` - Webhook idempotency records
- `store-player-entitlements` - Award projection by player

**Proxy Protocols**:

- `StorePurchaseFulfillmentProtocol` - Applies one paid purchase to a player entitlement projection

See [Store Payments](/docs/setup/store-payments) for Stripe dashboard and webhook setup.

## Communication Protocol

Services communicate via Redis pub/sub with a specific protocol:

### Request Format
```
{request_id};{serialized_message}
```

### Response Format
```
{request_id}}=-=---={serialized_response}
```

### Protocol Objects

Located in `net.swofty.commons.protocol.objects`, these handle serialization:

```java
// Example endpoint registration
ServiceInitializer.register(
    ServiceType.AUCTION_HOUSE,
    GetAuctionEndpoint.class,
    GetAuctionRequest.class
);
```

## Memory Requirements

| Service       | Minimum RAM | Recommended RAM |
|---------------|-------------|-----------------|
| API           | 256 MB      | 512 MB          |
| Auction House | 256 MB      | 512 MB          |
| Bazaar        | 256 MB      | 512 MB          |
| Party         | 128 MB      | 256 MB          |
| Item Tracker  | 128 MB      | 256 MB          |
| Dark Auction  | 128 MB      | 256 MB          |
| Orchestrator  | 128 MB      | 256 MB          |
| Friend        | 128 MB      | 256 MB          |
| Election      | 128 MB      | 256 MB          |
| Guild         | 128 MB      | 256 MB          |
| Punishment    | 128 MB      | 256 MB          |
| Replay        | 256 MB      | 512 MB          |
| Store         | 128 MB      | 256 MB          |

## Docker Reference

The Docker deployment lists every service; enable the ones you need in the installer's service selection screen:

```yaml
service_api:
  image: game_server_prepared
  environment:
    SERVICE_CMD: java -jar ServiceAPI.jar

service_auctionhouse:
  image: game_server_prepared
  environment:
    SERVICE_CMD: java -jar ServiceAuctionHouse.jar

service_bazaar:
  image: game_server_prepared
  environment:
    SERVICE_CMD: java -jar ServiceBazaar.jar
```

## Health Checks

Services don't expose HTTP health endpoints by default. Monitor them via:

1. **Redis connectivity** - Services publish heartbeats
2. **Log output** - Check for error messages
3. **MongoDB connectivity** - Verify database operations
