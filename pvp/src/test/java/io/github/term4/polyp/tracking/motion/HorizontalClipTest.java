package io.github.term4.polyp.tracking.motion;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Vanilla {@code positionChanged} is the SERVER move clipping a wall, not the client's WASD - reconstructed
 * by probing the tracked horizontal mot. A knockback residual into a ladder wall fires {@code motY = 0.2}
 * ({@code g()}: {@code positionChanged && k_()}); the water edge-bump fires {@code 0.3} only when the hop
 * target is collision- and liquid-free ({@code c()}).
 */
class HorizontalClipTest extends HeadlessServerTest {

    private static void tick(InstanceContainer inst) { EventDispatcher.call(new InstanceTickEvent(inst, 0, 0)); }

    @Test
    void aResidualIntoTheLadderWallFiresTheClimbUp() {
        InstanceContainer inst = flatInstance(null);
        inst.setBlock(8, 64, 8, Block.LADDER);
        inst.setBlock(8, 64, 9, Block.STONE);
        inst.setBlock(8, 65, 9, Block.STONE);
        Player p = FakePlayer.connect(inst, new Pos(8.5, 64, 8.65), "LadderClip").player;

        MotionTracker.foldDelivered(p, new Vec(0, 0, 0.5));
        tick(inst);
        assertEquals((ClimbModel.CLIMB_UP - 0.08) * 0.98, MotionTracker.serverMotY(p, 0, true), 1e-9,
                "clip on the ladder must fire the vanilla 0.2 climb-up");
        p.remove();
    }

    @Test
    void theEdgeBumpNeedsAClipAndALiquidFreeHopTarget() {
        InstanceContainer inst = flatInstance(null);
        inst.setBlock(8, 64, 8, Block.WATER);
        for (int y = 64; y <= 66; y++) inst.setBlock(8, y, 9, Block.STONE);
        // the vanilla trigger band is narrow: still in water (contracted box bottom <= the fluid top)
        // while the +0.6 hop box already clears the water CELL (c() checks cell materials)
        Player p = FakePlayer.connect(inst, new Pos(8.5, 64.45, 8.65), "EdgeBump").player;

        MotionTracker.foldDelivered(p, new Vec(0, 0, 0.5));
        tick(inst);
        assertEquals(0.3, MotionTracker.serverMotY(p, 0, true), 1e-9,
                "surface swim into the lip must bump 0.3");
        p.remove();

        // deep in the column the hop target is still water - vanilla's c() refuses the bump
        inst.setBlock(8, 65, 8, Block.WATER);
        inst.setBlock(8, 66, 8, Block.WATER);
        Player deep = FakePlayer.connect(inst, new Pos(8.5, 64.45, 8.65), "DeepNoBump").player;
        MotionTracker.foldDelivered(deep, new Vec(0, 0, 0.5));
        tick(inst);
        double v = MotionTracker.serverMotY(deep, 0, true);
        assertEquals(-0.02, v, 0.05, "mid-column: water drag/gravity, never the 0.3 bump");
        deep.remove();
    }
}
