package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.emblem.GUIEmblems;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.rewards.GUILevelRewards;
import net.swofty.type.skyblockgeneric.levels.LevelsGuide;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUISkyBlockLevels extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.levels.main.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        // Toggle SkyBlock Levels in Chat
        layout.slot(50, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            boolean enabled = player.getToggles().get(DatapointToggles.Toggles.ToggleType.SKYBLOCK_LEVELS_IN_CHAT);
            return ItemStacks.item(enabled ? Material.LIME_DYE : Material.GRAY_DYE, 1,
                    Text.key("gui_sbmenu.levels.main.chat_toggle"),
                    Text.keyLines("gui_sbmenu.levels.main.chat_toggle.lore",
                            Text.key(enabled ? "gui_sbmenu.levels.main.chat_toggle.enabled" : "gui_sbmenu.levels.main.chat_toggle.disabled")));
        }, (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            boolean enabled = player.getToggles().get(DatapointToggles.Toggles.ToggleType.SKYBLOCK_LEVELS_IN_CHAT);
            player.sendMessage(Text.key(enabled ? "gui_sbmenu.levels.main.msg.chat_disabled" : "gui_sbmenu.levels.main.msg.chat_enabled"));
            player.getToggles().set(DatapointToggles.Toggles.ToggleType.SKYBLOCK_LEVELS_IN_CHAT, !enabled);
            c.session(DefaultState.class).render();
        });

        // Level Rewards
        layout.slot(34, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<Text> displayList = GUILevelRewards.getAsDisplay(GUILevelRewards.getUnlocked(player),
                    GUILevelRewards.getTotalAwards());
            Text display = Text.join(Text.literal("\n"), displayList);

            return ItemStacks.item(Material.CHEST, 1, Text.key("gui_sbmenu.levels.main.level_rewards"),
                    Text.keyLines("gui_sbmenu.levels.main.level_rewards.lore", display));
        }, (click, c) -> c.player().openView(new GUILevelRewards()));

        // Your SkyBlock Level Ranking
        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            SkyBlockLevelRequirement level = player.getSkyBlockExperience().getLevel();
            int completedChallenges = player.getSkyBlockExperience().getCompletedExperienceCauses().size();
            int totalChallenges = SkyBlockLevelCause.getAmountOfCauses();

            return ItemStacks.item(Material.PAINTING, 1, Text.key("gui_sbmenu.levels.main.ranking"),
                    Text.keyLines("gui_sbmenu.levels.main.ranking.lore",
                            Text.parse(level.getColor() + level.toString()),
                            Math.round(player.getSkyBlockExperience().getTotalXP()),
                            new java.text.DecimalFormat("##.##").format((double) completedChallenges / totalChallenges * 100)));
        });

        // SkyBlock Guide
        layout.slot(25, (s, c) -> ItemStacks.item(Material.FILLED_MAP, 1, Text.key("gui_sbmenu.levels.main.guide"),
                        Text.keyLines("gui_sbmenu.levels.main.guide.lore")),
                (click, c) -> c.player().openView(new GUILevelsGuide(LevelsGuide.STARTER)));

        // Prefix Emblems
        layout.slot(43, (s, c) -> ItemStacks.item(Material.NAME_TAG, 1, Text.key("gui_sbmenu.levels.main.emblems"),
                        Text.keyLines("gui_sbmenu.levels.main.emblems.lore")),
                (click, c) -> c.player().openView(new GUIEmblems()));

        // Level progression slots
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockLevelRequirement currentLevel = player.getSkyBlockExperience().getLevel();
        List<SkyBlockLevelRequirement> levels = new ArrayList<>();
        levels.add(currentLevel);
        for (int i = 0; i < 5; i++) {
            if (currentLevel.getNextLevel() == null) break;
            levels.add(currentLevel.getNextLevel());
            currentLevel = currentLevel.getNextLevel();
        }

        int unlockedLevel = player.getSkyBlockExperience().getLevel().asInt();
        for (int i = 0; i < 5 && i < levels.size(); i++) {
            SkyBlockLevelRequirement level = levels.get(i);
            if (level == null) break;
            int slot = 19 + i;

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                List<Text> lore = new ArrayList<>();
                Material material = level.isMilestone() ? Material.RED_STAINED_GLASS : Material.RED_STAINED_GLASS_PANE;

                if (unlockedLevel == level.asInt()) {
                    lore.add(Text.key("gui_sbmenu.levels.main.your_level"));
                    lore.add(Text.literal(" "));
                    material = level.isMilestone() ? Material.LIME_STAINED_GLASS : Material.LIME_STAINED_GLASS_PANE;
                } else if (unlockedLevel + 1 == level.asInt()) {
                    lore.add(Text.key("gui_sbmenu.levels.main.next_level"));
                    lore.add(Text.literal(" "));
                    material = level.isMilestone() ? Material.YELLOW_STAINED_GLASS : Material.YELLOW_STAINED_GLASS_PANE;
                }

                lore.add(Text.key("gui_sbmenu.levels.main.rewards"));
                level.getUnlocks().forEach(unlock -> lore.addAll(unlock.getDisplay(p, level.asInt())));
                lore.add(Text.literal(" "));
                if (unlockedLevel == level.asInt()) {
                    lore.add(Text.key("gui_sbmenu.levels.main.unlocked"));
                    lore.add(Text.literal(" "));
                }
                lore.add(Text.key("gui_sbmenu.levels.main.click_to_view"));

                return ItemStacks.item(material, 1, Text.key("gui_sbmenu.levels.main.level", level.asInt()), lore);
            }, (click, c) -> c.player().openView(new GUISkyBlockLevel(level)));
        }

        // Next Milestone Level
        SkyBlockLevelRequirement currentMilestone = player.getSkyBlockExperience().getLevel().getNextMilestoneLevel();
        if (currentMilestone != null) {
            layout.slot(30, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_sbmenu.levels.main.milestone"));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_sbmenu.levels.main.rewards"));
                currentMilestone.getUnlocks().forEach(unlock -> lore.addAll(unlock.getDisplay(p, currentMilestone.asInt())));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_sbmenu.levels.main.xp_left",
                        (long) (currentMilestone.getCumulativeExperience() - p.getSkyBlockExperience().getTotalXP()),
                        (int) (p.getSkyBlockExperience().getTotalXP() / currentMilestone.getCumulativeExperience() * 100)));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_sbmenu.levels.main.click_to_view"));

                return ItemStacks.item(Material.PURPLE_STAINED_GLASS_PANE, 1,
                        Text.key("gui_sbmenu.levels.main.level", currentMilestone.asInt()), lore);
            }, (click, c) -> c.player().openView(new GUISkyBlockLevel(currentMilestone)));
        }
    }
}
