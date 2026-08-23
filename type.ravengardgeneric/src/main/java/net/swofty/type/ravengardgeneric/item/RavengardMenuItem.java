package net.swofty.type.ravengardgeneric.item;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.TooltipDisplay;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.Set;
import java.util.UUID;

public final class RavengardMenuItem {
    public static final int SLOT = 8;
    public static final String CUSTOM_DATA_ID = "menu_icon";

    private RavengardMenuItem() {
    }

    public static ItemStack build() {
        return ItemStacks.raw(Material.NETHER_STAR, """
                        <9>Ravengard Menu <7>(Right Click)

                        <7>View your lock box, manage skills,
                        <7>craft, and more!""")
                .set(DataComponents.UNBREAKABLE, net.minestom.server.utils.Unit.INSTANCE)
                .set(DataComponents.TOOLTIP_STYLE, "hypixel_ravengard:rare")
                .set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, Set.of(
                        DataComponents.PAINTING_VARIANT,
                        DataComponents.FIREWORKS,
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.ENCHANTMENTS,
                        DataComponents.STORED_ENCHANTMENTS,
                        DataComponents.TRIM,
                        DataComponents.CHARGED_PROJECTILES,
                        DataComponents.JUKEBOX_PLAYABLE,
                        DataComponents.MAP_ID,
                        DataComponents.UNBREAKABLE,
                        DataComponents.WRITTEN_BOOK_CONTENT,
                        DataComponents.BANNER_PATTERNS,
                        DataComponents.POTION_CONTENTS,
                        DataComponents.DYED_COLOR)))
                .set(DataComponents.CUSTOM_DATA, new CustomData(CompoundBinaryTag.builder()
                        .putString("id", CUSTOM_DATA_ID)
                        .putString("uuid", UUID.randomUUID().toString())
                        .build()))
                .build();
    }

    public static boolean isMenuItem(ItemStack stack) {
        if (stack == null || stack.isAir()) {
            return false;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && CUSTOM_DATA_ID.equals(data.nbt().getString("id"));
    }

    public static void give(RavengardPlayer player) {
        player.getInventory().setItemStack(SLOT, build());
    }
}
