package net.swofty.type.ravengarddungeon.generator;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.swofty.dungeons.ravengard.Direction;
import net.swofty.dungeons.ravengard.RavengardDungeon;
import net.swofty.dungeons.ravengard.RavengardDungeon.PlacedSocket;
import net.swofty.dungeons.ravengard.RavengardDungeon.RoomPlacement;
import net.swofty.dungeons.ravengard.RavengardRoomCatalog;
import net.swofty.dungeons.ravengard.Rotation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class RavengardDungeonGenerator {
    private static final Path CATALOG_PATH = Path.of("./configuration/ravengard/dungeon_rooms.json");
    private static final Block DOOR_PLUG = Block.POLISHED_BLACKSTONE_BRICKS;
    private static final Map<String, Block> ROTATED_BLOCKS = new ConcurrentHashMap<>();
    private static final String[] CARDINAL_PROPERTIES = {"north", "east", "south", "west"};

    private static RavengardRoomCatalog catalog;

    public record PlacedObject(String category, String type, double x, double y, double z, double yaw) {
    }

    public record GeneratedDungeon(RavengardDungeon dungeon, List<PlacedObject> objects,
                                   Pos spawn, long seed) {
    }

    public static synchronized RavengardRoomCatalog getCatalog() {
        if (catalog == null) {
            catalog = RavengardRoomCatalog.load(CATALOG_PATH);
        }
        return catalog;
    }

    public static GeneratedDungeon generate(long seed, int targetRoomCount) {
        RavengardDungeon dungeon = RavengardDungeon.generate(getCatalog(), seed, targetRoomCount);

        List<PlacedObject> objects = new ArrayList<>();
        for (RoomPlacement placement : dungeon.getPlacements()) {
            for (RavengardRoomCatalog.RoomObject object : placement.room().getObjects()) {
                objects.add(new PlacedObject(object.getCategory(), object.getType(),
                        placement.getWorldX(object.getX(), object.getZ()), object.getY(),
                        placement.getWorldZ(object.getX(), object.getZ()),
                        (object.getYaw() + placement.rotation().getDegrees()) % 360));
            }
        }

        RoomPlacement start = dungeon.getStartRoom();
        Pos spawn = new Pos(start.originX() + start.getFootprintWidth() / 2.0, 66,
                start.originZ() + start.getFootprintDepth() / 2.0);
        return new GeneratedDungeon(dungeon, objects, spawn, seed);
    }

    /** City-base generation only needs the swapped slot interiors restamped;
     * everything else is the loaded blueprint itself. */
    public static CompletableFuture<Void> stampSwaps(GeneratedDungeon generated, Instance instance) {
        DungeonTemplate template = DungeonTemplate.get();
        int floorY = getCatalog().getFloorY(), roofY = getCatalog().getRoofY();
        List<RoomPlacement> swaps = generated.dungeon().getSwaps();
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (swaps.isEmpty()) {
            future.complete(null);
            return future;
        }

        AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (RoomPlacement placement : swaps) {
            RavengardRoomCatalog.DungeonRoom room = placement.room();
            // the outer shell, walls and doors, belongs to the city; only the
            // interior swaps, so door positions never move between variants
            int footprintWidth = placement.getFootprintWidth();
            int footprintDepth = placement.getFootprintDepth();
            boolean[][] mask = new boolean[footprintWidth][footprintDepth];
            placement.forEachMaskCell((localX, localZ) -> {
                if (localX >= 0 && localX < footprintWidth && localZ >= 0 && localZ < footprintDepth) {
                    mask[localX][localZ] = true;
                }
            });
            for (int localX = 0; localX < footprintWidth; localX++) {
                for (int localZ = 0; localZ < footprintDepth; localZ++) {
                    if (!interiorCell(mask, localX, localZ, footprintWidth, footprintDepth)) continue;
                    int[] source = placement.toSourceFrame(localX, localZ);
                    int sourceX = room.getMinX() + source[0];
                    int sourceZ = room.getMinZ() + source[1];
                    int targetX = placement.originX() + localX;
                    int targetZ = placement.originZ() + localZ;
                    for (int y = floorY; y <= roofY; y++) {
                        batch.setBlock(targetX, y, targetZ,
                                rotate(template.blockAt(sourceX, y, sourceZ), placement.rotation()));
                    }
                }
            }
            minX = Math.min(minX, placement.originX() - 1);
            maxX = Math.max(maxX, placement.originX() + placement.getFootprintWidth() + 1);
            minZ = Math.min(minZ, placement.originZ() - 1);
            maxZ = Math.max(maxZ, placement.originZ() + placement.getFootprintDepth() + 1);
        }
        List<CompletableFuture<?>> chunkLoads = new ArrayList<>();
        for (int chunkX = minX >> 4; chunkX <= (maxX >> 4); chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= (maxZ >> 4); chunkZ++) {
                chunkLoads.add(instance.loadChunk(chunkX, chunkZ));
            }
        }
        CompletableFuture.allOf(chunkLoads.toArray(new CompletableFuture[0]))
                .thenRun(() -> batch.apply(instance, applied -> {
                    org.tinylog.Logger.info("Stamped {} room swaps", swaps.size());
                    future.complete(null);
                }))
                .exceptionally(throwable -> {
                    future.completeExceptionally(throwable);
                    return null;
                });
        return future;
    }

    public static CompletableFuture<Void> stamp(GeneratedDungeon generated, Instance instance) {
        DungeonTemplate template = DungeonTemplate.get();
        int floorY = getCatalog().getFloorY(), roofY = getCatalog().getRoofY();

        AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
        for (RoomPlacement placement : generated.dungeon().getPlacements()) {
            RavengardRoomCatalog.DungeonRoom room = placement.room();
            int footprintWidth = placement.getFootprintWidth();
            int footprintDepth = placement.getFootprintDepth();
            // stamp the raw footprint mask; a blanket dilation ring would drag
            // slivers of whatever neighboured this room in the template into the
            // gaps between generated rooms, so the seam half is carried only in
            // front of door apertures where the corridor floor actually lives
            boolean[][] cut = new boolean[footprintWidth + 2][footprintDepth + 2];
            placement.forEachMaskCell((localX, localZ) -> {
                int cutX = localX + 1, cutZ = localZ + 1;
                if (cutX >= 0 && cutX < footprintWidth + 2
                        && cutZ >= 0 && cutZ < footprintDepth + 2) {
                    cut[cutX][cutZ] = true;
                }
            });
            for (RavengardRoomCatalog.DoorSocket socket : room.getSockets()) {
                Direction worldSide = socket.getSide().rotated(placement.rotation());
                double[] center = placement.toPlacementFrame(socket.getX(), socket.getZ());
                int normalX = switch (worldSide) { case EAST -> 1; case WEST -> -1; default -> 0; };
                int normalZ = switch (worldSide) { case SOUTH -> 1; case NORTH -> -1; default -> 0; };
                int alongX = normalZ == 0 ? 0 : 1;
                int alongZ = normalX == 0 ? 0 : 1;
                int halfSpan = (int) Math.ceil(socket.getWidth() / 2) + 1;
                for (int along = -halfSpan; along <= halfSpan; along++) {
                    for (int outward = 0; outward <= 1; outward++) {
                        int cutX = (int) Math.round(center[0]) + alongX * along + normalX * outward + 1;
                        int cutZ = (int) Math.round(center[1]) + alongZ * along + normalZ * outward + 1;
                        if (cutX >= 0 && cutX < footprintWidth + 2
                                && cutZ >= 0 && cutZ < footprintDepth + 2) {
                            cut[cutX][cutZ] = true;
                        }
                    }
                }
            }
            for (int localX = -1; localX <= footprintWidth; localX++) {
                for (int localZ = -1; localZ <= footprintDepth; localZ++) {
                    if (!cut[localX + 1][localZ + 1]) continue;
                    int[] source = placement.toSourceFrame(localX, localZ);
                    int sourceX = room.getMinX() + source[0];
                    int sourceZ = room.getMinZ() + source[1];
                    int targetX = placement.originX() + localX;
                    int targetZ = placement.originZ() + localZ;
                    int roomRoofY = room.getRoofY() != null ? room.getRoofY() : roofY;
                    for (int y = floorY; y <= roomRoofY; y++) {
                        Block block = template.blockAt(sourceX, y, sourceZ);
                        if (block != Block.AIR) {
                            batch.setBlock(targetX, y, targetZ, rotate(block, placement.rotation()));
                        }
                    }
                }
            }
        }
        for (PlacedSocket socket : generated.dungeon().getSealedSockets()) {
            plugDoorway(batch, socket);
        }
        for (RoomPlacement placement : generated.dungeon().getPlacements()) {
            sealOpenBorders(batch, placement, template, floorY);
        }

        // batches only land in loaded chunks, and a fresh instance has none
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (RoomPlacement placement : generated.dungeon().getPlacements()) {
            minX = Math.min(minX, placement.originX() - 2);
            maxX = Math.max(maxX, placement.originX() + placement.getFootprintWidth() + 2);
            minZ = Math.min(minZ, placement.originZ() - 2);
            maxZ = Math.max(maxZ, placement.originZ() + placement.getFootprintDepth() + 2);
        }
        List<CompletableFuture<?>> chunkLoads = new ArrayList<>();
        for (int chunkX = minX >> 4; chunkX <= (maxX >> 4); chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= (maxZ >> 4); chunkZ++) {
                chunkLoads.add(instance.loadChunk(chunkX, chunkZ));
            }
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture.allOf(chunkLoads.toArray(new CompletableFuture[0]))
                .thenRun(() -> batch.apply(instance, applied -> {
                    fillLight(instance);
                    probeSpawnChunk(generated, instance);
                    future.complete(null);
                }))
                .exceptionally(throwable -> {
                    future.completeExceptionally(throwable);
                    return null;
                });
        return future;
    }

    /** Cells at least two blocks inside the mask edge: the interior. */
    private static boolean interiorCell(boolean[][] mask, int x, int z, int width, int depth) {
        if (x < 0 || z < 0 || x >= width || z >= depth || !mask[x][z]) return false;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int nx = x + dx, nz = z + dz;
                if (nx < 0 || nz < 0 || nx >= width || nz >= depth || !mask[nx][nz]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void sealOpenBorders(AbsoluteBlockBatch batch, RoomPlacement placement,
                                        DungeonTemplate template, int floorY) {
        RavengardRoomCatalog.DungeonRoom room = placement.room();
        int footprintWidth = placement.getFootprintWidth();
        int footprintDepth = placement.getFootprintDepth();
        boolean[][] mask = new boolean[footprintWidth][footprintDepth];
        placement.forEachMaskCell((localX, localZ) -> {
            if (localX >= 0 && localX < footprintWidth && localZ >= 0 && localZ < footprintDepth) {
                mask[localX][localZ] = true;
            }
        });

        java.util.Set<Long> doorCells = new java.util.HashSet<>();
        for (RavengardRoomCatalog.DoorSocket socket : room.getSockets()) {
            double[] center = placement.toPlacementFrame(socket.getX(), socket.getZ());
            int reach = (int) Math.ceil(socket.getWidth() / 2) + 2;
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    doorCells.add((((long) ((int) Math.round(center[0]) + dx)) << 32)
                            | (((int) Math.round(center[1]) + dz) & 0xFFFFFFFFL));
                }
            }
        }

        Map<Block, Integer> wallSamples = new HashMap<>();
        List<int[]> openCells = new ArrayList<>();
        for (int localX = 0; localX < footprintWidth; localX++) {
            for (int localZ = 0; localZ < footprintDepth; localZ++) {
                if (!mask[localX][localZ]) continue;
                boolean border = localX == 0 || localZ == 0
                        || localX == footprintWidth - 1 || localZ == footprintDepth - 1
                        || !mask[localX - 1][localZ] || !mask[localX + 1 == footprintWidth ? localX : localX + 1][localZ]
                        || !mask[localX][localZ - 1] || !mask[localX][localZ + 1 == footprintDepth ? localZ : localZ + 1];
                if (!border) continue;
                int[] source = placement.toSourceFrame(localX, localZ);
                int sourceX = room.getMinX() + source[0];
                int sourceZ = room.getMinZ() + source[1];
                int solid = 0;
                for (int y = 65; y <= 80; y++) {
                    if (template.blockAt(sourceX, y, sourceZ) != Block.AIR) solid++;
                }
                if (solid >= 3) {
                    Block sample = template.blockAt(sourceX, 70, sourceZ);
                    if (sample != Block.AIR && sample.solid()) {
                        wallSamples.merge(sample, 1, Integer::sum);
                    }
                } else if (!doorCells.contains((((long) localX) << 32) | (localZ & 0xFFFFFFFFL))) {
                    openCells.add(new int[]{localX, localZ, sourceX, sourceZ});
                }
            }
        }
        if (openCells.isEmpty()) return;
        Block wall = wallSamples.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(Block.POLISHED_BLACKSTONE_BRICKS);
        for (int[] cell : openCells) {
            for (int y = floorY + 4; y <= 84; y++) {
                if (template.blockAt(cell[2], y, cell[3]) == Block.AIR) {
                    batch.setBlock(placement.originX() + cell[0], y,
                            placement.originZ() + cell[1], wall);
                }
            }
        }
    }

    private static void materializeLight(Instance instance) {
        long started = System.currentTimeMillis();
        int sectionsTouched = 0;
        for (var chunk : instance.getChunks()) {
            for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {
                var section = chunk.getSection(sectionY);
                section.skyLight().array();
                section.blockLight().array();
                sectionsTouched++;
            }
        }
        org.tinylog.Logger.info("Materialized light for {} sections in {}ms",
                sectionsTouched, System.currentTimeMillis() - started);
    }

    /** The polar worlds ship baked light and the client renders them; sections
     * with no light data at all are skipped by this client's mesher, so the
     * generated layout is lit full-bright the way a dungeon interior plays. */
    private static void fillLight(Instance instance) {
        byte[] fullBright = new byte[2048];
        java.util.Arrays.fill(fullBright, (byte) 0xFF);
        int sections = 0;
        for (var chunk : instance.getChunks()) {
            for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {
                var section = chunk.getSection(sectionY);
                section.skyLight().set(fullBright);
                section.blockLight().set(fullBright);
                sections++;
            }
        }
        org.tinylog.Logger.info("Filled full-bright light for {} sections", sections);
    }

    private static void probeSpawnChunk(GeneratedDungeon generated, Instance instance) {
        try {
            int chunkX = generated.spawn().blockX() >> 4;
            int chunkZ = generated.spawn().blockZ() >> 4;
            var chunk = instance.getChunk(chunkX, chunkZ);
            if (chunk == null) {
                org.tinylog.Logger.warn("Probe: spawn chunk {},{} not loaded", chunkX, chunkZ);
                return;
            }
            StringBuilder report = new StringBuilder();
            for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {
                var section = chunk.getSection(sectionY);
                int blocks = section.blockPalette().count();
                if (blocks == 0) continue;
                report.append(String.format("[y%d blocks=%d sky=%d blk=%d] ",
                        sectionY, blocks,
                        section.skyLight().array().length,
                        section.blockLight().array().length));
            }
            org.tinylog.Logger.info("Probe chunk {},{} class={} sections: {}",
                    chunkX, chunkZ, chunk.getClass().getSimpleName(), report);
        } catch (Exception exception) {
            org.tinylog.Logger.error(exception, "Probe failed");
        }
    }

    private static Block rotate(Block block, Rotation rotation) {
        if (rotation == Rotation.NONE) return block;
        Map<String, String> properties = block.properties();
        if (properties.isEmpty()) return block;
        String cacheKey = block.stateId() + ":" + rotation;
        Block cached = ROTATED_BLOCKS.get(cacheKey);
        if (cached != null) return cached;

        int quarterTurns = rotation.getQuarterTurns();
        Map<String, String> rotated = new HashMap<>(properties);
        String facing = properties.get("facing");
        if (facing != null) {
            int index = cardinalIndex(facing);
            if (index >= 0) rotated.put("facing", CARDINAL_PROPERTIES[(index + quarterTurns) % 4]);
        }
        String axis = properties.get("axis");
        if (axis != null && rotation.swapsAxes()) {
            if (axis.equals("x")) rotated.put("axis", "z");
            else if (axis.equals("z")) rotated.put("axis", "x");
        }
        String standingRotation = properties.get("rotation");
        if (standingRotation != null) {
            rotated.put("rotation",
                    String.valueOf((Integer.parseInt(standingRotation) + quarterTurns * 4) % 16));
        }
        if (properties.containsKey("north") || properties.containsKey("east")) {
            for (int i = 0; i < 4; i++) {
                String value = properties.get(CARDINAL_PROPERTIES[i]);
                if (value != null) {
                    rotated.put(CARDINAL_PROPERTIES[(i + quarterTurns) % 4], value);
                }
            }
        }
        Block result = block.withProperties(rotated);
        ROTATED_BLOCKS.put(cacheKey, result);
        return result;
    }

    private static int cardinalIndex(String side) {
        for (int i = 0; i < 4; i++) {
            if (CARDINAL_PROPERTIES[i].equals(side)) return i;
        }
        return -1;
    }

    private static void plugDoorway(AbsoluteBlockBatch batch, PlacedSocket socket) {
        int worldX = socket.getWorldX();
        int worldZ = socket.getWorldZ();
        int baseY = (int) Math.floor(socket.getY()) - 1;
        Direction side = socket.getWorldSide();
        boolean alongX = side == Direction.NORTH || side == Direction.SOUTH;
        for (int depth = 0; depth <= 1; depth++) {
            int shift = side == Direction.EAST || side == Direction.SOUTH ? depth : -depth;
            for (int spread = -2; spread <= 2; spread++) {
                for (int y = baseY; y <= baseY + 5; y++) {
                    int x = alongX ? worldX + spread : worldX + shift;
                    int z = alongX ? worldZ + shift : worldZ + spread;
                    batch.setBlock(x, y, z, DOOR_PLUG);
                }
            }
        }
    }
}
