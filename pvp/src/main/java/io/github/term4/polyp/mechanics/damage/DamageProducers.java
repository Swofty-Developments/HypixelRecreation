package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;

/** Shared guards for self-driven environmental damage producers. */
public final class DamageProducers {

    private DamageProducers() {}

    /**
     * The dead, creative, spectator, and flying take no environmental damage (vanilla). Dead matters for fall: a player
     * who dies mid-fall keeps sending move packets on the death screen, and a quick respawn lands it as phantom damage.
     *
     * <p>A view-only observer is NOT exempt - producers read the block view, so an observer standing in base lava reads
     * that lava and collides with exactly what they read.
     */
    public static boolean exempt(LivingEntity living) {
        if (living.isDead()) return true;
        if (!(living instanceof Player p)) return false;
        GameMode gm = p.getGameMode();
        return p.isFlying() || gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR;
    }

    /** Emits a per-tick contact hit for {@code type}, dropped when the scope disabled it or the invul window absorbs it. */
    public static void emit(DamageSystem sys, LivingEntity living, DamageType type) {
        DamageSnapshot snap = DamageSnapshot.of(living, type);
        DamageContext ctx = sys.contextFor(snap);
        if (!ctx.typeConfig().enabled(ctx)) return;
        if (DamageSystem.absorbedByWindow(living, ctx.baseAmount())) return;
        sys.apply(snap);
    }
}
