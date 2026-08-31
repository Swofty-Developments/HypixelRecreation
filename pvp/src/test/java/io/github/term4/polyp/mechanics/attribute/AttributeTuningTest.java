package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Strength;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Per-source tunings resolved off the scoped {@link AttributeConfig}. */
class AttributeTuningTest extends HeadlessServerTest {

    @AfterEach
    void clearScope() { polyp.profiles().setGlobal(null); }

    private float meleeWithStrength(AttributeConfig scoped) {
        polyp.profiles().setGlobal(MechanicsProfile.builder().set(MechanicsKeys.ATTRIBUTES, scoped).build());
        LivingEntity atk = zombie(new Pos(0, 64, 90));
        PotionEffect strength = PotionEffect.fromKey(Strength.KEY);
        assertNotNull(strength);
        atk.addEffect(new Potion(strength, 0, 600)); // Strength I (LEGACY ×(1+1.3))
        try {
            return MeleeDamage.INSTANCE.snapshot(atk, target(), false, ItemStack.of(Material.DIAMOND_SWORD), services).amount();
        } finally {
            atk.clearEffects();
        }
    }

    private static LivingEntity target;
    private static LivingEntity target() {
        if (target == null) target = zombie(new Pos(0, 64, 91));
        return target;
    }

    @Test
    void scaleMultipliesTheSourceAmount() {
        // scaled ×2 -> amount 2.6 -> 7×(1+2.6)
        assertEquals(25.2f, meleeWithStrength(AttributeConfig.builder().scale(Strength.KEY, 2.0).build()), 1e-2f);
    }

    @Test
    void disableTurnsTheSourceOff() {
        assertEquals(7.0f, meleeWithStrength(AttributeConfig.builder().disable(Strength.KEY).build()), 1e-3f);
    }
}
