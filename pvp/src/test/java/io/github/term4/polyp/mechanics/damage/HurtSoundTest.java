package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.mechanics.damage.types.generic.GenericDamage;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Both eras play the hurt sound client-side off the hurt animation (1.8 {@code handleStatusUpdate(2)}, modern
 * {@code handleDamageEvent}), so the server must not send one of its own - Minestom does, and it doubles.
 */
class HurtSoundTest extends HeadlessServerTest {

    @Test
    void hurtSendsTheAnimationWithoutASoundPacket() {
        FakePlayer victim = FakePlayer.connect(instance, new Pos(40.5, 66, 40.5), "HurtSound");
        try {
            victim.sent.clear();
            services.damage().apply(DamageSnapshot.of(victim.player, GenericDamage.INSTANCE).withAmount(4f));

            assertEquals(1, victim.sent(DamageEventPacket.class).size(), "one hurt animation");
            assertTrue(victim.sent(SoundEffectPacket.class).isEmpty(),
                    "the client makes the hurt sound from the animation: " + victim.sent(SoundEffectPacket.class));
            assertTrue(victim.player.getHealth() < 20f, "the hit still landed");
        } finally {
            victim.player.remove();
        }
    }
}
