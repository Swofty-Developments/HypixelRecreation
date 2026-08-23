package net.swofty.type.generic.utility;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockTags;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTag;
import net.swofty.type.generic.entity.ai.vanilla.PathComputationType;

/**
 * Static helpers mapping a Minestom {@link Block} to the semantics the walk pathfinder needs.
 * Classification mirrors vanilla {@code WalkNodeEvaluator.getPathTypeFromState} and the block tags
 * it consults.
 *
 * <p>Fence/door/rail/climbable/... membership is resolved against the vanilla block tags
 * ({@code #minecraft:fences}, {@code #minecraft:climbable}, ...) rather than name matching, so any
 * block Mojang adds to those tags is classified correctly. Properties ({@code open}, {@code lit})
 * are read via {@link Block#getProperty(String)}.
 */
public final class BlockProps {
   private BlockProps() {
   }

   private static final Registry<Block> BLOCKS = Block.staticRegistry();

   private static final RegistryTag<Block> FENCES = BLOCKS.getTag(BlockTags.FENCES);
   private static final RegistryTag<Block> WALLS = BLOCKS.getTag(BlockTags.WALLS);
   private static final RegistryTag<Block> FENCE_GATES = BLOCKS.getTag(BlockTags.FENCE_GATES);
   private static final RegistryTag<Block> DOORS = BLOCKS.getTag(BlockTags.DOORS);
   private static final RegistryTag<Block> WOODEN_DOORS = BLOCKS.getTag(BlockTags.WOODEN_DOORS);
   private static final RegistryTag<Block> TRAPDOORS = BLOCKS.getTag(BlockTags.TRAPDOORS);
   private static final RegistryTag<Block> RAILS = BLOCKS.getTag(BlockTags.RAILS);
   private static final RegistryTag<Block> CLIMBABLE = BLOCKS.getTag(BlockTags.CLIMBABLE);
   private static final RegistryTag<Block> LEAVES = BLOCKS.getTag(BlockTags.LEAVES);
   private static final RegistryTag<Block> FIRE = BLOCKS.getTag(BlockTags.FIRE);
   private static final RegistryTag<Block> CAMPFIRES = BLOCKS.getTag(BlockTags.CAMPFIRES);

   /** True when {@code block} is a member of the given vanilla block tag. */
   private static boolean in(final RegistryTag<Block> blockTag, final Block block) {
      return blockTag != null && blockTag.contains(BLOCKS.getKey(block));
   }

   /** True when {@code block} is {@code type}, ignoring block-state properties. */
   private static boolean is(final Block block, final Block type) {
      return block.compare(type, Block.Comparator.ID);
   }

   // ----- basic physical predicates (delegate to Minestom registry) -----

   public static boolean isAir(final Block block) {
      return block.air();
   }

   public static boolean isSolid(final Block block) {
      return block.solid();
   }

   public static boolean blocksMotion(final Block block) {
      return block.solid();
   }

   public static boolean isFluid(final Block block) {
      // 26.1.2: BlockEntry has no isFluid(); block.isLiquid() covers water/lava (and flowing variants).
      return block.liquid();
   }

   public static boolean isWater(final Block block) {
      return is(block, Block.WATER) || is(block, Block.BUBBLE_COLUMN) || "true".equals(block.getProperty("waterlogged"));
   }

   public static boolean isLava(final Block block) {
      return is(block, Block.LAVA);
   }

   // ----- gates / doors / trapdoors -----

   public static boolean isFence(final Block block) {
      return in(FENCES, block) || in(WALLS, block);
   }

   public static boolean isFenceGate(final Block block) {
      return in(FENCE_GATES, block);
   }

   public static boolean isDoor(final Block block) {
      return in(DOORS, block);
   }

   public static boolean isWoodenDoor(final Block block) {
      return in(WOODEN_DOORS, block);
   }

   public static boolean isIronDoor(final Block block) {
      return in(DOORS, block) && !in(WOODEN_DOORS, block);
   }

   public static boolean isTrapdoor(final Block block) {
      // vanilla also treats lily pad and big dripleaf as trapdoor-like walkable tops (no shared tag)
      return in(TRAPDOORS, block) || is(block, Block.LILY_PAD) || is(block, Block.BIG_DRIPLEAF);
   }

   /** True if a door / trapdoor / fence gate is currently open. */
   public static boolean isOpen(final Block block) {
      return "true".equals(block.getProperty("open"));
   }

   // ----- misc terrain classifications -----

   public static boolean isRail(final Block block) {
      return in(RAILS, block);
   }

   public static boolean isClimbable(final Block block) {
      return in(CLIMBABLE, block);
   }

   public static boolean isLeaves(final Block block) {
      return in(LEAVES, block);
   }

   public static boolean isHoney(final Block block) {
      return is(block, Block.HONEY_BLOCK);
   }

   public static boolean isPowderSnow(final Block block) {
      return is(block, Block.POWDER_SNOW);
   }

   public static boolean isCocoa(final Block block) {
      return is(block, Block.COCOA);
   }

   public static boolean isWitherRose(final Block block) {
      return is(block, Block.WITHER_ROSE);
   }

   /** Vanilla {@code BlockTags.SPELEOTHEMS} — pointed dripstone. Treated as DAMAGE_CAUTIOUS. */
   public static boolean isSpeleothem(final Block block) {
      return is(block, Block.POINTED_DRIPSTONE);
   }

   public static boolean isCactusOrBerryBush(final Block block) {
      return is(block, Block.CACTUS) || is(block, Block.SWEET_BERRY_BUSH);
   }

   /** Mirrors {@code NodeEvaluator.isBurningBlock}. */
   public static boolean isBurningBlock(final Block block) {
      return in(FIRE, block) // fire + soul_fire
         || is(block, Block.LAVA)
         || is(block, Block.MAGMA_BLOCK)
         || is(block, Block.LAVA_CAULDRON)
         || isLitCampfire(block);
   }

   public static boolean isLitCampfire(final Block block) {
      return in(CAMPFIRES, block) && "true".equals(block.getProperty("lit"));
   }

   /** Whether a mob can path through this block (vanilla {@code BlockState.isPathfindable}). */
   public static boolean isPathfindable(final Block block, final PathComputationType type) {
      if (block.air()) {
         return true;
      }
      return switch (type) {
         case LAND -> !block.solid();
         case WATER -> block.liquid();
         case AIR -> !block.solid();
      };
   }

   /** Block friction used by ground travel (grass/dirt/stone = 0.6, ice higher, slime lower). */
   public static float getFriction(final Block block) {
      return block.friction();
   }
}
