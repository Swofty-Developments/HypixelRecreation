package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.fasttravel;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.warps.TravelScrollIslands;

import java.util.ArrayList;
import java.util.List;

public class GUIFastTravel extends StatelessView {
    private static final int[] SLOTS = new int[]{
            10, 11, 12, 13, 14, 15, 16,
            20, 21, 22, 23, 24,
            30, 32
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Fast Travel", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        boolean shouldBePaper = player.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);

        layout.slot(53, (s, c) -> {
            boolean isPaper = ((SkyBlockPlayer) c.player()).getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);
            return ItemStacks.item(isPaper ? Material.FILLED_MAP : Material.MAP, 1, """
                    <a>Paper Icons
                    <7>Use paper icons, which may load this menu
                    <7>faster on your computer.

                    <7>Enabled: {}

                    <e>Click to toggle!""", isPaper ? Text.of("<a>ON") : Text.of("<c>OFF"));
        }, (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            p.getToggles().inverse(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);
            c.replace(new GUIFastTravel());
        });

        TravelScrollIslands[] values = TravelScrollIslands.values();
        for (int i = 0; i < values.length && i < SLOTS.length; i++) {
            TravelScrollIslands island = values[i];
            int slot = SLOTS[i];
            boolean hasSubMenu = !island.getAssociatedScrolls().isEmpty();

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean hasUnlockedIsland = p.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.VISITED_ISLANDS, DatapointStringList.class)
                        .getValue()
                        .contains(island.getInternalName());
                boolean isPaper = p.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<8>/warp {}", island.getInternalName()));
                lore.add(Text.literal(" "));

                lore.addAll(Text.of("<7><wrap:30>{}</wrap>", island.getDescription().apply(hasUnlockedIsland)).lines());
                lore.add(Text.literal(" "));

                if (island.getAssociatedSkill() != null) {
                    lore.add(Text.of("<7>Main skill: <b>{}", island.getAssociatedSkill()));
                    lore.add(Text.of("<7>Island tier: <e>{}", island.getIslandTier()));
                    lore.add(Text.literal(" "));
                }

                if (!hasUnlockedIsland) {
                    lore.add(Text.of("<c>Warp not unlocked!"));
                } else {
                    if (hasSubMenu) {
                        lore.add(Text.of("<8>Right-Click to warp!"));
                        lore.add(Text.of("<e>Left-Click to open!"));
                    } else {
                        lore.add(Text.of("<e>Click to warp!"));
                    }
                }

                Text name = island.getDescriptiveName();
                if (isPaper) {
                    return ItemStacks.item(hasUnlockedIsland ? Material.PAPER : Material.BEDROCK, 1, name, lore);
                } else {
                    return ItemStacks.head(
                            hasUnlockedIsland ? island.getTexture() : "da99b05b9a1db4d29b5e673d77ae54a77eab66818586035c8a2005aeb810602a",
                            1, name, lore);
                }
            }, (click, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean hasUnlockedIsland = p.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.VISITED_ISLANDS, DatapointStringList.class)
                        .getValue()
                        .contains(island.getInternalName());

                if (!hasUnlockedIsland) {
                    p.sendMessage("<c>You haven't unlocked this fast travel destination!");
                    return;
                }

                if (!hasSubMenu) {
                    p.closeInventory();
                    p.sendMessage("<7>Warping you to {}<7>...", island.getDescriptiveName());
                    p.asProxyPlayer().transferToWithIndication(island.getServerType()).thenRun(() -> {
                        p.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", island.getDescriptiveName());
                    });
                    return;
                }

                if (click.click() instanceof Click.Right) {
                    p.closeInventory();
                    p.sendMessage("<7>Warping you to {}<7>...", island.getDescriptiveName());
                    p.asProxyPlayer().transferToWithIndication(island.getServerType()).thenRun(() -> {
                        p.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", island.getDescriptiveName());
                    });
                } else {
                    c.push(new GUIFastTravelSubMenu(island));
                }
            });
        }
    }
}
