package net.swofty.type.skywarslobby.hologram;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skywars.*;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsLeaderboardPreferences;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarslobby.gui.GUILeaderboardSettings;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardHologramManager {
    private static final Map<UUID, PlayerLeaderboardState> playerStates = new ConcurrentHashMap<>();
    private static final List<InteractionEntity> clickZones = new ArrayList<>();
    private static boolean initialized = false;
    private static final Pos SETTINGS_HOLOGRAM_POS = new Pos(36.5, 66, -29);

    public record PlayerLeaderboardState(
            SkywarsLeaderboardPeriod period,
            SkywarsLeaderboardMode mode,
            SkywarsLeaderboardView view,
            SkywarsTextAlignment textAlignment
    ) {
        public static PlayerLeaderboardState defaults() {
            return new PlayerLeaderboardState(
                    SkywarsLeaderboardPeriod.WEEKLY,
                    SkywarsLeaderboardMode.ALL,
                    SkywarsLeaderboardView.TOP_10,
                    SkywarsTextAlignment.CENTER
            );
        }
    }

    public static void initialize(Instance instance) {
        if (initialized) return;
        initialized = true;

        for (SkywarsLeaderboardHologram hologram : SkywarsLeaderboardHologram.values()) {
            createClickZone(instance, hologram);
        }
        createSettingsClickZone(instance);

        Logger.info("SkyWars LeaderboardHologramManager initialized with {} holograms + settings", SkywarsLeaderboardHologram.values().length);
    }

    private static void createSettingsClickZone(Instance instance) {
        InteractionEntity clickZone = new InteractionEntity(2.5f, 3.0f, (player, event) -> {
            if (player instanceof HypixelPlayer hypixelPlayer) {
                new GUILeaderboardSettings().open(hypixelPlayer);
            }
        });
        clickZone.setInstance(instance, SETTINGS_HOLOGRAM_POS.add(0, 1.5, 0));
        clickZones.add(clickZone);
    }

    private static void createClickZone(Instance instance, SkywarsLeaderboardHologram hologram) {
        Pos pos = hologram.getPosition();

        InteractionEntity clickZone = new InteractionEntity(3.5f, 9.0f, (player, event) -> {
            if (player instanceof HypixelPlayer hypixelPlayer) {
                handleClick(hypixelPlayer, hologram, event);
            }
        });
        clickZone.setInstance(instance, pos);
        clickZones.add(clickZone);
    }

    public static void handleClick(HypixelPlayer player, SkywarsLeaderboardHologram hologram, PlayerEntityInteractEvent event) {
        new GUILeaderboardSettings().open(player);
    }

    public static PlayerLeaderboardState getState(UUID playerUuid) {
        return playerStates.getOrDefault(playerUuid, PlayerLeaderboardState.defaults());
    }

    public static void setState(UUID playerUuid, PlayerLeaderboardState state) {
        playerStates.put(playerUuid, state);

        // Save preferences to data handler
        SkywarsDataHandler handler = SkywarsDataHandler.skywarsCache.get(playerUuid);
        if (handler != null) {
            SkywarsLeaderboardPreferences prefs = new SkywarsLeaderboardPreferences(
                    state.period(),
                    state.mode(),
                    state.view(),
                    state.textAlignment()
            );
            handler.get(SkywarsDataHandler.Data.LEADERBOARD_PREFS, DatapointSkywarsLeaderboardPreferences.class)
                    .setValue(prefs);
        }
    }

    public static void spawnHologramsForPlayer(HypixelPlayer player) {
        // Load preferences from data handler
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        if (handler != null) {
            SkywarsLeaderboardPreferences prefs = handler.get(
                    SkywarsDataHandler.Data.LEADERBOARD_PREFS,
                    DatapointSkywarsLeaderboardPreferences.class
            ).getValue();
            playerStates.put(player.getUuid(), new PlayerLeaderboardState(
                    prefs.period(),
                    prefs.mode(),
                    prefs.view(),
                    prefs.textAlignment()
            ));
        }

        for (SkywarsLeaderboardHologram hologram : SkywarsLeaderboardHologram.values()) {
            refreshHologramForPlayer(player, hologram);
        }
        refreshSettingsHologramForPlayer(player);

        for (InteractionEntity clickZone : clickZones) {
            clickZone.addViewer(player);
        }
    }

    public static void refreshHologramForPlayer(HypixelPlayer player, SkywarsLeaderboardHologram hologram) {
        PlayerLeaderboardState state = getState(player.getUuid());
        String[] lines = hologram.getHologramLines(player, state);

        PlayerHolograms.ExternalPlayerHologram externalHologram = PlayerHolograms.ExternalPlayerHologram.builder()
                .pos(hologram.getPosition())
                .text(lines)
                .player(player)
                .instance(player.getInstance())
                .spacing(0.42)
                .displayFunction(p -> {
                    PlayerLeaderboardState s = getState(p.getUuid());
                    return hologram.getHologramLines(p, s);
                })
                .build();

        PlayerHolograms.removeExternalPlayerHologramsAt(player, hologram.getPosition());
        PlayerHolograms.addExternalPlayerHologram(externalHologram);
    }

    public static void refreshSettingsHologramForPlayer(HypixelPlayer player) {
        PlayerLeaderboardState state = getState(player.getUuid());
        String[] lines = getSettingsHologramLines(state);

        PlayerHolograms.ExternalPlayerHologram externalHologram = PlayerHolograms.ExternalPlayerHologram.builder()
                .pos(SETTINGS_HOLOGRAM_POS)
                .text(lines)
                .player(player)
                .instance(player.getInstance())
                .spacing(0.42)
                .displayFunction(p -> {
                    PlayerLeaderboardState s = getState(p.getUuid());
                    return getSettingsHologramLines(s);
                })
                .build();

        PlayerHolograms.removeExternalPlayerHologramsAt(player, SETTINGS_HOLOGRAM_POS);
        PlayerHolograms.addExternalPlayerHologram(externalHologram);
    }

    private static String[] getSettingsHologramLines(PlayerLeaderboardState state) {
        String viewDisplay = state.view() == SkywarsLeaderboardView.TOP_10 ? "Top 10" : "Players Near";
        return new String[] {
                "§b§nLeaderboard Settings",
                "§7Mode: §a" + state.mode().getDisplayName(),
                "§7Time: §a" + state.period().getDisplayName(),
                "§7View: §a" + viewDisplay,
                "§7Players: §aAll",
                "§6Click to change settings!"
        };
    }

    public static void refreshAllHologramsForPlayer(HypixelPlayer player) {
        for (SkywarsLeaderboardHologram hologram : SkywarsLeaderboardHologram.values()) {
            refreshHologramForPlayer(player, hologram);
        }
        refreshSettingsHologramForPlayer(player);
    }

    public static void removeHologramsForPlayer(HypixelPlayer player) {
        for (SkywarsLeaderboardHologram hologram : SkywarsLeaderboardHologram.values()) {
            PlayerHolograms.removeExternalPlayerHologramsAt(player, hologram.getPosition());
        }
        PlayerHolograms.removeExternalPlayerHologramsAt(player, SETTINGS_HOLOGRAM_POS);
        playerStates.remove(player.getUuid());
    }

    public static void shutdown() {
        for (InteractionEntity clickZone : clickZones) {
            clickZone.remove();
        }
        clickZones.clear();
        playerStates.clear();
        initialized = false;
    }
}
