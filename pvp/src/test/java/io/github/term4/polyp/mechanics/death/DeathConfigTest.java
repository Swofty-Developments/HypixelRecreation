package io.github.term4.polyp.mechanics.death;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Speed;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DeathConfigTest extends HeadlessServerTest {

    @Test
    void fromBaseFillsUnsetKnobsFromBase() {
        DeathConfig.DeathContext ctx = new DeathConfig.DeathContext(looseZombie());
        DeathConfig base = DeathConfig.builder()
                .clearEffects(true).resetMechanicsState(true).hideCorpse(true).deathAnimationTicks(20).build();
        DeathConfig merged = DeathConfig.builder().hideCorpse(false).build().fromBase(base);
        assertEquals(Boolean.TRUE, merged.clearEffects(ctx));
        assertEquals(Boolean.TRUE, merged.resetMechanicsState(ctx));
        assertEquals(Boolean.FALSE, merged.hideCorpse(ctx));
        assertEquals(Integer.valueOf(20), merged.deathAnimationTicks(ctx));
    }

    @Test
    void emptyBuilderLeavesEveryKnobUnset() {
        DeathConfig.DeathContext ctx = new DeathConfig.DeathContext(looseZombie());
        DeathConfig empty = DeathConfig.builder().build();
        assertNull(empty.clearEffects(ctx));
        assertNull(empty.resetMechanicsState(ctx));
        assertNull(empty.hideCorpse(ctx));
        assertNull(empty.deathAnimationTicks(ctx));
    }

    @Test
    void knobsResolvePerVictim() {
        DeathConfig cfg = DeathConfig.builder()
                .deathAnimationTicks(ctx -> ctx.victim() instanceof net.minestom.server.entity.Player ? 20 : 0)
                .build();
        assertEquals(Integer.valueOf(0), cfg.deathAnimationTicks(new DeathConfig.DeathContext(looseZombie())));
    }

    @Test
    void deathHonorsClearEffectsDisabled() {
        MechanicsProfile prev = polyp.profiles().global();
        polyp.profiles().setGlobal(MechanicsProfile.builder()
                .set(MechanicsKeys.DEATH, DeathConfig.builder().clearEffects(false).build()).build());
        try {
            LivingEntity e = zombie(new Pos(0, 64, 68));
            PotionEffect speed = PotionEffect.fromKey(Speed.KEY);
            assertNotNull(speed, "speed effect");
            e.addEffect(new Potion(speed, 0, 600));
            e.kill();
            assertFalse(e.getActiveEffects().isEmpty(), "clearEffects=false keeps effects on death");
        } finally {
            polyp.profiles().setGlobal(prev);
        }
    }
}
