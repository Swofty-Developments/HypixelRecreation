package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.swofty.type.garden.config.GardenBarnSkinDefinition;
import net.swofty.type.garden.user.SkyBlockGarden;
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

public class GUIBarnSkins extends StatelessView {
    private static final int[] SKIN_SLOTS = {10, 11, 12, 13, 14, 15};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Barn Skins", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockGarden garden = GardenGuiSupport.garden(player);
        List<GardenBarnSkinDefinition> skins = garden == null ? List.of() : garden.getPlotService().getBarnSkins();

        for (int index = 0; index < Math.min(skins.size(), SKIN_SLOTS.length); index++) {
            GardenBarnSkinDefinition skin = skins.get(index);
            int slot = SKIN_SLOTS[index];
            layout.slot(slot, (s, c) -> buildSkinItem((SkyBlockPlayer) c.player(), skin), (click, c) -> {
                SkyBlockPlayer target = (SkyBlockPlayer) c.player();
                SkyBlockGarden targetGarden = GardenGuiSupport.garden(target);
                if (targetGarden == null) {
                    target.sendMessage("§cYour Garden is not ready yet.");
                    return;
                }

                GardenData.GardenCoreData core = GardenGuiSupport.core(target);
                if (!GardenGuiSupport.isBarnSkinUnlocked(target, skin)) {
                    target.sendMessage("§cYou haven't unlocked that Barn Skin yet.");
                    return;
                }
                if (targetGarden.isBarnSwapInProgress()) {
                    target.sendMessage("§cA Barn Skin swap is already in progress.");
                    return;
                }
                if (core.getSelectedBarnSkin().equalsIgnoreCase(skin.id())) {
                    target.sendMessage("§eThat Barn Skin is already selected.");
                    return;
                }

                target.closeInventory();
                target.sendMessage("§aSwapping to " + skin.displayName() + "§a...");
                targetGarden.changeBarnSkin(skin.id()).thenRun(() -> {
                    core.setSelectedBarnSkin(skin.id());
                    core.getOwnedBarnSkins().add(skin.id());
                    target.sendMessage("§aYour Barn Skin is now " + skin.displayName() + "§a.");
                }).exceptionally(throwable -> {
                    target.sendMessage("§cFailed to swap Barn Skin: " + throwable.getMessage());
                    return null;
                });
            });
        }
    }

    private net.minestom.server.item.ItemStack.Builder buildSkinItem(SkyBlockPlayer player, GardenBarnSkinDefinition skin) {
        GardenData.GardenCoreData core = GardenGuiSupport.core(player);
        boolean selected = core.getSelectedBarnSkin().equalsIgnoreCase(skin.id());
        boolean unlocked = GardenGuiSupport.isBarnSkinUnlocked(player, skin);
        List<String> lore = new ArrayList<>(List.of(
            "§7Select this skin for your Barn!",
            ""
        ));
        lore.add(GardenGuiSupport.colorForRarity(skin.rarity()) + "§l" + skin.rarity() + " COSMETIC");
        lore.add("");
        if (selected) {
            lore.add("§aSELECTED");
        } else if (unlocked) {
            lore.add("§eClick to select!");
        } else {
            lore.add("§7Unlock source: §b" + skin.unlockSource());
            lore.add("");
            lore.add("§c§lLOCKED");
        }
        return GardenGuiSupport.itemWithLore(
            skin.id(),
            GardenGuiSupport.colorForRarity(skin.rarity()) + skin.displayName().replace(" Barn Skin", ""),
            lore
        );
    }
}
