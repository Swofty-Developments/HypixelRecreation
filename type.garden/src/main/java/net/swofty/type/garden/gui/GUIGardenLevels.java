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
import java.util.Map;

public class GUIGardenLevels extends StatelessView {
    private static final int[] LEVEL_SLOTS = {
        9, 18, 27, 28, 29,
        20, 11, 2, 3, 4,
        13, 22, 31, 32, 33
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Garden Levels", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        layout.slot(0, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            int level = GardenGuiSupport.core(player).getLevel();
            return ItemStackCreator.getStack(
                "§aGarden Levels",
                Material.SUNFLOWER,
                1,
                "§7Earn Garden experience by",
                "§7accepting visitors' offers and",
                "§7unlocking new milestones!",
                "",
                "§7Current level: §e" + StringUtility.getAsRomanNumeral(level),
                "§7Garden XP: §e" + StringUtility.commaify(GardenGuiSupport.core(player).getExperience()),
                "",
                "§8Increase your Garden Level to",
                "§8unlock new visitors, crops and more!"
            );
        });

        List<Map<String, Object>> levels = net.swofty.type.garden.GardenServices.levels().getLevels();
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int currentLevel = GardenGuiSupport.core(player).getLevel();
        for (int index = 0; index < Math.min(levels.size(), LEVEL_SLOTS.length); index++) {
            Map<String, Object> level = levels.get(index);
            int levelNumber = net.swofty.type.garden.config.GardenConfigRegistry.getInt(level, "level", index + 1);
            int slot = LEVEL_SLOTS[index];
            layout.slot(slot, buildLevelItem(levelNumber, currentLevel, level));
        }
    }

    private net.minestom.server.item.ItemStack.Builder buildLevelItem(int levelNumber, int currentLevel, Map<String, Object> level) {
        boolean unlocked = currentLevel > levelNumber;
        boolean current = currentLevel == levelNumber;
        String color = current ? "§e" : unlocked ? "§a" : "§c";
        Material material = current ? Material.DIAMOND_HOE : unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        List<String> lore = new ArrayList<>();
        lore.add("§7Rewards:");
        for (String reward : net.swofty.type.garden.GardenServices.levels().getRewardsForLevel(levelNumber)) {
            lore.add("  §8+§7" + reward);
        }
        lore.add("");
        if (current) {
            lore.add("§e§lCURRENT LEVEL");
        } else if (unlocked) {
            lore.add("§a§lUNLOCKED");
        } else {
            lore.add("§7Requires: §aGarden Level " + StringUtility.getAsRomanNumeral(levelNumber));
            lore.add("");
            lore.add("§c§lLOCKED");
        }
        return ItemStackCreator.getStack(
            color + "Garden Level " + StringUtility.getAsRomanNumeral(levelNumber),
            material,
            1,
            lore
        );
    }
}
