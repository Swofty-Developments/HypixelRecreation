package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;

public class WoodenSwordWeapon implements LuckyBlockWeapon {

    public static final String ID = "wooden_sword_weapon";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Wooden Sword";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.WOODEN_SWORD;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.WOODEN_SWORD, """
                        <f>Wooden Sword

                        <7>Sharpness II

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public double getAttackDamage() {
        return 4.0 + 2.5;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }
}
