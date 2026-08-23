package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;

public class IronSwordWeapon implements LuckyBlockWeapon {

    public static final String ID = "iron_sword_weapon";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Iron Sword";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.raw(Material.IRON_SWORD, """
                        <f>Iron Sword

                        <7>+6 Attack Damage

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public double getAttackDamage() {
        return 6.0;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }
}
