package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIGreenhouseSkins extends StatelessView {
    private static final int[] SKIN_SLOTS = {10, 11, 12, 13, 14, 15, 16};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Greenhouse Skins", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GardenData.GardenGreenhouseData greenhouse = GardenGuiSupport.greenhouse(player);
        List<String> skins = new ArrayList<>(greenhouse.getOwnedGreenhouseSkins());
        if (!skins.contains("default")) {
            skins.add(0, "default");
        }
        skins.sort(String::compareToIgnoreCase);

        for (int index = 0; index < Math.min(skins.size(), SKIN_SLOTS.length); index++) {
            String skin = skins.get(index);
            layout.slot(SKIN_SLOTS[index], buildSkinItem(greenhouse, skin), (click, c) -> {
                greenhouse.setSelectedGreenhouseSkin(skin);
                player.playSuccessSound();
                c.session(Object.class).refresh();
            });
        }

        layout.slot(31, ItemStackCreator.getStack(
            "§aUnlock Sources",
            Material.PAPER,
            1,
            "§7Owned Greenhouse skins: §e" + greenhouse.getOwnedGreenhouseSkins().size(),
            "§7Selected: §a" + StringUtility.toNormalCase(greenhouse.getSelectedGreenhouseSkin()),
            "",
            "§7Greenhouse skins are profile data and",
            "§7can be granted by future milestone,",
            "§7SkyMart, or event unlock flows."
        ));
    }

    private net.minestom.server.item.ItemStack.Builder buildSkinItem(GardenData.GardenGreenhouseData greenhouse, String skin) {
        boolean selected = greenhouse.getSelectedGreenhouseSkin().equalsIgnoreCase(skin);
        List<String> lore = new ArrayList<>(List.of(
            "§7Select this skin for your Greenhouse.",
            "",
            "§f§lCOMMON COSMETIC",
            ""
        ));
        lore.add(selected ? "§aCurrently selected!" : "§eClick to select!");
        return ItemStackCreator.getStack(
            "§f" + StringUtility.toNormalCase(skin),
            Material.WHITE_STAINED_GLASS,
            1,
            lore
        );
    }
}
