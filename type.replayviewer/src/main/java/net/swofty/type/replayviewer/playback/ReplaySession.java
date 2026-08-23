package net.swofty.type.replayviewer.playback;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.server.scoreboard.BelowNameTag;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.TeamColorUtil;
import net.swofty.type.game.replay.api.ReplayGameMetadata;
import net.swofty.type.game.replay.api.ReplayPlaybackContext;
import net.swofty.type.game.replay.api.ReplayScoreboard;
import net.swofty.type.game.replay.api.ReplayViewerAdapter;
import net.swofty.type.game.replay.model.ReplayMetadata;
import net.swofty.type.game.replay.model.ReplayParticipant;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.entity.ReplayEntity;
import net.swofty.type.replayviewer.entity.ReplayEntityManager;
import net.swofty.type.replayviewer.entity.ReplayPlayerEntity;
import net.swofty.type.replayviewer.playback.bedwars.BedWarsViewerMetadata;
import net.swofty.type.replayviewer.playback.display.DynamicTextManager;
import net.swofty.type.replayviewer.playback.npc.NpcReplayManager;
import net.swofty.type.replayviewer.util.ReplaySettingsUtil;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ReplaySession implements ReplayPlaybackContext {
    private static final GsonComponentSerializer COMPONENTS = GsonComponentSerializer.gson();
    private final UUID replayId;
    private final Set<Player> viewers = ConcurrentHashMap.newKeySet();
    private final Map<String, List<UUID>> currentTeams = new HashMap<>();
    private final Map<String, Boolean> liveBeds = new HashMap<>();
    private final Map<String, Integer> generatorTiers = new HashMap<>();
    private final Set<String> eliminatedTeams = new HashSet<>();
    private final ReplayMetadata metadata;
    private final ReplayGameMetadata gameMetadata;
    private final ReplayViewerAdapter viewerAdapter;
    private final InstanceContainer instance;

    private final ReplayEntityManager entityManager;
    private final ReplayTimeline replayData;
    private final ReplayWorldState worldState;
    private final ReplayEntityStore entityStore;
    private final ReplayStateRestorer stateRestorer;
    private final ReplayViewerProjection viewerProjection;

    private final DynamicTextManager dynamicTextManager;
    private final NpcReplayManager npcManager;
    private final BelowNameTag belowNameTag = new BelowNameTag("health", Text.of("<c>❤").asComponent());
    private final Map<Integer, PlayerNameTag> playerNameTags = new ConcurrentHashMap<>();

    private volatile boolean rebuildingState = false;
    private volatile int skipSeconds = 30;
    private final ReplayPlaybackSession playback;
    private Task actionBarTask;

    public static final float[] SPEED_PRESETS = {0.25f, 0.5f, 1.0f, 2.0f, 4.0f};
    public static final short[] SKIP_PRESETS = {1, 5, 10, 30, 60};

    public ReplaySession(
            ReplayMetadata metadata,
            ReplayGameMetadata gameMetadata,
            ReplayViewerAdapter viewerAdapter,
            InstanceContainer instance,
            ReplayTimeline replayData
    ) {
        this.replayId = metadata.descriptor().replayId();
        this.metadata = metadata;
        this.gameMetadata = gameMetadata;
        this.viewerAdapter = viewerAdapter;
        this.instance = instance;
        this.replayData = replayData;
        this.entityManager = new ReplayEntityManager(instance);
        this.worldState = new ReplayWorldState(instance, replayData.overlayPositions());
        this.entityStore = new ReplayEntityStore(entityManager, this::getParticipant);
        this.viewerProjection = new ReplayViewerProjection(entityManager);
        this.stateRestorer = new ReplayStateRestorer(this, replayData, worldState, entityStore, viewerAdapter);
        resetCurrentTeams();

        this.dynamicTextManager = new DynamicTextManager(this);
        this.npcManager = new NpcReplayManager(this);
        this.playback = new ReplayPlaybackSession(this::getTotalTicks,
                () -> !viewers.isEmpty() && viewers.stream().anyMatch(Player::isOnline), this::playTick, this::onReplayEnd);

        stateRestorer.restore(0);

    }

    public void addViewer(Player viewer) {
        viewers.add(viewer);

        viewer.setGameMode(GameMode.ADVENTURE);
        viewer.setFlying(true);
        viewer.setAllowFlying(true);
        ReplaySettingsUtil.applyVisualSettings((HypixelPlayer) viewer);

        ReplayScoreboard scoreboard = viewerAdapter.createScoreboard(this);
        viewerProjection.addScoreboard(viewer, scoreboard);

        belowNameTag.addViewer(viewer);
        replayBelowNameScores();
        replayNameTags(viewer);
        viewerProjection.applyEntityVisibility(List.of(viewer), entityStore.states());

        TypeReplayViewerLoader.populateInventory((HypixelPlayer) viewer);
        TypeReplayViewerLoader.registerSession(viewer.getUuid(), this);
        sendEquipmentSync(viewer);
        ScheduleUtility.delay(() -> sendEquipmentSync(viewer), 1);
        startActionBarUpdates();
        updateActionBar();

        ScheduleUtility.delay(() -> autoFollowForViewer(viewer), 20);


        Logger.info("Added viewer {} to replay session {}", viewer.getUsername(), replayId);
    }

    public void removeViewer(Player viewer) {
        viewers.remove(viewer);
        viewerProjection.removeViewer(viewer);

        belowNameTag.removeViewer(viewer);

        TypeReplayViewerLoader.removeSession(viewer.getUuid());

        Logger.info("Removed viewer {} from replay session {}", viewer.getUsername(), replayId);

        // If no viewers left, stop the session
        if (viewers.isEmpty()) {
            stop();
        }
    }

    public void updateBelowNameScore(int entityId, int score) {
        Entity entity = entityManager.getEntity(entityId);
        if (entity instanceof ReplayPlayerEntity playerEntity) {
            playerEntity.setBelowScore(score);
            sendBelowNameScore(playerEntity.getScoreboardEntryName(), score);
        }
    }

    public void applyPlayerTeam(UUID playerUuid, String teamId) {
        currentTeams.values().forEach(players -> players.remove(playerUuid));
        currentTeams.computeIfAbsent(teamId, ignored -> new ArrayList<>()).add(playerUuid);
    }

    void resetCurrentTeams() {
        currentTeams.clear();
        if (gameMetadata instanceof BedWarsViewerMetadata bedWars) {
            bedWars.teams().forEach(team -> currentTeams.put(team.id(), new ArrayList<>(team.initialMembers())));
        }
    }

    public void autoFollowForViewer(Player viewer) {
        UUID viewerUuid = viewer.getUuid();

        if (getParticipant(viewerUuid) == null) {
            return;
        }

        for (int entityId : entityManager.getEntityIds()) {
            Entity entity = entityManager.getEntity(entityId);
            if (entity instanceof ReplayPlayerEntity playerEntity) {
                if (viewerUuid.equals(playerEntity.getActualUuid())) {
                    viewer.teleport(playerEntity.getPosition());
                    viewer.sendMessage(Text.key("replays.teleported_to_player",
                            playerEntity.getActualUuid().toString()));
                    applyTeamGlow(viewer, entity, entityId);
                    return;
                }
            }
        }
    }

    public void play() {
        playback.play();
        updateActionBar();
    }

    public void pause() {
        playback.pause();
        updateActionBar();
    }

    public void togglePlayPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void stop() {
        pause();
        stopActionBarUpdates();
        entityManager.cleanup();

        dynamicTextManager.cleanup();
        npcManager.cleanup();

        viewerProjection.clearScoreboards(viewers);

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (instance.getPlayers().isEmpty()) {
                MinecraftServer.getInstanceManager().unregisterInstance(instance);
            }
        }).delay(TaskSchedule.seconds(5)).schedule();

        Logger.info("Replay session stopped for replay {}", replayId);
    }

    public void seekTo(int targetTick) {
        boolean wasPlaying = isPlaying();
        pause();

        targetTick = Math.clamp(targetTick, 0, getTotalTicks());

        playerNameTags.clear();
        playback.seek(targetTick);
        stateRestorer.restore(targetTick);
        replayBelowNameScores();
        viewerProjection.reattachCameras(viewers);
        viewerProjection.applyEntityVisibility(viewers, entityStore.states());

        // Update all viewer scoreboards
        viewerProjection.updateScoreboards(this);

        if (wasPlaying) {
            play();
        }

        updateActionBar();
        showSeekTitle(targetTick);
    }

    public void skipForward(int seconds) {
        seekTo(getCurrentTick() + seconds * 20);
    }

    public void skipBackward(int seconds) {
        seekTo(getCurrentTick() - seconds * 20);
    }

    public void setPlaybackSpeed(float speed) {
        playback.speed(speed);
        for (Player viewer : viewers) {
            viewer.sendMessage(Text.key("replays.playback_speed",
                    String.valueOf(getPlaybackSpeed())));
        }
    }

    public void refreshViewerProjection(Player viewer) {
        viewerProjection.applyEntityVisibility(List.of(viewer), entityStore.states());
    }

    public int getTotalTicks() {
        return metadata.descriptor().durationTicks();
    }

    public String getFormattedTime() {
        int seconds = getCurrentTick() / 20;
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    public String getFormattedTotalTime() {
        int seconds = getTotalTicks() / 20;
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    private void playTick(int tick) {
        for (var delta : replayData.stateDeltasAt(tick)) {
            try {
                if (delta instanceof net.swofty.type.game.replay.delta.ReplayBlockDelta block) {
                    worldState.apply(block.position(), block.blockStateId());
                } else if (delta instanceof net.swofty.type.game.replay.delta.ReplayEntityUpsertDelta upsert) {
                    entityStore.upsert(upsert.entity());
                    updateEntityPresentation(upsert.entity());
                } else if (delta instanceof net.swofty.type.game.replay.delta.ReplayEntityRemoveDelta remove) {
                    removeEntityPresentation(remove.replayEntityId());
                    entityStore.remove(remove.replayEntityId());
                } else {
                    viewerAdapter.applyDelta(this, delta);
                }
            } catch (Exception exception) {
                failPlayback(tick, exception);
                return;
            }
        }
        for (var event : replayData.transientEventsAt(tick)) {
            try {
                viewerAdapter.renderEvent(this, event);
            } catch (Exception exception) {
                failPlayback(tick, exception);
                return;
            }
        }

        viewerProjection.applyEntityVisibility(viewers, entityStore.states());
        viewerProjection.reattachCameras(viewers);

        dynamicTextManager.tick(tick);
        npcManager.tick();

        if (tick % 5 == 0) {
            viewerProjection.updateScoreboards(this);
        }
    }

    private void startActionBarUpdates() {
        if (actionBarTask != null) return;
        actionBarTask = MinecraftServer.getSchedulerManager().buildTask(this::updateActionBar)
                .repeat(TaskSchedule.tick(5)).schedule();
    }

    private void stopActionBarUpdates() {
        if (actionBarTask == null) return;
        actionBarTask.cancel();
        actionBarTask = null;
    }

    private void updateActionBar() {
        Text status = Text.of("<color:{}>{}", isPlaying() ? NamedTextColor.GREEN : NamedTextColor.RED,
                Text.key(isPlaying() ? "replays.playing" : "replays.paused"));
        Text actionBar = Text.key("replays.playback_status",
                status,
                Text.of("<e>{} / {}", getFormattedTime(), getFormattedTotalTime()),
                Text.of("<6>{}", String.format("%.1fx", getPlaybackSpeed())));
        for (Player viewer : viewers) {
            viewer.sendActionBar(actionBar.asComponent());
        }
    }

    void beginStateRebuild() {
        rebuildingState = true;
    }

    void endStateRebuild() {
        for (int entityId : entityManager.getEntityIds()) {
            Entity entity = entityManager.getEntity(entityId);
            if (entity != null) {
                entity.setAutoViewable(true);
            }
        }

        for (Player viewer : viewers) {
            sendEquipmentSync(viewer);
        }
        ScheduleUtility.delay(() -> viewers.forEach(this::sendEquipmentSync), 1);

        rebuildingState = false;
    }

    private void showSeekTitle(int tick) {
        Title title = Title.title(
                Text.of("<a>{}", getFormattedTime()).asComponent(),
                Text.of("<7>/{}", getFormattedTotalTime()).asComponent(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(500), Duration.ofMillis(200))
        );
        for (Player viewer : viewers) {
            viewer.showTitle(title);
        }
    }

    private void replayBelowNameScores() {
        for (int entityId : entityManager.getEntityIds()) {
            Entity entity = entityManager.getEntity(entityId);
            if (entity instanceof ReplayPlayerEntity playerEntity) {
                int score = playerEntity.getBelowScore();
                if (score >= 0) {
                    sendBelowNameScore(playerEntity.getScoreboardEntryName(), score);
                }
            }
        }
    }

    private void sendBelowNameScore(String entryName, int score) {
        UpdateScorePacket packet = new UpdateScorePacket(
                entryName,
                belowNameTag.getObjectiveName(),
                score,
                null,
                null
        );
        for (Player viewer : viewers) {
            viewer.sendPacket(packet);
        }
    }

    private void replayNameTags(Player viewer) {
        for (var entry : playerNameTags.entrySet()) {
            sendNameTagTeam(entry.getKey(), entry.getValue(), viewer);
        }
    }

    private void sendNameTagTeam(int entityId, PlayerNameTag tag, Player viewer) {
        NamedTextColor teamColor = tag.nameColor >= 0
                ? NamedTextColor.nearestTo(TextColor.color(tag.nameColor))
                : NamedTextColor.WHITE;
        String teamName = "REPLAY_NAME_" + entityId;

        TeamsPacket packet = new TeamsPacket(
                teamName,
                new TeamsPacket.CreateTeamAction(
                        new TeamsPacket.Settings(
                                Text.empty().asComponent(),
                                tag.prefix(),
                                tag.suffix(),
                                TeamsPacket.NameTagVisibility.ALWAYS,
                                TeamsPacket.CollisionRule.NEVER,
                                TeamColorUtil.fromNamedColor(teamColor),
                                (byte) 0x00
                        ),
                        new ArrayList<>(List.of(tag.entryName()))
                )
        );

        if (viewer != null) {
            viewer.sendPacket(packet);
        } else {
            for (Player v : viewers) {
                v.sendPacket(packet);
            }
        }
    }

    private void onReplayEnd() {
        pause();
        Title title = Title.title(
                Text.key("replays.replay_ended").asComponent(),
                Text.key("replays.replay_end_instruction").asComponent(),
                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(500))
        );
        for (Player viewer : viewers) {
            viewer.showTitle(title);
        }
    }

    public void followEntity(Player viewer, int entityId) {
        viewerProjection.follow(viewer, entityId);
    }

    public void stopFollowing(Player viewer) {
        viewerProjection.stopFollowing(viewer);
    }

    public Integer getFollowedEntityId(Player viewer) {
        return viewerProjection.cameraTarget(viewer);
    }

    private void sendEquipmentSync(Player viewer) {
        if (viewer == null || !viewer.isOnline()) {
            return;
        }

        for (int entityId : entityManager.getEntityIds()) {
            Entity entity = entityManager.getEntity(entityId);
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            Map<EquipmentSlot, ItemStack> equipmentBySlot = new EnumMap<>(EquipmentSlot.class);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = livingEntity.getEquipment(slot);
                if (stack != null && !stack.isAir()) {
                    equipmentBySlot.put(slot, stack);
                }
            }

            if (!equipmentBySlot.isEmpty()) {
                viewer.sendPacket(new EntityEquipmentPacket(entity.getEntityId(), equipmentBySlot));
            }
        }
    }

    private void applyTeamGlow(Player viewer, Entity entity, int entityId) {
        UUID entityUuid = null;
        if (entity instanceof ReplayPlayerEntity playerEntity) {
            entityUuid = playerEntity.getActualUuid();
        }

        if (entityUuid == null) return;

        String teamId = null;
        for (Map.Entry<String, List<UUID>> entry : currentTeams.entrySet()) {
            if (entry.getValue().contains(entityUuid)) {
                teamId = entry.getKey();
                break;
            }
        }

        if (teamId == null) return;

        BedWarsViewerMetadata.Team teamInfo = getBedWarsTeam(teamId);
        NamedTextColor teamColor = NamedTextColor.WHITE;
        if (teamInfo != null) {
            teamColor = NamedTextColor.nearestTo(TextColor.color(teamInfo.color()));
        }

        entity.setGlowing(true);

        String teamName = "REPLAY_GLOW_" + entityId;
        ReplayPlayerEntity playerEntity = (ReplayPlayerEntity) entity;
        String entityName = playerEntity.getPlayerName();

        viewer.sendPacket(new TeamsPacket(
                teamName,
                new TeamsPacket.CreateTeamAction(
                        new TeamsPacket.Settings(
                                Text.empty().asComponent(),
                                Text.empty().asComponent(),
                                Text.empty().asComponent(),
                                TeamsPacket.NameTagVisibility.ALWAYS,
                                TeamsPacket.CollisionRule.NEVER,
                                TeamColorUtil.fromNamedColor(teamColor),
                                (byte) 0x02
                        ),
                        new ArrayList<>(List.of(entityName))
                )
        ));
    }

    public String getEntityDisplayName(int entityId) {
        Entity entity = entityManager.getEntity(entityId);
        if (entity instanceof ReplayPlayerEntity playerEntity) {
            return playerEntity.getPlayerName();
        }

        if (entity instanceof ReplayEntity replayEntity) {
            UUID uuid = replayEntity.getRecordedUuid();
            ReplayParticipant participant = getParticipant(uuid);
            if (participant != null) return participant.username();
        }

        return String.valueOf(entityId);
    }

    public short cycleSkip(int previous) {
        for (short preset : SKIP_PRESETS) {
            if (preset > previous) {
                return preset;
            }
        }
        return SKIP_PRESETS[0];
    }

    public void cycleSpeedUp() {
        for (float preset : SPEED_PRESETS) {
            if (preset > getPlaybackSpeed()) {
                setPlaybackSpeed(preset);
                return;
            }
        }
        int speed = (int) SPEED_PRESETS[SPEED_PRESETS.length - 1];
        setPlaybackSpeed(speed);
    }

    public void cycleSpeedDown() {
        for (int i = SPEED_PRESETS.length - 1; i >= 0; i--) {
            if (SPEED_PRESETS[i] < getPlaybackSpeed()) {
                setPlaybackSpeed(SPEED_PRESETS[i]);
                return;
            }
        }
        setPlaybackSpeed(SPEED_PRESETS[0]);
    }

    public float getProgress() {
        if (getTotalTicks() == 0) return 0;
        return (float) getCurrentTick() / getTotalTicks() * 100;
    }

    public void seekToPercent(float percent) {
        int targetTick = (int) (getTotalTicks() * (percent / 100f));
        seekTo(targetTick);
    }

    void rebuildEntityPresentation() {
        playerNameTags.clear();
        for (var state : entityStore.states().values()) {
            updateEntityPresentation(state);
        }
    }

    private void updateEntityPresentation(net.swofty.type.game.replay.model.ReplayEntityState state) {
        if (state.player() == null) return;
        ReplayParticipant participant = getParticipant(state.player().participantUuid());
        if (participant == null) return;
        int color = -1;
        BedWarsViewerMetadata.Team team = getBedWarsTeam(state.player().teamId());
        if (team != null) color = team.color();
        Text prefix = team == null || team.name().isEmpty() ? Text.component(COMPONENTS.deserialize(participant.prefixJson()))
                : Text.of("<color:{}>{} ", TextColor.color(team.color()), team.name().substring(0, 1).toUpperCase());
        PlayerNameTag tag = new PlayerNameTag(participant.username(), prefix.asComponent(),
                COMPONENTS.deserialize(participant.suffixJson()), color);
        PlayerNameTag previous = playerNameTags.put(state.replayEntityId(), tag);
        if (!tag.equals(previous)) {
            if (previous != null) sendRemoveTeam("REPLAY_NAME_" + state.replayEntityId());
            sendNameTagTeam(state.replayEntityId(), tag, null);
        }
        Entity entity = entityManager.getEntity(state.replayEntityId());
        if (entity instanceof ReplayPlayerEntity player && player.getBelowScore() != (int) state.health()) {
            player.setBelowScore((int) state.health());
            sendBelowNameScore(player.getScoreboardEntryName(), (int) state.health());
        }
    }

    private void clearProjectionTeams() {
        playerNameTags.keySet().forEach(entityId -> sendRemoveTeam("REPLAY_NAME_" + entityId));
        entityManager.getEntityIds().forEach(entityId -> sendRemoveTeam("REPLAY_GLOW_" + entityId));
        playerNameTags.clear();
    }

    private void removeEntityPresentation(int entityId) {
        if (playerNameTags.remove(entityId) != null) sendRemoveTeam("REPLAY_NAME_" + entityId);
        sendRemoveTeam("REPLAY_GLOW_" + entityId);
    }

    private void sendRemoveTeam(String name) {
        TeamsPacket packet = new TeamsPacket(name, new TeamsPacket.RemoveTeamAction());
        viewers.forEach(viewer -> viewer.sendPacket(packet));
    }

    private record PlayerNameTag(String entryName, Component prefix, Component suffix, int nameColor) {
    }

    @Override
    public int tick() {
        return getCurrentTick();
    }

    public int getCurrentTick() {
        return playback.currentTick();
    }

    public boolean isPlaying() {
        return playback.playing();
    }

    public float getPlaybackSpeed() {
        return playback.speed();
    }

    @Override
    public InstanceContainer instance() {
        return instance;
    }

    public ReplayParticipant getParticipant(UUID uuid) {
        return metadata.participants().stream().filter(participant -> participant.uuid().equals(uuid)).findFirst().orElse(null);
    }

    public BedWarsViewerMetadata.Team getBedWarsTeam(String teamId) {
        if (!(gameMetadata instanceof BedWarsViewerMetadata bedWars)) return null;
        return bedWars.teams().stream().filter(team -> team.id().equals(teamId)).findFirst().orElse(null);
    }

    public String gameModeId() {
        return gameMetadata instanceof BedWarsViewerMetadata bedWars ? bedWars.modeId() : metadata.descriptor().gameType();
    }

    public void replaceCurrentTeams(Map<String, List<UUID>> teams) {
        currentTeams.clear();
        teams.forEach((team, players) -> currentTeams.put(team, new ArrayList<>(players)));
    }

    public void restoreBedWarsState(Map<String, List<UUID>> teams, Map<String, Boolean> beds, Map<String, Integer> generators,
                                    Collection<String> eliminated) {
        replaceCurrentTeams(teams);
        liveBeds.clear();
        liveBeds.putAll(beds);
        generatorTiers.clear();
        generatorTiers.putAll(generators);
        eliminatedTeams.clear();
        eliminatedTeams.addAll(eliminated);
    }

    public void applyBedState(String teamId, boolean live) {
        liveBeds.put(teamId, live);
    }

    public void eliminateTeam(String teamId) {
        eliminatedTeams.add(teamId);
        liveBeds.put(teamId, false);
    }

    public void applyGeneratorTier(String generatorId, int tier) {
        generatorTiers.put(generatorId, tier);
    }

    void clearReplayOwnedState() {
        clearProjectionTeams();
        entityManager.cleanup();
        dynamicTextManager.cleanup();
        npcManager.cleanup();
        resetCurrentTeams();
    }

    void failPlayback(int tick, Exception exception) {
        pause();
        Logger.error(exception, "Replay playback stopped: replay={}, gameType={}, version={}, tick={}",
                replayId, metadata.descriptor().gameType(), metadata.descriptor().formatVersion(), tick);
        viewers.forEach(viewer -> viewer.sendMessage(Text.key("replays.playback_corrupt")));
    }

}
