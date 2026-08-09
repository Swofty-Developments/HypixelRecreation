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
import java.util.Map;

public class GUIDesk extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Desk", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);
        Components.fill(layout);

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            GardenData.GardenCoreData core = GardenGuiSupport.core(player);
            int nextLevel = Math.min(core.getLevel() + 1, 15);
            Map<String, Object> next = net.swofty.type.garden.GardenServices.levels().getLevel(nextLevel);

            List<String> lore = new ArrayList<>(List.of(
                "§7Earn Garden experience by",
                "§7accepting visitors' offers and",
                "§7unlocking new milestones!",
                "",
                "§7Garden XP: §e" + StringUtility.commaify(core.getExperience())
            ));

            if (core.getLevel() < 15 && !next.isEmpty()) {
                lore.add("§7Level " + StringUtility.getAsRomanNumeral(nextLevel) + " Rewards:");
                for (String reward : net.swofty.type.garden.GardenServices.levels().getRewardsForLevel(nextLevel)) {
                    lore.add("  §8+§7" + reward);
                }
                lore.add("");
            }

            lore.add("§7You currently have §2"
                + (core.getLevel() * net.swofty.type.garden.GardenServices.levels().getCropGrowthPerLevelPercent())
                + " Crop Growth§7!");
            lore.add("");
            lore.add("§eClick to view!");

            return ItemStackCreator.getStack(
                "§aGarden Level " + StringUtility.getAsRomanNumeral(core.getLevel()),
                Material.SUNFLOWER,
                1,
                lore
            );
        }, (click, c) -> c.push(new GUIGardenLevels()));

        layout.slot(19, ItemStackCreator.getStack(
            "§aConfigure Plots",
            Material.GRASS_BLOCK,
            1,
            "§7Unlock access to new plots or modify",
            "§7plots that you have already unlocked!",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUIConfigurePlots()));

        layout.slot(21, ItemStackCreator.getStack(
            "§aGarden Upgrades",
            Material.GLISTERING_MELON_SLICE,
            1,
            "§7Upgrade various aspects of your",
            "§7garden to increase yield,",
            "§7experience, and more.",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUIGardenUpgrades()));

        layout.slot(23, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            return ItemStackCreator.getStack(
                "§aSkyMart",
                Material.EMERALD,
                1,
                "§7Browse the wide variety of products",
                "§7SkyMart has to offer. We are not",
                "§7responsible for any injuries,",
                "§7accidents, headaches, paper-cuts or",
                "§7sudden outburst of tears. SkyMart",
                "§7wishes you happy shopping.",
                "",
                "§7Copper: §c" + StringUtility.commaify(GardenGuiSupport.core(player).getCopper()),
                "",
                "§eClick to view!"
            );
        }, (click, c) -> c.push(new GUISkyMart()));

        layout.slot(25, ItemStackCreator.getStack(
            "§aGarden Milestones",
            Material.GOLD_BLOCK,
            1,
            "§7Achieve milestones on your Garden",
            "§7to earn Garden XP and Farming XP.",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUIGardenMilestones()));

        layout.slot(31, ItemStackCreator.getStack(
            "§aGarden Skins",
            Material.BEACON,
            1,
            "§7View and select different skins for",
            "§7your Garden!",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUIGardenSkins()));

        layout.slot(50, (s, c) -> {
            SkyBlockPlayer current = (SkyBlockPlayer) c.player();
            String mode = GardenGuiSupport.core(current).getSelectedTimeMode();
            return ItemStackCreator.getStack(
                "§aGarden Time",
                Material.CLOCK,
                1,
                "§7Modifies your Garden time.",
                "",
                "§7Current mode: §e" + StringUtility.toNormalCase(mode)
            );
        }, (click, c) -> {
            SkyBlockPlayer current = (SkyBlockPlayer) c.player();
            GardenData.GardenCoreData core = GardenGuiSupport.core(current);
            String next = switch (core.getSelectedTimeMode()) {
                case "DAY" -> "NIGHT";
                case "NIGHT" -> "DYNAMIC";
                default -> "DAY";
            };
            core.setSelectedTimeMode(next);
            c.session(Object.class).refresh();
        });
    }

}
