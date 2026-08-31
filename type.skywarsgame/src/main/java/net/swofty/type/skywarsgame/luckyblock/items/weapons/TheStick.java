package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;

public class TheStick implements LuckyBlockWeapon {

    public static final String ID = "the_stick";
    private static final double KNOCKBACK_MULTIPLIER = 4.0;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "The Stick";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.STICK;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.STICK,
                        Text.of("<e>The Stick"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Knockback IV"),
                                Text.empty(),
                                Text.of("<8><o>A legendary weapon"),
                                Text.of("<8><o>feared by all."),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        if (target instanceof LivingEntity living) {
            Vec direction = target.getPosition().asVec()
                    .sub(holder.getPosition().asVec())
                    .normalize();

            Vec knockback = new Vec(
                    direction.x() * 25 * KNOCKBACK_MULTIPLIER,
                    8 * KNOCKBACK_MULTIPLIER,
                    direction.z() * 25 * KNOCKBACK_MULTIPLIER
            );

            living.setVelocity(knockback);
        }

        return damage;
    }

    @Override
    public double getAttackDamage() {
        return 2.0;
    }

    @Override
    public double getKnockbackMultiplier() {
        return KNOCKBACK_MULTIPLIER;
    }

    @Override
    public boolean hasOnHitEffect() {
        return true;
    }
}
