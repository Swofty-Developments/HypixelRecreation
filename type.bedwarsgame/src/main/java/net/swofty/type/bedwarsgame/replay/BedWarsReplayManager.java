package net.swofty.type.bedwarsgame.replay;

import lombok.Getter;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.protocol.objects.replay.ReplayMapUploadProtocolObject;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.death.BedWarsDeathType;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.game.v2.BedWarsTeam;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.replay.ReplayRecorder;
import net.swofty.type.game.replay.ReplayVersion;
import net.swofty.type.game.replay.delta.ReplayBlockDelta;
import net.swofty.type.game.replay.delta.ReplayEntityRemoveDelta;
import net.swofty.type.game.replay.dispatcher.BlockChangeDispatcher;
import net.swofty.type.game.replay.dispatcher.DispatcherManager;
import net.swofty.type.game.replay.dispatcher.EntityLocationDispatcher;
import net.swofty.type.game.replay.event.*;
import net.swofty.type.game.replay.model.ReplayBlockPosition;
import net.swofty.type.game.replay.model.ReplayDescriptor;
import net.swofty.type.game.replay.model.ReplayParticipant;
import net.swofty.type.generic.HypixelConst;
import org.tinylog.Logger;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BedWarsReplayManager {
    private final BedWarsGame game;
    private final ProxyService replayService;
    private CompletableFuture<Void> deliveryChain = CompletableFuture.completedFuture(null);

    @Getter
    private final ReplayRecorder recorder;
    @Getter
    private final DispatcherManager dispatchers;
    private final BedWarsReplayAdapter adapter;

    private Task tickTask;

    @Getter
    private boolean recording = false;

    public BedWarsReplayManager(BedWarsGame game, ProxyService replayService) {
        this.game = game;
        this.replayService = replayService;
        this.recorder = new ReplayRecorder(game.getGameId(), ServerType.BEDWARS_GAME, this::sendToService);
        this.dispatchers = new DispatcherManager(recorder);
        this.adapter = TypeBedWarsGameLoader.getReplayAdapters().require(BedWarsReplayAdapter.GAME_TYPE).apply(game);
        this.recorder.configureEntityCapture(adapter::captureEntity, adapter::isReplayVisible);
    }

    /**
     * Sends replay data to the replay service.
     */
    private synchronized void sendToService(Object data) {
        if (replayService == null) {
            Logger.debug("No replay service configured, skipping: {}", data.getClass().getSimpleName());
            return;
        }

        deliveryChain = deliveryChain.thenCompose(ignored -> deliverWithRetry(data, 0));
    }

    private CompletableFuture<Void> deliverWithRetry(Object data, int attempt) {
        CompletableFuture<Void> delivery = replayService.handleRequest(data).thenApply(response -> {
            if (!responseAcknowledged(response)) {
                throw new IllegalStateException("Replay service rejected " + data.getClass().getSimpleName());
            }
            return (Void) null;
        });
        return delivery.exceptionallyCompose(error -> {
            if (attempt >= 3) {
                Logger.error(error, "Replay delivery failed after {} attempts: {}", attempt + 1, data.getClass().getSimpleName());
                return CompletableFuture.failedFuture(error);
            }
            long delayMillis = 100L << attempt;
            return CompletableFuture.<Void>supplyAsync(() -> null, CompletableFuture.delayedExecutor(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS))
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

        // Set map center for coordinate optimization
        var locations = game.getMapEntry().getConfiguration().getLocations();
        int centerChunkX = 0, centerChunkZ = 0;
        if (locations.getWaiting() != null) {
            centerChunkX = (int) locations.getWaiting().x() >> 4;
            centerChunkZ = (int) locations.getWaiting().z() >> 4;
        }

        GsonComponentSerializer componentSerializer = GsonComponentSerializer.gson();
        List<ReplayParticipant> participants = new ArrayList<>();
        for (BedWarsPlayer player : game.getPlayers()) {
            BedWarsTeam team = game.getTeam(player.getTeamKey().name()).orElse(null);
            var skin = player.getSkin();
            Text prefix = team == null ? Text.empty() : Text.of("<color:{}><b>{} ", TextColor.color(team.getTeamKey().rgb()), team.firstLetter());
            participants.add(new ReplayParticipant(
                    player.getUuid(),
                    player.getEntityId(),
                    player.getUsername(),
                    skin != null ? skin.textures() : null,
                    skin != null ? skin.signature() : null,
                    componentSerializer.serialize(player.getDisplayName()),
                    componentSerializer.serialize(prefix.asComponent()),
                    componentSerializer.serialize(Text.empty().asComponent())
            ));
        }

        // Serialize and upload map, get the hash
        String mapName = game.getMapEntry().getName();
        final Instance instance = game.getInstance();
        String mapHash = serializeAndUploadMap(instance, mapName, centerChunkX, centerChunkZ);

        long startTime = System.currentTimeMillis();
        ReplayDescriptor descriptor = new ReplayDescriptor(
                recorder.getReplayId(), game.getGameId(), adapter.gameType(), ServerType.BEDWARS_GAME,
                HypixelConst.getServerName(), mapName, mapHash, locations.getWaiting() == null ? 0 : locations.getWaiting().x(),
                locations.getWaiting() == null ? 0 : locations.getWaiting().z(), ReplayVersion.CURRENT_VERSION,
                startTime, 0, 0, 0);
        recorder.start(
                descriptor,
                participants,
                adapter,
                () -> adapter.captureSnapshot(recorder)
        );

        // Register dispatchers
        dispatchers.register(new EntityLifecycleDispatcher(instance));
        dispatchers.register(new EntityLocationDispatcher(instance));
        dispatchers.register(new BlockChangeDispatcher());

        // Record initial player appearances
        recordInitialPlayerStates();

        // Record NPCs and generator displays created before recording started
        game.getWorldManager().recordShopNpcsForReplay();
        game.getGeneratorManager().recordInitialGeneratorDisplays();

        // Start tick task (every tick for accurate timing)
        tickTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!recording) return;
            recorder.tick();
            dispatchers.tick();
        }).repeat(TaskSchedule.tick(1)).schedule();

        Logger.info("Started replay recording for game {} (map: {})", game.getGameId(), mapName);
    }

    private void recordInitialPlayerStates() {
        for (BedWarsPlayer player : game.getPlayers()) {
            recorder.recordEntityState(player);
        }
    }

    public void recordPlayerAppearance(BedWarsPlayer player) {
        if (!recording) return;

        recorder.recordEntityState(player);
    }

    public void recordPlayerTeam(BedWarsPlayer player) {
        if (!recording) return;
        recorder.recordDelta(adapter.teamMembershipDelta(player.getUuid(), player.getTeamKey().name()));
        recorder.recordEntityState(player);
    }

    public void recordPlayerHealth(BedWarsPlayer player) {
        if (!recording) return;
        recorder.recordEntityState(player);
    }

    public void recordBedRespawned(TeamKey teamKey) {
        if (!recording) return;
        recorder.recordDelta(adapter.bedStateDelta(teamKey.name(), true));
        // Record the block placement for the bed
        var team = game.getMapEntry().getConfiguration().getTeams().get(teamKey);
        if (team != null && team.getBed() != null) {
            var bedPos = team.getBed();
            if (bedPos.feet() != null) {
                int x = (int) bedPos.feet().x();
                int y = (int) bedPos.feet().y();
                int z = (int) bedPos.feet().z();
                int blockState = game.getInstance().getBlock(x, y, z).stateId();
                recorder.recordDelta(new ReplayBlockDelta(new ReplayBlockPosition(x, y, z), blockState));
            }
            if (bedPos.head() != null) {
                int x = (int) bedPos.head().x();
                int y = (int) bedPos.head().y();
                int z = (int) bedPos.head().z();
                int blockState = game.getInstance().getBlock(x, y, z).stateId();
                recorder.recordDelta(new ReplayBlockDelta(new ReplayBlockPosition(x, y, z), blockState));
            }
        }
    }

    public void recordKill(BedWarsPlayer killer, BedWarsPlayer victim, BedWarsDeathType deathType, boolean isFinalKill) {
        if (!recording) return;

        if (isFinalKill) {
            recorder.recordEvent(new ReplayBookmarkEvent(Text.literal("Final Death").asComponent(), victim.getUuid()));
        }
    }

    public void recordPlayerDeath(BedWarsPlayer victim, BedWarsPlayer killer, Text deathMessage) {
        if (!recording) return;
        adapter.markDying(victim.getUuid());
        recorder.recordEvent(new ReplayEntityAnimationEvent(victim.getEntityId(),
                ReplayEntityAnimationEvent.Animation.TAKE_DAMAGE));
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.DEATH_MESSAGE, deathMessage.asComponent()));
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!recording) return;
            adapter.markDead(victim.getUuid());
            recorder.recordEntityState(victim);
        }).delay(TaskSchedule.tick(20)).schedule();
    }

    public void recordPlayerState(BedWarsPlayer player) {
        if (recording) recorder.recordEntityState(player);
    }

    public void recordPlayerRespawn(BedWarsPlayer player) {
        if (!recording) return;
        adapter.markAlive(player.getUuid());
        recorder.recordEntityState(player);
    }

    /**
     * Stops recording and finalizes the replay.
     */
    public void stopRecording() {
        if (!recording) return;
        recording = false;

        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }

        dispatchers.cleanup();
        recorder.finish();

        Logger.info("Stopped replay recording for game {}", game.getGameId());
    }

    public void recordBedDestroyed(TeamKey teamKey, BedWarsPlayer destroyer) {
        if (!recording) return;
        recorder.recordDelta(adapter.bedStateDelta(teamKey.name(), false));

        var team = game.getMapEntry().getConfiguration().getTeams().get(teamKey);
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.ANNOUNCEMENT,
                BedWarsReplayMessages.bedDestroyed(teamKey, destroyer).asComponent()));
        recorder.recordEvent(new ReplayBookmarkEvent(Text.of("{} Bed Destroyed", teamKey.getName()).asComponent(),
                destroyer == null ? null : destroyer.getUuid()));

        // Also record the block change
        if (team != null && team.getBed() != null) {
            var bedPos = team.getBed();
            if (bedPos.feet() != null) {
                int x = (int) bedPos.feet().x();
                int y = (int) bedPos.feet().y();
                int z = (int) bedPos.feet().z();
                recorder.recordDelta(new ReplayBlockDelta(new ReplayBlockPosition(x, y, z), Block.AIR.stateId()));
            }
            if (bedPos.head() != null) {
                int x = (int) bedPos.head().x();
                int y = (int) bedPos.head().y();
                int z = (int) bedPos.head().z();
                recorder.recordDelta(new ReplayBlockDelta(new ReplayBlockPosition(x, y, z), Block.AIR.stateId()));
            }
        }
    }

    public void recordTeamElimination(TeamKey teamKey) {
        if (!recording) return;
        recorder.recordDelta(adapter.teamEliminationDelta(teamKey.name()));
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.ANNOUNCEMENT,
                BedWarsReplayMessages.teamEliminated(teamKey).asComponent()));
    }

    /**
     * Records a generator upgrade event.
     *
     * @param generatorType // 0=diamond, 1=emerald
     * @param tier          // 1, 2, 3
     */
    public void recordGeneratorUpgrade(byte generatorType, byte tier) {
        if (!recording) return;
        recorder.recordDelta(adapter.generatorTierDelta(generatorType, tier));
    }

    public void recordDroppedItem(ItemEntity itemEntity) {
        if (!recording) return;
        recorder.recordEntityState(itemEntity);
    }

    public void recordItemPickup(int itemEntityId, int collectorEntityId) {
        if (!recording) return;
        recorder.recordDelta(new ReplayEntityRemoveDelta(itemEntityId));
    }

    public void recordEntityDespawn(int entityId) {
        if (!recording) return;
        recorder.recordDelta(new ReplayEntityRemoveDelta(entityId));
    }

    public void recordPlayerChat(BedWarsPlayer player, Text message, boolean isShout) {
        if (!recording) return;
        recorder.recordEvent(new ReplayComponentEvent(ReplayComponentEvent.Kind.CHAT,
                BedWarsReplayMessages.chat(player, message, isShout).asComponent()));
    }

    public void recordParticle(ParticlePacket particlePacket) {
        if (!recording) return;
        byte[] data = NetworkBuffer.makeArray(networkBuffer -> ParticlePacket.SERIALIZER.write(networkBuffer, particlePacket));
        recorder.recordEvent(new ReplayParticleEvent(data));
    }

    public void recordSound(Sound sound, double x, double y, double z) {
        if (!recording) return;
        recorder.recordEvent(new ReplaySoundEvent(
            sound.name().asString(),
            (byte) sound.source().ordinal(),
            x, y, z,
            sound.volume(),
            sound.pitch()
        ));
    }

    public void recordEntityAnimation(EntityAnimationPacket packet) {
        if (!recording) return;
        ReplayEntityAnimationEvent.Animation animationType = switch (packet.animation()) {
            case SWING_MAIN_ARM -> ReplayEntityAnimationEvent.Animation.SWING_MAIN_HAND;
            case SWING_OFF_HAND -> ReplayEntityAnimationEvent.Animation.SWING_OFF_HAND;
            case TAKE_DAMAGE -> ReplayEntityAnimationEvent.Animation.TAKE_DAMAGE;
            case LEAVE_BED -> ReplayEntityAnimationEvent.Animation.LEAVE_BED;
            case CRITICAL_EFFECT -> ReplayEntityAnimationEvent.Animation.CRITICAL_EFFECT;
            case MAGICAL_CRITICAL_EFFECT -> ReplayEntityAnimationEvent.Animation.MAGIC_CRITICAL_EFFECT;
        };
        recorder.recordEvent(new ReplayEntityAnimationEvent(packet.entityId(), animationType));
    }

    public void recordBlockBreakAnimation(BlockBreakAnimationPacket packet) {
        if (!recording) return;
        recorder.recordEvent(new ReplayBlockBreakEvent(
            packet.entityId(),
                new ReplayBlockPosition(packet.blockPosition().blockX(), packet.blockPosition().blockY(),
                        packet.blockPosition().blockZ()),
            packet.destroyStage()
        ));
    }

    public void recordPlayerInvisibility(BedWarsPlayer player, boolean invisible) {
        if (!recording) return;
        recorder.recordEntityState(player);
    }

    public void recordGeneratorDisplay(int entityId, UUID entityUuid, Pos position,
                                       List<String> textLines, String displayType, String identifier) {
        if (!recording) return;
        recorder.recordDelta(adapter.displayCreateDelta(entityId, entityUuid, position, textLines, displayType, identifier));
    }

    public void recordTextDisplayUpdate(int entityId, List<String> newTextLines, boolean replaceAll, int startIndex) {
        if (!recording) return;
        recorder.recordDelta(adapter.displayUpdateDelta(entityId, newTextLines, replaceAll, startIndex));
    }

    public void recordBelowNameTag(BedWarsPlayer player, int health) {
        if (!recording) return;
        recorder.recordEntityState(player);
    }

    public void recordShopNpc(
        int entityId,
        Pos position,
        String[] holograms,
        String npcType,
        int replayEntityTypeId,
        String replayTextureValue,
        String replayTextureSignature
    ) {
        if (!recording) return;

        UUID uuid = UUID.randomUUID();
        recorder.recordDelta(adapter.syntheticNpc(entityId, uuid, replayEntityTypeId, position,
                replayTextureValue, replayTextureSignature == null ? "" : replayTextureSignature));
        recorder.recordDelta(adapter.npcPresentationDelta(entityId, npcType, Arrays.asList(holograms)));
    }

    public static byte[] serializeItemStack(ItemStack itemStack) {
        try {
            CompoundBinaryTag nbt = itemStack.toItemNBT();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryTagIO.writer().writeNameless(nbt, out);

            return out.toByteArray();
        } catch (Exception e) {
            Logger.error(e, "Failed to serialize item stack");
            return new byte[0];
        }
    }

    public BlockChangeDispatcher getBlockChangeDispatcher() {
        return dispatchers.getDispatcher(BlockChangeDispatcher.class);
    }

    public EntityLifecycleDispatcher getEntityLifecycleDispatcher() {
        return dispatchers.getDispatcher(EntityLifecycleDispatcher.class);
    }

    private static final int MAP_CHUNK_RADIUS = 8; // this is a max limit.


    private String serializeAndUploadMap(Instance instance, String mapName, int centerChunkX, int centerChunkZ) {
        try {
            MapSerializer.SerializedMap serializedMap = MapSerializer.serializeRegion(
                instance, centerChunkX, centerChunkZ, MAP_CHUNK_RADIUS
            );

            String mapHash = serializedMap.hash();

            var uploadMsg = new ReplayMapUploadProtocolObject.MapUploadMessage(
                mapHash, mapName, serializedMap.compressedData()
            );
            sendToService(uploadMsg);

            Logger.info("Map {} uploaded: {} -> {} bytes ({}% compression)",
                mapName, serializedMap.uncompressedSize(), serializedMap.compressedSize(),
                100 - (serializedMap.compressedSize() * 100 / Math.max(1, serializedMap.uncompressedSize())));

            return mapHash;

        } catch (Exception e) {
            Logger.error(e, "Failed to serialize map {}", mapName);
            return mapName.toLowerCase().replace(" ", "_");
        }
    }
}
