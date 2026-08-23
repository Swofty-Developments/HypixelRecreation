package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Attachment;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Facing;

public class BellPlacement extends PlacementRule {

    public BellPlacement() {
        super(Block.BELL);
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        return true;
    }

    @Override
    public boolean canUpdate(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        return true;
    }

    @Override
    public void update(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        Facing facing = blockState.get(Facing.class);
        Attachment attachment = blockState.get(Attachment.class);
        Instance instance = (Instance) updateState.instance();
        Point blockPosition = updateState.blockPosition();

        BlockUtils selfBlock = new BlockUtils(instance, blockPosition);
        BlockUtils facingBlock = selfBlock.getRelativeTo(facing);
        BlockUtils oppositeBlock = selfBlock.getRelativeTo(facing.opposite());

        if (attachment == Attachment.SINGLE_WALL) {
            if (!facingBlock.getBlock().solid()) {
                instance.setBlock(selfBlock.position(), Block.AIR);
            } else if (oppositeBlock.getBlock().solid()) {
                blockState.set(Attachment.DOUBLE_WALL);
            }
        } else if (attachment == Attachment.DOUBLE_WALL) {
            if (!facingBlock.getBlock().solid() && oppositeBlock.getBlock().solid()) {
                blockState.set(Attachment.SINGLE_WALL);
                blockState.set(facing.opposite());
            } else if (!oppositeBlock.getBlock().solid() && facingBlock.getBlock().solid()) {
                blockState.set(Attachment.SINGLE_WALL);
            } else if (!oppositeBlock.getBlock().solid() && !facingBlock.getBlock().solid()) {
                instance.setBlock(selfBlock.position(), Block.AIR);
            }
        }
    }

    @Override
    public void place(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        BlockFace blockFace = placementState.blockFace();
        Point blockPosition = placementState.placePosition();

        if (blockFace == BlockFace.BOTTOM) {
            blockState.set(Attachment.CEILING);
        } else if (blockFace == BlockFace.TOP) {
            blockState.set(Attachment.FLOOR);
        } else {
            Facing facing = blockState.get(Facing.class);
            BlockUtils selfBlock = new BlockUtils(instance, blockPosition);
            BlockUtils facingBlock = selfBlock.getRelativeTo(facing);
            BlockUtils oppositeBlock = selfBlock.getRelativeTo(facing.opposite());

            if (facingBlock.getBlock().solid() && oppositeBlock.getBlock().solid()) {
                blockState.set(Attachment.DOUBLE_WALL);
            } else if (facingBlock.getBlock().solid() && !oppositeBlock.getBlock().solid()) {
                blockState.set(Attachment.SINGLE_WALL);
            } else if (oppositeBlock.getBlock().solid() && !facingBlock.getBlock().solid()) {
                blockState.set(Attachment.SINGLE_WALL);
                blockState.set(facing.opposite());
            }
        }
    }
}
