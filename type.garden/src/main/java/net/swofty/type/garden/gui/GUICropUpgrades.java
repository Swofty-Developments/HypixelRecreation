package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.StringUtility;
import net.swofty.type.garden.config.GardenConfigRegistry;
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
import java.util.Map;

public class GUICropUpgrades extends StatelessView {
    private static final int[] CROP_SLOTS = {
        11, 12, 13, 14, 15,
        20, 21, 22, 23, 24,
        30, 31, 32
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Crop Upgrades", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        List<String> crops = GardenConfigRegistry.getList(
            GardenConfigRegistry.getConfig("crop_upgrades.yml"),
            "crops"
        ).stream().map(String::valueOf).toList();

        for (int index = 0; index < Math.min(crops.size(), CROP_SLOTS.length); index++) {
            String cropId = crops.get(index);
            int slot = CROP_SLOTS[index];
            layout.slot(slot, (s, c) -> buildCropItem((SkyBlockPlayer) c.player(), cropId), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                GardenData.GardenCoreData core = GardenGuiSupport.core(player);
                int currentTier = core.getCropUpgrades().getOrDefault(cropId, 0);
                Map<String, Object> nextTier = nextTier(currentTier + 1);
                if (nextTier.isEmpty()) {
                    player.sendMessage("§eThat crop is already maxed.");
                    return;
                }

                int requiredGardenLevel = GardenConfigRegistry.getInt(nextTier, "garden_level", 0);
                int copperCost = GardenConfigRegistry.getInt(nextTier, "copper", 0);
                if (core.getLevel() < requiredGardenLevel) {
                    player.sendMessage("§cYou need Garden Level " + StringUtility.getAsRomanNumeral(requiredGardenLevel) + " first.");
                    return;
                }
                if (core.getCopper() < copperCost) {
                    player.sendMessage("§cYou don't have enough Copper.");
                    return;
                }

                core.setCopper(core.getCopper() - copperCost);
                core.getCropUpgrades().put(cropId, currentTier + 1);
                player.playSuccessSound();
                c.session(Object.class).refresh();
            });
        }
    }

    private net.minestom.server.item.ItemStack.Builder buildCropItem(SkyBlockPlayer player, String cropId) {
        GardenData.GardenCoreData core = GardenGuiSupport.core(player);
        int currentTier = core.getCropUpgrades().getOrDefault(cropId, 0);
        int fortune = currentTier * net.swofty.type.garden.GardenServices.desk().getCropUpgradeFortunePerTier();
        List<String> lore = new ArrayList<>(List.of(
            "§7Upgrade your §a" + StringUtility.toNormalCase(cropId) + " §7tier to",
            "§7increase your §6☘ " + StringUtility.toNormalCase(cropId) + " Fortune§7.",
            "",
            "§7Current Tier: §e" + currentTier + "§7/§a9",
            "§7" + StringUtility.toNormalCase(cropId) + " Fortune: §6+" + fortune + "☘",
            ""
        ));

        Map<String, Object> nextTier = nextTier(currentTier + 1);
        if (nextTier.isEmpty()) {
            lore.add("§a§lMAXED");
        } else {
            int requiredGardenLevel = GardenConfigRegistry.getInt(nextTier, "garden_level", 0);
            int copperCost = GardenConfigRegistry.getInt(nextTier, "copper", 0);
            lore.add("§7Next Tier: §e" + (currentTier + 1));
            lore.add("§7Requires Garden Level §a" + StringUtility.getAsRomanNumeral(requiredGardenLevel));
            lore.add("§7Cost: §c" + StringUtility.commaify(copperCost) + " Copper");
            lore.add("");
            lore.add(core.getCopper() >= copperCost && core.getLevel() >= requiredGardenLevel
                ? "§eClick to upgrade!"
                : "§cRequirements not met");
        }

        return GardenGuiSupport.itemWithLore(
            cropId,
            "§a" + StringUtility.toNormalCase(cropId),
            lore
        );
    }

    private Map<String, Object> nextTier(int tier) {
        return net.swofty.type.garden.GardenServices.desk().getCropUpgradeTiers().stream()
            .filter(entry -> GardenConfigRegistry.getInt(entry, "tier", 0) == tier)
            .findFirst()
            .orElse(Map.of());
    }
}
