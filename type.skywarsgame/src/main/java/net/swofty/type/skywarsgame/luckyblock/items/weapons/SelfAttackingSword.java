package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class SelfAttackingSword implements LuckyBlockWeapon {

    public static final String ID = "self_attacking_sword";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Self-Attacking Sword";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.IRON_SWORD, """
                        <c>Self-Attacking Sword

                        <7>This cursed blade attacks
                        <7>its wielder instead!

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        holder.damage(Damage.fromEntity(null, damage));
        return 0;
    }

    @Override
    public double getAttackDamage() {
        return 6.0;
    }

    @Override
    public boolean hasOnHitEffect() {
        return true;
    }
}
