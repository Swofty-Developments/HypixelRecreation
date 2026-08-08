package io.github.term4.polyp.world;

import java.util.HashSet;
import java.util.Set;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

/**
 * Vanilla fire support (1.8 {@code BlockFire.doPhysics} == modern {@code FireBlock.updateShape}): fire survives a
 * neighbor change iff the block below has a sturdy top face OR any of the 6 neighbors is flammable; soul fire iff
 * the block below is soul sand/soil. Minestom runs no neighbor updates, so break paths sweep themselves.
 */
public final class FireSupport {

    /** Modern fire-extinguish world event; Via maps it onto 1.8's 1004. */
    public static final int FIZZ = 1009;

    private static final Vec[] NEIGHBORS = {
            new Vec(1, 0, 0), new Vec(-1, 0, 0), new Vec(0, 1, 0),
            new Vec(0, -1, 0), new Vec(0, 0, 1), new Vec(0, 0, -1),
    };

    private FireSupport() {}

    public static boolean isFire(Block block) {
        return block.compare(Block.FIRE) || block.compare(Block.SOUL_FIRE);
    }

    /**
     * Vanilla's neighbor update after {@code broken} is destroyed: silently removes adjacent fire the break left
     * unsupported. {@code broken} reads as air - break events run before the world write.
     */
    public static void sweep(MechanicsWorld world, Point broken) {
        for (Vec offset : NEIGHBORS) {
            Point cell = broken.add(offset);
            if (!world.isChunkLoaded(cell)) continue;
            Block block = world.getBlock(cell, Block.Getter.Condition.TYPE);
            if (!isFire(block)) continue;
            if (!survives(world, cell, block, broken)) world.setBlock(cell, Block.AIR);
        }
    }

    private static boolean survives(MechanicsWorld world, Point fire, Block type, Point broken) {
        Point below = fire.add(0, -1, 0);
        if (type.compare(Block.SOUL_FIRE)) {
            Block ground = read(world, below, broken);
            return ground.compare(Block.SOUL_SAND) || ground.compare(Block.SOUL_SOIL);
        }
        if (read(world, below, broken).registry().collisionShape().isFaceFull(BlockFace.TOP)) return true;
        for (Vec offset : NEIGHBORS) {
            Point cell = fire.add(offset);
            if (!world.isChunkLoaded(cell)) return true; // unknown neighbor: keep rather than guess-remove
            if (FLAMMABLE.contains(read(world, cell, broken).id())) return true;
        }
        return false;
    }

    private static Block read(MechanicsWorld world, Point at, Point broken) {
        return at.sameBlock(broken) ? Block.AIR : world.getBlock(at);
    }

    /** Modern {@code FireBlock.bootstrap} ignite table (superset of 1.8's; 1.8's missing acacia/dark-oak stairs quirk not kept). */
    private static final Set<Integer> FLAMMABLE = flammable();

