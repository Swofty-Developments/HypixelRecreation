package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.BooleanState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Directional;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Facing;

import java.util.Arrays;
import java.util.List;

public class CardinalPlacement extends PlacementRule {

    private static final List<String> cardinalPlacement = Arrays.asList(
            "minecraft:acacia_fence",
            "minecraft:birch_fence",
            "minecraft:black_stained_glass_pane",
            "minecraft:blue_stained_glass_pane",
            "minecraft:brown_stained_glass_pane",
            "minecraft:crimson_fence",
            "minecraft:cyan_stained_glass_pane",
            "minecraft:dark_oak_fence",
            "minecraft:glass_pane",
            "minecraft:gray_stained_glass_pane",
            "minecraft:green_stained_glass_pane",
            "minecraft:iron_bars",
            "minecraft:jungle_fence",
            "minecraft:light_blue_stained_glass_pane",
            "minecraft:light_gray_stained_glass_pane",
            "minecraft:lime_stained_glass_pane",
            "minecraft:magenta_stained_glass_pane",
            "minecraft:nether_brick_fence",
            "minecraft:oak_fence",
            "minecraft:orange_stained_glass_pane",
            "minecraft:pink_stained_glass_pane",
            "minecraft:purple_stained_glass_pane",
            "minecraft:red_stained_glass_pane",
            "minecraft:spruce_fence",
            "minecraft:tripwire",
            "minecraft:warped_fence",
            "minecraft:white_stained_glass_pane",
            "minecraft:yellow_stained_glass_pane");

    private static final List<String> noDown = Arrays.asList(
            "minecraft:fire",
            "minecraft:vine");

    private static final List<String> cardinalWithUp = Arrays.asList(
            "minecraft:brown_mushroom_block",
            "minecraft:chorus_plant",
            "minecraft:fire",
            "minecraft:mushroom_stem",
            "minecraft:red_mushroom_block",
            "minecraft:vine");

    private final boolean isMushroom;

    public CardinalPlacement(Block block) {
        super(block);
        this.isMushroom = block.name().contains("mushroom");
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        Point blockPosition = placementState.placePosition();
        BlockFace blockFace = placementState.blockFace();
        BlockUtils block = new BlockUtils(instance, blockPosition);

        if (noDown.contains(blockState.block().name())) {
            boolean blockNorth = block.north().getBlock().solid();
            boolean blockSouth = block.south().getBlock().solid();
            boolean blockEast = block.east().getBlock().solid();
            boolean blockWest = block.west().getBlock().solid();
            boolean blockUp = block.above().getBlock().solid();
            boolean blockDown = block.below().getBlock().solid();

            return blockNorth || blockSouth || blockEast || blockWest || blockUp || blockDown;
        }

        Facing oppositeFace = Facing.parse(blockFace.getOppositeFace());
        return (block() != Block.VINE || blockFace != BlockFace.TOP) &&
                canAttach(block.getRelativeTo(oppositeFace).getBlock());
    }

    @Override
    public boolean canUpdate(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        return true;
    }

    @Override
    public void update(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        placeOrUpdate((Instance) updateState.instance(), updateState.blockPosition(), blockState);
    }

    @Override
    public void place(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        placeOrUpdate((Instance) placementState.instance(), placementState.placePosition(), blockState);
    }

    public void placeOrUpdate(Instance instance, Point blockPosition, BlockState blockState) {
        BlockUtils block = new BlockUtils(instance, blockPosition);

        BlockUtils blockNorth = block.north();
        BlockUtils blockSouth = block.south();
        BlockUtils blockEast = block.east();
        BlockUtils blockWest = block.west();

        boolean east = isCardinalBlock(blockEast.getBlock()) || canAttach(blockEast.getBlock());
        boolean north = isCardinalBlock(blockNorth.getBlock()) || canAttach(blockNorth.getBlock());
        boolean south = isCardinalBlock(blockSouth.getBlock()) || canAttach(blockSouth.getBlock());
        boolean west = isCardinalBlock(blockWest.getBlock()) || canAttach(blockWest.getBlock());

        if (isMushroom) {
            east = !east;
            north = !north;
            south = !south;
            west = !west;
        }

        blockState.set(Directional.EAST, BooleanState.Of(east));
        blockState.set(Directional.NORTH, BooleanState.Of(north));
        blockState.set(Directional.SOUTH, BooleanState.Of(south));
        blockState.set(Directional.WEST, BooleanState.Of(west));

        if (hasUp(blockState.block())) {
            BlockUtils blockUp = block.above();
            blockState.set(Directional.UP, BooleanState.Of(isMushroom != blockUp.getBlock().solid()));
        }

        if (hasDown(blockState.block())) {
            BlockUtils blockDown = block.below();
            blockState.set(Directional.DOWN, BooleanState.Of(isMushroom != blockDown.getBlock().solid()));
        }
    }

    public static boolean isCardinalBlock(Block block) {
        return cardinalPlacement.contains(block.name()) || cardinalWithUp.contains(block.name());
    }

    public boolean hasUp(Block block) {
        return cardinalWithUp.contains(block.name());
    }

    public boolean hasDown(Block block) {
        return cardinalWithUp.contains(block.name()) && !noDown.contains(block.name());
    }

    public boolean canAttach(Block block) {
        return block() == Block.VINE ? (block != Block.VINE && block.solid()) : !block.air() && block.solid();
    }
}
