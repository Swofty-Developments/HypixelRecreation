package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockStateManager;
import net.swofty.type.skyblockgeneric.block.placement.states.FenceGateState;
import net.swofty.type.skyblockgeneric.block.placement.states.WallState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.BooleanState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Directional;
import net.swofty.type.skyblockgeneric.block.placement.states.state.WallHeight;

import java.util.stream.Stream;

public class WallPlacement extends PlacementRule {

    public WallPlacement(Block block) {
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
        updateState(blockState, (Instance) updateState.instance(), updateState.blockPosition());
    }

    @Override
    public void place(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        updateState(blockState, (Instance) placementState.instance(), placementState.placePosition());
    }

    public void updateState(BlockState blockState, Instance instance, Point blockPosition) {
        BlockUtils selfBlock = new BlockUtils(instance, blockPosition);
        BlockUtils northBlock = selfBlock.north();
        BlockUtils southBlock = selfBlock.south();
        BlockUtils westBlock = selfBlock.west();
        BlockUtils eastBlock = selfBlock.east();
        BlockUtils upBlock = selfBlock.above();

        BlockState northState = northBlock.getState();
        BlockState southState = southBlock.getState();
        BlockState westState = westBlock.getState();
        BlockState eastState = eastBlock.getState();
        BlockState upState = upBlock.getState();

        boolean northConnect = shouldConnect(northState, isSolidBlock(northBlock.getBlock()), Directional.SOUTH);
        boolean eastConnect = shouldConnect(eastState, isSolidBlock(eastBlock.getBlock()), Directional.WEST);
        boolean southConnect = shouldConnect(southState, isSolidBlock(southBlock.getBlock()), Directional.NORTH);
        boolean westConnect = shouldConnect(westState, isSolidBlock(westBlock.getBlock()), Directional.EAST);

        blockState.set(Directional.SOUTH, getWallHeight(southConnect, Directional.SOUTH, selfBlock));
        blockState.set(Directional.EAST, getWallHeight(eastConnect, Directional.EAST, selfBlock));
        blockState.set(Directional.WEST, getWallHeight(westConnect, Directional.WEST, selfBlock));
        blockState.set(Directional.NORTH, getWallHeight(northConnect, Directional.NORTH, selfBlock));

        blockState.set(BooleanState.Waterlogged(false));

        blockState.set(BooleanState.Of(Directional.UP, needUp(blockState, upState)));
    }

    public WallHeight getWallHeight(boolean connected, Directional directional, BlockUtils selfBlock) {
        if (connected) {
            BlockUtils upBlock = selfBlock.above();
            if (upBlock.getBlock().solid() && !isWall(upBlock.getState()))
                return WallHeight.TALL;
            else if (isWall(upBlock.getState())) {
                BlockState upState = upBlock.getState();
                if (upState.get(directional, WallHeight.class) != WallHeight.NONE)
                    return WallHeight.TALL;
                else
                    return WallHeight.LOW;
            } else
                return WallHeight.LOW;
        } else
            return WallHeight.NONE;
    }

    public boolean shouldConnect(BlockState state, boolean sideColid, Directional directional) {
        Block block = state.block();
        boolean flag = isFenceGate(block) && ((FenceGateState) BlockStateManager.get(block)).isParallel(state, directional);
        return isWall(state) || !cannotAttach(block) && sideColid || isPane(block) || flag;
    }

    public boolean needUp(BlockState state, BlockState upState) {
        if (isWall(upState) && upState.get(BooleanState.Of("up"))) {
            return true;
        } else {

            long connected = Stream.of(Directional.axis)
                    .map(directional -> state.get(directional, WallHeight.class))
                    .filter(wallHeight -> wallHeight != WallHeight.NONE)
                    .count();

            if (connected == 0 || connected == 1 || connected == 3) {
                return true;
            } else if (connected == 2 || connected == 4) {
                if (connected == 2) {
                    boolean east = state.get(Directional.EAST, WallHeight.class) != WallHeight.NONE;
                    boolean west = state.get(Directional.WEST, WallHeight.class) != WallHeight.NONE;
                    boolean north = state.get(Directional.NORTH, WallHeight.class) != WallHeight.NONE;
                    boolean south = state.get(Directional.SOUTH, WallHeight.class) != WallHeight.NONE;

                    return !(east == west) || !(north == south);
                } else {
                    if (isWall(upState)) {
                        return upState.get(BooleanState.Of("up"));
                    } else {
                        return isSolidBlock(upState.block());
                    }
                }
            } else {
                return true;
            }
        }
    }

    public boolean cannotAttach(Block blockIn) {
        return blockIn.name().contains("leaves") || blockIn == Block.BARRIER || blockIn == Block.CARVED_PUMPKIN || blockIn == Block.JACK_O_LANTERN
                || blockIn == Block.MELON || blockIn == Block.PUMPKIN || blockIn.name().contains("shulker_block");
    }

    public boolean isSolidBlock(Block block) {
        return block.solid();
    }

    public boolean isFenceGate(Block block) {
        return block.name().contains("fence_gate");
    }

    public boolean isWall(BlockState state) {
        return state instanceof WallState;
    }

    public boolean isPane(Block block) {
        return block.name().contains("pane") || block == Block.IRON_BARS;
    }
}
