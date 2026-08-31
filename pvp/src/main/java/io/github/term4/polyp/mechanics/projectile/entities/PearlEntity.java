package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.api.event.projectile.PearlTeleportEvent;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.world.WorldPolicy;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.types.fall.FallDamage;
import io.github.term4.polyp.mechanics.damage.types.generic.GenericDamage;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver.ProjectileContext;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.util.Teleports;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ender pearl: impact teleports the shooter there and deals {@code teleportDamage} fall damage (vanilla 5).
 * 1.8 ignores self-hits, wired via {@code selfHit(PASS_THROUGH)}. A configured behavior OWNS the teleport
 * (like the fireball's detonation) and calls {@link #teleportShooter} with its own target.
 *
 * <p>{@link WorldPolicy#canAffect} gates the teleport (vanilla's same-dimension check); a stasis-style
 * cross-world reach is a policy override plus {@link #setCrossInstanceTeleport}.
 */
public class PearlEntity extends ManagedProjectile {

    /** Vanilla 5. */
    private final float teleportDamage;
    private boolean crossInstanceTeleport = false;

    public PearlEntity(@Nullable Entity shooter, @NotNull EntityType entityType,
                       ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        super(shooter, entityType, snap, effectiveConfig);
        ProjectileContext ctx = ProjectileContext.of(snap, services());
        this.teleportDamage = FieldValue.resolve(effectiveConfig.teleportDamage, ctx, 5.0).floatValue();
    }

    /** Teleport the shooter even after they left the pearl's instance (stasis chambers); default false = vanilla. */
    public void setCrossInstanceTeleport(boolean v) { this.crossInstanceTeleport = v; }

    @Override
    protected void onImpact(@Nullable Entity hitEntity) {
        if (hasBehavior()) return; // the behavior owns the teleport
        teleportShooter(getPosition()); // pre-move position (1.8 EntityEnderPearl)
    }

    /** Behavior entry point: full pearl gating (world policy, instance, view-preserving wire, fall reset,
     *  {@code teleportDamage}). {@link PearlTeleportEvent} cancel = the pearl is consumed, nobody moves. */
    public void teleportShooter(@NotNull Pos target) {
        Entity shooter = getShooter();
        if (shooter == null || shooter.isRemoved()) return;
        if (!WorldPolicy.canAffect(this, shooter)) return; // a cross-world pearl (e.g. a replayed one) never yanks the thrower
        // vanilla only teleports within the pearl's world (1.8 entityplayer.world == this.world)
        boolean sameInstance = shooter.getInstance() == getInstance();
        if (!sameInstance && (!crossInstanceTeleport || getInstance() == null)) return;
        PearlTeleportEvent event = new PearlTeleportEvent(this, shooter, target);
        EventDispatcher.call(event);
        if (event.isCancelled()) return;
        target = event.target();
        // RELATIVE-view teleport so the camera isn't snapped; the cross-instance spawn can't carry relative flags
        if (sameInstance) {
            Teleports.place(shooter, target.withView(0f, 0f), RelativeFlags.VIEW);
        } else {
            Pos view = shooter.getPosition();
            MechanicsWorld.of(this).spawn(shooter, target.withView(view.yaw(), view.pitch()));
        }
        // at the DESTINATION, like ThrownEnderpearl.playSound(level, teleportPos) - only on a real move, so a
        // refused pearl (consumeOnShooter) stays silent
        Services fxServices = services();
        if (fxServices != null) {
            Fx.play(fxServices, Fx.PEARL_TELEPORT, FxContext.at(MechanicsWorld.of(shooter), target, shooter));
        }
        landingEffects(shooter);
        // TODO(endermite): vanilla 5% endermite spawn on teleport (cosmetic, deferred)
    }

    /** Refused teleport: the pearl still lands on the shooter (fall reset + {@code teleportDamage}, no movement)
     *  with a real position echo on the wire - a silent refusal desyncs the falling client's prediction. */
    public void consumeOnShooter() {
        Entity shooter = getShooter();
        if (shooter == null || shooter.isRemoved()) return;
        if (!WorldPolicy.canAffect(this, shooter)) return;
        if (shooter.getInstance() == getInstance()) {
            Teleports.place(shooter, shooter.getPosition().withView(0f, 0f), RelativeFlags.VIEW);
        }
        landingEffects(shooter);
    }

    private void landingEffects(Entity shooter) {
        // before the damage below, so the teleport drop adds no extra fall damage
        FallDamage.resetFallDistance(shooter);
        if (teleportDamage > 0 && shooter instanceof Player) {
            Services s = services();
            if (s != null && s.damage() != null) {
                // GenericDamage stands in for a dedicated pearl/fall type. TODO(verify): hurt + invul in-game
                s.damage().apply(DamageSnapshot.of(shooter, GenericDamage.INSTANCE).withAmount(teleportDamage).withSource(this));
            }
        }
    }
}
