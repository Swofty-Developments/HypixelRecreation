package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.mechanics.attribute.source.Behavior;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;

/**
 * Regeneration - heals {@code +1} every {@code max(1, 50 >> amplifier)} ticks, so amplifier >= 6 heals every tick, not
 * never (vanilla's {@code interval <= 0 ? true} branch). Identical in 1.8 and 26.
 */
public final class Regeneration {

    public static final Key KEY = Key.key("minecraft:regeneration");

    private Regeneration() {}

    private static final Behavior HEAL = new Behavior() {
        @Override public int tickInterval(int level) { return Math.max(1, 50 >> (level - 1)); }

        @Override public void onTick(Entity entity, int level) {
            if (!(entity instanceof LivingEntity living)) return;
            float max = (float) living.getAttributeValue(Attribute.MAX_HEALTH);
            if (living.getHealth() < max) living.setHealth(Math.min(living.getHealth() + 1, max));
        }
    };

    public static final Source INSTANCE = new EntitySource(KEY) {
        @Override public Behavior behavior() { return HEAL; }
    };
}
