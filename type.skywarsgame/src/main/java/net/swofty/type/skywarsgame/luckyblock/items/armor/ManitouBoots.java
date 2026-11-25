package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;

import java.util.List;

public class ManitouBoots implements LuckyBlockArmor {

    public static final String ID = "manitou_boots";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Manitou Boots";
    }

    @Override
    public EquipmentSlot getSlot() {
        return EquipmentSlot.BOOTS;
    }

    @Override
    public Material getBaseMaterial() {
        return Material.LEATHER_BOOTS;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.LEATHER_BOOTS,
                        Text.of("<f>Manitou Boots"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Protection I"),
                                Text.of("<7>Fire Protection II"),
                                Text.of("<7>Feather Falling III"),
                                Text.of("<7>Unbreaking III"),
                                Text.of("<7>Blast Protection II"),
                                Text.of("<7>Projectile Protection I"),
                                Text.of("<7>Thorns I"),
                                Text.of("<7>Depth Strider III"),
                                Text.empty(),
                                Text.of("<8><o>Blessed by the Great Spirit"),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(DataComponents.DYED_COLOR, new Color(255, 255, 255))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }
}
