package net.swofty.type.murdermysterygame.replay;

import lombok.Getter;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.replay.ReplayMapUploadProtocolObject;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.game.replay.ReplayRecorder;
import net.swofty.type.game.replay.ReplayVersion;
import net.swofty.type.game.replay.delta.ReplayBlockDelta;
import net.swofty.type.game.replay.dispatcher.BlockChangeDispatcher;
import net.swofty.type.game.replay.dispatcher.DispatcherManager;
import net.swofty.type.game.replay.dispatcher.EntityLocationDispatcher;
import net.swofty.type.game.replay.event.ReplayBookmarkEvent;
import net.swofty.type.game.replay.event.ReplayComponentEvent;
import net.swofty.type.game.replay.event.ReplaySoundEvent;
import net.swofty.type.game.replay.model.ReplayBlockPosition;
import net.swofty.type.game.replay.model.ReplayDescriptor;
import net.swofty.type.game.replay.model.ReplayParticipant;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;
import org.tinylog.Logger;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MurderMysteryReplayManager {
    private static final int MAP_CHUNK_RADIUS = 8;

    private final Game game;
    private final ProxyService replayService;
    private final MurderMysteryReplayAdapter adapter;
    private CompletableFuture<Void> deliveryChain = CompletableFuture.completedFuture(null);

    @Getter
    private final ReplayRecorder recorder;
    @Getter
    private final DispatcherManager dispatchers;
    private Task tickTask;

    @Getter
    private boolean recording;

    public MurderMysteryReplayManager(Game game, ProxyService replayService) {
        this.game = game;
        this.replayService = replayService;
        this.recorder = new ReplayRecorder(game.getGameId(), ServerType.MURDER_MYSTERY_GAME, this::sendToService);
        this.dispatchers = new DispatcherManager(recorder);
        this.adapter = TypeMurderMysteryGameLoader.getReplayAdapters()
                .require(MurderMysteryReplayAdapter.GAME_TYPE).apply(game);
        recorder.configureEntityCapture(adapter::captureEntity, adapter::isReplayVisible);
    }

    private synchronized void sendToService(Object data) {
        if (replayService == null) return;
        deliveryChain = deliveryChain.thenCompose(ignored -> deliverWithRetry(data, 0));
    }

    private CompletableFuture<Void> deliverWithRetry(Object data, int attempt) {
        CompletableFuture<Void> delivery = replayService.handleRequest(data).thenApply(response -> {
            if (!responseAcknowledged(response)) {
                throw new IllegalStateException("Replay service rejected " + data.getClass().getSimpleName());
            }
            return null;
        });
        return delivery.exceptionallyCompose(error -> {
            if (attempt >= 3) {
                Logger.error(error, "Replay delivery failed after {} attempts: {}", attempt + 1,
                        data.getClass().getSimpleName());
                return CompletableFuture.failedFuture(error);
            }
            long delayMillis = 100L << attempt;
            return CompletableFuture.<Void>supplyAsync(() -> null,
                            CompletableFuture.delayedExecutor(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS))
                    .thenCompose(ignored -> deliverWithRetry(data, attempt + 1));
        });
    }

    private boolean responseAcknowledged(Object response) {
        if (response == null) return false;
        try {
            Object success = response.getClass().getMethod("success").invoke(response);
            return Boolean.TRUE.equals(success);
        } catch (ReflectiveOperationException exception) {
            return true;
        }
    }

    public void startRecording() {
        if (recording) return;
        recording = true;

        var configuration = game.getMapEntry().getConfiguration();
        var locations = configuration == null ? null : configuration.getLocations();
        var waiting = locations == null ? null : locations.getWaiting();
        int centerChunkX = waiting == null ? 0 : (int) waiting.x() >> 4;
        int centerChunkZ = waiting == null ? 0 : (int) waiting.z() >> 4;
        double centerX = waiting == null ? 0 : waiting.x();
        double centerZ = waiting == null ? 0 : waiting.z();

        GsonComponentSerializer components = GsonComponentSerializer.gson();
        List<ReplayParticipant> participants = new ArrayList<>();
        for (MurderMysteryPlayer player : game.getPlayers()) {
            var skin = player.getSkin();
            participants.add(new ReplayParticipant(
                    player.getUuid(), player.getEntityId(), player.getUsername(),
                    skin == null ? null : skin.textures(), skin == null ? null : skin.signature(),
                    components.serialize(player.getDisplayName()), components.serialize(Component.empty()),
                    components.serialize(Component.empty())
            ));
        }

        String mapName = game.getMapEntry().getName();
        String mapHash = serializeAndUploadMap(game.getInstance(), mapName, centerChunkX, centerChunkZ);
        long startTime = System.currentTimeMillis();
        ReplayDescriptor descriptor = new ReplayDescriptor(
                recorder.getReplayId(), game.getGameId(), adapter.gameType(), ServerType.MURDER_MYSTERY_GAME,
                HypixelConst.getServerName(), mapName, mapHash, centerX, centerZ, ReplayVersion.CURRENT_VERSION,
                startTime, 0, 0, 0);

        recorder.start(descriptor, participants, adapter, () -> adapter.captureSnapshot(recorder));
        dispatchers.register(new EntityLifecycleDispatcher(game.getInstance()));
        dispatchers.register(new EntityLocationDispatcher(game.getInstance()));
        dispatchers.register(new BlockChangeDispatcher());

        tickTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!recording) return;
            recorder.tick();
            dispatchers.tick();
        }).repeat(TaskSchedule.tick(1)).schedule();
        Logger.info("Started Murder Mystery replay recording for game {}", game.getGameId());
    }

    public void stopRecording() {
        if (!recording) return;
        recording = false;
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        dispatchers.cleanup();
        recorder.finish();
        Logger.info("Stopped Murder Mystery replay recording for game {}", game.getGameId());
    }

    public void recordKill(MurderMysteryPlayer killer, MurderMysteryPlayer victim, Game.KillType killType) {
        if (!recording) return;
        recorder.recordDelta(adapter.killDelta(killer.getUuid(), victim.getUuid(), killType));
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.DEATH_MESSAGE,
                Component.text(victim.getUsername() + " was eliminated.")));
        recorder.recordEvent(new ReplayBookmarkEvent(Component.text("Kill: " + victim.getUsername()), killer.getUuid()));
        recorder.recordEntityState(killer);
        recorder.recordEntityState(victim);
    }

    public void recordEnvironmentalDeath(MurderMysteryPlayer victim, String reason) {
        if (!recording) return;
        recorder.recordDelta(adapter.environmentalDeathDelta(victim.getUuid(), reason));
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.DEATH_MESSAGE,
                Component.text(victim.getUsername() + " died: " + reason)));
        recorder.recordEvent(new ReplayBookmarkEvent(Component.text("Elimination: " + victim.getUsername()),
                victim.getUuid()));
        recorder.recordEntityState(victim);
    }

    public void recordGameEnd(String condition, MurderMysteryPlayer winner) {
        if (!recording) return;
        UUID winnerUuid = winner == null ? null : winner.getUuid();
        recorder.recordDelta(adapter.gameEndDelta(condition, winnerUuid));
        Component message = Component.text("Murder Mystery ended: " + condition);
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.ANNOUNCEMENT, message));
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.TITLE, message));
        recorder.recordEvent(new ReplayBookmarkEvent(Component.text("Game ended"), winnerUuid));
    }

    public void recordPlayerChat(MurderMysteryPlayer player, Component message) {
        if (!recording) return;
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.CHAT,
                Component.text(player.getUsername() + ": ").append(message)));
    }

    public void recordAnnouncement(Component message) {
        if (!recording) return;
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.ANNOUNCEMENT, message));
    }

    public void recordBowDrop() {
        if (!recording) return;
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.ANNOUNCEMENT,
                Component.text("The Detective Bow has been dropped.")));
    }

    public void recordBowPickup(MurderMysteryPlayer player) {
        if (!recording) return;
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.ANNOUNCEMENT,
                Component.text(player.getUsername() + " picked up the Detective Bow.")));
        recorder.recordEntityState(player);
    }

    public void recordSound(Sound sound, double x, double y, double z) {
        if (!recording) return;
        recorder.recordEvent(new ReplaySoundEvent(sound.name().asString(), (byte) sound.source().ordinal(),
                x, y, z, sound.volume(), sound.pitch()));
    }

    public void recordBlockChange(int x, int y, int z, int previousBlock, int newBlock) {
        if (!recording) return;
        BlockChangeDispatcher dispatcher = dispatchers.getDispatcher(BlockChangeDispatcher.class);
        if (dispatcher == null) {
            recorder.recordDelta(new ReplayBlockDelta(new ReplayBlockPosition(x, y, z), newBlock));
            return;
        }
        dispatcher.recordBlockChange(x, y, z, previousBlock, newBlock);
    }

    public static byte[] serializeItemStack(ItemStack itemStack) {
        try {
            CompoundBinaryTag nbt = itemStack.toItemNBT();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            BinaryTagIO.writer().writeNameless(nbt, output);
            return output.toByteArray();
        } catch (Exception exception) {
            Logger.error(exception, "Failed to serialize Murder Mystery replay item stack");
            return new byte[0];
        }
    }

    private String serializeAndUploadMap(Instance instance, String mapName, int centerChunkX, int centerChunkZ) {
        try {
            MapSerializer.SerializedMap map = MapSerializer.serializeRegion(instance, centerChunkX, centerChunkZ,
                    MAP_CHUNK_RADIUS);
            sendToService(new ReplayMapUploadProtocolObject.MapUploadMessage(map.hash(), mapName, map.compressedData()));
            return map.hash();
        } catch (Exception exception) {
            Logger.error(exception, "Failed to serialize Murder Mystery map {}", mapName);
            return mapName.toLowerCase().replace(' ', '_');
        }
    }
}
