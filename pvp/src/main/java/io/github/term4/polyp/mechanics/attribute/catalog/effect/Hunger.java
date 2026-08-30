package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.attribute.source.Behavior;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import io.github.term4.polyp.mechanics.hunger.HungerSystem;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

/**
 * Hunger - exhaustion every tick while active, {@code amplifier+1} per tick; the hunger preset prices it (1.8 x0.025,
 * modern x0.005). Vanilla's tick gate is unconditionally true for hunger in both versions.
 */
public final class Hunger {

    public static final Key KEY = Key.key("minecraft:hunger");

    private Hunger() {}

    private static final Behavior EXHAUST = new Behavior() {
        @Override public int tickInterval(int level) { return 1; }

        @Override public void onTick(Services services, Entity entity, int level) {
            HungerSystem hunger = services.hunger();
            if (hunger != null && entity instanceof Player p)
                hunger.chargePriced(p, HungerSystem.HUNGER_EFFECT_COST, level);
        }
    };

    public static final Source INSTANCE = new EntitySource(KEY) {
        @Override public Behavior behavior() { return EXHAUST; }
    };
}
