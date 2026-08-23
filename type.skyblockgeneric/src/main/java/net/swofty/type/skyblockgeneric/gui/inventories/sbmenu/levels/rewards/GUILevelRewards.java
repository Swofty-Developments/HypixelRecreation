package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.rewards;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.levels.CustomLevelAward;
import net.swofty.type.skyblockgeneric.levels.SkyBlockEmblems;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.levels.causes.LevelCause;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUILevelRewards extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.levels.rewards.title", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);

        // Feature Rewards
        layout.slot(11, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
            List<Text> lore = new ArrayList<>();
            lore.addAll(Text.keyLines("gui_sbmenu.levels.rewards.feature.lore"));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.levels.rewards.next_reward"));

            Map.Entry<Integer, List<CustomLevelAward>> nextAward = CustomLevelAward.getNextReward(experience.getLevel().asInt());
            if (nextAward == null) {
                lore.add(Text.key("gui_sbmenu.levels.rewards.no_more"));
            } else {
                nextAward.getValue().forEach(award -> lore.add(Text.of("<7>{}", award.getDisplay())));
                lore.add(Text.key("gui_sbmenu.levels.rewards.at_level", nextAward.getKey()));
            }

            lore.add(Text.literal(" "));
            lore.addAll(getAsDisplay(CustomLevelAward.getFromLevel(experience.getLevel().asInt()).size(),
                    CustomLevelAward.getTotalLevelAwards()));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.levels.rewards.click_to_view"));

            return ItemStacks.item(Material.NETHER_STAR, 1, Text.key("gui_sbmenu.levels.rewards.feature"), lore);
        }, (click, c) -> c.player().openView(new GUILevelFeatureRewards()));

        // Prefix Color Rewards
        layout.slot(12, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<Text> lore = new ArrayList<>();
            lore.addAll(Text.keyLines("gui_sbmenu.levels.rewards.prefix.lore"));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.levels.rewards.next_reward"));

            Map.Entry<SkyBlockLevelRequirement, String> nextPrefix = player.getSkyBlockExperience()
                    .getLevel().getNextPrefixChange();
            if (nextPrefix == null) {
                lore.add(Text.key("gui_sbmenu.levels.rewards.no_more"));
            } else {
                lore.add(Text.parse(nextPrefix.getValue() + nextPrefix.getKey().getPrefixDisplay()));
                lore.add(Text.key("gui_sbmenu.levels.rewards.at_level", nextPrefix.getKey().asInt()));
            }
            lore.add(Text.literal(" "));
            lore.addAll(getAsDisplay(
                    player.getSkyBlockExperience().getLevel().getPreviousPrefixChanges().size(),
                    SkyBlockLevelRequirement.getAllPrefixChanges().size()
            ));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.levels.rewards.click_to_view"));

            return ItemStacks.item(Material.GRAY_DYE, 1, Text.key("gui_sbmenu.levels.rewards.prefix"), lore);
        }, (click, c) -> c.player().openView(new GUILevelPrefixRewards()));

        // Emblem Rewards
        layout.slot(13, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<Text> lore = new ArrayList<>();
            lore.addAll(Text.keyLines("gui_sbmenu.levels.rewards.emblem.lore"));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.levels.rewards.next_reward"));

            List<SkyBlockEmblems.SkyBlockEmblem> levelEmblems = SkyBlockEmblems.getEmblemsWithLevelCause();
            SkyBlockEmblems.SkyBlockEmblem nextEmblem = null;
            for (SkyBlockEmblems.SkyBlockEmblem emblem : levelEmblems) {
                if (player.getSkyBlockExperience().hasExperienceFor(emblem.cause())) continue;
                nextEmblem = emblem;
                break;
            }

            if (nextEmblem == null) {
                lore.add(Text.key("gui_sbmenu.levels.rewards.no_more"));
            } else {
                lore.add(Text.of("<f>{} ", nextEmblem.displayName()).append(nextEmblem.emblem()));
                lore.add(Text.key("gui_sbmenu.levels.rewards.at_level", ((LevelCause) nextEmblem.cause()).getLevel()));
            }

            lore.add(Text.literal(" "));
            lore.addAll(getAsDisplay(
                    player.getSkyBlockExperience().getOfType(LevelCause.class).size(),
                    levelEmblems.size()
            ));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.levels.rewards.click_to_view"));

            return ItemStacks.item(Material.NAME_TAG, 1, Text.key("gui_sbmenu.levels.rewards.emblem"), lore);
        }, (click, c) -> c.player().openView(new GUILevelEmblemRewards()));

        // Statistic Rewards
        layout.slot(14, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            SkyBlockLevelRequirement nextLevel = player.getSkyBlockExperience().getLevel().getNextLevel();

            return ItemStacks.item(Material.DIAMOND_HELMET, 1, Text.key("gui_sbmenu.levels.rewards.statistic"),
                    Text.keyLines("gui_sbmenu.levels.rewards.statistic.lore",
                            nextLevel == null ? Text.of("<c>MAX") : Text.literal(String.valueOf(nextLevel.asInt()))));
        });
    }

    public static int getTotalAwards() {
        int amountToReturn = 0;
        amountToReturn += CustomLevelAward.getTotalLevelAwards();
        amountToReturn += SkyBlockLevelRequirement.getAllPrefixChanges().size();
        amountToReturn += SkyBlockEmblems.getEmblemsWithLevelCause().size();
        return amountToReturn;
    }

    public static int getUnlocked(SkyBlockPlayer player) {
        int amountToReturn = 0;
        amountToReturn += CustomLevelAward.getFromLevel(player.getSkyBlockExperience().getLevel().asInt()).size();
        amountToReturn += player.getSkyBlockExperience().getLevel().getPreviousPrefixChanges().size();
        amountToReturn += player.getSkyBlockExperience().getOfType(LevelCause.class).size();
        return amountToReturn;
    }

    public static List<Text> getAsDisplay(int unlocked, int total) {
        List<Text> toReturn = new ArrayList<>();

        String unlockedPercentage = String.format("%.2f", (unlocked / (double) total) * 100);
        toReturn.add(Text.key("gui_sbmenu.levels.rewards.unlocked", unlockedPercentage));

        String baseLoadingBar = "─────────────────";
        int maxBarLength = baseLoadingBar.length();
        int completedLength = Math.min((int) ((unlocked / (double) total) * maxBarLength), maxBarLength);

        String completedDashes = baseLoadingBar.substring(0, completedLength);
        String uncompletedDashes = baseLoadingBar.substring(completedLength);

        toReturn.add(Text.of("<b><m>{}<7>{}<r> <e>{}<6>/<e>{}", completedDashes, uncompletedDashes, unlocked, total));
        return toReturn;
    }
}
