package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.platform.compatibility.CompatConfig.SwimSuppression;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import net.minestom.server.network.packet.server.play.UpdateHealthPacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Water entry/exit drives the configured sprint gate; the outgoing clamp holds FOOD against server re-sends. */
class CompatSwimTest extends HeadlessServerTest {

    private static final Pos WATER = new Pos(0.5, 64, 832.5);
    private static final Pos DRY = new Pos(8.5, 64, 832.5);

    private static final CompatConfig FOOD = CompatConfig.builder().suppressSwim(SwimSuppression.FOOD).build();
    private static final CompatConfig BLINDNESS = CompatConfig.builder().suppressSwim(SwimSuppression.BLINDNESS).build();

    @Test
    void lifecycle() {
        instance.loadChunk(0, 52).join();
        instance.setBlock(0, 64, 832, Block.WATER);
        FakePlayer fp = FakePlayer.connect(instance, WATER, "SwimSuppress");
        OptimizedPlayer op = (OptimizedPlayer) fp.player;
        try {
            op.compat().apply(FOOD);

            // entry: the fresh re-send goes out clamped
            fp.sent.clear();
            CompatSwim.tick(op);
            assertEquals(SwimSuppression.FOOD, op.compat().activeSwimFix());
            assertEquals(6, lastFood(fp), "activation re-send clamped");

            // any server food update mid-water stays clamped (eat/regen/exhaustion all pass through here)
            fp.sent.clear();
            op.sendPacket(new UpdateHealthPacket(20f, 20, 5f));
            assertEquals(6, lastFood(fp), "outgoing clamp holds the gate shut");

            // a swim pose that slipped in before the gate landed is forced back
            op.setPose(EntityPose.SWIMMING);
            assertEquals(EntityPose.STANDING, op.getPose(), "engaged fix intercepts the pose directly");

            // exit: the real bar comes back
            fp.player.teleport(DRY).join();
            fp.sent.clear();
            CompatSwim.tick(op);
            assertNull(op.compat().activeSwimFix());
            assertEquals(20, lastFood(fp), "restore on leaving water");

            // FOOD -> BLINDNESS mid-water: food restored, hidden effect starts
            fp.player.teleport(WATER).join();
            op.compat().apply(FOOD);
            CompatSwim.tick(op);
            op.compat().apply(BLINDNESS);
            fp.sent.clear();
            CompatSwim.tick(op);
            assertEquals(20, lastFood(fp), "mode switch restores the food bar");
            assertEquals(1, fp.sent(EntityEffectPacket.class).size());

            // refreshed every tick, saturated duration
            fp.sent.clear();
            CompatSwim.tick(op);
            EntityEffectPacket effect = fp.sent(EntityEffectPacket.class).getFirst();
            assertEquals(PotionEffect.BLINDNESS, effect.potion().effect());
            assertTrue(effect.potion().duration() >= 20, "saturated: the fog factor must not enter the fade region");

            // configured duration takes over on the next refresh, no transition needed
            op.compat().apply(BLINDNESS.toBuilder().swimBlindnessTicks(40).build());
            fp.sent.clear();
            CompatSwim.tick(op);
            assertEquals(40, fp.sent(EntityEffectPacket.class).getFirst().potion().duration());

            // exit removes the hidden effect
            fp.player.teleport(DRY).join();
            fp.sent.clear();
            CompatSwim.tick(op);
            assertEquals(1, fp.sent(RemoveEntityEffectPacket.class).size());

            // a real blindness effect owns the wire: no hidden refresh over it, no remove on exit
            fp.player.teleport(WATER).join();
            op.addEffect(new Potion(PotionEffect.BLINDNESS, 0, 200));
            fp.sent.clear();
            CompatSwim.tick(op);
            assertEquals(0, fp.sent(EntityEffectPacket.class).size(), "the real effect already gates sprint");
            fp.player.teleport(DRY).join();
            CompatSwim.tick(op);
            assertEquals(0, fp.sent(RemoveEntityEffectPacket.class).size(), "must not strip the real effect");
            op.removeEffect(PotionEffect.BLINDNESS);
        } finally {
            fp.player.remove();
        }
    }

    @Test
    void exemptions() {
        instance.loadChunk(0, 53).join();
        instance.setBlock(0, 64, 848, Block.WATER);
        FakePlayer fp = FakePlayer.connect(instance, new Pos(0.5, 64, 848.5), "SwimExempt");
        OptimizedPlayer op = (OptimizedPlayer) fp.player;
        try {
            op.compat().apply(FOOD);

            op.compat().setLegacyClient(true);
            CompatSwim.tick(op);
            assertNull(op.compat().activeSwimFix(), "1.8 clients can't swim-pose");
            op.compat().setLegacyClient(false);

            op.compat().setNativeFeatures(Set.of(AnimatiumFeature.DISABLE_SWIM_POSE));
            CompatSwim.tick(op);
            assertNull(op.compat().activeSwimFix(), "Animatium disables natively");
            op.compat().setNativeFeatures(Set.of());

            op.setGameMode(GameMode.CREATIVE);
            CompatSwim.tick(op);
            assertNull(op.compat().activeSwimFix(), "mayfly bypasses the food gate anyway");
            op.setGameMode(GameMode.SURVIVAL);

            // pose entered before activation (same-tick water entry) is reset by the tick pass
            op.setPose(EntityPose.SWIMMING);
            assertEquals(EntityPose.SWIMMING, op.getPose());
            CompatSwim.tick(op);
            assertEquals(SwimSuppression.FOOD, op.compat().activeSwimFix());
            assertEquals(EntityPose.STANDING, op.getPose());
        } finally {
            fp.player.remove();
        }
    }

    private static int lastFood(FakePlayer fp) {
        var updates = fp.sent(UpdateHealthPacket.class);
        assertTrue(!updates.isEmpty(), "expected an UpdateHealthPacket");
        return updates.getLast().food();
    }
}
