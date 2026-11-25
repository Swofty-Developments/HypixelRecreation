package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import io.github.term4.polyp.mechanics.attribute.source.ItemSource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import io.github.term4.polyp.mechanics.attribute.combat.HitContext;
import io.github.term4.polyp.mechanics.attribute.combat.OnHit;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.burning.Ignite;
import net.kyori.adventure.key.Key;

import java.util.List;

/**
 * Fire Aspect - {@code level × 4} seconds of fire on a melee hit ({@code EntityHuman.attack}, identical 1.8/26). An
 * {@link ItemSource} with no modifiers, pure {@link OnHit}; the burn itself is the existing {@code on_fire} damage type.
 */
public final class FireAspect {

    public static final Key KEY = Key.key("minecraft:fire_aspect");
    private static final int TICKS_PER_LEVEL = 4 * 20;

    public static final Source INSTANCE = new FireAspectSource();

    private FireAspect() {}

    private static final class FireAspectSource extends ItemSource implements OnHit {
        private FireAspectSource() { super(KEY); }

        @Override public List<Mod> modifiers(int level) { return List.of(); }

        @Override public void onHit(HitContext ctx) {
            if (ctx.level() <= 0) return;
            Ignite.ignite(ctx.victim(), ctx.level() * TICKS_PER_LEVEL, DamageSystem.KEY);
        }
    }
}
