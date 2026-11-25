package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.FishingBobberEntity;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import io.github.term4.polyp.mechanics.projectile.shootables.Shootable;
import io.github.term4.polyp.world.WorldPolicy;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MineMen pseudo-hook (the old lib's bobberFix LEGACY): a player hit lands once and only FLASHES the hook for
 * {@link #DISPLAY_TICKS}, re-flashing on every victim position move ({@link Installer}) until retract; after the
 * flash the bobber pops away from the shooter. Attach per launch - the instance carries the flash/fall state.
 */
public final class PseudoHook implements ProjectileBehavior {

    /** On the victim: the flashes their position moves re-arm. */
    public static final Tag<Set<PseudoHook>> HOOKED_BY = Tag.Transient("mmc18:pseudo-hooked-by");

    private static final int DISPLAY_TICKS = 1;
    // old-lib values, fed to setVelocity as blocks/SECOND: a 0.04 b/t slide away, NOT a b/t impulse
    private static final int FALL_DELAY_TICKS = 3;
    private static final double FALL_AWAY_BS = 0.8;
    private static final double FALL_DOWN_BS = 0.4;

    private @Nullable FishingBobberEntity bobber;
    private @Nullable Player victim;
    private int pinTicksLeft;
    private boolean fallArmed;
    private long fallAtAlive;
    private boolean fallApplied;

    @Override
    public void onSpawn(ManagedProjectile projectile) {
        this.bobber = (FishingBobberEntity) projectile;
    }

    @Override
    public boolean canHit(ManagedProjectile projectile, Entity target) {
        // the pseudo hit lands once; after it the rod passes through players until retract (old-lib LEGACY)
        return victim == null || !(target instanceof Player);
    }

    @Override
    public void onImpact(ManagedProjectile projectile, @Nullable Entity hit) {
        if (victim != null || !(hit instanceof Player player)) return; // non-players keep the vanilla hook
        victim = player;
        Set<PseudoHook> set = player.getTag(HOOKED_BY);
        if (set == null) set = ConcurrentHashMap.newKeySet(); // victim-thread rehook can overlap a bobber-thread detach
        set.add(this);
        player.setTag(HOOKED_BY, set);
        pinTicksLeft = DISPLAY_TICKS;
    }

    @Override
    public void onTick(ManagedProjectile projectile, long time) {
        if (victim == null || bobber == null) return;
        // victim left the shard mid-rod: release, or the hook drives an entity another domain owns
        if (victim.isRemoved() || !WorldPolicy.canAffect(bobber, victim)) {
            if (bobber.getHookedEntity() == victim) bobber.setHookedEntity(null);
            detach();
            return;
        }
        if (bobber.getHookedEntity() == victim) {
            if (pinTicksLeft-- <= 0) bobber.setHookedEntity(null); // flash over: the bobber falls from the victim
            return;
        }
        if (fallApplied) return;
        if (!fallArmed) {
            // released next to the victim and moving again (gravity resumed) -> pop away after the delay
            Pos eye = victim.getPosition().add(0, victim.getEyeHeight(), 0);
            if (bobber.getPosition().distanceSquared(eye) < 0.25 && bobber.velocityBt().lengthSquared() > 2.5e-5) {
                fallArmed = true;
                fallAtAlive = bobber.getAliveTicks() + FALL_DELAY_TICKS;
            }
        } else if (bobber.getAliveTicks() >= fallAtAlive) {
            fallApplied = true;
            applyFallVelocity();
        }
    }

    private void applyFallVelocity() {
        Entity shooter = bobber.getShooter();
        if (shooter == null) return;
        Pos pos = bobber.getPosition();
        Pos anglerPos = shooter.getPosition();
        double dx = pos.x() - anglerPos.x(), dz = pos.z() - anglerPos.z();
        double dist = Math.sqrt(dx * dx + dz * dz);
        Vec fall = dist > 0.01 ? new Vec(dx / dist * FALL_AWAY_BS, -FALL_DOWN_BS, dz / dist * FALL_AWAY_BS)
                : new Vec(0, -FALL_DOWN_BS, 0);
        bobber.setVelocity(fall); // b/s; broadcasts the redirect
    }

    /** Re-flashes the hook (the victim's position move); the entity's pin broadcast draws the 1.8 line. */
    private void rehook() {
        if (bobber == null || bobber.isRemoved() || victim == null || victim.isRemoved()) return;
        if (!WorldPolicy.canAffect(bobber, victim)) return; // left the shard: the bobber's own tick detaches
        if (bobber.getHookedEntity() != victim) bobber.setHookedEntity(victim); // still pinned: just extend the flash
        pinTicksLeft = DISPLAY_TICKS;
    }

    @Override
    public void onRemove(ManagedProjectile projectile) {
        detach();
    }

    private void detach() {
        Player player = victim;
        victim = null;
        if (player == null) return;
        Set<PseudoHook> set = player.getTag(HOOKED_BY);
        if (set == null) return;
        set.remove(this);
        if (set.isEmpty()) player.removeTag(HOOKED_BY);
        else player.setTag(HOOKED_BY, set);
    }

    /** The re-flash-on-move listener; goes in the preset's {@code shootables} so the config installs it. */
    public static final class Installer implements Shootable {
        @Override
        public void install(@NotNull EventNode<@NotNull Event> node, @NotNull ProjectileSystem system) {
            node.addListener(PlayerMoveEvent.class, e -> {
                Set<PseudoHook> hooks = e.getPlayer().getTag(HOOKED_BY);
                if (hooks == null || hooks.isEmpty()) return;
                if (e.getPlayer().getPosition().samePoint(e.getNewPosition())) return; // look-only must not re-flash
                for (PseudoHook hook : Set.copyOf(hooks)) hook.rehook();
            });
        }
    }
}
