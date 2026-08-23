package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.skills;

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
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.skill.SkillCategory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUISkills extends StatelessView {
    private static final int[] DISPLAY_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 32, 33, 34
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.skills.main.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> ItemStacks.item(Material.DIAMOND_SWORD, 1,
                Text.key("gui_sbmenu.skills.main.info"),
                Text.keyLines("gui_sbmenu.skills.main.info.lore")));

        SkillCategories[] allCategories = SkillCategories.values();
        for (int i = 0; i < DISPLAY_SLOTS.length && i < allCategories.length; i++) {
            SkillCategories category = allCategories[i];
            SkillCategory skillCategory = category.asCategory();
            int slot = DISPLAY_SLOTS[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                List<Text> lore = new ArrayList<>();

                if (category == SkillCategories.CARPENTRY && !player.getMissionData().hasCompleted("give_wool_to_carpenter")) {
                    lore.addAll(Text.keyLines("gui_sbmenu.skills.main.carpentry_locked.lore"));
                } else {
                    List<String> textLore = new ArrayList<>(skillCategory.getDescription());
                    textLore.add(" ");

                    Integer nextLevel = player.getSkills().getNextLevel(category);

                    if (nextLevel != null) {
                        SkillCategory.SkillReward reward = skillCategory.getRewards()[nextLevel - 1];
                        player.getSkills().getDisplay(textLore, category, reward.requirement(),
                                Text.of("<7>Progress to Level {:roman}: ", nextLevel).serialize());
                        textLore.add(" ");
                        reward.getDisplay(textLore);
                    }

                    textLore.forEach(entry -> lore.add(Text.parse(entry)));

                    if (nextLevel == null) {
                        lore.add(Text.key("gui_sbmenu.skills.main.max_level"));
                    }

                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_sbmenu.skills.main.click_to_view"));
                }

                return ItemStacks.item(skillCategory.getDisplayIcon(), 1,
                        Text.of("<a>{} {:roman}", skillCategory.getName(),
                                player.getSkills().getCurrentLevel(category)),
                        lore);
            }, (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (category == SkillCategories.CARPENTRY && !player.getMissionData().hasCompleted("give_wool_to_carpenter")) return;
                c.push(new GUISkillCategory(category, 0));
            });
        }
    }
}
