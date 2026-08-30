package io.github.term4.polyp.platform.fixes;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The base pipeline notifies neighbors in vanilla's order once the fix reorders Minestom's face array. */
class BlockUpdateOrderFixTest extends HeadlessServerTest {

    private static final List<Point> updated = new ArrayList<>();

    @BeforeAll
    static void install() {
        BlockUpdateOrderFix.install();
        MinecraftServer.getBlockManager().registerBlockPlacementRule(new BlockPlacementRule(Block.BAMBOO) {
            @Override
            public @Nullable Block blockPlace(PlacementState placementState) {
                return placementState.block();
            }

            @Override
            public Block blockUpdate(UpdateState updateState) {
                updated.add(updateState.blockPosition());
                return updateState.currentBlock();
            }
        });
    }

    @Test
    void neighborsUpdateInVanillaOrder() {
        var center = new Vec(8.0, 70, 40);
        for (var n : new Vec[]{new Vec(7, 70, 40), new Vec(9, 70, 40), new Vec(8, 69, 40),
                new Vec(8, 71, 40), new Vec(8, 70, 39), new Vec(8, 70, 41)}) {
            instance.setBlock(n, Block.BAMBOO);
        }
        updated.clear();

        instance.setBlock(center, Block.STONE);

        // NeighborUpdater.UPDATE_ORDER
        assertEquals(List.of(new Vec(7, 70, 40), new Vec(9, 70, 40), new Vec(8, 69, 40),
                new Vec(8, 71, 40), new Vec(8, 70, 39), new Vec(8, 70, 41)), updated);
    }
}
