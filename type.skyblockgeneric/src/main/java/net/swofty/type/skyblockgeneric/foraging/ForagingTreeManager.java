package net.swofty.type.skyblockgeneric.foraging;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.entity.hologram.HologramEntity;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.hunting.AttributeRarity;
import net.swofty.type.skyblockgeneric.hunting.AttributeRegistry;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AxeComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the trees embedded in the Park and Galatea maps. Trees are discovered
 * from the original map the first time one of their logs is hit, which means we
 * restore the authored tree instead of replacing it with a generated variant.
 */
public final class ForagingTreeManager {
    private static final int MAX_TREE_BLOCKS = 4096;
    private static final int MAX_LOGS_PER_SWEEP = 35;
    /**
     * Public-island trees remain felled for roughly thirty seconds.
     */
    private static final int REGEN_DELAY_TICKS = 20 * 30;
    private static final int REGEN_LAYER_TICKS = 3;
    private static final int LEAF_SEARCH_DISTANCE = 5;
    private static final int[][] FACES = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
    };
    private static final int[][] TOUCHING_BLOCKS = touchingBlockOffsets();

    private static final Map<Instance, Map<BlockPosition, TrackedTree>> TREES = new ConcurrentHashMap<>();

    private ForagingTreeManager() {
    }

    public static boolean isTreeLog(Block block) {
        return getTreeKind(block) != null;
    }

    public static boolean isTreeLeaf(Block block) {
        ServerType serverType = HypixelConst.getTypeLoader().getType();
        if (serverType != ServerType.SKYBLOCK_THE_PARK
                && serverType != ServerType.SKYBLOCK_GALATEA
                && serverType != ServerType.SKYBLOCK_HUB) return false;
        Material material = Material.fromKey(block.key());
        return material != null && material.key().value().endsWith("_leaves");
    }

    /**
     * Allows authored leaves to be removed without making all map blocks mineable.
     */
    public static boolean breakLeaf(SkyBlockPlayer player, Point hitPoint) {
        Instance instance = player.getInstance();
        BlockPosition hit = BlockPosition.from(hitPoint);
        Block leaf = instance.getBlock(hitPoint);
        if (!isTreeLeaf(leaf)) return false;

        TrackedTree tree = findTreeForLeaf(instance, hit);
        instance.setBlock(hitPoint, Block.AIR);
        if (tree == null) {
            scheduleLooseLeafRegeneration(instance, hit, leaf);
        } else {
            tree.brokenLeaves.add(hit);
            tree.scheduleLooseLeafRegeneration(instance, hit);
        }
        return true;
    }

    /**
     * Breaks the initially hit log plus all logs selected by Sweep.
     */
    public static List<HarvestedLog> breakLogs(SkyBlockPlayer player, Point hitPoint, SkyBlockItem tool) {
        Instance instance = player.getInstance();
        BlockPosition hit = BlockPosition.from(hitPoint);
        Block hitBlock = instance.getBlock(hitPoint);
        TreeKind kind = getTreeKind(hitBlock);
        if (kind == null) return List.of();

        TrackedTree tree = getOrDiscoverTree(instance, hit, hitBlock, kind);

        double sweep = tool.hasComponent(AxeComponent.class)
                ? player.getStatistics().allStatistics().getOverall(ItemStatistic.SWEEP)
                : 0;
        if (tool.hasComponent(AxeComponent.class)) {
            if (kind == TreeKind.FIG)
                sweep += AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("C19"));
            if (kind == TreeKind.MANGROVE)
                sweep += AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("U16"));
            int commonAttributes = (int) AttributeRegistry.values().stream()
                    .filter(definition -> definition.rarity() == AttributeRarity.COMMON)
                    .filter(definition -> player.getHuntingData().level(definition.id()) > 0).count();
            sweep += AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("C20")) * commonAttributes;
        }
        TreePart hitPart = tree.parts.getOrDefault(hit, TreePart.TRUNK);
        boolean correctOrder = tree.isCorrectPart(hitPart);
        int logCount = calculateLogs(sweep, tree.kind.toughness(hitPart), tree.kind != TreeKind.BASIC && !correctOrder);

        List<BlockPosition> selected = selectConnectedLogs(instance, tree, hit, logCount);
        if (selected.isEmpty()) return List.of();

        List<HarvestedLog> harvested = new ArrayList<>(selected.size());
        for (BlockPosition position : selected) {
            Block original = tree.originalBlocks.get(position);
            if (original == null || instance.getBlock(position.asPoint()).air()) continue;

            instance.setBlock(position.asPoint(), Block.AIR);
            tree.broken.add(position);
            harvested.add(new HarvestedLog(position.asPoint(), original, tree.kind.dropType(original)));
        }

        if (!harvested.isEmpty()) {
            tree.recordContribution(player, harvested.size());
            if (tree.kind != TreeKind.BASIC) {
                for (HarvestedLog log : harvested) {
                    tree.animateLog(instance, log.position, log.originalBlock);
                    tree.decayLeavesNear(instance, BlockPosition.from(log.position));
                }
                tree.updateHologram(instance);
            }

            if (tree.isFullyBroken()) {
                tree.decayAllLeaves(instance);
                tree.playCompletionSound(instance);
                tree.awardGifts(instance);
                tree.scheduleRegeneration(instance);
            }
        }
        return harvested;
    }

    private static TrackedTree getOrDiscoverTree(Instance instance, BlockPosition hit, Block hitBlock, TreeKind kind) {
        Map<BlockPosition, TrackedTree> index = TREES.computeIfAbsent(instance, ignored -> new ConcurrentHashMap<>());
        TrackedTree tree = index.get(hit);
        if (tree != null) return tree;

        Material hitMaterial = Material.fromKey(hitBlock.key());
        String woodFamily = hitMaterial == null ? "" : TreeKind.baseWoodName(hitMaterial);
        tree = discoverTree(instance, hit, kind, woodFamily);
        for (BlockPosition position : tree.originalBlocks.keySet()) index.putIfAbsent(position, tree);
        for (BlockPosition position : tree.originalLeaves.keySet()) index.putIfAbsent(position, tree);
        return index.getOrDefault(hit, tree);
    }

    private static TrackedTree findTreeForLeaf(Instance instance, BlockPosition leaf) {
        Map<BlockPosition, TrackedTree> index = TREES.computeIfAbsent(instance, ignored -> new ConcurrentHashMap<>());
        TrackedTree indexed = index.get(leaf);
        if (indexed != null) return indexed;

        BlockPosition nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (int x = -LEAF_SEARCH_DISTANCE; x <= LEAF_SEARCH_DISTANCE; x++) {
            for (int y = -LEAF_SEARCH_DISTANCE; y <= LEAF_SEARCH_DISTANCE; y++) {
                for (int z = -LEAF_SEARCH_DISTANCE; z <= LEAF_SEARCH_DISTANCE; z++) {
                    BlockPosition candidate = leaf.add(x, y, z);
                    Block block = instance.getBlock(candidate.asPoint());
                    if (!isTreeLog(block)) continue;
                    int distance = Math.abs(x) + Math.abs(y) + Math.abs(z);
                    if (distance < nearestDistance) {
                        nearest = candidate;
                        nearestDistance = distance;
                    }
                }
            }
        }
        if (nearest == null) return null;
        Block log = instance.getBlock(nearest.asPoint());
        return getOrDiscoverTree(instance, nearest, log, getTreeKind(log));
    }

    private static void scheduleLooseLeafRegeneration(Instance instance, BlockPosition position, Block leaf) {
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (instance.getBlock(position.asPoint()).air()) instance.setBlock(position.asPoint(), leaf);
        }, TaskSchedule.tick(REGEN_DELAY_TICKS), TaskSchedule.stop());
    }

    static int calculateLogs(double sweep, double toughness, boolean wrongOrder) {
        double effectiveSweep = Math.max(0, sweep) * (wrongOrder ? 0.5 : 1.0);
        int extra;
        if (toughness <= 0) {
            extra = (int) Math.floor(effectiveSweep);
        } else {
            double logarithmInput = effectiveSweep - toughness + Math.sqrt(toughness) + 0.646;
            if (logarithmInput <= 0) {
                extra = 0;
            } else {
                double estimate = 0.515 + 3.245 * Math.log(logarithmInput) - 1.708 * Math.log(toughness);
                extra = Math.max(0, (int) Math.floor(estimate));
            }
        }
        return 1 + Math.min(MAX_LOGS_PER_SWEEP - 1, extra);
    }

    private static TrackedTree discoverTree(Instance instance, BlockPosition start, TreeKind kind, String woodFamily) {
        ArrayDeque<BlockPosition> queue = new ArrayDeque<>();
        Map<BlockPosition, Block> blocks = new HashMap<>();
        queue.add(start);

        while (!queue.isEmpty() && blocks.size() < MAX_TREE_BLOCKS) {
            BlockPosition current = queue.removeFirst();
            if (blocks.containsKey(current)) continue;

            Block block = instance.getBlock(current.asPoint());
            if (!kind.matches(block, woodFamily)) continue;
            blocks.put(current, block);

            for (int[] offset : TOUCHING_BLOCKS) {
                queue.addLast(current.add(offset[0], offset[1], offset[2]));
            }
        }

        Map<BlockPosition, Block> leaves = discoverLeaves(instance, blocks.keySet());
        return new TrackedTree(kind, blocks, leaves, classifyParts(kind, blocks.keySet()));
    }

    private static Map<BlockPosition, Block> discoverLeaves(Instance instance, Set<BlockPosition> logs) {
        record LeafSearch(BlockPosition position, int distance) {
        }
        ArrayDeque<LeafSearch> queue = new ArrayDeque<>();
        Set<BlockPosition> visited = new HashSet<>();
        Map<BlockPosition, Block> leaves = new HashMap<>();
        for (BlockPosition log : logs) {
            for (int[] face : FACES) queue.addLast(new LeafSearch(log.add(face[0], face[1], face[2]), 1));
        }

        while (!queue.isEmpty() && leaves.size() + logs.size() < MAX_TREE_BLOCKS) {
            LeafSearch search = queue.removeFirst();
            if (!visited.add(search.position) || search.distance > LEAF_SEARCH_DISTANCE) continue;
            Block block = instance.getBlock(search.position.asPoint());
            if (!isTreeLeaf(block)) continue;
            leaves.put(search.position, block);
            for (int[] face : FACES) {
                queue.addLast(new LeafSearch(search.position.add(face[0], face[1], face[2]), search.distance + 1));
            }
        }
        return leaves;
    }

    private static Map<BlockPosition, TreePart> classifyParts(TreeKind kind, Set<BlockPosition> positions) {
        Map<BlockPosition, TreePart> result = new HashMap<>();
        if (kind == TreeKind.BASIC) {
            positions.forEach(position -> result.put(position, TreePart.TRUNK));
            return result;
        }

        int minimumY = positions.stream().mapToInt(BlockPosition::y).min().orElse(0);
        Map<Column, Integer> verticalCounts = new HashMap<>();
        positions.forEach(position -> verticalCounts.merge(new Column(position.x, position.z), 1, Integer::sum));

        for (BlockPosition position : positions) {
            if (kind == TreeKind.MANGROVE && position.y <= minimumY + 1) {
                result.put(position, TreePart.ROOT);
            } else if (verticalCounts.getOrDefault(new Column(position.x, position.z), 0) >= 3) {
                result.put(position, TreePart.TRUNK);
            } else {
                result.put(position, TreePart.BRANCH);
            }
        }
        return result;
    }

    private static List<BlockPosition> selectConnectedLogs(Instance instance, TrackedTree tree,
                                                           BlockPosition start, int count) {
        ArrayDeque<BlockPosition> queue = new ArrayDeque<>();
        Set<BlockPosition> visited = new HashSet<>();
        List<BlockPosition> selected = new ArrayList<>();
        queue.add(start);

        while (!queue.isEmpty() && selected.size() < count) {
            BlockPosition current = queue.removeFirst();
            if (!visited.add(current) || !tree.originalBlocks.containsKey(current)) continue;
            if (instance.getBlock(current.asPoint()).air()) continue;

            selected.add(current);
            for (int[] offset : TOUCHING_BLOCKS) {
                queue.addLast(current.add(offset[0], offset[1], offset[2]));
            }
        }
        return selected;
    }

    private static int[][] touchingBlockOffsets() {
        List<int[]> offsets = new ArrayList<>(26);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) offsets.add(new int[]{x, y, z});
                }
            }
        }
        return offsets.toArray(int[][]::new);
    }

    private static TreeKind getTreeKind(Block block) {
        Material material = Material.fromKey(block.key());
        if (material == null) return null;

        ServerType serverType = HypixelConst.getTypeLoader().getType();
        if (serverType == ServerType.SKYBLOCK_GALATEA) {
            if (material == Material.STRIPPED_SPRUCE_LOG || material == Material.STRIPPED_SPRUCE_WOOD) {
                return TreeKind.FIG;
            }
            if (material == Material.MANGROVE_LOG || material == Material.MANGROVE_WOOD) {
                return TreeKind.MANGROVE;
            }
            if (material.key().value().equals("pale_oak_log") || material.key().value().equals("pale_oak_wood")) {
                return TreeKind.HELIX;
            }
            return null;
        }

        if (serverType != ServerType.SKYBLOCK_THE_PARK && serverType != ServerType.SKYBLOCK_HUB) return null;
        String name = material.key().value();
        return (name.endsWith("_log") || name.endsWith("_wood")) ? TreeKind.BASIC : null;
    }

    public record HarvestedLog(Point position, Block originalBlock, ItemType dropType) {
    }

    private enum TreePart {BRANCH, TRUNK, ROOT}

    private enum TreeKind {
        BASIC,
        FIG,
        MANGROVE,
        HELIX;

        boolean matches(Block block, String woodFamily) {
            TreeKind other = getTreeKind(block);
            if (this != BASIC) return this == other;
            if (other != BASIC) return false;

            Material material = Material.fromKey(block.key());
            return material != null && baseWoodName(material).equals(woodFamily);
        }

        double toughness(TreePart part) {
            return switch (this) {
                case BASIC -> 0;
                case FIG -> part == TreePart.BRANCH ? 3.5 : 7;
                case MANGROVE -> part == TreePart.BRANCH ? 25 : 50;
                case HELIX -> part == TreePart.BRANCH ? 40 : 80;
            };
        }

        ItemType dropType(Block block) {
            if (this == FIG) return ItemType.FIG_LOG;
            if (this == MANGROVE) return ItemType.MANGROVE_LOG;
            if (this == HELIX) return null;

            Material material = Material.fromKey(block.key());
            if (material == null) return null;
            String wood = baseWoodName(material).toUpperCase() + "_LOG";
            try {
                return ItemType.valueOf(wood);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        private static String baseWoodName(Material material) {
            return material.key().value().replace("stripped_", "").replace("_log", "").replace("_wood", "");
        }
    }

    private static final class TrackedTree {
        private final TreeKind kind;
        private final Map<BlockPosition, Block> originalBlocks;
        private final Map<BlockPosition, Block> originalLeaves;
        private final Map<BlockPosition, TreePart> parts;
        private final Set<BlockPosition> broken = new LinkedHashSet<>();
        private final Set<BlockPosition> brokenLeaves = new LinkedHashSet<>();
        private final Map<UUID, Contribution> contributions = new HashMap<>();
        private final List<HologramEntity> hologram = new ArrayList<>();
        private long regenerationVersion;

        private TrackedTree(TreeKind kind, Map<BlockPosition, Block> originalBlocks,
                            Map<BlockPosition, Block> originalLeaves,
                            Map<BlockPosition, TreePart> parts) {
            this.kind = kind;
            this.originalBlocks = originalBlocks;
            this.originalLeaves = originalLeaves;
            this.parts = parts;
        }

        private boolean isFullyBroken() {
            return broken.size() == originalBlocks.size();
        }

        private void recordContribution(SkyBlockPlayer player, int logs) {
            contributions.compute(player.getUuid(), (ignored, current) -> current == null
                    ? new Contribution(player.getFullDisplayName(), logs)
                    : new Contribution(current.name, current.logs + logs));
        }

        private Pos center() {
            double x = originalBlocks.keySet().stream().mapToInt(BlockPosition::x).average().orElse(0) + 0.5;
            double y = originalBlocks.keySet().stream().mapToInt(BlockPosition::y).average().orElse(0);
            double z = originalBlocks.keySet().stream().mapToInt(BlockPosition::z).average().orElse(0) + 0.5;
            return new Pos(x, y, z);
        }

        private Pos hologramPosition() {
            int rootY = originalBlocks.keySet().stream().mapToInt(BlockPosition::y).min().orElse(0);
            List<BlockPosition> roots = originalBlocks.keySet().stream()
                    .filter(position -> position.y == rootY).toList();
            double x = roots.stream().mapToInt(BlockPosition::x).average().orElse(0) + 0.5;
            double z = roots.stream().mapToInt(BlockPosition::z).average().orElse(0) + 0.5;
            return new Pos(x, rootY + 2, z);
        }

        private void updateHologram(Instance instance) {
            int percent = Math.min(100, (int) Math.round(broken.size() * 100.0 / originalBlocks.size()));
            String title = "§2§l" + kind.name() + " TREE §b" + percent + "%";
            String contributors = "§7by " + contributions.values().stream()
                    .sorted(Comparator.comparingInt(Contribution::logs).reversed())
                    .map(Contribution::name)
                    .reduce((left, right) -> left + "§7, " + right).orElse("§7Unknown");
            String[] lines = {title, contributors};
            Pos position = hologramPosition();
            if (hologram.isEmpty()) {
                for (int i = 0; i < lines.length; i++) {
                    HologramEntity entity = new HologramEntity(lines[i]);
                    entity.setAutoViewable(true);
                    entity.setInstance(instance, position.add(0, 0.3 - i * 0.3, 0));
                    hologram.add(entity);
                }
            } else {
                for (int i = 0; i < lines.length; i++) hologram.get(i).setText(lines[i]);
            }
        }

        private void animateLog(Instance instance, Point source, Block block) {
            Pos start = new Pos(source.x() + 0.5, source.y() + 0.5, source.z() + 0.5);
            Pos target = center().add(0, -0.45, 0);
            Entity display = new Entity(EntityType.BLOCK_DISPLAY);
            display.editEntityMeta(BlockDisplayMeta.class, meta -> {
                meta.setBlockState(block);
                meta.setHasNoGravity(true);
                meta.setScale(new Vec(0.55, 0.55, 0.55));
                meta.setTranslation(new Vec(-0.275, -0.275, -0.275));
                meta.setPosRotInterpolationDuration(1);
            });
            display.setAutoViewable(true);
            display.setInstance(instance, start);

            int[] tick = {0};
            Task[] animation = {null};
            animation[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (display.isRemoved()) {
                    animation[0].cancel();
                    return;
                }
                tick[0]++;
                if (tick[0] <= 10) {
                    double progress = tick[0] / 10.0;
                    double eased = 1 - Math.pow(1 - progress, 3);
                    display.teleport(new Pos(
                            start.x() + (target.x() - start.x()) * eased,
                            start.y() + (target.y() - start.y()) * eased + Math.sin(progress * Math.PI) * 0.8,
                            start.z() + (target.z() - start.z()) * eased));
                } else if (tick[0] <= 70) {
                    double angle = tick[0] * 0.18 + source.blockX();
                    display.teleport(new Pos(target.x() + Math.cos(angle) * 0.75,
                            target.y() + Math.sin(tick[0] * 0.15) * 0.18,
                            target.z() + Math.sin(angle) * 0.75,
                            (float) Math.toDegrees(angle), 0));
                } else {
                    display.remove();
                    animation[0].cancel();
                }
            }).repeat(TaskSchedule.tick(1)).schedule();
        }

        private void decayLeavesNear(Instance instance, BlockPosition log) {
            List<BlockPosition> nearby = originalLeaves.keySet().stream()
                    .filter(position -> !brokenLeaves.contains(position))
                    .filter(position -> position.distanceSquared(log) <= 16)
                    .toList();
            decayLeaves(instance, nearby);
        }

        private void decayAllLeaves(Instance instance) {
            decayLeaves(instance, originalLeaves.keySet().stream()
                    .filter(position -> !brokenLeaves.contains(position)).toList());
        }

        private void decayLeaves(Instance instance, List<BlockPosition> leaves) {
            for (int i = 0; i < leaves.size(); i++) {
                BlockPosition position = leaves.get(i);
                MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                    if (!isTreeLeaf(instance.getBlock(position.asPoint()))) return;
                    instance.setBlock(position.asPoint(), Block.AIR);
                    brokenLeaves.add(position);
                }, TaskSchedule.tick(i + i / 12 + 1), TaskSchedule.stop());
            }
        }

        private void playCompletionSound(Instance instance) {
            instance.playSound(Sound.sound(Key.key("entity.creaking.death"), Sound.Source.BLOCK, 1.25f, 0.8f), center());
        }

        private void awardGifts(Instance instance) {
            if (kind == TreeKind.BASIC) return;
            Map<UUID, Integer> amounts = new HashMap<>();
            contributions.forEach((uuid, contribution) -> amounts.put(uuid, contribution.logs));
            TreeGiftService.award(instance, kind.name(), amounts, originalBlocks.size());
        }

        private void scheduleLooseLeafRegeneration(Instance instance, BlockPosition leaf) {
            long version = regenerationVersion;
            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                if (version != regenerationVersion || !brokenLeaves.remove(leaf)) return;
                if (instance.getBlock(leaf.asPoint()).air())
                    instance.setBlock(leaf.asPoint(), originalLeaves.get(leaf));
            }, TaskSchedule.tick(REGEN_DELAY_TICKS), TaskSchedule.stop());
        }

        private boolean isCorrectPart(TreePart part) {
            if (kind == TreeKind.BASIC) return true;
            List<TreePart> order = kind == TreeKind.FIG
                    ? List.of(TreePart.TRUNK, TreePart.BRANCH)
                    : List.of(TreePart.BRANCH, TreePart.TRUNK, TreePart.ROOT);
            for (TreePart expected : order) {
                boolean hasRemaining = originalBlocks.keySet().stream()
                        .anyMatch(position -> parts.get(position) == expected && !broken.contains(position));
                if (hasRemaining) return part == expected;
            }
            return true;
        }

        private void scheduleRegeneration(Instance instance) {
            long version = ++regenerationVersion;
            MinecraftServer.getSchedulerManager().scheduleTask(
                    () -> regenerateBottomUp(instance, version),
                    TaskSchedule.tick(REGEN_DELAY_TICKS),
                    TaskSchedule.stop()
            );
        }

        private void regenerateBottomUp(Instance instance, long version) {
            if (version != regenerationVersion || broken.isEmpty()) return;

            Axis axis = growthAxis();
            List<Integer> layers = broken.stream().map(position -> position.coordinate(axis)).distinct().sorted().toList();
            for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
                int layerCoordinate = layers.get(layerIndex);
                int delay = 1 + layerIndex * REGEN_LAYER_TICKS;
                MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                    if (version != regenerationVersion) return;
                    List<BlockPosition> layer = broken.stream()
                            .filter(position -> position.coordinate(axis) == layerCoordinate)
                            .sorted(Comparator.comparingInt(BlockPosition::x).thenComparingInt(BlockPosition::z))
                            .toList();
                    for (BlockPosition position : layer) {
                        instance.setBlock(position.asPoint(), originalBlocks.get(position));
                        broken.remove(position);
                    }
                    if (!layer.isEmpty()) {
                        instance.playSound(Sound.sound(Key.key("block.wood.place"), Sound.Source.BLOCK, 1f, 0.75f),
                                layer.getFirst().asPoint());
                    }
                    if (broken.isEmpty()) finishRegeneration(instance, version);
                }, TaskSchedule.tick(delay), TaskSchedule.stop());
            }
        }

        private Axis growthAxis() {
            if (kind == TreeKind.BASIC) {
                Map<Axis, Integer> authoredAxes = new HashMap<>();
                for (Block block : originalBlocks.values()) {
                    String property = block.getProperty("axis");
                    if (property == null) continue;
                    try {
                        authoredAxes.merge(Axis.valueOf(property.toUpperCase()), 1, Integer::sum);
                    } catch (IllegalArgumentException ignored) {
                        // Wood blocks without an axis fall back to the structure's dimensions.
                    }
                }
                Axis authored = authoredAxes.entrySet().stream()
                        .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
                if (authored == Axis.X || authored == Axis.Z) return authored;
            }

            int xRange = range(Axis.X);
            int yRange = range(Axis.Y);
            int zRange = range(Axis.Z);
            if (xRange > yRange && xRange >= zRange) return Axis.X;
            if (zRange > yRange && zRange > xRange) return Axis.Z;
            return Axis.Y;
        }

        private int range(Axis axis) {
            int min = originalBlocks.keySet().stream().mapToInt(position -> position.coordinate(axis)).min().orElse(0);
            int max = originalBlocks.keySet().stream().mapToInt(position -> position.coordinate(axis)).max().orElse(0);
            return max - min;
        }

        private void finishRegeneration(Instance instance, long version) {
            if (version != regenerationVersion) return;
            List<BlockPosition> leaves = new ArrayList<>(brokenLeaves);
            for (int i = 0; i < leaves.size(); i++) {
                BlockPosition leaf = leaves.get(i);
                MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                    if (version != regenerationVersion) return;
                    if (instance.getBlock(leaf.asPoint()).air())
                        instance.setBlock(leaf.asPoint(), originalLeaves.get(leaf));
                    brokenLeaves.remove(leaf);
                }, TaskSchedule.tick(i / 20 + 1), TaskSchedule.stop());
            }
            hologram.forEach(Entity::remove);
            hologram.clear();
            contributions.clear();
        }
    }

    private enum Axis {X, Y, Z}

    private record Contribution(String name, int logs) {
    }

    private record Column(int x, int z) {
    }

    private record BlockPosition(int x, int y, int z) {
        static BlockPosition from(Point point) {
            return new BlockPosition(point.blockX(), point.blockY(), point.blockZ());
        }

        BlockPosition add(int x, int y, int z) {
            return new BlockPosition(this.x + x, this.y + y, this.z + z);
        }

        Point asPoint() {
            return new BlockVec(x, y, z);
        }

        int coordinate(Axis axis) {
            return switch (axis) {
                case X -> x;
                case Y -> y;
                case Z -> z;
            };
        }

        int distanceSquared(BlockPosition other) {
            int dx = x - other.x;
            int dy = y - other.y;
            int dz = z - other.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
