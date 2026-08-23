package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;

public class BambooPlacement extends PlacementRule {
    public BambooPlacement() {
        super(Block.BAMBOO);
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        Point blockPosition = placementState.placePosition();
        Block bellowBlock = instance.getBlock(blockPosition.sub(0, 1, 0));
        Block block = instance.getBlock(blockPosition);
        return (bellowBlock.solid() || bellowBlock == Block.BAMBOO_SAPLING) && block.air();
    }

    @Override
    public boolean canUpdate(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        return true;
    }

    @Override
    public void update(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        Instance instance = (Instance) updateState.instance();
        Point blockPosition = updateState.blockPosition();

        BlockUtils upper = new BlockUtils(instance, blockPosition);
        BlockUtils bellow = new BlockUtils(instance, blockPosition).below();

        if (bellow.getBlock().air() || !bellow.getBlock().solid()) {
            while (upper.getBlock() == Block.BAMBOO) {
                instance.setBlock(blockPosition, Block.AIR);
                blockPosition.add(0, 1, 0);
                upper.above();
            }
        }
    }

    @Override
    public void place(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        Point blockPosition = placementState.placePosition();

        BlockUtils blockUtils = new BlockUtils(instance, blockPosition);
        BlockUtils below = blockUtils.below();

        if (below.getBlock() != Block.BAMBOO && below.getBlock() != Block.BAMBOO_SAPLING) {
            blockState.withBlock(Block.BAMBOO_SAPLING);
        } else if (below.getBlock() == Block.BAMBOO_SAPLING) {
            instance.setBlock(blockPosition.sub(0, 1, 0), Block.BAMBOO);
        }
    }
}
