package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
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

public class GUIVisitorMilestones extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Visitor Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<GardenMilestoneService.VisitorMilestoneTrack> tracks = GardenServices.milestones().getVisitorTracks();
        int completedTiers = GardenGuiSupport.core(player).getVisitorMilestones().values().stream()
            .mapToInt(Integer::intValue)
            .sum();

        layout.slot(4, ItemStackCreator.getStackHead(
            "§aVisitor Milestones",
            "8d34f38c1bb106e11908ad3cc90162c18b863d678265c84a84a358903f8f7a1c",
            1,
            "§7Serve unique visitors and accept offers",
            "§7to unlock visitor milestone rewards.",
            "",
            "§7Unique visitors served: §e" + GardenGuiSupport.core(player).getServedUniqueVisitors().size(),
            "§7Offers accepted: §e" + GardenGuiSupport.visitors(player).getServedCounts().values().stream().mapToInt(Integer::intValue).sum(),
            "§7Completed tiers: §e" + StringUtility.commaify(completedTiers)
        ));

        int[] trackSlots = {20, 24};
        for (int index = 0; index < Math.min(tracks.size(), trackSlots.length); index++) {
            GardenMilestoneService.VisitorMilestoneTrack track = tracks.get(index);
            GardenMilestoneService.MilestoneProgress progress = GardenServices.milestones().getVisitorProgress(player, track.id());
            List<String> lore = new ArrayList<>();
            lore.add("§7Tier: §e" + progress.completedTiers() + "§7/§a" + track.tiers().size());
            lore.add("§7Progress: §e" + StringUtility.commaify(progress.progress()));
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
                lore.add("§7This milestone track has been");
                lore.add("§7completed in full.");
            }

            layout.slot(trackSlots[index], GardenGuiSupport.itemWithLore(
                track.iconItemId(),
                "§a" + track.displayName(),
                lore
            ));
        }

        layout.slot(31, ItemStackCreator.getStack(
            "§aVisitor's Logbook",
            Material.BOOK,
            1,
            "§7Browse every Garden visitor and",
            "§7review their visit history.",
            "",
            "§eClick to browse!"
        ), (click, c) -> c.push(new GUIVisitorLogbook()));
    }
}
