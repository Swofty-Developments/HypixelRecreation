package net.swofty.type.skywarsgame.luckyblock.spawns;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

/**
 * Rideable Blaze from Lucky Block.
 * Can fire fireballs while riding. Lasts 45 seconds.
 */
public class RidingBlaze extends RideableMob {

    private static final int DURATION_SECONDS = 45;
    private static final int FIREBALL_COOLDOWN_TICKS = 30; // 1.5 seconds

    private int fireballCooldown = 0;

    public RidingBlaze(SkywarsPlayer rider, Instance instance) {
        super(rider, instance);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.BLAZE;
    }

    @Override
    public String getDisplayName() {
        return "Blaze";
    }

    @Override
    public int getDurationSeconds() {
        return DURATION_SECONDS;
    }

    @Override
    protected void onMountTick() {
        if (fireballCooldown > 0) {
            fireballCooldown--;
        }
    }

    /**
     * Fire a fireball in the direction the player is looking.
     * Called when player right-clicks while riding.
     */
    public void fireFireball() {
        if (fireballCooldown > 0 || mount == null || !active) return;

        fireballCooldown = FIREBALL_COOLDOWN_TICKS;

        Vec direction = rider.getPosition().direction().mul(1.5);
        var projectiles = Polyp.getInstance().services().projectiles();
        if (projectiles == null) return;
        projectiles.launch(ProjectileSnapshot.of(rider, Fireball.INSTANCE)
                .withSpawnPos(mount.getPosition().add(0, 1.5, 0))
                .withVelocity(direction));
    }

    @Override
    protected void onMountRemoved() {
        // Blaze disappears in a puff of smoke (visual effect would be added here)
    }
}
