package io.github.term4.polyp.mechanics.damage.types.burning;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.attribute.defense.ProtectionCategory;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import net.kyori.adventure.key.Key;

import java.util.Set;

/**
 * On-fire (burn tick) damage ({@code minecraft:on_fire}). Vanilla 1.8: 1.0 damage every 20 fire ticks while burning.
 * Rides Minestom's per-entity fire ticks, so anything that ignites an entity feeds it. Self-driven via the shared
 * {@link BurningTicker}; tunables come from {@link BurningConfig}.
 */
public final class BurningDamage extends DamageType {

    public static final Key KEY = Key.key("minecraft:on_fire");
    public static final BurningDamage INSTANCE = new BurningDamage();

    private BurningDamage() {
        super(KEY, "Burning", VanillaTypes.ON_FIRE, BurningConfig.builder().key(KEY).build());
    }

    @Override public Set<ProtectionCategory> protectionCategories() { return Set.of(ProtectionCategory.FIRE); }

    @Override
    public void enable(DamageSystem system, Polyp polyp) {
        BurningTicker.activate(KEY, system);
    }

    @Override
    public void disable() {
        BurningTicker.deactivate(KEY);
    }
}
