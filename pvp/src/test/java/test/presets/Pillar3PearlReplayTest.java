package test.presets;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * mmcpillarpearl3: 32 throws at a 3-block pillar with a 1-block overhang, thrower never under it - minemen
 * teleported every one. The capture that killed target-anchored refusal.
 */
class Pillar3PearlReplayTest extends PearlReplayTest {

    @Test
    void pillar3ReplayMatchesMinemen() throws Exception {
        for (int y = 70; y <= 72; y++) instance.setBlock(940737, y, 940771, Block.STONE);
        instance.setBlock(940738, 72, 940771, Block.STONE); // overhang toward the thrower
        for (int x = 940734; x <= 940746; x++)
            for (int z = 940765; z <= 940777; z++) instance.setBlock(x, 69, z, Block.STONE);

        Replay r = replay("/mmc-pillar3-corpus.json", "pillar3", new Pos(940742, 70, 940771));
        // one row crosses the face plane 0.05 from a tick boundary; table-vs-real trig flips its death tick
        assertTrue(r.falseRefusal() == 0 && r.ok() >= r.n() - 1, r.summary());
    }
}
