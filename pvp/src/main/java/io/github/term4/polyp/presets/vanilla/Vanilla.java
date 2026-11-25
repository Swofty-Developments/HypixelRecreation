package io.github.term4.polyp.presets.vanilla;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.entity.DroppedItemEntity;

/**
 * Modern (26.1+) preset - the composed, pure-mechanics {@link MechanicsProfile} built from the config classes in this
 * package. Mechanics only - no compat or fixes (those install separately). Incomplete: attack, knockback, player, and
 * blocking are still TODO. Assign with {@code polyp.profiles().setGlobal(Vanilla.profile())}.
 */
public final class Vanilla {

    private Vanilla() {}

    public static MechanicsProfile profile() {
        return MechanicsProfile.builder()
                .set(MechanicsKeys.DAMAGE, Damage.config())
                .set(MechanicsKeys.DEATH, Death.config())
                .set(MechanicsKeys.PROJECTILES, Projectiles.config())
                .set(MechanicsKeys.ATTRIBUTES, Attributes.config())
                .set(MechanicsKeys.CONSUMABLES, Consumables.config())
                .set(MechanicsKeys.VELOCITY, Movement.velocity())
                .set(MechanicsKeys.ITEM_PHYSICS, DroppedItemEntity.Model.MODERN)
                .set(MechanicsKeys.ITEM_DAMAGE, io.github.term4.polyp.presets.vanilla18.Items.damage())
                .set(MechanicsKeys.EXPLOSION, Explosion.config())
                .set(MechanicsKeys.HUNGER, Hunger.config())
                .set(MechanicsKeys.ITEMS, Items.registry())
                .set(MechanicsKeys.FX, Fx.modern())
                .build();
    }
}
