package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.api.event.projectile.ProjectileHitEvent;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver.ProjectileContext;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver.ResolvedHit;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.util.Directions;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generic config-driven projectile: hit knobs resolve at impact against a {@link ProjectileContext}; entity/block hits
 * apply the resolved damage + knockback, fire the cancellable {@link ProjectileHitEvent}, and remove per the
 * {@code removeOn*} knobs. Egg/pearl override {@link #onImpact}.
 */
public class ManagedProjectile extends ProjectileEntity {

    /** Merged per-type config, FieldValues unresolved. */
    private final ProjectileTypeConfig effectiveConfig;
    private final ProjectileSnapshot snap;
    private ProjectileBehavior behavior = ProjectileBehavior.NONE;
    /** Latches the one-time {@link ProjectileBehavior#onSpawn}. */
    private boolean spawned;

    public ManagedProjectile(@Nullable Entity shooter, @NotNull EntityType entityType,
                             ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        super(shooter, entityType);
        this.snap = snap;
        this.effectiveConfig = effectiveConfig;
    }

    public void setBehavior(@Nullable ProjectileBehavior behavior) { this.behavior = behavior != null ? behavior : ProjectileBehavior.NONE; }

    /** Fireball/pearl read this: bare = the built-in impact effect, carrying a behavior = that behavior owns it. */
    protected boolean hasBehavior() { return behavior != ProjectileBehavior.NONE; }

    /** {@code target} is the struck entity, or {@code null} for a block hit. */
    private ResolvedHit resolveHit(@Nullable Entity target) {
        ProjectileContext ctx = ProjectileContext.of(snap, services())
                .atHit(target, getShooterOriginPos(), getPosition());
        return ProjectileConfigResolver.resolveHit(effectiveConfig, ctx);
    }

    @Override
    protected boolean onHit(@NotNull Entity target) {
        ResolvedHit hit = resolveHit(target);
        ProjectileHitEvent ev = new ProjectileHitEvent(this, snap, shooter, target, getPosition(),
                getShooterOriginPos(), hit, hitDamage(hit, target));
        EventDispatcher.call(ev);
        if (ev.isCancelled()) return false;

        // pre-damage outcome: a forced response wins, else the self-hit / entity-hit knob
        ProjectileTypeConfig.HitResponse pre = ev.response() != null ? ev.response()
                : (target == shooter ? hit.selfHit() : hit.entityHit());
        switch (pre) {
            case HIT -> { /* normal hit below */ }
            case PASS_THROUGH -> { passThrough(target); return false; }
            case DEFLECT -> { bounce(hit, target); return false; }
            case DESTROY -> { fireImpact(target); return true; }
        }

        beforeEntityDamage(target);
        DamageSystem.DamageOutcome result = applyDamageAndKnockback(target, ev);
        // didn't land: the InvulnResponse picks by why - IMMUNE (creative/spectator) vs BLOCKED (invul window)
        if (!result.landed()) {
            ProjectileTypeConfig.InvulnResponse ir = hit.invulnHit();
            ProjectileTypeConfig.HitResponse blocked = result == DamageSystem.DamageOutcome.IMMUNE ? ir.immune() : ir.invulWindow();
            switch (blocked) {
                case PASS_THROUGH -> { passThrough(target); return false; }
                case DEFLECT -> { bounce(hit, target); return false; }
                case HIT, DESTROY -> { /* fall through to onImpact + removeOnHit */ }
            }
        }
        fireImpact(target);
        return ev.removeOnHit();
    }

    /**
     * Damage first (vanilla: even a 0-damage thrown hit calls damageEntity - the hurt flash + invul gate), then knockback
     * only if it landed. No damage type = the hit always lands.
     */
    private DamageSystem.DamageOutcome applyDamageAndKnockback(@NotNull Entity target, ProjectileHitEvent ev) {
        Services s = services();
        if (s == null) return DamageSystem.DamageOutcome.FRESH_DAMAGE;
        DamageSystem.DamageOutcome result = DamageSystem.DamageOutcome.FRESH_DAMAGE; // no damage type -> nothing absorbs the hit
        DamageType dt = ev.resolvedHit().damageType();
        if (dt != null && s.damage() != null) {
            result = s.damage().apply(DamageSnapshot.of(target, dt)
                    .withSource(shooter).withPoint(getPosition()).withAmount((float) ev.damage()));
        }
        if (result.landed() && ev.knockback() != null && s.knockback() != null) {
            s.knockback().apply(buildKnockback(target, ev.knockbackSource(), ev.knockback()));
        }
        return result;
    }

    private void bounce(ResolvedHit hit, @Nullable Entity hitEntity) {
        deflect(hit.deflect());
        if (deflectTrailEnabled()) deflectVisible = true;
        behavior.onDeflect(this, hitEntity);
    }

    /** The cosmetic {@code deflectParticles} crit trail, off by default. */
    private boolean deflectTrailEnabled() {
        Services s = services();
        if (s == null) return false;
        var fixes = s.fixes();
        return fixes != null && fixes.legacyArrowDeflectParticles(shooter);
    }

    private void passThrough(@Nullable Entity hitEntity) {
        if (deflectTrailEnabled()) deflectVisible = true;
        behavior.onDeflect(this, hitEntity);
    }

    @Override
    protected boolean canHit(@NotNull Entity entity) {
        return behavior.canHit(this, entity) && super.canHit(entity);
    }

    @Override
    protected boolean onStuck() {
        ResolvedHit hit = resolveHit(null);
        ProjectileHitEvent ev = new ProjectileHitEvent(this, snap, shooter, null, getPosition(),
                getShooterOriginPos(), hit, 0);
        EventDispatcher.call(ev);
        if (ev.isCancelled()) return false;
        behavior.onStuck(this);
        fireImpact(null);
        return ev.removeOnHit(); // block hit -> removeOnBlockHit (override ?? resolved)
    }

    /** Impact effect once a hit lands, after damage/knockback and before removal. {@code hitEntity} {@code null} = block hit. */
    /** Runs before the damage roll, so it fires even when the hit then i-frame-deflects (vanilla arrow flame order). */
    protected void beforeEntityDamage(@NotNull Entity target) {}

    protected void onImpact(@Nullable Entity hitEntity) {}

    private void fireImpact(@Nullable Entity hit) {
        onImpact(hit);
        behavior.onImpact(this, hit);
    }

    @Override
    protected void onUnstuck() {
        super.onUnstuck();
        behavior.onUnstuck(this);
    }

    @Override
    protected void updateProjectile(long time) {
        super.updateProjectile(time);
        if (!spawned) { spawned = true; behavior.onSpawn(this); }
        behavior.onTick(this, time);
    }

    @Override
    public void remove() {
        if (!isRemoved()) behavior.onRemove(this); // once, before Minestom tears the entity down
        super.remove();
    }

    /** Damage for an entity hit; arrows override with vanilla velocity-based damage ({@code ceil(speed * 2) + crit}). */
    protected float hitDamage(ResolvedHit hit, @NotNull Entity target) { return (float) hit.damage(); }

    /** Bounces off an entity it may not damage: apply the {@link ProjectileTypeConfig.Deflect}, then re-arm shooter
     *  immunity so the bounced arrow can't instantly re-hit the shooter. */
    protected void deflect(ProjectileTypeConfig.Deflect d) {
        setVelocityBt(applyDeflect(d, velocityBt));
        rearmShooterImmunity();
        setDeflected();
    }

    private static Vec applyDeflect(ProjectileTypeConfig.Deflect d, Vec velocity) {
        Vec v = velocity.mul(d.multiplier());
        double extra = d.turn() + (d.maxJitter() > d.minJitter()
                ? ThreadLocalRandom.current().nextDouble(d.minJitter(), d.maxJitter()) : d.minJitter());
        return extra == 0 ? v : Directions.rotateY(v, extra);
    }

    /** {@link #punchLevel()} rides as the extra-KB level (vanilla's {@code i}, the melee Knockback-enchant channel), {@code 0} = inert. */
    private KnockbackSnapshot buildKnockback(@NotNull Entity target, ProjectileTypeConfig.KnockbackSource source, KnockbackConfig kb) {
        // a SELF hit always rides the shooter-source form, whatever the mode: the projectile-relative frame is
        // degenerate (the projectile is on the victim), and a preset's self-hit rule needs source == target to detect
        if (shooter != null && (target == shooter || source == ProjectileTypeConfig.KnockbackSource.SHOOTER)) {
            return new KnockbackSnapshot(target, false, shooter, null, null, kb, punchLevel());
        }
        // projectile source: origin = projectile, direction = horizontal flight
        Vec h = new Vec(velocityBt.x(), 0, velocityBt.z());
        Vec flightDir = h.lengthSquared() < 1e-9 ? null : h.normalize();
        return new KnockbackSnapshot(target, false, null, getPosition(), flightDir, kb, punchLevel());
    }

    /** {@code null} when nothing is installed. */
    protected @Nullable Services services() {
        var polyp = Polyp.getInstance();
        return polyp.isInitialized() ? polyp.services() : null;
    }
}
