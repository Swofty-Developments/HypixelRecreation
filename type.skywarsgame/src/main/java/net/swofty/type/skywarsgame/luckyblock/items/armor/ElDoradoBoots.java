package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;

import java.util.List;

public class ElDoradoBoots implements LuckyBlockArmor {

    public static final String ID = "el_dorado_boots";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "El Dorado Boots";
    }

    @Override
    public EquipmentSlot getSlot() {
        return EquipmentSlot.BOOTS;
    }

    @Override
    public Material getBaseMaterial() {
        return Material.GOLDEN_BOOTS;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.raw(Material.GOLDEN_BOOTS,
                        Text.of("<6>El Dorado Boots"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Protection III"),
                                Text.empty(),
                                Text.of("<8><o>The legendary city of gold"),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }
}
