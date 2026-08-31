package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Regeneration heals {@code +1} every {@code 50 >> amplifier} ticks (identical in 1.8 + 26). */
class RegenerationTest extends HeadlessServerTest {

    @Test
    void intervalIs50ShiftedByAmplifier() {
        assertEquals(50, Regeneration.INSTANCE.behavior().tickInterval(1)); // amp 0
        assertEquals(25, Regeneration.INSTANCE.behavior().tickInterval(2)); // amp 1
        assertEquals(12, Regeneration.INSTANCE.behavior().tickInterval(3)); // amp 2 (50>>2)
        assertEquals(1, Regeneration.INSTANCE.behavior().tickInterval(6));  // amp 5 (50>>5)
    }

    @Test
    void onTickHealsOneCappedAtMax() {
        LivingEntity e = zombie(new Pos(0, 64, 70));
        e.setHealth(5f);
        Regeneration.INSTANCE.behavior().onTick(e, 1);
        assertEquals(6f, e.getHealth(), 1e-4);

        float max = (float) e.getAttributeValue(net.minestom.server.entity.attribute.Attribute.MAX_HEALTH);
        e.setHealth(max);
        Regeneration.INSTANCE.behavior().onTick(e, 1);
        assertEquals(max, e.getHealth(), 1e-4);
    }

    @Test
    void periodicDispatchHealsThroughTheTick() {
        LivingEntity e = zombie(new Pos(0, 64, 71));
        e.setHealth(5f);
        PotionEffect regen = PotionEffect.fromKey(Regeneration.KEY);
        assertNotNull(regen, "regeneration effect");
        e.addEffect(new Potion(regen, (byte) 5, 200)); // amplifier 5 -> interval 50>>5 = 1 (heals each tick)
        try {
            e.tick(0L); // EntityTickEvent -> AttributeSystem.tickEffects
            assertEquals(6f, e.getHealth(), 1e-4);
        } finally {
            e.removeEffect(regen);
        }
    }
}
