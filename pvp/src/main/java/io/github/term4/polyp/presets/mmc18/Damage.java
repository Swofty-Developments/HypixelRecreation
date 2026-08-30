package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.api.event.damage.DamageEvent;
import io.github.term4.polyp.mechanics.damage.DamageConfig;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * mmc18 damage: the vanilla 1.8 damage ({@link io.github.term4.polyp.presets.vanilla18.Damage}) with
 * silent overdamage, no hurt-tick knockback, and the same-weapon overdamage block below.
 */
public final class Damage {

    private Damage() {}

    public static DamageConfig config() {
        return DamageConfig.builder(Vanilla18.damage())
                .overdamageSilent(true)
                // no KB on generic damage ticks; killing the broadcast is the only way off an inherited
                // hurtKnockback (merge semantics can't null it)
                .syncHurtVelocity(false)
                .addCustomComponent(Damage::blockSameItemOverdamage)
                .build();
    }

    // no overdamage replacement when the incoming melee hit uses the same material as the hit that opened the window
    @Nullable
    private static Float blockSameItemOverdamage(DamageContext ctx, DamageEvent event, float amount, boolean overdamage) {
        if (!overdamage || amount <= 0) return null;
        if (!MeleeDamage.KEY.equals(event.type().key())) return null;
        if (!(event.target() instanceof LivingEntity le)) return null;
        if (sameItem(event.item(), DamageSystem.openingHitItem(le))) return 0f;
        return null;
    }

    private static boolean sameItem(@Nullable ItemStack a, @Nullable ItemStack b) {
        boolean aFist = a == null || a.isAir();
        boolean bFist = b == null || b.isAir();
        if (aFist && bFist) return true;
        if (aFist != bFist) return false;
        return a.material().equals(b.material());
    }
}
