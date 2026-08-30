package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Vanilla splits a hit's packets: animation to viewers + victim, sound to viewers only (the victim's client
 * predicts its own; a remote entity's hurt sound is a client no-op in both eras). Minestom sends the sound to
 * self too - doubling the victim - and dropping it instead left attackers hearing nothing.
 */
class HurtSoundRoutingTest extends HeadlessServerTest {

    private static long count(FakePlayer p, Class<?> type) {
        return p.sent.stream()
                .map(sp -> SendablePacket.extractServerPacket(ConnectionState.PLAY, sp))
                .filter(type::isInstance)
                .count();
    }

    @Test
    void theSoundGoesToTheAttackerAndTheAnimationToBoth() {
        FakePlayer victim = FakePlayer.connect(instance, new Pos(500.5, 65, 500.5), "HurtVictim");
        FakePlayer attacker = FakePlayer.connect(instance, new Pos(501.5, 65, 500.5), "HurtAttacker");
        try {
            victim.player.addViewer(attacker.player);
            victim.sent.clear();
            attacker.sent.clear();

            victim.player.damage(new Damage(DamageType.PLAYER_ATTACK, attacker.player, attacker.player,
                    attacker.player.getPosition(), 3.0f));

            assertEquals(1, count(attacker, SoundEffectPacket.class),
                    "the attacker only hears the hit if the server sends them the sound");
            assertEquals(0, count(victim, SoundEffectPacket.class),
                    "the victim predicts their own hurt sound; a packet too would double it");
            assertEquals(1, count(victim, DamageEventPacket.class),
                    "the victim still needs the animation - it is what drives their prediction");
            assertEquals(1, count(attacker, DamageEventPacket.class),
                    "and viewers still get the animation");
        } finally {
            attacker.player.remove();
            victim.player.remove();
        }
    }
}
