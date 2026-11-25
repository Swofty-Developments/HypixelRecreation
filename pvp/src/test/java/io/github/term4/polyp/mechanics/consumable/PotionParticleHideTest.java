package io.github.term4.polyp.mechanics.consumable;

import io.github.term4.polyp.mechanics.consumable.ConsumableTypeConfig.ParticleVisibility;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link ParticleVisibility} flag bytes, and that HIDDEN reaches viewers as a no-swirl effect. */
class PotionParticleHideTest extends HeadlessServerTest {

    @Test
    void visibilityFlagsEncodeTheSwirlButKeepTheIcon() {
        assertTrue(potion(ParticleVisibility.SHOWN).hasParticles(), "SHOWN swirls");
        assertFalse(potion(ParticleVisibility.HIDDEN).hasParticles(), "HIDDEN hides the swirl");
        assertFalse(potion(ParticleVisibility.CUSTOM).hasParticles(), "CUSTOM suppresses the vanilla swirl (the seam for a custom effect)");
        for (ParticleVisibility v : ParticleVisibility.values())
            assertTrue(potion(v).hasIcon(), v + " keeps the HUD icon");
    }

    @Test
    void hiddenEffectReachesViewersWithoutTheSwirl() {
        FakePlayer target = FakePlayer.connect(instance, new Pos(5.5, 65, 5.5), "Sipper");
        FakePlayer viewer = FakePlayer.connect(instance, new Pos(7.5, 65, 5.5), "PViewer");
        try {
            assertTrue(target.player.getViewers().contains(viewer.player), "a nearby player tracks the target");
            target.player.addEffect(new Potion(PotionEffect.SPEED, 0, 200, ParticleVisibility.HIDDEN.potionFlags()));
            EntityEffectPacket eff = viewer.sent(EntityEffectPacket.class).stream()
                    .filter(p -> p.entityId() == target.player.getEntityId()).findFirst().orElse(null);
            assertNotNull(eff, "the viewer is told about the effect");
            assertFalse(eff.potion().hasParticles(), "HIDDEN suppresses the swirl for viewers");
            assertTrue(eff.potion().hasIcon(), "the icon still shows");
        } finally {
            target.player.remove();
            viewer.player.remove();
        }
    }

    private static Potion potion(ParticleVisibility v) {
        return new Potion(PotionEffect.SPEED, 0, 1, v.potionFlags());
    }
}
