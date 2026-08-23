package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.BooleanState;

public class LanternPlacement extends PlacementRule {

    private final BooleanState HANGING = BooleanState.Of("hanging", false);

    public LanternPlacement(Block block) {
        super(block);
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
        Instance instance = (Instance) updateState.instance();
        Point blockPosition = updateState.blockPosition();
        BlockUtils block = new BlockUtils(instance, blockPosition);

        boolean hanging = blockState.get(HANGING);
        boolean blockUp = block.above().getBlock().solid();

        if (!hanging && blockUp)
            blockState.set(HANGING.setValue(true));
        else if (hanging && !blockUp)
            blockState.set(HANGING.setValue(false));
    }

    @Override
    public void place(BlockState blockState, BlockPlacementRule.PlacementState placementState) {

    }
}
