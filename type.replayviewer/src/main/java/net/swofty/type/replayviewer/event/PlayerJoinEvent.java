package net.swofty.type.replayviewer.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.replay.ReplayLoadProtocolObject;
import net.swofty.commons.protocol.objects.replay.ReplayMapLoadProtocolObject;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.game.replay.ReplayVersion;
import net.swofty.type.game.replay.api.ReplayGameMetadata;
import net.swofty.type.game.replay.api.ReplayViewerAdapter;
import net.swofty.type.game.replay.model.ReplayDescriptor;
import net.swofty.type.game.replay.model.ReplayGameMetadataEnvelope;
import net.swofty.type.game.replay.model.ReplayMetadata;
import net.swofty.type.game.replay.model.ReplayParticipant;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.playback.MapDeserializer;
import net.swofty.type.replayviewer.playback.ReplaySession;
import net.swofty.type.replayviewer.playback.ReplayTimeline;
import net.swofty.type.replayviewer.redis.service.TypedViewReplayHandler;
import net.swofty.type.replayviewer.util.ReplayShareCodec;
import org.tinylog.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerJoinEvent implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, phase = EventPhase.CONNECT)
    public void run(AsyncPlayerConfigurationEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        tryGame(player, false, event);
    }

    private void tryGame(HypixelPlayer player, boolean isRetry, AsyncPlayerConfigurationEvent event) {
        String replayStr = TypedViewReplayHandler.replay.remove(player.getUuid());
        if (replayStr == null) {
            if (!isRetry) {
                ScheduleUtility.delay(() -> tryGame(player, true, event), 20);
                return;
            }
            event.setSpawningInstance(HypixelConst.getEmptyInstance());
            player.sendTo(ServerType.PROTOTYPE_LOBBY);
            return;
        }

        UUID replayId;
        try {
            replayId = UUID.fromString(replayStr);
        } catch (IllegalArgumentException e) {
            event.setSpawningInstance(HypixelConst.getEmptyInstance());
            player.sendTo(ServerType.PROTOTYPE_LOBBY);
            return;
        }

        var existingSession = TypeReplayViewerLoader.getSessionByReplayId(replayId);
        if (existingSession.isPresent()) {
            ReplaySession session = existingSession.get();
            event.setSpawningInstance(session.getInstance());

            Pos spawnPos = new Pos(session.getMetadata().descriptor().mapCenterX(), 100, session.getMetadata().descriptor().mapCenterZ());
            event.getPlayer().setRespawnPoint(spawnPos);

            TypeReplayViewerLoader.registerSession(player.getUuid(), session);
            ScheduleUtility.delay(() -> session.addViewer(player), 1);
            player.setRespawnPoint(spawnPos);
            return;
        }

        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkSupplier(LightingChunk::new);

        event.setSpawningInstance(HypixelConst.getEmptyInstance());
        event.getPlayer().setRespawnPoint(new Pos(0, 100, 0));

        CompletableFuture.runAsync(() -> loadReplay(player, replayId, instance));
    }

    private void loadReplay(HypixelPlayer player, UUID replayId, InstanceContainer instance) {
        try {
            // Get share code if present
            String shareCode = TypedViewReplayHandler.getAndRemoveShareCode(player.getUuid());

            ProxyService replayService = new ProxyService(ServiceType.REPLAY);
            var request = new ReplayLoadProtocolObject.LoadRequest(replayId);

            ReplayLoadProtocolObject.LoadResponse response = replayService
                .<ReplayLoadProtocolObject.LoadRequest, ReplayLoadProtocolObject.LoadResponse>handleRequest(request)
                .join();

            if (!response.success()) {
                Logger.error("Response failed: " + response.errorMessage());
                failReplayLoad(player, instance, Text.key("replays.replay_load_failed"));
                return;
            }

            if (response.metadata() == null) {
                Logger.error("Response is missing metadata.");
                failReplayLoad(player, instance, Text.key("replays.replay_incomplete"));
                return;
            }

            var protocolMetadata = response.metadata();
            var protocolDescriptor = protocolMetadata.descriptor();
            if (protocolDescriptor.formatVersion() != ReplayVersion.CURRENT_VERSION) {
                failReplayLoad(player, instance, Text.key("replays.replay_unsupported_format"));
                Logger.warn("Rejected replay {} with format version {}", replayId, protocolDescriptor.formatVersion());
                return;
            }
            ReplayViewerAdapter<?, ?> adapter = TypeReplayViewerLoader.getReplayAdapters().require(protocolDescriptor.gameType());
            if (adapter.metadataSchemaVersion() != protocolMetadata.gameMetadata().schemaVersion()) {
                throw new IllegalArgumentException("Unsupported " + protocolDescriptor.gameType() + " replay metadata schema: "
                        + protocolMetadata.gameMetadata().schemaVersion());
            }
            ReplayGameMetadata gameMetadata;
            try (ReplayDataReader reader = new ReplayDataReader(protocolMetadata.gameMetadata().payload())) {
                gameMetadata = adapter.readMetadata(reader);
                if (reader.available() != 0) throw new IllegalArgumentException("Trailing replay metadata payload");
            }
            ReplayDescriptor descriptor = new ReplayDescriptor(
                    protocolDescriptor.replayId(), protocolDescriptor.gameId(), protocolDescriptor.gameType(), protocolDescriptor.serverType(),
                    protocolDescriptor.serverId(), protocolDescriptor.mapName(), protocolDescriptor.mapHash(), protocolDescriptor.mapCenterX(),
                    protocolDescriptor.mapCenterZ(), protocolDescriptor.formatVersion(), protocolDescriptor.startTime(), protocolDescriptor.endTime(),
                    protocolDescriptor.durationTicks(), protocolDescriptor.dataSize());
            var participants = protocolMetadata.participants().stream().map(value -> new ReplayParticipant(
                    value.uuid(), value.entityId(), value.username(), value.textureValue(), value.textureSignature(),
                    value.displayNameJson(), value.prefixJson(), value.suffixJson())).toList();
            ReplayMetadata metadata = new ReplayMetadata(descriptor, participants,
                    new ReplayGameMetadataEnvelope(protocolMetadata.gameMetadata().gameType(),
                            protocolMetadata.gameMetadata().schemaVersion(), protocolMetadata.gameMetadata().payload()));
            ReplayTimeline timeline = new ReplayTimeline();
            timeline.load(response.chunks(), descriptor.durationTicks());

            loadMapData(descriptor.mapHash(), instance, player).join();

            // Determine spawn position - use share code if available
            Pos spawnPos;
            int startTick = 0;

            if (shareCode != null) {
                ReplayShareCodec.ShareData shareData = ReplayShareCodec.decode(
                    shareCode,
                        descriptor.mapCenterX(),
                        descriptor.mapCenterZ()
                );
                if (shareData != null) {
                    spawnPos = shareData.position();
                    startTick = Math.min(shareData.tick(), Math.max(0, descriptor.durationTicks() - 1));
                    sendPlayerMessage(player, Text.key("replays.shared_position_restored"));
                } else {
                    spawnPos = new Pos(descriptor.mapCenterX(), 100, descriptor.mapCenterZ());
                    sendPlayerMessage(player, Text.key("replays.invalid_share_code"));
                }
            } else {
                spawnPos = new Pos(descriptor.mapCenterX(), 100, descriptor.mapCenterZ());
            }

            scheduleReplayInitialization(player, instance, metadata, gameMetadata, adapter, timeline, spawnPos, startTick);
        } catch (Exception e) {
            Logger.error(e, "Failed to load replay {}", replayId);
            failReplayLoad(player, instance, Text.key("replays.replay_corrupt"));
        }
    }

    private void scheduleReplayInitialization(
            HypixelPlayer player,
            InstanceContainer instance,
            ReplayMetadata metadata,
            ReplayGameMetadata gameMetadata,
            ReplayViewerAdapter<?, ?> adapter,
            ReplayTimeline timeline,
            Pos spawnPos,
            int startTick
    ) {
        ScheduleUtility.nextTick(() -> initializeReplay(
                player, instance, metadata, gameMetadata, adapter, timeline, spawnPos, startTick));
    }

    private void initializeReplay(
            HypixelPlayer player,
            InstanceContainer instance,
            ReplayMetadata metadata,
            ReplayGameMetadata gameMetadata,
            ReplayViewerAdapter<?, ?> adapter,
            ReplayTimeline timeline,
            Pos spawnPos,
            int startTick
    ) {
        if (!player.isOnline()) {
            scheduleInstanceCleanup(instance);
            return;
        }

        ReplaySession session;
        try {
            player.setRespawnPoint(spawnPos);
            session = new ReplaySession(metadata, gameMetadata, adapter, instance, timeline);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to initialize replay {}", metadata.descriptor().replayId());
            failReplayLoadOnServer(player, instance, Text.key("replays.replay_corrupt"));
            return;
        }

        try {
            player.setInstance(instance, spawnPos).whenComplete((ignored, throwable) ->
                    ScheduleUtility.nextTick(() -> {
                        if (throwable != null) {
                            Logger.error(throwable, "Failed to move player into replay {}", session.getReplayId());
                            abortSession(player, session);
                            failReplayLoadOnServer(player, instance, Text.key("replays.replay_corrupt"));
                            return;
                        }
                        if (!player.isOnline()) {
                            abortSession(player, session);
                            scheduleInstanceCleanup(instance);
                            return;
                        }
                        try {
                            session.addViewer(player);
                            TypeReplayViewerLoader.registerSession(player.getUuid(), session);
                            if (startTick > 0) session.seekTo(startTick);
                            session.play();
                        } catch (Exception exception) {
                            Logger.error(exception, "Failed to start replay {} for {}", session.getReplayId(), player.getUsername());
                            abortSession(player, session);
                            failReplayLoadOnServer(player, instance, Text.key("replays.replay_corrupt"));
                        }
                    }));
        } catch (Exception exception) {
            Logger.error(exception, "Failed to move player into replay {}", session.getReplayId());
            abortSession(player, session);
            failReplayLoadOnServer(player, instance, Text.key("replays.replay_corrupt"));
        }
    }

    private void abortSession(HypixelPlayer player, ReplaySession session) {
        TypeReplayViewerLoader.removeSession(player.getUuid());
        session.stop();
    }

    private CompletableFuture<Void> loadMapData(String mapHash, InstanceContainer instance, HypixelPlayer player) {
        if (mapHash == null || mapHash.isEmpty()) {
            Logger.warn("No map hash provided, skipping map load");
            return CompletableFuture.completedFuture(null);
        }

        try {
            ProxyService replayService = new ProxyService(ServiceType.REPLAY);
            var request = new ReplayMapLoadProtocolObject.MapLoadRequest(mapHash);

            ReplayMapLoadProtocolObject.MapLoadResponse response = replayService
                .<ReplayMapLoadProtocolObject.MapLoadRequest, ReplayMapLoadProtocolObject.MapLoadResponse>handleRequest(request)
                .join();

            if (!response.success() || !response.found()) {
                Logger.warn("Map {} not found in replay service", mapHash);
                sendPlayerMessage(player, Text.key("replays.map_unavailable"));
                return CompletableFuture.completedFuture(null);
            }

            if (response.compressedData() == null || response.compressedData().length == 0) {
                Logger.warn("Map {} has no data", mapHash);
                return CompletableFuture.completedFuture(null);
            }

            // Deserialize and apply map
            return MapDeserializer.loadMap(instance, response.compressedData())
                    .whenComplete((ignored, throwable) -> {
                        if (throwable != null) {
                            Logger.error(throwable, "Failed to load map");
                        } else {
                            Logger.info("Loaded map {} ({} bytes)", mapHash, response.compressedData().length);
                        }
                    });
        } catch (Exception e) {
            Logger.error(e, "Failed to load map {}", mapHash);
            sendPlayerMessage(player, Text.key("replays.map_load_failed", String.valueOf(e.getMessage())));
            return CompletableFuture.failedFuture(e);
        }
    }

    private void failReplayLoad(HypixelPlayer player, InstanceContainer instance, Text message) {
        ScheduleUtility.nextTick(() -> failReplayLoadOnServer(player, instance, message));
    }

    private void failReplayLoadOnServer(HypixelPlayer player, InstanceContainer instance, Text message) {
        if (player.isOnline()) {
            player.sendMessage(message);
            player.sendTo(ServerType.PROTOTYPE_LOBBY);
        }
        scheduleInstanceCleanup(instance);
    }

    private void sendPlayerMessage(HypixelPlayer player, Text message) {
        ScheduleUtility.nextTick(() -> {
            if (player.isOnline()) player.sendMessage(message);
        });
    }

    private void scheduleInstanceCleanup(InstanceContainer instance) {
        ScheduleUtility.delay(() -> cleanupInstance(instance, 0), TaskSchedule.seconds(1));
    }

    private void cleanupInstance(InstanceContainer instance, int attempt) {
        if (instance.getPlayers().isEmpty()) {
            MinecraftServer.getInstanceManager().unregisterInstance(instance);
            return;
        }
        if (attempt >= 20) {
            Logger.warn("Could not clean up replay instance after failure; players are still present");
            return;
        }
        ScheduleUtility.delay(() -> cleanupInstance(instance, attempt + 1), TaskSchedule.seconds(1));
    }
}
