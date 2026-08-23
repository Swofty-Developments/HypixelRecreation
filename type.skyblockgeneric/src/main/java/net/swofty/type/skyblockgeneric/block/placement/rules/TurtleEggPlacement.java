package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Facing;
import net.swofty.type.skyblockgeneric.block.placement.states.state.IntegerState;

public class TurtleEggPlacement extends PlacementRule {

    private static final IntegerState EGGS = IntegerState.Of("eggs");

    public TurtleEggPlacement() {
        super(Block.TURTLE_EGG);
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        return true;
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
        Point blockPosition = placementState.placePosition();
        BlockFace blockFace = placementState.blockFace();
        BlockUtils selfBlock = new BlockUtils(instance, blockPosition);
        Facing oppositeFace = Facing.parse(blockFace.getOppositeFace());
        BlockUtils oppositeBlock = selfBlock.getRelativeTo(oppositeFace);

        if (oppositeBlock.getBlock() == block()) {
            BlockState oppositeState = oppositeBlock.getState();
            int eggs = oppositeState.get(EGGS);

            if (eggs < 4) {
                blockState.withBlock(Block.AIR);
                eggs = eggs + 1;
                oppositeState.set(EGGS, eggs);
                instance.setBlock(oppositeBlock.position(), oppositeState.block());
            } else {
                if (selfBlock.getBlock().air()) {
                    return;
                }

                eggs = blockState.get(EGGS);

                if (eggs < 4) {
                    eggs = eggs + 1;
                    blockState.set(EGGS, eggs);
                }
            }
        }
    }
}
