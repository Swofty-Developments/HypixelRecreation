package test.presets;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * mmcpillarpearllow: 16 mid-range throws at a wall with a low arm at head height one block out - minemen
 * teleported every one, landing short of the arm.
 */
class LowArmPearlReplayTest extends PearlReplayTest {

    @Test
    void lowArmReplayMatchesMinemen() throws Exception {
        // wall x=918 only ("face at 917" was a tp-base misread) + the recorded arm; built wide, throws spread z 24-29
        for (int z = 938022; z <= 938031; z++)
            for (int y = 70; y <= 72; y++) instance.setBlock(937918, y, z, Block.STONE);
        instance.setBlock(937917, 72, 938026, Block.STONE);
        for (int x = 937910; x <= 937918; x++)
            for (int z = 938020; z <= 938032; z++) instance.setBlock(x, 69, z, Block.STONE);

        Replay r = replay("/mmc-lowarm-corpus.json", "low-arm", new Pos(937914, 70, 938026));
        assertTrue(r.falseRefusal() == 0 && r.ok() == r.n(), r.summary());
    }
}
