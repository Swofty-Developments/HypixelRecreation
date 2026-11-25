package io.github.term4.polyp.presets.scrims18;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;

/**
 * <b>Scrims 1.8</b> preset - the {@link Vanilla18} 1.8 baseline tuned for competitive scrims. WIP: today it differs
 * only in {@link Projectiles} (fully client-predicted - spawn + velocity, then never synchronizes), the rest inherits
 * vanilla18. More scrims deltas land here as the preset is fleshed out.
 */
public final class Scrims18 {

    private Scrims18() {}

    /** The scrims 1.8 mechanics profile: vanilla 1.8 with never-synchronizing projectiles. */
    public static MechanicsProfile profile() {
        return Vanilla18.profile().toBuilder()
                .set(MechanicsKeys.PROJECTILES, Projectiles.config())
                // arrow hit-marker ding to the shooter (mmc18/hypixel/scrims18 enable it; vanilla presets don't)
                .set(MechanicsKeys.FX, Fx.vanilla18().register(Fx.ARROW_HIT_PLAYER, Fx.arrowHitMarker()))
                .build();
    }

    public static ProjectileConfig projectiles() { return Projectiles.config(); }
}
