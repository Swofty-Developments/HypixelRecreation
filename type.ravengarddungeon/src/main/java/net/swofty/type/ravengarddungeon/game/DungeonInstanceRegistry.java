package net.swofty.type.ravengarddungeon.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.ravengarddungeon.generator.RavengardDungeonGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class DungeonInstanceRegistry {
    public static final int MAX_INSTANCES = 4;
    private static final int STANDARD_ROOM_COUNT = 120;
    private static final int STANDARD_MAX_PLAYERS = 5;

    private static final Map<UUID, DungeonInstance> INSTANCES = new ConcurrentHashMap<>();

    private DungeonInstanceRegistry() {
    }

    public static final long EMPTY_LIFETIME_MILLIS = 30_000;

    public static final class DungeonInstance {
        private final UUID gameId = UUID.randomUUID();
        private final String mode;
        private final long seed;
        private final InstanceContainer instance;
        private final net.minestom.server.instance.SharedInstance shared;
        private final CompletableFuture<Void> ready;
        private final RavengardDungeonGenerator.GeneratedDungeon generated;
        private final Set<UUID> players = ConcurrentHashMap.newKeySet();
        private volatile long emptySince = System.currentTimeMillis();

        public void markPlayerJoined(UUID player) {
            players.add(player);
            emptySince = 0;
        }

        public void markPlayerLeft(UUID player) {
            if (players.remove(player) && players.isEmpty()) {
                emptySince = System.currentTimeMillis();
            }
        }

        public long getRemainingLifeSeconds() {
            if (!players.isEmpty() || emptySince == 0) return -1;
            return Math.max(0, (EMPTY_LIFETIME_MILLIS - (System.currentTimeMillis() - emptySince)) / 1000);
        }

        public boolean isExpired() {
            return players.isEmpty() && emptySince > 0
                    && System.currentTimeMillis() - emptySince > EMPTY_LIFETIME_MILLIS;
        }

        private DungeonInstance(String mode, long seed, InstanceContainer instance,
                                net.minestom.server.instance.SharedInstance shared,
                                RavengardDungeonGenerator.GeneratedDungeon generated,
                                CompletableFuture<Void> ready) {
            this.mode = mode;
            this.seed = seed;
            this.instance = instance;
            this.shared = shared;
            this.generated = generated;
            this.ready = ready;
        }

        public UUID getGameId() {
            return gameId;
        }

        public String getMode() {
            return mode;
        }

        public long getSeed() {
            return seed;
        }

        /** The instance players actually join, sharing the container's chunks. */
        public net.minestom.server.instance.Instance getInstance() {
            return shared;
        }

        public InstanceContainer getContainer() {
            return instance;
        }

        public CompletableFuture<Void> whenReady() {
            return ready;
        }

        public RavengardDungeonGenerator.GeneratedDungeon getGenerated() {
            return generated;
        }

        public Set<UUID> getPlayers() {
            return players;
        }

        public boolean isAcceptingJoins() {
            return mode.equals("STANDARD") && players.size() < STANDARD_MAX_PLAYERS;
        }

        public record MapTile(double worldX, double worldZ, int roomIndex) {
        }

        private volatile List<MapTile> mapTiles;
        private final Set<Integer> visitedRooms = ConcurrentHashMap.newKeySet();

        public Set<Integer> getVisitedRooms() {
            return visitedRooms;
        }

        /** Fixed world-grid mosaic of the layout, one tile per grid cell a room covers. */
        public List<MapTile> getMapTiles(double blocksPerTile) {
            if (mapTiles == null) {
                synchronized (this) {
                    if (mapTiles == null) {
                        List<MapTile> tiles = new ArrayList<>();
                        var placements = generated.dungeon().getPlacements();
                        java.util.Map<Long, Integer> grid = new java.util.HashMap<>();
                        for (int index = 0; index < placements.size(); index++) {
                            var placement = placements.get(index);
                            final int roomIndex = index;
                            placement.forEachMaskCell((localX, localZ) -> {
                                long gridX = Math.round((placement.originX() + localX) / blocksPerTile);
                                long gridZ = Math.round((placement.originZ() + localZ) / blocksPerTile);
                                grid.putIfAbsent((gridX << 32) | (gridZ & 0xFFFFFFFFL), roomIndex);
                            });
                        }
                        for (var entry : grid.entrySet()) {
                            long key = entry.getKey();
                            tiles.add(new MapTile((key >> 32) * blocksPerTile,
                                    ((int) key) * blocksPerTile, entry.getValue()));
                        }
                        mapTiles = tiles;
                    }
                }
            }
            return mapTiles;
        }

        /** Index of the placement whose footprint contains the position, or -1. */
        public int roomIndexAt(double worldX, double worldZ) {
            var placements = generated.dungeon().getPlacements();
            for (int index = 0; index < placements.size(); index++) {
                var placement = placements.get(index);
                int localX = (int) Math.floor(worldX) - placement.originX();
                int localZ = (int) Math.floor(worldZ) - placement.originZ();
                if (localX < 0 || localZ < 0 || localX >= placement.getFootprintWidth()
                        || localZ >= placement.getFootprintDepth()) {
                    continue;
                }
                int[] source = placement.toSourceFrame(localX, localZ);
                for (int[] run : placement.room().getMask()) {
                    if (run[0] == source[1] && source[0] >= run[1] && source[0] <= run[2]) {
                        return index;
                    }
                }
            }
            return -1;
        }
    }

    public static DungeonInstance create(String mode) {
        long seed;
        int roomCount = STANDARD_ROOM_COUNT;
        if (mode.startsWith("ADMIN")) {
            String[] parts = mode.split(":");
            seed = parts.length > 1 ? Long.parseLong(parts[1]) : ThreadLocalRandom.current().nextLong();
            if (parts.length > 2) roomCount = Integer.parseInt(parts[2]);
        } else {
            seed = ThreadLocalRandom.current().nextLong();
        }

        RavengardDungeonGenerator.GeneratedDungeon generated =
                RavengardDungeonGenerator.generate(seed, roomCount);
        // the skyblock island recipe: fullbright dimension, container for the
        // blocks, shared instance for the players. The blueprint city IS the
        // dungeon; only swapped slot interiors get restamped on top of it.
        var dimensionKey = MinecraftServer.getDimensionTypeRegistry()
                .getKey(net.kyori.adventure.key.Key.key("ravengard:dungeon"));
        InstanceContainer instance = MinecraftServer.getInstanceManager()
                .createInstanceContainer(dimensionKey);
        net.minestom.server.instance.SharedInstance shared;
        try {
            shared = net.swofty.type.generic.world.HypixelWorldLoader.loadFrom(
                    java.nio.file.Path.of("./configuration/ravengard/dungeon_1.polar"),
                    instance, MinecraftServer.getInstanceManager());
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not load dungeon city base", exception);
        }
        CompletableFuture<Void> ready = RavengardDungeonGenerator.stampSwaps(generated, instance);

        DungeonInstance dungeonInstance = new DungeonInstance(
                mode.startsWith("ADMIN") ? "ADMIN" : "STANDARD", seed, instance, shared, generated, ready);
        ready.thenRun(() -> spawnInteractables(dungeonInstance));
        INSTANCES.put(dungeonInstance.getGameId(), dungeonInstance);
        return dungeonInstance;
    }

    private static void spawnInteractables(DungeonInstance dungeonInstance) {
        try {
            var config = net.swofty.type.ravengarddungeon.TypeRavengardDungeonLoader.getGame().getConfig();
            var container = dungeonInstance.getContainer();

            java.util.Set<Long> chunks = new java.util.HashSet<>();
            for (var object : config.objects()) {
                if (object.type().equals("door_1") || object.type().equals("door_1_top")) {
                    chunks.add((((long) ((int) object.x() >> 4)) << 32)
                            | (((int) object.z() >> 4) & 0xFFFFFFFFL));
                }
            }
            java.util.List<CompletableFuture<?>> loads = new java.util.ArrayList<>();
            for (long key : chunks) {
                loads.add(container.loadChunk((int) (key >> 32), (int) key));
            }
            CompletableFuture.allOf(loads.toArray(new CompletableFuture[0])).join();
            net.swofty.type.ravengarddungeon.interactables.DungeonDoor.spawnAll(
                    dungeonInstance.getInstance(), config.objects());

            int chests = net.swofty.type.ravengarddungeon.interactables.DungeonChest
                    .convertDecorChests(dungeonInstance.getInstance(), dungeonInstance.getSeed());
            org.tinylog.Logger.info("Spawned interactables: {} door chunks loaded, {} chests converted",
                    chunks.size(), chests);
        } catch (Exception exception) {
            org.tinylog.Logger.error(exception, "Failed to spawn dungeon interactables");
        }
    }

    private static Integer groundAt(net.minestom.server.instance.Instance instance,
                                    double x, double z) {
        var template = net.swofty.type.ravengarddungeon.generator.DungeonTemplate.get();
        int blockX = (int) Math.floor(x), blockZ = (int) Math.floor(z);
        for (int y = 75; y >= 60; y--) {
            if (template.blockAt(blockX, y, blockZ) != net.minestom.server.instance.block.Block.AIR) {
                if (template.blockAt(blockX, y + 1, blockZ) == net.minestom.server.instance.block.Block.AIR
                        && template.blockAt(blockX, y + 2, blockZ) == net.minestom.server.instance.block.Block.AIR) {
                    return y + 1;
                }
                return null;
            }
        }
        return null;
    }

    public static DungeonInstance get(UUID gameId) {
        return INSTANCES.get(gameId);
    }

    public static List<DungeonInstance> all() {
        return new ArrayList<>(INSTANCES.values());
    }

    public static int remainingSlots() {
        return Math.max(0, MAX_INSTANCES - INSTANCES.size());
    }

    public static DungeonInstance findByIdPrefix(String prefix) {
        for (DungeonInstance instance : INSTANCES.values()) {
            if (instance.getGameId().toString().equals(prefix)
                    || instance.getGameId().toString().startsWith(prefix)) {
                return instance;
            }
        }
        return null;
    }

    private static final Set<UUID> PENDING_TELEPORTS = ConcurrentHashMap.newKeySet();

    public static void sendPlayerIn(HypixelPlayer player, DungeonInstance instance) {
        if (!PENDING_TELEPORTS.add(player.getUuid())) {
            player.sendMessage("<7>Your dungeon is still being prepared, hold on...");
            return;
        }
        instance.markPlayerJoined(player.getUuid());
        player.sendMessage("<7>Preparing your dungeon (seed <f>{}<7>)...", instance.getSeed());
        boolean aerial = instance.getMode().equals("ADMIN");
        instance.whenReady().thenRun(() -> player.scheduler().scheduleNextTick(() -> {
            net.minestom.server.coordinate.Pos spawn;
            if (aerial) {
                // hover over the start room, which always exists; the bounds centre
                // of an organic layout can be a void pocket
                var start = instance.getGenerated().dungeon().getStartRoom();
                Integer startRoof = start.room().getRoofY();
                double hoverY = (startRoof != null ? startRoof : 97) + 40;
                spawn = new net.minestom.server.coordinate.Pos(
                        start.originX() + start.getFootprintWidth() / 2.0, hoverY,
                        start.originZ() + start.getFootprintDepth() / 2.0, 0, 90);
                player.setGameMode(net.minestom.server.entity.GameMode.CREATIVE);
            } else {
                spawn = instance.getGenerated().spawn().withY(67);
            }
            try {
                if (player.getInstance() == instance.getInstance()) {
                    player.teleport(spawn);
                } else {
                    player.setInstance(instance.getInstance(), spawn);
                }
                player.sendMessage("<a>Entered dungeon <f>{}<a> ({} rooms, mode {}).",
                        instance.getGameId().toString().substring(0, 8),
                        instance.getGenerated().dungeon().getRoomCount(),
                        instance.getMode());
                scheduleChunkRefresh(player, instance);
            } finally {
                PENDING_TELEPORTS.remove(player.getUuid());
            }
        }));
    }

    /** Chunk sections that reach the client while a resource reload is in flight
     * are silently dropped from its renderer and a client-side rebuild cannot
     * recover them, so the world is re-sent once the player has settled in. */
    private static void scheduleChunkRefresh(net.minestom.server.entity.Player player,
                                             DungeonInstance instance) {
        player.scheduler().buildTask(() -> {
            if (player.getInstance() != instance.getInstance()) {
                return;
            }
            int viewDistance = net.minestom.server.MinecraftServer.getChunkViewDistance() + 2;
            int playerChunkX = player.getPosition().chunkX();
            int playerChunkZ = player.getPosition().chunkZ();
            int sent = 0;
            for (var chunk : instance.getInstance().getChunks()) {
                if (Math.abs(chunk.getChunkX() - playerChunkX) <= viewDistance
                        && Math.abs(chunk.getChunkZ() - playerChunkZ) <= viewDistance) {
                    chunk.sendChunk(player);
                    sent++;
                }
            }
            org.tinylog.Logger.info("Refreshed {} chunks for {}", sent, player.getUsername());
        }).delay(net.minestom.server.timer.TaskSchedule.tick(40)).schedule();
    }

    public static double[] boundsCenter(DungeonInstance instance) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (var placement : instance.getGenerated().dungeon().getPlacements()) {
            minX = Math.min(minX, placement.originX());
            maxX = Math.max(maxX, placement.originX() + placement.getFootprintWidth());
            minZ = Math.min(minZ, placement.originZ());
            maxZ = Math.max(maxZ, placement.originZ() + placement.getFootprintDepth());
        }
        return new double[]{(minX + maxX) / 2.0, (minZ + maxZ) / 2.0};
    }

    public static void startExpiryTask() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (DungeonInstance instance : INSTANCES.values()) {
                if (instance.isExpired()) {
                    INSTANCES.remove(instance.getGameId());
                    MinecraftServer.getInstanceManager().unregisterInstance(instance.getInstance());
                }
            }
        }).repeat(net.minestom.server.timer.TaskSchedule.seconds(1)).schedule();
    }
}
