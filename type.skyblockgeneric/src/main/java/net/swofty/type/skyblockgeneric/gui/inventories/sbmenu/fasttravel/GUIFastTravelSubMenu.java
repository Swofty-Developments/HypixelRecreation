package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.fasttravel;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
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
import net.swofty.type.skyblockgeneric.warps.ScrollUnlockReason;
import net.swofty.type.skyblockgeneric.warps.TravelScrollIslands;
import net.swofty.type.skyblockgeneric.warps.TravelScrollType;

import java.util.ArrayList;
import java.util.List;

public class GUIFastTravelSubMenu extends StatelessView {
    private static final int[] SLOTS = new int[]{20, 21, 22, 23, 24};
    private final TravelScrollIslands island;

    public GUIFastTravelSubMenu(TravelScrollIslands island) {
        this.island = island;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(StringUtility.toNormalCase(island.getInternalName()) + " Warps", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        Components.back(layout, 39, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        boolean shouldBePaper = player.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);

        // Main island warp
        layout.slot(13, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            boolean isPaper = p.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);

            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<8>/warp {}", island.getInternalName()));
            lore.add(Text.literal(" "));

            lore.addAll(Text.of("<7><wrap:30>{}</wrap>", island.getDescription().apply(true)).lines());
            lore.add(Text.literal(" "));

            if (island.getAssociatedSkill() != null) {
                lore.add(Text.of("<7>Main skill: <b>{}", island.getAssociatedSkill()));
                lore.add(Text.of("<7>Island tier: <e>{}", island.getIslandTier()));
                lore.add(Text.literal(" "));
            }

            lore.add(Text.of("<e>Click to warp!"));

            Text name = island.getDescriptiveName();
            if (isPaper) {
                return ItemStacks.item(Material.PAPER, 1, name, lore);
            } else {
                return ItemStacks.head(island.getTexture(), 1, name, lore);
            }
        }, (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            p.closeInventory();
            p.sendMessage("<7>Warping you to {}<7>...", island.getDescriptiveName());
            p.asProxyPlayer().transferToWithIndication(island.getServerType()).thenRun(() -> {
                p.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", island.getDescriptiveName());
            });
        });

        List<TravelScrollType> scrolls = island.getAssociatedScrolls();
        for (int i = 0; i < scrolls.size() && i < SLOTS.length; i++) {
            TravelScrollType scroll = scrolls.get(i);
            int slot = SLOTS[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean isUnlocked = p.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.USED_SCROLLS, DatapointStringList.class)
                        .getValue()
                        .contains(scroll.getInternalName());
                boolean isPaper = p.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<8>/warp {}", scroll.getInternalName()));
                lore.add(Text.literal(" "));

                lore.addAll(Text.of("<7><wrap:50>{}</wrap>", scroll.getDescription()).lines());
                lore.add(Text.literal(" "));

                if (!isUnlocked) {
                    ScrollUnlockReason unlockReason = scroll.getUnlockReason();
                    lore.add(unlockReason.getTitleReason());
                    lore.add(unlockReason.getSubReason());
                    lore.add(Text.literal(" "));
                    lore.add(Text.of("<c>Warp not unlocked!"));
                } else {
                    lore.add(Text.of("<e>Click to warp!"));
                }

                Text name = scroll.getDisplayName();
                if (isPaper) {
                    return ItemStacks.item(isUnlocked ? Material.PAPER : Material.BEDROCK, 1, name, lore);
                } else {
                    return ItemStacks.head(
                            isUnlocked ? scroll.getHeadTexture() : "da99b05b9a1db4d29b5e673d77ae54a77eab66818586035c8a2005aeb810602a",
                            1, name, lore);
                }
            }, (click, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean isUnlocked = p.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.USED_SCROLLS, DatapointStringList.class)
                        .getValue()
                        .contains(scroll.getInternalName());

                if (!isUnlocked) {
                    p.sendMessage("<c>You haven't unlocked this fast travel destination!");
                    return;
                }

                p.closeInventory();
                p.sendMessage("<7>Warping you to {}<7>...", scroll.getDescription());
                p.asProxyPlayer().transferToWithIndication(island.getServerType()).thenRun(() -> {
                    p.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", scroll.getDisplayName());
                    p.asProxyPlayer().teleport(scroll.getLocation());
                });
            });
        }
    }
}
