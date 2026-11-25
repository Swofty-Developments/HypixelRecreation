package net.swofty.type.replayviewer.playback;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.swofty.type.game.replay.api.ReplayScoreboard;
import net.swofty.type.game.replay.model.ReplayEntityState;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.entity.ReplayEntityManager;
import net.swofty.type.replayviewer.util.ReplaySettingsUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReplayViewerProjection {
    private final Map<UUID, Integer> cameraTargets = new ConcurrentHashMap<>();
    private final Map<UUID, ReplayScoreboard> scoreboards = new ConcurrentHashMap<>();
    private final ReplayEntityManager entities;

    public ReplayViewerProjection(ReplayEntityManager entities) {
        this.entities = entities;
    }

    public void addScoreboard(Player viewer, ReplayScoreboard scoreboard) {
        scoreboard.create(viewer);
        scoreboards.put(viewer.getUuid(), scoreboard);
    }

    public void removeViewer(Player viewer) {
        cameraTargets.remove(viewer.getUuid());
        ReplayScoreboard scoreboard = scoreboards.remove(viewer.getUuid());
        if (scoreboard != null) scoreboard.remove(viewer);
    }

    public void updateScoreboards(ReplaySession session) {
        scoreboards.values().forEach(scoreboard -> scoreboard.update(session));
    }

    public void clearScoreboards(Iterable<Player> viewers) {
        for (Player viewer : viewers) {
            ReplayScoreboard scoreboard = scoreboards.remove(viewer.getUuid());
            if (scoreboard != null) scoreboard.remove(viewer);
        }
    }

    public void follow(Player viewer, int entityId) {
        cameraTargets.put(viewer.getUuid(), entityId);
        var entity = entities.getEntity(entityId);
        if (entity != null) {
            viewer.setGameMode(GameMode.SPECTATOR);
            viewer.spectate(entity);
        }
    }

    public void stopFollowing(Player viewer) {
        cameraTargets.remove(viewer.getUuid());
        viewer.stopSpectating();
        viewer.setGameMode(GameMode.ADVENTURE);
        viewer.setAllowFlying(true);
        viewer.setFlying(true);
    }

    public Integer cameraTarget(Player viewer) {
        return cameraTargets.get(viewer.getUuid());
    }

    public void reattachCameras(Iterable<Player> viewers) {
        for (Player viewer : viewers) {
            Integer targetId = cameraTargets.get(viewer.getUuid());
            var target = targetId == null ? null : entities.getEntity(targetId);
            if (target == null) {
                cameraTargets.remove(viewer.getUuid());
                viewer.stopSpectating();
                viewer.setGameMode(GameMode.ADVENTURE);
                viewer.setAllowFlying(true);
                viewer.setFlying(true);
            } else {
                viewer.setGameMode(GameMode.SPECTATOR);
                viewer.spectate(target);
            }
        }
    }

    public void applyEntityVisibility(Iterable<Player> viewers, Map<Integer, ReplayEntityState> states) {
        for (var entry : states.entrySet()) {
            var entity = entities.getEntity(entry.getKey());
            if (entity == null) continue;
            ReplayEntityState state = entry.getValue();
            for (Player viewer : viewers) {
                boolean show = state.lifecycle() != ReplayEntityState.Lifecycle.SPECTATOR
                        || state.player() != null && state.player().legitimateSpectator()
                        && (!(viewer instanceof HypixelPlayer player)
                        || ReplaySettingsUtil.getSettings(player).isShowSpectators());
                if (show) entity.addViewer(viewer);
                else entity.removeViewer(viewer);
            }
        }
    }
}
