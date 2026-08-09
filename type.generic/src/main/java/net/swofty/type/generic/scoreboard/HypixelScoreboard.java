package net.swofty.type.generic.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HypixelScoreboard {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private final Map<UUID, Sidebar> sidebarCache = new HashMap<>();
    private final Map<UUID, List<Component>> lineCache = new HashMap<>();

    private static String lineId(int index) {
        return "line_" + index;
    }

    public void createScoreboard(Player player, Component title) {
        if (sidebarCache.containsKey(player.getUuid())) return;

        Sidebar sidebar = new Sidebar(title);
        sidebar.addViewer(player);
        sidebarCache.put(player.getUuid(), sidebar);
        lineCache.put(player.getUuid(), new ArrayList<>());
    }

    public void updateTitle(Player player, Component title) {
        Sidebar sidebar = sidebarCache.get(player.getUuid());
        if (sidebar == null) return;

        sidebar.setTitle(title);
    }

    public void updateLines(Player player, List<Component> lines) {
        Sidebar sidebar = sidebarCache.get(player.getUuid());
        if (sidebar == null) return;

        List<Component> cached = lineCache.getOrDefault(player.getUuid(), new ArrayList<>());
        if (cached.equals(lines)) return;

        int oldCount = cached.size();
        int newCount = lines.size();
        int commonCount = Math.min(oldCount, newCount);

        for (int i = 0; i < commonCount; i++) {
            if (!cached.get(i).equals(lines.get(i))) {
                sidebar.updateLineContent(lineId(i), lines.get(i));
            }
            int oldScore = oldCount - 1 - i;
            int newScore = newCount - 1 - i;
            if (oldScore != newScore) {
                sidebar.updateLineScore(lineId(i), newScore);
            }
        }

        for (int i = oldCount; i < newCount; i++) {
            sidebar.createLine(new Sidebar.ScoreboardLine(
                lineId(i),
                lines.get(i),
                newCount - 1 - i,
                Sidebar.NumberFormat.blank()
            ));
        }

        for (int i = newCount; i < oldCount; i++) {
            sidebar.removeLine(lineId(i));
        }

        lineCache.put(player.getUuid(), new ArrayList<>(lines));
    }

    public void removeScoreboard(Player player) {
        Sidebar sidebar = sidebarCache.remove(player.getUuid());
        if (sidebar != null) {
            sidebar.removeViewer(player);
        }
        lineCache.remove(player.getUuid());
    }

    public boolean hasScoreboard(Player player) {
        return sidebarCache.containsKey(player.getUuid());
    }

    public Sidebar getSidebar(Player player) {
        return sidebarCache.get(player.getUuid());
    }

    public static Component getSidebarName(String baseText, int counter, boolean guest) {
        Component suffix = guest
            ? Component.text(" GUEST", NamedTextColor.GREEN, TextDecoration.BOLD)
            : Component.empty();

        if (baseText == null || baseText.isEmpty()) {
            return suffix;
        }

        int highlightedIndex = Math.max(0, Math.min(baseText.length() - 1, counter - 1));
        if (counter > 0 && counter <= baseText.length()) {
            return Component.empty()
                .append(Component.text(baseText.substring(0, highlightedIndex), NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text(String.valueOf(baseText.charAt(highlightedIndex)), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(baseText.substring(highlightedIndex + 1), NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(suffix);
        }

        if ((counter >= baseText.length() + 1 && counter <= baseText.length() + 11)
            || (counter >= baseText.length() + 17 && counter <= baseText.length() + 21)) {
            return Component.empty()
                .append(Component.text(baseText, NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(suffix);
        }

        return Component.empty()
            .append(Component.text(baseText, NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(suffix);
    }

    public static Component legacy(String text) {
        return LEGACY.deserialize(text == null ? "" : text);
    }

    public static Component animatedSidebarName(
        String baseText,
        int counter,
        NamedTextColor prefixColor,
        NamedTextColor highlightColor,
        NamedTextColor suffixColor,
        NamedTextColor steadyColor,
        NamedTextColor fallbackColor,
        int highlightEndInclusive,
        int steadyStartOne,
        int steadyEndOne,
        int steadyStartTwo,
        int steadyEndTwo,
        Component ending
    ) {
        Component suffix = ending == null ? Component.empty() : ending;
        if (baseText == null || baseText.isEmpty()) {
            return suffix;
        }

        if (counter > 0 && counter <= highlightEndInclusive && counter <= baseText.length()) {
            int index = counter - 1;
            return Component.empty()
                .append(Component.text(baseText.substring(0, index), prefixColor, TextDecoration.BOLD))
                .append(Component.text(String.valueOf(baseText.charAt(index)), highlightColor, TextDecoration.BOLD))
                .append(Component.text(baseText.substring(index + 1), suffixColor, TextDecoration.BOLD))
                .append(suffix);
        }

        if ((counter >= steadyStartOne && counter <= steadyEndOne)
            || (steadyStartTwo >= 0 && counter >= steadyStartTwo && counter <= steadyEndTwo)) {
            return Component.empty()
                .append(Component.text(baseText, steadyColor, TextDecoration.BOLD))
                .append(suffix);
        }

        return Component.empty()
            .append(Component.text(baseText, fallbackColor, TextDecoration.BOLD))
            .append(suffix);
    }
}
