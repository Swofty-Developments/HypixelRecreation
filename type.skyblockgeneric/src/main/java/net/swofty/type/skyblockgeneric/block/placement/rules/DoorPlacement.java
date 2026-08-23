package net.swofty.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.BlockUtils;
import net.swofty.type.skyblockgeneric.block.placement.PlacementRule;
import net.swofty.type.skyblockgeneric.block.placement.states.BlockState;
import net.swofty.type.skyblockgeneric.block.placement.states.state.DoorHalf;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Facing;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Hinge;

public class DoorPlacement extends PlacementRule {

    public DoorPlacement(Block block) {
        super(block);
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        Point blockPosition = placementState.placePosition();
        BlockFace blockFace = placementState.blockFace();
        BlockUtils self = new BlockUtils(instance, blockPosition);

        boolean isAir = self.getBlock().air() && self.above().getBlock().air();
        boolean withinHeightLimit = blockPosition.y() < 255;

        return withinHeightLimit && isAir || !(blockFace == BlockFace.BOTTOM);
    }

    @Override
    public boolean canUpdate(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        return false; // Doors do not have update logic in this implementation
    }

    @Override
    public void update(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        // No updates required for doors
    }

    @Override
    public void place(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        Instance instance = (Instance) placementState.instance();
        Point blockPosition = placementState.placePosition();
        Facing facing = blockState.get(Facing.class);

        // Upper block state
        BlockUtils upBlock = new BlockUtils(instance, blockPosition).above();
        BlockState upState = upBlock.getState();
        upState.withBlock(block());

        Hinge hinge = calculateHinge(placementState.cursorPosition(), placementState.playerPosition().yaw());

        BlockFace blockFace = placementState.blockFace();
        if (blockFace != BlockFace.BOTTOM && blockFace != BlockFace.TOP) {
            facing = facing.opposite();
        }

        // Set upper door properties
        upState.set(DoorHalf.UPPER);
        upState.set(hinge);
        upState.set(facing);
        instance.setBlock(upBlock.position(), upState.block());

        // Set lower door properties
        blockState.set(DoorHalf.LOWER);
        blockState.set(hinge);
        blockState.set(facing);
    }

    /**
     * Calculate the hinge side based on the player's cursor position and yaw.
     *
     * @param hit The cursor position
     * @param yaw The player's yaw
     * @return The hinge side (LEFT or RIGHT)
     */
    public Hinge calculateHinge(Point hit, float yaw) {
        Facing facing = Facing.fromYaw(yaw);

        double z = Math.abs(hit.z() - Math.floor(hit.z()));
        double x = Math.abs(hit.x() - Math.floor(hit.x()));

        return switch (facing) {
            case EAST -> z < 0.5 ? Hinge.LEFT : Hinge.RIGHT;
            case WEST -> z > 0.5 ? Hinge.LEFT : Hinge.RIGHT;
            case NORTH -> x < 0.5 ? Hinge.RIGHT : Hinge.LEFT;
            case SOUTH -> x > 0.5 ? Hinge.RIGHT : Hinge.LEFT;
            default -> Hinge.LEFT;
        };
    }
}
