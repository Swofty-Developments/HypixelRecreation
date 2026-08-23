package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;

public class KnockbackSlimeball implements LuckyBlockWeapon {

    public static final String ID = "knockback_slimeball";
    private static final double KNOCKBACK_MULTIPLIER = 10.0;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Knockback Slimeball";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.SLIME_BALL;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.raw(Material.SLIME_BALL,
                        Text.of("<a>Knockback Slimeball"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Knockback X"),
                                Text.empty(),
                                Text.of("<8><o>\"Boing!\""),
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
        return 1.0;
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
