package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.IntegerState;

public class SnowLayerPlacement extends PlacementRule {

    private static final IntegerState LAYERS = IntegerState.Of("layers");

    public SnowLayerPlacement() {
        super(Block.SNOW);
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Point blockPosition = placementState.placePosition();
        Instance instance = (Instance) placementState.instance();
        BlockFace blockFace = placementState.blockFace();

        Block block = instance.getBlock(blockPosition);
        return (block.air() || block == block()) && blockFace == BlockFace.TOP;
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
        Point blockPosition = placementState.placePosition();
        Instance instance = (Instance) placementState.instance();
        BlockFace blockFace = placementState.blockFace();

        BlockUtils selfBlock = new BlockUtils(instance, blockPosition);
        BlockUtils downBlock = selfBlock.below();

        if (blockFace == BlockFace.TOP) {
            if (downBlock.getBlock() == block()) {
                BlockState downState = downBlock.getState();
                int layers = downState.get(LAYERS);

                if (layers < 8) {
                    blockState.withBlock(Block.AIR);
                    layers = layers + 1;
                    downState.set(LAYERS, layers);
                    instance.setBlock(downBlock.position(), downState.block());
                }

            } else {
                blockState.set(LAYERS, 1);
            }
        }
    }
}
