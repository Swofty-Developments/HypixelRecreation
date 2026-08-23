package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.SlabType;

public class SlabPlacement extends PlacementRule {
    public SlabPlacement(Block block) {
        super(block);
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Block currentBlock = placementState.instance().getBlock(placementState.placePosition());
        return currentBlock.air() || currentBlock == block();
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
        BlockUtils centerBlock = new BlockUtils(instance, placementState.placePosition());
        BlockFace blockFace = placementState.blockFace();
        BlockUtils downBlock = centerBlock.below();
        BlockUtils upBlock = centerBlock.above();

        if (blockFace == BlockFace.TOP) {
            if (downBlock.getBlock() == block()) {
                BlockState downStates = downBlock.getState();
                SlabType slabType = downStates.get(SlabType.class);

                if (slabType == SlabType.BOTTOM) {
                    downStates.set(SlabType.DOUBLE);
                    instance.setBlock(downBlock.position(), downStates.block());
                    blockState.withBlock(Block.AIR);
                    return;
                }
            }
        }

        if (blockFace == BlockFace.BOTTOM) {
            if (upBlock.getBlock().compare(block())) {
                BlockState upStates = upBlock.getState();
                SlabType slabType = upStates.get(SlabType.class);

                if (slabType == SlabType.TOP) {
                    blockState.withBlock(Block.AIR);
                    upStates.set(SlabType.DOUBLE);
                    instance.setBlock(upBlock.position(), upStates.block());
                    return;
                }
            }
        }

        if (blockFace != BlockFace.TOP && blockFace != BlockFace.BOTTOM) {
            var hit = placementState.cursorPosition();
            double y = hit.y() - hit.blockY();
            SlabType currentSlabType = blockState.get(SlabType.class);

            if (y >= 0.5 && currentSlabType == SlabType.BOTTOM) {
                blockState.set(SlabType.TOP);
            } else if (y <= 0.5 && currentSlabType == SlabType.TOP) {
                blockState.set(SlabType.DOUBLE);
            } else {
                blockState.set((y >= 0.5) ? SlabType.TOP : SlabType.BOTTOM);
            }
        }
    }
}
