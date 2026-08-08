package net.swofty.type.replayviewer.event;

import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.replay.ReplayLoadProtocolObject;
import net.swofty.commons.protocol.objects.replay.ReplayMapLoadProtocolObject;
import net.swofty.commons.replay.protocol.ReplayDataReader;
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
import net.swofty.type.generic.i18n.I18n;
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

    @SneakyThrows
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

        event.setSpawningInstance(instance);
        event.getPlayer().setRespawnPoint(new Pos(0, 100, 0));

        CompletableFuture.runAsync(() -> loadReplay(player, replayId, instance));
    }

    private void loadReplay(Player player, UUID replayId, InstanceContainer instance) {
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
                player.sendMessage(I18n.t("replays.replay_load_failed"));
                return;
            }

            if (response.metadata() == null) {
                Logger.error("Response is missing metadata.");
                player.sendMessage(I18n.t("replays.replay_incomplete"));
                return;
            }

            var protocolMetadata = response.metadata();
            var protocolDescriptor = protocolMetadata.descriptor();
            if (protocolDescriptor.formatVersion() != ReplayVersion.CURRENT_VERSION) {
                player.sendMessage(I18n.t("replays.replay_unsupported_format"));
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
                    player.sendMessage(I18n.t("replays.shared_position_restored"));
                } else {
                    spawnPos = new Pos(descriptor.mapCenterX(), 100, descriptor.mapCenterZ());
                    player.sendMessage(I18n.t("replays.invalid_share_code"));
                }
            } else {
                spawnPos = new Pos(descriptor.mapCenterX(), 100, descriptor.mapCenterZ());
            }

            player.teleport(spawnPos);

            ReplaySession session = new ReplaySession(metadata, gameMetadata, adapter, instance, timeline);
            session.addViewer(player);
            TypeReplayViewerLoader.registerSession(player.getUuid(), session);

            if (startTick > 0) {
                session.seekTo(startTick);
            }

            session.play();
        } catch (Exception e) {
            Logger.error(e, "Failed to load replay {}", replayId);
            player.sendMessage(I18n.t("replays.replay_corrupt"));
            if (player instanceof HypixelPlayer hp) {
                hp.sendTo(ServerType.PROTOTYPE_LOBBY);
            }
        }
    }

    private CompletableFuture<Void> loadMapData(String mapHash, InstanceContainer instance, Player player) {
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
                player.sendMessage(I18n.t("replays.map_unavailable"));
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
                            return;
                        }

                        Logger.info("Loaded map {} ({} bytes)", mapHash, response.compressedData().length);
                    });
        } catch (Exception e) {
            Logger.error(e, "Failed to load map {}", mapHash);
            player.sendMessage(I18n.t("replays.map_load_failed", Argument.string("error", String.valueOf(e.getMessage()))));
            return CompletableFuture.failedFuture(e);
        }
    }
}
