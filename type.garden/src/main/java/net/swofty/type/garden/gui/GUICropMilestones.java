package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.StringUtility;
import net.swofty.type.garden.GardenServices;
import net.swofty.type.garden.milestone.GardenMilestoneService;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICropMilestones extends StatelessView {
    private static final int[] CROP_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Crop Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<GardenMilestoneService.CropMilestoneDefinition> crops = GardenServices.milestones().getCropDefinitions();

        int completedTiers = GardenGuiSupport.core(player).getCropMilestones().values().stream()
            .mapToInt(Integer::intValue)
            .sum();
        long harvestedCrops = GardenGuiSupport.core(player).getCropMilestoneProgress().values().stream()
            .mapToLong(Long::longValue)
            .sum();

        layout.slot(4, ItemStackCreator.getStack(
            "§aCrop Milestones",
            net.minestom.server.item.Material.WHEAT,
            1,
            "§7Harvest crops on your Garden to",
            "§7unlock milestone tiers for each crop.",
            "",
            "§7Tracked crops: §e" + crops.size(),
            "§7Total harvested: §e" + StringUtility.commaify(harvestedCrops),
            "§7Completed tiers: §e" + StringUtility.commaify(completedTiers)
        ));

        for (int index = 0; index < Math.min(crops.size(), CROP_SLOTS.length); index++) {
            GardenMilestoneService.CropMilestoneDefinition definition = crops.get(index);
            GardenMilestoneService.MilestoneProgress progress = GardenServices.milestones().getCropProgress(player, definition.id());
            List<String> lore = new ArrayList<>();
            lore.add("§7Tier: §e" + progress.completedTiers() + "§7/§a" + definition.tiers().size());
            lore.add("§7Harvested: §e" + StringUtility.commaify(progress.progress()));
            lore.add("");

            if (progress.nextTier() != null) {
                long progressIntoTier = Math.max(0L, progress.progress() - progress.previousThreshold());
                long neededThisTier = Math.max(1L, progress.nextThreshold() - progress.previousThreshold());
                long remaining = Math.max(0L, progress.nextThreshold() - progress.progress());
                lore.add("§7Progress to Tier §a" + StringUtility.getAsRomanNumeral(progress.nextTier().tier()) + "§7:");
                lore.add(GardenGuiSupport.progressBar(progressIntoTier, neededThisTier));
                lore.add("§7Remaining: §e" + StringUtility.commaify(remaining));
                lore.add("");
                lore.add("§7Next Rewards:");
                lore.add(" §8+§3" + StringUtility.commaify(progress.nextTier().farmingXp()) + " Farming XP");
                lore.add(" §8+§2" + progress.nextTier().gardenXp() + " Garden XP");
                lore.add(" §8+§b" + progress.nextTier().skyblockXp() + " SkyBlock XP");
            } else {
                lore.add("§aMAXED");
                lore.add("");
                lore.add("§7You have completed every");
                lore.add("§7milestone tier for this crop.");
            }

            layout.slot(CROP_SLOTS[index], GardenGuiSupport.itemWithLore(
                definition.iconItemId(),
                "§a" + definition.displayName(),
                lore
            ));
        }
    }
}
