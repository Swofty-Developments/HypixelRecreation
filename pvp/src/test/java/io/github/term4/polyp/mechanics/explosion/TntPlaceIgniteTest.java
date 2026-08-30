package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.presets.vanilla18.Explosion;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Place-ignition is the scope's choice: a profile with {@code igniteOnPlace} converts the placement, the default leaves a block. */
class TntPlaceIgniteTest extends HeadlessServerTest {

    @BeforeAll
    static void mechanics() {
        ExplosionSystem.install(polyp, Explosion.config());
    }

    private static PlayerBlockPlaceEvent place(FakePlayer p, BlockVec pos) {
        var event = new PlayerBlockPlaceEvent(p.player, instance, Block.TNT, BlockFace.TOP, pos,
                new Vec(0.5, 1, 0.5), PlayerHand.MAIN);
        EventDispatcher.call(event);
        return event;
    }

    @Test
    void anOptedInScopePrimesInsteadOfPlacing() {
        FakePlayer placer = FakePlayer.connect(instance, new Pos(60.5, 65, 20.5), "TntPlacer");
        polyp.profiles().setPlayer(placer.player, MechanicsProfile.builder()
                .set(MechanicsKeys.TNT, io.github.term4.polyp.presets.hypixel.Tnt.config()).build());
        try {
            var event = place(placer, new BlockVec(62, 65, 20));
            assertTrue(event.isCancelled(), "the block never exists");
            assertTrue(instance.getEntities().stream().anyMatch(e -> e instanceof PrimedTnt), "primed instead");
        } finally {
            polyp.profiles().setPlayer(placer.player, null);
        }
    }

    @Test
    void theDefaultScopePlacesABlock() {
        FakePlayer placer = FakePlayer.connect(instance, new Pos(70.5, 65, 20.5), "TntBuilder");
        var event = place(placer, new BlockVec(72, 65, 20));
        assertFalse(event.isCancelled(), "no opt-in: TNT is just a block");
    }
}
