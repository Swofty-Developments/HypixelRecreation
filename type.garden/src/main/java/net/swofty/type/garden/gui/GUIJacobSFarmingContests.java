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
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIJacobSFarmingContests extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Jacob's Farming Contests", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 31, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        layout.slot(11, ItemStackCreator.getStack(
            "§aWhat is this?",
            Material.OAK_SIGN,
            1,
            "§8Instructions",
            "",
            "§7Every 3 SkyBlock days, Jacob holds a",
            "§7contest for 3 §efarming §7collections.",
            "",
            "§7The contests last 1 SkyBlock day.",
            "§8(20 minutes).",
            "",
            "§bCo-ops do NOT pool their collection!",
            "",
            "§7Requirement: §aFarming X",
            "§7Requirement: §aTalk to Jacob",
            "§7Reward floor: §e100 crops"
        ));

        List<String> scheduleLore = new ArrayList<>(List.of("§8Schedule", ""));
        List<GardenGuiSupport.UpcomingContestDisplay> upcoming = GardenGuiSupport.getUpcomingContests(3);
        for (GardenGuiSupport.UpcomingContestDisplay display : upcoming) {
            scheduleLore.add("§e" + GardenGuiSupport.formatContestDate(display));
            for (String crop : display.entry().crops()) {
                scheduleLore.add("§e○ §7" + StringUtility.toNormalCase(crop));
            }
            scheduleLore.add("");
        }
        if (upcoming.isEmpty()) {
            scheduleLore.add("§cContest service unavailable");
        } else {
            scheduleLore.add("§8View this info in your full");
            scheduleLore.add("§8SkyBlock calendar!");
        }
        layout.slot(13, ItemStackCreator.getStack("§6Upcoming Contests", Material.CLOCK, 1, scheduleLore));

        layout.slot(15, ItemStackCreator.getStack(
            "§bContest Rewards",
            Material.WHEAT,
            1,
            "§8Your progress",
            "",
            "§7Jacob's Tickets: §a" + GardenGuiSupport.personal(player).getJacobsTickets(),
            "§6§lGOLD §7medals: §6" + GardenGuiSupport.getMedalCount(player, "gold"),
            "§f§lSILVER §7medals: §f" + GardenGuiSupport.getMedalCount(player, "silver"),
            "§c§lBRONZE §7medals: §c" + GardenGuiSupport.getMedalCount(player, "bronze"),
            "",
            "§7Personal best crops: §e" + GardenGuiSupport.personal(player).getContestPersonalBests().size(),
            "§7Claimed rewards tracked: §e" + GardenGuiSupport.personal(player).getClaimedContestRewards().size()
        ));
    }
}