    private static Set<Integer> flammable() {
        Block[] blocks = {
                Block.OAK_PLANKS, Block.SPRUCE_PLANKS, Block.BIRCH_PLANKS, Block.JUNGLE_PLANKS, Block.ACACIA_PLANKS,
                Block.CHERRY_PLANKS, Block.DARK_OAK_PLANKS, Block.PALE_OAK_PLANKS, Block.MANGROVE_PLANKS,
                Block.BAMBOO_PLANKS, Block.BAMBOO_MOSAIC,
                Block.OAK_SLAB, Block.SPRUCE_SLAB, Block.BIRCH_SLAB, Block.JUNGLE_SLAB, Block.ACACIA_SLAB,
                Block.CHERRY_SLAB, Block.DARK_OAK_SLAB, Block.PALE_OAK_SLAB, Block.MANGROVE_SLAB,
                Block.BAMBOO_SLAB, Block.BAMBOO_MOSAIC_SLAB,
                Block.OAK_FENCE_GATE, Block.SPRUCE_FENCE_GATE, Block.BIRCH_FENCE_GATE, Block.JUNGLE_FENCE_GATE,
                Block.ACACIA_FENCE_GATE, Block.CHERRY_FENCE_GATE, Block.DARK_OAK_FENCE_GATE, Block.PALE_OAK_FENCE_GATE,
                Block.MANGROVE_FENCE_GATE, Block.BAMBOO_FENCE_GATE,
                Block.OAK_FENCE, Block.SPRUCE_FENCE, Block.BIRCH_FENCE, Block.JUNGLE_FENCE, Block.ACACIA_FENCE,
                Block.CHERRY_FENCE, Block.DARK_OAK_FENCE, Block.PALE_OAK_FENCE, Block.MANGROVE_FENCE, Block.BAMBOO_FENCE,
                Block.OAK_STAIRS, Block.BIRCH_STAIRS, Block.SPRUCE_STAIRS, Block.JUNGLE_STAIRS, Block.ACACIA_STAIRS,
                Block.CHERRY_STAIRS, Block.DARK_OAK_STAIRS, Block.PALE_OAK_STAIRS, Block.MANGROVE_STAIRS,
                Block.BAMBOO_STAIRS, Block.BAMBOO_MOSAIC_STAIRS,
                Block.OAK_LOG, Block.SPRUCE_LOG, Block.BIRCH_LOG, Block.JUNGLE_LOG, Block.ACACIA_LOG, Block.CHERRY_LOG,
                Block.PALE_OAK_LOG, Block.DARK_OAK_LOG, Block.MANGROVE_LOG, Block.BAMBOO_BLOCK,
                Block.STRIPPED_OAK_LOG, Block.STRIPPED_SPRUCE_LOG, Block.STRIPPED_BIRCH_LOG, Block.STRIPPED_JUNGLE_LOG,
                Block.STRIPPED_ACACIA_LOG, Block.STRIPPED_CHERRY_LOG, Block.STRIPPED_DARK_OAK_LOG,
                Block.STRIPPED_PALE_OAK_LOG, Block.STRIPPED_MANGROVE_LOG, Block.STRIPPED_BAMBOO_BLOCK,
                Block.STRIPPED_OAK_WOOD, Block.STRIPPED_SPRUCE_WOOD, Block.STRIPPED_BIRCH_WOOD,
                Block.STRIPPED_JUNGLE_WOOD, Block.STRIPPED_ACACIA_WOOD, Block.STRIPPED_CHERRY_WOOD,
                Block.STRIPPED_DARK_OAK_WOOD, Block.STRIPPED_PALE_OAK_WOOD, Block.STRIPPED_MANGROVE_WOOD,
                Block.OAK_WOOD, Block.SPRUCE_WOOD, Block.BIRCH_WOOD, Block.JUNGLE_WOOD, Block.ACACIA_WOOD,
                Block.CHERRY_WOOD, Block.PALE_OAK_WOOD, Block.DARK_OAK_WOOD, Block.MANGROVE_WOOD, Block.MANGROVE_ROOTS,
                Block.OAK_LEAVES, Block.SPRUCE_LEAVES, Block.BIRCH_LEAVES, Block.JUNGLE_LEAVES, Block.ACACIA_LEAVES,
                Block.CHERRY_LEAVES, Block.DARK_OAK_LEAVES, Block.PALE_OAK_LEAVES, Block.MANGROVE_LEAVES,
                Block.AZALEA_LEAVES, Block.FLOWERING_AZALEA_LEAVES,
                Block.BOOKSHELF, Block.TNT,
                Block.SHORT_GRASS, Block.FERN, Block.DEAD_BUSH, Block.SHORT_DRY_GRASS, Block.TALL_DRY_GRASS,
                Block.SUNFLOWER, Block.LILAC, Block.ROSE_BUSH, Block.PEONY, Block.TALL_GRASS, Block.LARGE_FERN,
                Block.DANDELION, Block.GOLDEN_DANDELION, Block.POPPY, Block.OPEN_EYEBLOSSOM, Block.CLOSED_EYEBLOSSOM,
                Block.BLUE_ORCHID, Block.ALLIUM, Block.AZURE_BLUET, Block.RED_TULIP, Block.ORANGE_TULIP,
                Block.WHITE_TULIP, Block.PINK_TULIP, Block.OXEYE_DAISY, Block.CORNFLOWER, Block.LILY_OF_THE_VALLEY,
                Block.TORCHFLOWER, Block.PITCHER_PLANT, Block.WITHER_ROSE, Block.PINK_PETALS, Block.WILDFLOWERS,
                Block.LEAF_LITTER, Block.CACTUS_FLOWER,
                Block.WHITE_WOOL, Block.ORANGE_WOOL, Block.MAGENTA_WOOL, Block.LIGHT_BLUE_WOOL, Block.YELLOW_WOOL,
                Block.LIME_WOOL, Block.PINK_WOOL, Block.GRAY_WOOL, Block.LIGHT_GRAY_WOOL, Block.CYAN_WOOL,
                Block.PURPLE_WOOL, Block.BLUE_WOOL, Block.BROWN_WOOL, Block.GREEN_WOOL, Block.RED_WOOL, Block.BLACK_WOOL,
                Block.VINE, Block.COAL_BLOCK, Block.HAY_BLOCK, Block.TARGET,
                Block.WHITE_CARPET, Block.ORANGE_CARPET, Block.MAGENTA_CARPET, Block.LIGHT_BLUE_CARPET,
                Block.YELLOW_CARPET, Block.LIME_CARPET, Block.PINK_CARPET, Block.GRAY_CARPET, Block.LIGHT_GRAY_CARPET,
                Block.CYAN_CARPET, Block.PURPLE_CARPET, Block.BLUE_CARPET, Block.BROWN_CARPET, Block.GREEN_CARPET,
                Block.RED_CARPET, Block.BLACK_CARPET,
                Block.PALE_MOSS_BLOCK, Block.PALE_MOSS_CARPET, Block.PALE_HANGING_MOSS, Block.DRIED_KELP_BLOCK,
                Block.BAMBOO, Block.SCAFFOLDING, Block.LECTERN, Block.COMPOSTER, Block.SWEET_BERRY_BUSH,
                Block.BEEHIVE, Block.BEE_NEST, Block.CAVE_VINES, Block.CAVE_VINES_PLANT, Block.SPORE_BLOSSOM,
                Block.AZALEA, Block.FLOWERING_AZALEA, Block.BIG_DRIPLEAF, Block.BIG_DRIPLEAF_STEM, Block.SMALL_DRIPLEAF,
                Block.HANGING_ROOTS, Block.GLOW_LICHEN, Block.FIREFLY_BUSH, Block.BUSH,
                Block.ACACIA_SHELF, Block.BAMBOO_SHELF, Block.BIRCH_SHELF, Block.CHERRY_SHELF, Block.DARK_OAK_SHELF,
                Block.JUNGLE_SHELF, Block.MANGROVE_SHELF, Block.OAK_SHELF, Block.PALE_OAK_SHELF, Block.SPRUCE_SHELF,
        };
        Set<Integer> ids = new HashSet<>();
        for (Block b : blocks) ids.add(b.id());
        return Set.copyOf(ids);
    }
}
