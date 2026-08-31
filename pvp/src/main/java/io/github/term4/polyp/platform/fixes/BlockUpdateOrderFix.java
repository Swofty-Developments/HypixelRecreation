package io.github.term4.polyp.platform.fixes;

import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.BlockFace;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Minestom notifies neighbors north/south before the vertical pair; vanilla is west, east, down, up, north,
 * south ({@code NeighborUpdater.UPDATE_ORDER}, unchanged since 1.8). Reorders the private face array in
 * place - final pins the reference, not the elements. Remove once the upstream fix ships.
 */
public final class BlockUpdateOrderFix {

    private static final BlockFace[] VANILLA_ORDER = {
            BlockFace.WEST, BlockFace.EAST, BlockFace.BOTTOM, BlockFace.TOP, BlockFace.NORTH, BlockFace.SOUTH
    };

    private BlockUpdateOrderFix() {}

    public static void install() {
        try {
            Field field = InstanceContainer.class.getDeclaredField("BLOCK_UPDATE_FACES");
            field.setAccessible(true);
            BlockFace[] faces = (BlockFace[]) field.get(null);
            if (faces.length != VANILLA_ORDER.length) throw new IllegalStateException("unexpected face count " + faces.length);
            System.arraycopy(VANILLA_ORDER, 0, faces, 0, faces.length);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LoggerFactory.getLogger(BlockUpdateOrderFix.class)
                    .warn("block update order fix unavailable (upstream changed?): {}", e.toString());
        }
    }
}
