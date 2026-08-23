package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Facing;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Part;

public class BedPlacement extends PlacementRule {

    public BedPlacement(Block block) {
        super(block);
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        BlockUtils blockUtils = new BlockUtils(instance, placementState.placePosition());
        Facing playerFacing = Facing.fromYaw(placementState.playerPosition().yaw());
        return blockUtils.getBlock().air() && blockUtils.getRelativeTo(playerFacing).getBlock().air();
    }

    @Override
    public boolean canUpdate(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        return false;
    }

    @Override
    public void update(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        // Empty implementation
    }

    @Override
    public void place(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        net.minestom.server.coordinate.Point blockPosition = placementState.placePosition();
        BlockUtils footBlock = new BlockUtils(instance, blockPosition);
        Facing playerFacing = Facing.fromYaw(placementState.playerPosition().yaw());
        BlockUtils headBlock = footBlock.getRelativeTo(playerFacing);

        if (footBlock.getBlock().air() && headBlock.getBlock().air()) {
            blockState.set(Part.FOOT);
            BlockState headStates = headBlock.getState();
            headStates.withBlock(block());
            headStates.set(Part.HEAD);
            headStates.set(playerFacing);
            blockState.set(playerFacing);
            instance.setBlock(headBlock.position(), headStates.block());
        }
    }
}
