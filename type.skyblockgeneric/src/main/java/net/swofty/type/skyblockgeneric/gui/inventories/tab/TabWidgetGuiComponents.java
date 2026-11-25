package net.swofty.type.skyblockgeneric.gui.inventories.tab;

import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistLocation;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistWidget;

final class TabWidgetGuiComponents {
    private TabWidgetGuiComponents() {
    }

    static <S> void preview(ViewLayout<S> layout, TablistLocation location) {
        String mode = location.display();
        layout.slot(3, ItemStackCreator.getStackHead("§a" + mode + " Widgets Preview", "e9b5cee460e7df86cecab6c9da79c16ebb14c5a2da06e0f11be0e01ca7d01271", 1,
                "§7This column is currently used to", "§7show information like online players", "§7and guests.", "", "§7Enable the third column to show", "§7widgets here instead."));
        layout.slot(4, ItemStackCreator.getStackHead("§a" + mode + " Widgets Preview", "d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8", 1,
                "§3⬛               §3§lInfo", "§8⬛§b§lArea: §7" + mode, "§8⬛§f Server: §8mini7AV", "§8⬛§f Gems: §a25", "§8⬛§f Fairy Souls: §d80§5/§d80", "§8⬛", "§8⬛§e§lProfile: §aKiwi", "§8⬛§f SB Level: §8[§a142§8] §b64§3/§b100 XP", "§8⬛§f Bank: §6221.8M", "§8⬛", "§8⬛§e§lPet:", "§8⬛§f §7[Lvl 41] §6Wolf", "§8⬛§f §e20,120.3§6/§e25.2k XP §6(79.8%)", "§8⬛", "§8⬛§c§lFire Sales: §f(1)"));
        layout.slot(5, ItemStackCreator.getStackHead("§a" + mode + " Widgets Preview", "d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8", 1,
                "§3⬛               §3§lInfo", "§8⬛§e§lElection: §b2d", "§8⬛§f §dDiana: §d|||||§f||||| §f(48%)", "§8⬛§f §aFinnegan: §a||||§f|||||| §f(35%)", "§8⬛", "§8⬛§e§lEvent: §6New Year Celebration", "§8⬛§f Ends In: §e10h", "§8⬛", "§8⬛§e§lSkills:", "§8⬛§f Farming 30: §a34.2%", "§8⬛§f Mining 50: §a16.2%", "§8⬛§f Combat 24: §a43.7%", "§8⬛", "§8⬛§e§lStats:", "§8⬛§f Speed: §f132", "§8⬛§f Strength: §c261", "§8⬛§f Crit Chance: §9117"));
    }

    static String previewLine(TablistWidget widget) {
        return switch (widget) {
            case GENERAL_INFO -> "§8⬛§b§lArea: §7Hub";
            case PROFILE -> "§8⬛§e§lProfile: §aKiwi";
            case PET -> "§8⬛§e§lPet: §7[Lvl 41] §6Wolf";
            case FIRE_SALES -> "§8⬛§c§lFire Sales: §f(1)";
            case ELECTION -> "§8⬛§e§lElection: §b2d";
            case EVENTS -> "§8⬛§e§lEvent: §6New Year Celebration";
            case SKILLS -> "§8⬛§e§lSkills: §fFarming 30";
            case STATS -> "§8⬛§e§lStats: §fSpeed: 132";
            default -> "§8⬛§e§l" + widget.display + ":";
        };
    }
}
