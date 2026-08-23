package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.PlantHalf;

import java.util.Arrays;
import java.util.List;

public class DoublePlantPlacement extends PlacementRule {
    private static final List<Block> blockList = Arrays.asList(Block.SUNFLOWER, Block.LILAC, Block.ROSE_BUSH, Block.PEONY, Block.TALL_GRASS, Block.LARGE_FERN);

    public DoublePlantPlacement(Block block) {
        super(block);
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        net.minestom.server.coordinate.Point blockPosition = placementState.placePosition();
        BlockFace blockFace = placementState.blockFace();
        BlockUtils self = new BlockUtils(instance, blockPosition);
        return blockPosition.y() < 255 && self.getBlock().air() && self.above().getBlock().air() || !(blockFace == BlockFace.BOTTOM);
    }

    @Override
    public boolean canUpdate(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        return false;
    }

    @Override
    public void update(BlockState blockState, BlockPlacementRule.UpdateState updateState) {

    }

    @Override
    public void place(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        net.minestom.server.coordinate.Point blockPosition = placementState.placePosition();
        BlockUtils downBlock = new BlockUtils(instance, blockPosition).below();
        PlantHalf plantHalf = PlantHalf.LOWER;

        if (downBlock.equals(blockState.block()))
            plantHalf = PlantHalf.UPPER;

        blockState.set(plantHalf);
    }

    public static boolean isDoublePlant(Block block) {
        return blockList.contains(block);
    }
}
