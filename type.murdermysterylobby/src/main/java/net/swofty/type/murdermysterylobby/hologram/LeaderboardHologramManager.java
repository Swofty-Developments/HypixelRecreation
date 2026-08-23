package net.swofty.type.murdermysterylobby.hologram;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.swofty.commons.murdermystery.*;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointMurderMysteryLeaderboardPreferences;
import net.swofty.type.generic.data.handlers.MurderMysteryDataHandler;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.murdermysterylobby.gui.GUILeaderboardSettings;
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
    private static final Pos SETTINGS_HOLOGRAM_POS = new Pos(33, 68.2, -23);

    public record PlayerLeaderboardState(
            MurderMysteryLeaderboardPeriod period,
            MurderMysteryLeaderboardMode mode,
            MurderMysteryLeaderboardView view,
            MurderMysteryTextAlignment textAlignment
    ) {
        public static PlayerLeaderboardState defaults() {
            return new PlayerLeaderboardState(
                    MurderMysteryLeaderboardPeriod.WEEKLY,
                    MurderMysteryLeaderboardMode.CLASSIC,
                    MurderMysteryLeaderboardView.TOP_10,
                    MurderMysteryTextAlignment.CENTER
            );
        }
    }

    public static void initialize(Instance instance) {
        if (initialized) return;
        initialized = true;

        for (MurderMysteryLeaderboardHologram hologram : MurderMysteryLeaderboardHologram.values()) {
            createClickZone(instance, hologram);
        }
        createSettingsClickZone(instance);

        Logger.info("Murder Mystery LeaderboardHologramManager initialized with {} holograms + settings", MurderMysteryLeaderboardHologram.values().length);
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

    private static void createClickZone(Instance instance, MurderMysteryLeaderboardHologram hologram) {
        Pos pos = hologram.getPosition();

        InteractionEntity clickZone = new InteractionEntity(3.5f, 9.0f, (player, event) -> {
            if (player instanceof HypixelPlayer hypixelPlayer) {
                handleClick(hypixelPlayer, hologram, event);
            }
        });
        clickZone.setInstance(instance, pos);
        clickZones.add(clickZone);
    }

    public static void handleClick(HypixelPlayer player, MurderMysteryLeaderboardHologram hologram, PlayerEntityInteractEvent event) {
        new GUILeaderboardSettings().open(player);
    }

    public static PlayerLeaderboardState getState(UUID playerUuid) {
        return playerStates.getOrDefault(playerUuid, PlayerLeaderboardState.defaults());
    }

    public static void setState(UUID playerUuid, PlayerLeaderboardState state) {
        playerStates.put(playerUuid, state);

        // Save preferences to data handler
        MurderMysteryDataHandler handler = MurderMysteryDataHandler.murderMysteryCache.get(playerUuid);
        if (handler != null) {
            MurderMysteryLeaderboardPreferences prefs = new MurderMysteryLeaderboardPreferences(
                    state.period(),
                    state.mode(),
                    state.view(),
                    state.textAlignment()
            );
            handler.get(MurderMysteryDataHandler.Data.LEADERBOARD_PREFS, DatapointMurderMysteryLeaderboardPreferences.class)
                    .setValue(prefs);
        }
    }

    public static void spawnHologramsForPlayer(HypixelPlayer player) {
        // Load preferences from data handler
        MurderMysteryDataHandler handler = MurderMysteryDataHandler.getUser(player);
        if (handler != null) {
            MurderMysteryLeaderboardPreferences prefs = handler.get(
                    MurderMysteryDataHandler.Data.LEADERBOARD_PREFS,
                    DatapointMurderMysteryLeaderboardPreferences.class
            ).getValue();
            playerStates.put(player.getUuid(), new PlayerLeaderboardState(
                    prefs.period(),
                    prefs.mode(),
                    prefs.view(),
                    prefs.textAlignment()
            ));
        }

        for (MurderMysteryLeaderboardHologram hologram : MurderMysteryLeaderboardHologram.values()) {
            refreshHologramForPlayer(player, hologram);
        }
        refreshSettingsHologramForPlayer(player);

        for (InteractionEntity clickZone : clickZones) {
            clickZone.addViewer(player);
        }
    }

    public static void refreshHologramForPlayer(HypixelPlayer player, MurderMysteryLeaderboardHologram hologram) {
        PlayerLeaderboardState state = getState(player.getUuid());
        String[] lines = serialize(hologram.getHologramLines(player, state));

        PlayerHolograms.ExternalPlayerHologram externalHologram = PlayerHolograms.ExternalPlayerHologram.builder()
                .pos(hologram.getPosition())
                .text(lines)
                .player(player)
                .instance(player.getInstance())
                .spacing(0.42)
                .displayFunction(p -> {
                    PlayerLeaderboardState s = getState(p.getUuid());
                    return serialize(hologram.getHologramLines(p, s));
                })
                .build();

        PlayerHolograms.removeExternalPlayerHologramsAt(player, hologram.getPosition());
        PlayerHolograms.addExternalPlayerHologram(externalHologram);
    }

    public static void refreshSettingsHologramForPlayer(HypixelPlayer player) {
        PlayerLeaderboardState state = getState(player.getUuid());
        String[] lines = serialize(getSettingsHologramLines(state));

        PlayerHolograms.ExternalPlayerHologram externalHologram = PlayerHolograms.ExternalPlayerHologram.builder()
                .pos(SETTINGS_HOLOGRAM_POS)
                .text(lines)
                .player(player)
                .instance(player.getInstance())
                .spacing(0.42)
                .displayFunction(p -> {
                    PlayerLeaderboardState s = getState(p.getUuid());
                    return serialize(getSettingsHologramLines(s));
                })
                .build();

        PlayerHolograms.removeExternalPlayerHologramsAt(player, SETTINGS_HOLOGRAM_POS);
        PlayerHolograms.addExternalPlayerHologram(externalHologram);
    }

    private static List<Text> getSettingsHologramLines(PlayerLeaderboardState state) {
        String viewDisplay = state.view() == MurderMysteryLeaderboardView.TOP_10 ? "Top 10" : "Players Near";
        return List.of(
                Text.of("<b><n>Leaderboard Settings"),
                Text.of("<7>Mode: <a>{}", state.mode().getDisplayName()),
                Text.of("<7>Time: <a>{}", state.period().getDisplayName()),
                Text.of("<7>View: <a>{}", viewDisplay),
                Text.of("<7>Players: <a>All"),
                Text.of("<6>Click to change settings!")
        );
    }

    private static String[] serialize(List<Text> lines) {
        String[] serialized = new String[lines.size()];
        for (int index = 0; index < lines.size(); index++) {
            serialized[index] = lines.get(index).serialize();
        }
        return serialized;
    }

    public static void refreshAllHologramsForPlayer(HypixelPlayer player) {
        for (MurderMysteryLeaderboardHologram hologram : MurderMysteryLeaderboardHologram.values()) {
            refreshHologramForPlayer(player, hologram);
        }
        refreshSettingsHologramForPlayer(player);
    }

    public static void removeHologramsForPlayer(HypixelPlayer player) {
        for (MurderMysteryLeaderboardHologram hologram : MurderMysteryLeaderboardHologram.values()) {
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
