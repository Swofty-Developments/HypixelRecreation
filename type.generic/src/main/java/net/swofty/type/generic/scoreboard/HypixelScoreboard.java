package net.swofty.type.generic.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextBody;
import net.swofty.type.generic.text.HypixelTextRenderer;
import net.swofty.type.generic.text.RenderContext;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HypixelScoreboard {
    private final Map<UUID, Sidebar> sidebarCache = new HashMap<>();
    private final Map<UUID, List<Component>> lineCache = new HashMap<>();

    private static String lineId(int index) {
        return "line_" + index;
    }

    private static Component render(Player player, Text text) {
        Component component = text.asComponent();
        return player instanceof HypixelPlayer viewer
                ? HypixelTextRenderer.render(component, RenderContext.of(viewer))
                : component;
    }

    private static List<Component> render(Player player, List<Text> texts) {
        List<Component> out = new ArrayList<>(texts.size());
        for (Text text : texts) {
            out.add(render(player, text));
        }
        return out;
    }

    public void update(Player player, Text title, TextBody body) {
        update(player, title, body.render());
    }

    public void update(Player player, Text title, List<Text> lines) {
        createScoreboard(player, title);
        updateLines(player, lines);
        updateTitle(player, title);
    }

    public void createScoreboard(Player player, Text title) {
        if (sidebarCache.containsKey(player.getUuid())) return;

        Sidebar sidebar = new Sidebar(render(player, title));
        sidebar.addViewer(player);
        sidebarCache.put(player.getUuid(), sidebar);
        lineCache.put(player.getUuid(), new ArrayList<>());
    }

    public void updateTitle(Player player, Text title) {
        Sidebar sidebar = sidebarCache.get(player.getUuid());
        if (sidebar == null) return;

        sidebar.setTitle(render(player, title));
    }

    public void updateLines(Player player, TextBody body) {
        updateLines(player, body.render());
    }

    public void updateLines(Player player, List<Text> rawLines) {
        Sidebar sidebar = sidebarCache.get(player.getUuid());
        if (sidebar == null) return;

        List<Component> lines = render(player, rawLines);
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
}
