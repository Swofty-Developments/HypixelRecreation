package test.presets;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * mmcnotstillweird: 32 throws at a diagonal wool staircase fronted by iron bars. Pinned the walk-back law -
 * every mmc tp snapped the hit axis to face minus 0.4 (pane planes included), none clipped the bars.
 */
class StaircasePearlReplayTest extends PearlReplayTest {

    @Test
    void staircaseReplayMatchesMinemen() throws Exception {
        // Minestom doesn't auto-connect pane states; each bar column joins the wool east + south
        Block bars = Block.IRON_BARS.withProperty("east", "true").withProperty("south", "true");
        int[][] wool = {{938975, 938954}, {938976, 938953}, {938977, 938952}, {938978, 938951}};
        int[][] pane = {{938975, 938953}, {938976, 938952}, {938977, 938951}};
        for (int y = 70; y <= 72; y++) {
            for (int[] c : wool) instance.setBlock(c[0], y, c[1], Block.RED_WOOL);
            for (int[] c : pane) instance.setBlock(c[0], y, c[1], bars);
        }
        for (int x = 938965; x <= 938990; x++)
            for (int z = 938940; z <= 938965; z++) instance.setBlock(x, 69, z, Block.STONE);

        Replay r = replay("/mmc-staircase-corpus.json", "staircase", new Pos(938975, 70, 938948));
        // rows threading the razor gaps between panes pick a different contact face under table-vs-real trig
        assertTrue(r.falseRefusal() == 0 && r.ok() >= r.n() - 3, r.summary());
    }
}
