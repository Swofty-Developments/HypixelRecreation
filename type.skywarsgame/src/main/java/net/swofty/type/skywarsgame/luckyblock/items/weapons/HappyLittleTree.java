package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;

import java.util.List;

public class HappyLittleTree implements LuckyBlockWeapon {

    public static final String ID = "happy_little_tree";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Happy Little Tree";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.OAK_SAPLING;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.OAK_SAPLING,
                        Text.of("<a>Happy Little Tree"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Sharpness III"),
                                Text.empty(),
                                Text.of("<8><o>\"There are no mistakes,"),
                                Text.of("<8><o>only happy accidents.\""),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public double getAttackDamage() {
        return 1.0 + 3.75;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }
}
