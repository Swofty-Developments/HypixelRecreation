package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.skills;

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
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.GUIBestiary;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.skills.GUIHeartOfTheMountain;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.skill.SkillCategory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUISkillCategory extends StatelessView {
    private static final int[] DISPLAY_SLOTS = {
            9, 18, 27, 28, 29, 20, 11, 2, 3, 4, 13, 22, 31, 32, 33, 24, 15, 6, 7, 8, 17, 26, 35, 44, 53
    };

    private final SkillCategories category;
    private final int page;

    public GUISkillCategory(SkillCategories category, int page) {
        this.category = category;
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key("gui_sbmenu.skills.category.title", category.toString()),
                InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointSkills.PlayerSkills skills = player.getSkills();
        int level = skills.getCurrentLevel(category);
        Integer nextLevel = skills.getNextLevel(category);

        // Bestiary button for combat
        if (category == SkillCategories.COMBAT && player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BRAMASS_BEASTSLAYER)) {
            layout.slot(39, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                List<String> raw = new ArrayList<>();
                p.getBestiaryData().getTotalDisplay(raw);

                List<Text> lore = new ArrayList<>();
                raw.forEach(entry -> lore.add(Text.parse(entry)));
                lore.add(Text.empty());
                lore.add(Text.key("gui_sbmenu.skills.category.bestiary.click"));

                return ItemStacks.item(Material.WRITTEN_BOOK, 1,
                        Text.key("gui_sbmenu.skills.category.bestiary"), lore);
            }, (click, c) -> c.push(new GUIBestiary()));
        }

        // Skill info
        layout.slot(0, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            List<String> baseLore = new ArrayList<>(category.asCategory().getDescription());
            baseLore.add(" ");

            Integer next = p.getSkills().getNextLevel(category);
            if (next != null) {
                p.getSkills().getDisplay(baseLore, category, category.asCategory().getReward(next).requirement(),
                        Text.of("<7>Progress to Level {:roman}: ", next).serialize());
            }

            List<Text> lore = new ArrayList<>();
            baseLore.forEach(entry -> lore.add(Text.parse(entry)));
            if (next == null) {
                lore.add(Text.key("gui_sbmenu.skills.category.max_level"));
            }

            lore.add(Text.literal(" "));
            lore.addAll(Text.keyLines("gui_sbmenu.skills.category.increase_level", category.toString()));

            return ItemStacks.item(category.asCategory().getDisplayIcon(), 1,
                    Text.of("<a>{} Skill", category.toString()), lore);
        });

        if (category == SkillCategories.MINING) {
            layout.slot(47, ItemStacks.item(Material.REDSTONE_BLOCK, 1, """
                    <5>Heart of the Mountain
                    <7>Improve your mining abilities with
                    <7>perks powered by Powder and Tokens.

                    <e>Click to view!"""),
                    (_, c) -> c.push(new GUIHeartOfTheMountain(false)));
        }

        List<SkillCategory.SkillReward> rewards = List.of(category.asCategory().getRewards());

        // Next page button
        if (rewards.size() > (page + 1) * DISPLAY_SLOTS.length) {
            layout.slot(50, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                            Text.key("gui_sbmenu.skills.category.next_page"),
                            Text.keyLines("gui_sbmenu.skills.category.next_page.lore")),
                    (click, c) -> c.replace(new GUISkillCategory(category, page + 1)));
        }

        // Previous page button
        if (page > 0) {
            layout.slot(48, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                            Text.key("gui_sbmenu.skills.category.previous_page"),
                            Text.keyLines("gui_sbmenu.skills.category.previous_page.lore")),
                    (click, c) -> c.replace(new GUISkillCategory(category, page - 1)));
        }

        // Rewards
        int startIndex = page * DISPLAY_SLOTS.length;
        int endIndex = Math.min(rewards.size(), (page + 1) * DISPLAY_SLOTS.length);
        List<SkillCategory.SkillReward> pageRewards = rewards.subList(startIndex, endIndex);

        for (int i = 0; i < pageRewards.size() && i < DISPLAY_SLOTS.length; i++) {
            SkillCategory.SkillReward reward = pageRewards.get(i);
            int slot = DISPLAY_SLOTS[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                int currentLevel = p.getSkills().getCurrentLevel(category);
                List<String> raw = new ArrayList<>();
                reward.getDisplay(raw);

                List<Text> lore = new ArrayList<>();
                raw.forEach(entry -> lore.add(Text.parse(entry)));

                Material icon;
                String namePattern;
                if (currentLevel >= reward.level()) {
                    icon = Material.LIME_STAINED_GLASS_PANE;
                    namePattern = "<a>{} Level {:roman}";
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_sbmenu.skills.category.unlocked"));
                } else if ((currentLevel + 1) == reward.level()) {
                    icon = Material.YELLOW_STAINED_GLASS_PANE;
                    namePattern = "<e>{} Level {:roman}";
                    lore.add(Text.literal(" "));
                    List<String> progress = new ArrayList<>();
                    p.getSkills().getDisplay(progress, category, reward.requirement(), "<7>Progress: ");
                    progress.forEach(entry -> lore.add(Text.parse(entry)));
                } else {
                    icon = Material.RED_STAINED_GLASS_PANE;
                    namePattern = "<c>{} Level {:roman}";
                }

                return ItemStacks.item(icon, 1,
                        Text.of(namePattern, category.toString(), reward.level()), lore);
            });
        }
    }
}
