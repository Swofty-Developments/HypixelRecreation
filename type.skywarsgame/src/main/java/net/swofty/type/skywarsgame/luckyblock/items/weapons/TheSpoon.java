package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;

import java.util.List;

public class TheSpoon implements LuckyBlockWeapon {

    public static final String ID = "the_spoon";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "The Spoon";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.WOODEN_SHOVEL;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.WOODEN_SHOVEL,
                        Text.of("<e>The Spoon"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Sharpness V"),
                                Text.empty(),
                                Text.of("<8><o>\"There is no spoon.\""),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public double getAttackDamage() {
        return 2.5 + 6.25;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }
}
