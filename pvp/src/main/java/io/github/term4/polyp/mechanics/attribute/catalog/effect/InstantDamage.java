package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.mechanics.attribute.source.Behavior;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;

/**
 * Instant Damage - {@code 6 << amplifier} magic damage on apply (vanilla {@code HealOrHarmMobEffect}, identical 1.8/26).
 * Minestom-native MAGIC damage, not a routed pipeline hit. Splash proximity-scaling / undead inversion live on the
 * potion-throw path.
 */
public final class InstantDamage {

    public static final Key KEY = Key.key("minecraft:instant_damage");

    private InstantDamage() {}

    private static final Behavior HARM = new Behavior() {
        @Override public void onApply(Entity entity, int level) {
            if (entity instanceof LivingEntity living) living.damage(DamageType.MAGIC, 6 << (level - 1));
        }
    };

    public static final Source INSTANCE = new EntitySource(KEY) {
        @Override public Behavior behavior() { return HARM; }
    };
}
