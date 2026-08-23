package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.itzkatze.thehypixelrecreationmod.mixin.BossHealthOverlayAccessor;
import gg.itzkatze.thehypixelrecreationmod.mixin.PlayerTabOverlayAccessor;
import gg.itzkatze.thehypixelrecreationmod.utils.StringUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class HudTracker {
    private final EventSink sink;

    private String lastSidebar;
    private String lastTab;
    private String lastBossbar;
    private int sidebarUpdates;

    HudTracker(EventSink sink) {
        this.sink = sink;
    }

    int sidebarUpdates() {
        return sidebarUpdates;
    }

    void reset() {
        lastSidebar = null;
        lastTab = null;
        lastBossbar = null;
    }

    void tick(Minecraft client) {
        trackSidebar(client);
        trackTab(client);
        trackBossbar(client);
    }

    private void trackSidebar(Minecraft client) {
        if (client.level == null) {
            return;
        }

        Scoreboard scoreboard = client.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) {
            if (lastSidebar != null) {
                lastSidebar = null;
                sink.emit("scoreboard", "sb:cleared", new JsonObject());
            }
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("objective", objective.getName());
        payload.addProperty("title", StringUtility.toLegacyString(objective.getDisplayName()));
        payload.add("titleJson", CaptureJson.componentTree(objective.getDisplayName()));

        JsonArray lines = new JsonArray();
        List<PlayerScoreEntry> entries = scoreboard.listPlayerScores(objective).stream()
                .sorted(Comparator.comparingInt(PlayerScoreEntry::value).reversed())
                .toList();
        for (PlayerScoreEntry entry : entries) {
            Component rendered = renderEntry(scoreboard, entry);
            JsonObject line = new JsonObject();
            line.addProperty("owner", entry.owner());
            line.addProperty("score", entry.value());
            line.addProperty("text", StringUtility.toLegacyString(rendered));
            lines.add(line);
        }
        payload.add("lines", lines);

        String fingerprint = payload.toString();
        if (fingerprint.equals(lastSidebar)) {
            return;
        }
        lastSidebar = fingerprint;
        sidebarUpdates++;
        sink.emit("scoreboard", "sb:sidebar", payload);
    }

    private void trackTab(Minecraft client) {
        if (client.getConnection() == null) {
            return;
        }

        PlayerTabOverlay overlay = client.gui.hud.getTabList();
        PlayerTabOverlayAccessor accessor = (PlayerTabOverlayAccessor) overlay;

        JsonObject payload = new JsonObject();
        payload.addProperty("header", accessor.recreation$getHeader() == null
                ? null
                : StringUtility.toLegacyString(accessor.recreation$getHeader()));
        payload.addProperty("footer", accessor.recreation$getFooter() == null
                ? null
                : StringUtility.toLegacyString(accessor.recreation$getFooter()));

        JsonArray players = new JsonArray();
        StringBuilder stable = new StringBuilder();
        List<PlayerInfo> listed = client.getConnection().getListedOnlinePlayers().stream()
                .sorted(Comparator.comparingInt(PlayerInfo::getTabListOrder)
                        .thenComparing(info -> info.getProfile().name(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        for (PlayerInfo info : listed) {
            String display = StringUtility.toLegacyString(overlay.getNameForDisplay(info));
            JsonObject entry = new JsonObject();
            entry.addProperty("name", info.getProfile().name());
            entry.addProperty("uuid", String.valueOf(info.getProfile().id()));
            entry.addProperty("display", display);
            entry.addProperty("latency", info.getLatency());
            entry.addProperty("gameMode", info.getGameMode().name());
            entry.addProperty("order", info.getTabListOrder());
            players.add(entry);
            stable.append(display).append('|');
        }
        payload.add("players", players);

        String fingerprint = payload.get("header") + "/" + payload.get("footer") + "/" + stable;
        if (fingerprint.equals(lastTab)) {
            return;
        }
        lastTab = fingerprint;
        sink.emit("tab", "tab:snapshot", payload);
    }

    private void trackBossbar(Minecraft client) {
        Map<UUID, LerpingBossEvent> events =
                ((BossHealthOverlayAccessor) client.gui.hud.getBossOverlay()).recreation$getEvents();

        JsonArray bars = new JsonArray();
        StringBuilder fingerprint = new StringBuilder();
        for (Map.Entry<UUID, LerpingBossEvent> entry : events.entrySet()) {
            LerpingBossEvent event = entry.getValue();
            JsonObject bar = new JsonObject();
            bar.addProperty("id", entry.getKey().toString());
            bar.addProperty("name", StringUtility.toLegacyString(event.getName()));
            bar.add("nameJson", CaptureJson.componentTree(event.getName()));
            bar.addProperty("progress", CaptureJson.round(event.getProgress()));
            bar.addProperty("color", event.getColor().name());
            bar.addProperty("overlay", event.getOverlay().name());
            bars.add(bar);
            fingerprint.append(entry.getKey())
                    .append(StringUtility.toLegacyString(event.getName()))
                    .append(Math.round(event.getProgress() * 100.0f))
                    .append(event.getColor())
                    .append(event.getOverlay())
                    .append('|');
        }

        String key = fingerprint.toString();
        if (key.equals(lastBossbar)) {
            return;
        }
        lastBossbar = key;

        JsonObject payload = new JsonObject();
        payload.add("bars", bars);
        sink.emit("bossbar", "boss:snapshot", payload);
    }

    private static Component renderEntry(Scoreboard scoreboard, PlayerScoreEntry entry) {
        Component base = entry.display() != null ? entry.display() : Component.literal(entry.owner());
        PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
        if (team == null) {
            return base;
        }
        return Component.empty().append(team.getPlayerPrefix()).append(base).append(team.getPlayerSuffix());
    }
}
