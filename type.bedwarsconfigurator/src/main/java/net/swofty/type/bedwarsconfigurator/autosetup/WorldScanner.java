package net.swofty.type.bedwarsconfigurator.autosetup;

import lombok.Getter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.mc.Vec3i;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WorldScanner {

    private final Instance instance;
    private final AutoSetupSession session;

    public WorldScanner(Instance instance, AutoSetupSession session) {
        this.instance = instance;
        this.session = session;
    }

    private void ensureChunksLoaded() {
        if (!session.hasBounds()) return;

        int minChunkX = Math.min(session.getMinX().intValue(), session.getMaxX().intValue()) >> 4;
        int maxChunkX = Math.max(session.getMinX().intValue(), session.getMaxX().intValue()) >> 4;
        int minChunkZ = Math.min(session.getMinZ().intValue(), session.getMaxZ().intValue()) >> 4;
        int maxChunkZ = Math.max(session.getMinZ().intValue(), session.getMaxZ().intValue()) >> 4;

        List<CompletableFuture<Chunk>> futures = new ArrayList<>();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                Chunk chunk = instance.getChunk(cx, cz);
                if (chunk == null) {
                    futures.add(instance.loadChunk(cx, cz));
                }
            }
        }

        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    public ScanResult fullScan() {
        ScanResult result = new ScanResult();

        if (!session.hasBounds()) {
            result.addError("No bounds set. Use /autosetup bounds first.");
            return result;
        }

        ensureChunksLoaded();
        List<BedLocation> beds = findBeds();
        result.addMessage("Found " + beds.size() + " beds");

        for (BedLocation bed : beds) {
            TeamKey team = detectTeamFromNearbyWool(bed);
            if (team != null) {
                AutoSetupSession.TeamConfig teamConfig = session.getOrCreateTeam(team);
                teamConfig.setBedFeet(bed.feet);
                teamConfig.setBedHead(bed.head);

                HypixelPosition spawn = findSpawnNearBed(bed);
                if (spawn != null) {
                    teamConfig.setSpawn(new HypixelPosition(spawn.x(), spawn.y(), spawn.z(), 0, calculateYawTowardsBed(spawn, bed)));
                }

                HypixelPosition generator = findTeamGenerator(bed, 20);
                if (generator != null) {
                    teamConfig.setGenerator(generator);
                }

                result.addMessage("Configured team " + team.getName() + " from bed");
            } else {
                result.addWarning("Could not determine team for bed at " + bed.feet);
            }
        }

        // Find diamond generators
        List<HypixelPosition> diamondGens = findGlobalGenerators(Block.DIAMOND_BLOCK);
        session.getDiamondGenerators().clear();
        session.getDiamondGenerators().addAll(diamondGens);
        result.addMessage("Found " + diamondGens.size() + " diamond generators");

        // Find emerald generators
        List<HypixelPosition> emeraldGens = findGlobalGenerators(Block.EMERALD_BLOCK);
        session.getEmeraldGenerators().clear();
        session.getEmeraldGenerators().addAll(emeraldGens);
        result.addMessage("Found " + emeraldGens.size() + " emerald generators");

        return result;
    }

    public List<BedLocation> findBeds() {
        List<BedLocation> beds = new ArrayList<>();
        Set<Point> processedHeads = new HashSet<>();

        int minX = Math.min(session.getMinX().intValue(), session.getMaxX().intValue());
        int maxX = Math.max(session.getMinX().intValue(), session.getMaxX().intValue());
        int minY = Math.min(session.getMinY().intValue(), session.getMaxY().intValue());
        int maxY = Math.max(session.getMinY().intValue(), session.getMaxY().intValue());
        int minZ = Math.min(session.getMinZ().intValue(), session.getMaxZ().intValue());
        int maxZ = Math.max(session.getMinZ().intValue(), session.getMaxZ().intValue());

        Logger.info("Scanning for beds in area: X[" + minX + "," + maxX + "] Y[" + minY + "," + maxY + "] Z[" + minZ + "," + maxZ + "]");

        int blocksChecked = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = instance.getBlock(x, y, z);
                    String blockName = block.name();

                    // Check if it's a bed foot part
                    if (blockName.contains("_bed") && "foot".equals(block.getProperty("part"))) {
                        String facing = block.getProperty("facing");

                        if (facing == null) {
                            facing = "south"; // Default facing if not found
                        }

                        // Calculate head HypixelPosition based on facing
                        Point headPos = calculateHeadHypixelPosition(x, y, z, facing);

                        if (!processedHeads.contains(headPos)) {
                            processedHeads.add(headPos);
                            beds.add(new BedLocation(
                                new Vec3i(x, y, z),
                                new Vec3i((int) headPos.x(), (int) headPos.y(), (int) headPos.z()),
                                    facing
                            ));
                        }
                    }
                    blocksChecked++;
                }
            }
        }
        Logger.info("Total blocks checked for beds: " + blocksChecked);
        return beds;
    }

    private Point calculateHeadHypixelPosition(int footX, int footY, int footZ, String facing) {
        return switch (facing) {
            case "north" -> Pos.ZERO.add(footX, footY, footZ - 1);
            case "south" -> Pos.ZERO.add(footX, footY, footZ + 1);
            case "east" -> Pos.ZERO.add(footX + 1, footY, footZ);
            case "west" -> Pos.ZERO.add(footX - 1, footY, footZ);
            default -> Pos.ZERO.add(footX, footY, footZ);
        };
    }

    private TeamKey detectTeamFromNearbyWool(BedLocation bed) {
        int searchRadius = 10;
        Map<TeamKey, Integer> woolCounts = new EnumMap<>(TeamKey.class);

        int centerX = (int) bed.feet.x();
        int centerY = (int) bed.feet.y();
        int centerZ = (int) bed.feet.z();

        for (int x = centerX - searchRadius; x <= centerX + searchRadius; x++) {
            for (int y = centerY - 3; y <= centerY + 3; y++) {
                for (int z = centerZ - searchRadius; z <= centerZ + searchRadius; z++) {
                    Block block = instance.getBlock(x, y, z);
                    TeamKey team = getTeamFromWoolBlock(block);
                    if (team != null) {
                        woolCounts.merge(team, 1, Integer::sum);
                    }
                }
            }
        }

        return woolCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private TeamKey getTeamFromWoolBlock(Block block) {
        String name = block.name();
        if (!name.contains("wool")) return null;

        if (name.contains("red")) return TeamKey.RED;
        if (name.contains("blue") && !name.contains("light")) return TeamKey.BLUE;
        if (name.contains("green") || name.contains("lime")) return TeamKey.GREEN;
        if (name.contains("yellow")) return TeamKey.YELLOW;
        if (name.contains("light_blue") || name.contains("cyan")) return TeamKey.AQUA;
        if (name.contains("white")) return TeamKey.WHITE;
        if (name.contains("pink") || name.contains("magenta")) return TeamKey.PINK;
        if (name.contains("gray")) return TeamKey.GRAY;

        return null;
    }

    // suitable area around the bed for placeholder
    private HypixelPosition findSpawnNearBed(BedLocation bed) {
        int[][] offsets = {{0, 0, 2}, {0, 0, -2}, {2, 0, 0}, {-2, 0, 0},
                {0, 0, 3}, {0, 0, -3}, {3, 0, 0}, {-3, 0, 0}};

        int baseX = (int) bed.feet.x();
        int baseY = (int) bed.feet.y();
        int baseZ = (int) bed.feet.z();

        for (int[] offset : offsets) {
            int x = baseX + offset[0];
            int y = baseY + offset[1];
            int z = baseZ + offset[2];

            Block ground = instance.getBlock(x, y - 1, z);
            Block feet = instance.getBlock(x, y, z);
            Block head = instance.getBlock(x, y + 1, z);

            if (ground.solid() && feet.air() && head.air()) {
                return new HypixelPosition(x + 0.5, y, z + 0.5);
            }
        }

        return new HypixelPosition(bed.feet.x() + 0.5, bed.feet.y(), bed.feet.z() + 2.5);
    }

    private float calculateYawTowardsBed(HypixelPosition spawn, BedLocation bed) {
        double dx = bed.feet.x() - spawn.x();
        double dz = bed.feet.z() - spawn.z();
        return (float) (Math.toDegrees(Math.atan2(-dx, dz)));
    }

    private HypixelPosition findTeamGenerator(BedLocation bed, int radius) {
        int centerX = (int) bed.feet.x();
        int centerY = (int) bed.feet.y();
        int centerZ = (int) bed.feet.z();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - 5; y <= centerY + 5; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    if (isSlabPlatform(x, y, z)) {
                        return new HypixelPosition(x + 1.5, y + 0.5, z + 0.5);
                    }
                }
            }
        }

        return null;
    }

    // checks those 3x2 slab platforms
    private boolean isSlabPlatform(int startX, int startY, int startZ) {
        int slabCount = 0;
        for (int orientation = 0; orientation < 2; orientation++) {
            slabCount = 0;
            int width = orientation == 0 ? 3 : 2;
            int depth = orientation == 0 ? 2 : 3;

            for (int dx = 0; dx < width; dx++) {
                for (int dz = 0; dz < depth; dz++) {
                    Block block = instance.getBlock(startX + dx, startY, startZ + dz);
                    if (isSlabBlock(block)) {
                        slabCount++;
                    }
                }
            }

            if (slabCount >= 6) {
                return true;
            }
        }

        return false;
    }

    private boolean isSlabBlock(Block block) {
        return block.name().contains("slab");
    }

    public List<HypixelPosition> findGlobalGenerators(Block centerBlock) {
        List<HypixelPosition> generators = new ArrayList<>();

        // Normalize bounds to handle cases where min > max
        int minX = Math.min(session.getMinX().intValue(), session.getMaxX().intValue());
        int maxX = Math.max(session.getMinX().intValue(), session.getMaxX().intValue());
        int minY = Math.min(session.getMinY().intValue(), session.getMaxY().intValue());
        int maxY = Math.max(session.getMinY().intValue(), session.getMaxY().intValue());
        int minZ = Math.min(session.getMinZ().intValue(), session.getMaxZ().intValue());
        int maxZ = Math.max(session.getMinZ().intValue(), session.getMaxZ().intValue());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = instance.getBlock(x, y, z);

                    if (block.compare(centerBlock)) {
                        if (hasStairPattern(x, y, z)) {
                            generators.add(new HypixelPosition(x + 0.5, y + 1, z + 0.5));
                        }
                    }
                }
            }
        }

        return generators;
    }

    private boolean hasStairPattern(int centerX, int centerY, int centerZ) {
        int stairCount = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               for (int dy = 0; dy <= 1; dy++) {
                   if (dx == 0 && dz == 0) continue;

                   Block block = instance.getBlock(centerX + dx, centerY + dy, centerZ + dz);
                   if (block.name().contains("stair")) {
                       stairCount++;
                   }
               }
            }
        }

        return stairCount >= 4; // At least 4 stairs (corners or sides)
    }

    public record BedLocation(Vec3i feet, Vec3i head, String facing) {
    }

    @Getter
    public static class ScanResult {
        private final List<String> messages = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        public void addMessage(String message) {
            messages.add(message);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public void addError(String error) {
            errors.add(error);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}

