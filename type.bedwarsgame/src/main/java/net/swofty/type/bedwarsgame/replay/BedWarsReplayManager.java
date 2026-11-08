package net.swofty.type.bedwarsgame.replay;

import lombok.Getter;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.SwingAnimationPacket;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.protocol.objects.replay.ReplayMapUploadProtocolObject;
import net.swofty.commons.protocol.objects.replay.ReplayStartProtocolObject;
import net.swofty.commons.scoreboard.ScoreboardData;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.bedwarsgame.death.BedWarsDeathType;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.game.v2.BedWarsTeam;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.replay.ReplayRecorder;
import net.swofty.type.game.replay.dispatcher.BlockChangeDispatcher;
import net.swofty.type.game.replay.dispatcher.DispatcherManager;
import net.swofty.type.game.replay.dispatcher.EntityLocationDispatcher;
import net.swofty.type.game.replay.recordable.*;
import net.swofty.type.game.replay.recordable.bedwars.RecordableBedDestruction;
import net.swofty.type.game.replay.recordable.bedwars.RecordableGeneratorUpgrade;
import net.swofty.type.game.replay.recordable.bedwars.RecordableKill;
import net.swofty.type.game.replay.recordable.bedwars.RecordableTeamElimination;
import net.swofty.type.generic.HypixelConst;
import org.tinylog.Logger;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BedWarsReplayManager {
    private final BedWarsGame game;
    private final ProxyService replayService;
    private final ExecutorService replayExecutor;

    @Getter
    private final ReplayRecorder recorder;
    @Getter
    private final DispatcherManager dispatchers;

    private Task tickTask;
    private Task scoreboardTask;

    private ScoreboardData lastScoreboardState;

    @Getter
    private boolean recording = false;

    public BedWarsReplayManager(BedWarsGame game, ProxyService replayService) {
        this.game = game;
        this.replayService = replayService;
        this.replayExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "bedwars-replay-send");
            thread.setDaemon(true);
            return thread;
        });
        this.recorder = new ReplayRecorder(game.getGameId(), ServerType.BEDWARS_GAME, this::sendToService);
        this.dispatchers = new DispatcherManager(recorder);
    }

    /**
     * Sends replay data to the replay service.
     */
    private void sendToService(Object data) {
        if (replayService == null) {
            Logger.debug("No replay service configured, skipping: {}", data.getClass().getSimpleName());
            return;
        }

        replayExecutor.execute(() -> {
            try {
                replayService.handleRequest(data).exceptionally(e -> {
                    Logger.error(e, "Failed to send replay data to service");
                    return null;
                });
            } catch (Exception e) {
                Logger.error(e, "Failed to send replay data to service");
            }
        });
    }

    public void startRecording() {
        if (recording) return;
        recording = true;

        // Set map center for coordinate optimization
        var locations = game.getMapEntry().getConfiguration().getLocations();
        int centerChunkX = 0, centerChunkZ = 0;
        if (locations.getWaiting() != null) {
            recorder.setMapCenter(locations.getWaiting().x(), locations.getWaiting().z());
            centerChunkX = (int) locations.getWaiting().x() >> 4;
            centerChunkZ = (int) locations.getWaiting().z() >> 4;
        }

        // Collect player info
        Map<UUID, String> players = new HashMap<>();
        game.getPlayers().forEach(p -> players.put(p.getUuid(), p.getUsername()));

        // Collect team info
        Map<String, List<UUID>> teams = new HashMap<>();
        Map<String, ReplayStartProtocolObject.TeamInfo> teamInfo = new HashMap<>();

        for (BedWarsTeam team : game.getTeams()) {
            String teamId = team.getTeamKey().name();
            List<UUID> teamPlayers = new ArrayList<>(team.getPlayerIds());
            teams.put(teamId, teamPlayers);

            teamInfo.put(teamId, new ReplayStartProtocolObject.TeamInfo(
                team.getName(),
                Text.colorTag(team.getColor()),
                team.getTeamKey().rgb()
            ));
        }

        // Serialize and upload map, get the hash
        String mapName = game.getMapEntry().getName();
        final Instance instance = game.getInstance();
        String mapHash = serializeAndUploadMap(instance, mapName, centerChunkX, centerChunkZ);

        // Start recording session
        recorder.start(
            game.getGameType().name(),
            HypixelConst.getServerName(),
            mapName,
            mapHash,
            players,
            teams,
            teamInfo
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
            recordPlayerAppearance(player);
        }
    }

    public void recordPlayerAppearance(BedWarsPlayer player) {
        if (!recording) return;

        int entityId = player.getEntityId();
        UUID uuid = player.getUuid();

        var skin = player.getSkin();
        if (skin != null) {
            recorder.record(new RecordablePlayerSkin(
                entityId, uuid, skin.textures(), skin.signature()
            ));
        }

        BedWarsTeam team = game.getTeam(player.getTeamKey().name()).orElse(null);
        String prefix = team != null
                ? Text.of("<color:{}><l>{}</l> ", team.getColor(), team.getTeamKey().name().charAt(0)).serialize()
                : "";
        int nameColor = team != null ? team.getTeamKey().rgb() : -1;

        recorder.record(new RecordablePlayerDisplayName(
            entityId, player.getUsername(), prefix, "", nameColor
        ));

        // Record initial health (use 20 as default max health)
        recorder.record(new RecordablePlayerHealth(
            entityId, player.getHealth(), 20.0f
        ));
    }

    public void recordPlayerHealth(BedWarsPlayer player) {
        if (!recording) return;
        recorder.record(new RecordablePlayerHealth(
            player.getEntityId(), player.getHealth(), 20.0f
        ));
    }

    public void recordBedRespawned(TeamKey teamKey) {
        if (!recording) return;
        // Record the block placement for the bed
        var team = game.getMapEntry().getConfiguration().getTeams().get(teamKey);
        if (team != null && team.getBed() != null) {
            var bedPos = team.getBed();
            if (bedPos.feet() != null) {
                int x = (int) bedPos.feet().x();
                int y = (int) bedPos.feet().y();
                int z = (int) bedPos.feet().z();
                int blockState = game.getInstance().getBlock(x, y, z).stateId();
                recorder.record(new RecordableBlockChange(
                    x, y, z,
                    blockState, Block.AIR.stateId()
                ));
            }
            if (bedPos.head() != null) {
                int x = (int) bedPos.head().x();
                int y = (int) bedPos.head().y();
                int z = (int) bedPos.head().z();
                int blockState = game.getInstance().getBlock(x, y, z).stateId();
                recorder.record(new RecordableBlockChange(
                    x, y, z,
                    blockState, Block.AIR.stateId()
                ));
            }
        }
    }

    public void recordKill(BedWarsPlayer killer, BedWarsPlayer victim, BedWarsDeathType deathType, boolean isFinalKill) {
        if (!recording) return;

        byte victimTeamId = (byte) victim.getTeamKey().ordinal();
        byte deathCause = mapDeathCause(deathType);
        byte finalKillFlag = (byte) (isFinalKill ? 1 : 0);

        recorder.record(new RecordableKill(
            victim.getEntityId(),
            victim.getUuid(),
            killer != null ? killer.getEntityId() : -1,
            killer != null ? killer.getUuid() : null,
            victimTeamId,
            deathCause,
            finalKillFlag
        ));
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

        if (scoreboardTask != null) {
            scoreboardTask.cancel();
            scoreboardTask = null;
        }

        dispatchers.cleanup();
        recorder.finish();
        replayExecutor.shutdown();

        Logger.info("Stopped replay recording for game {}", game.getGameId());
    }

    public void recordBedDestroyed(TeamKey teamKey, BedWarsPlayer destroyer) {
        if (!recording) return;

        byte teamId = (byte) teamKey.ordinal();
        int destroyerEntityId = destroyer != null ? destroyer.getEntityId() : -1;
        UUID destroyerUuid = destroyer != null ? destroyer.getUuid() : null;

        // Get bed position
        var team = game.getMapEntry().getConfiguration().getTeams().get(teamKey);
        int bedX = 0, bedY = 0, bedZ = 0;
        if (team != null && team.getBed() != null && team.getBed().feet() != null) {
            bedX = (int) team.getBed().feet().x();
            bedY = (int) team.getBed().feet().y();
            bedZ = (int) team.getBed().feet().z();
        }

        recorder.record(new RecordableBedDestruction(
            teamId, destroyerEntityId, destroyerUuid, bedX, bedY, bedZ
        ));

        // Also record the block change
        if (team != null && team.getBed() != null) {
            var bedPos = team.getBed();
            if (bedPos.feet() != null) {
                int x = (int) bedPos.feet().x();
                int y = (int) bedPos.feet().y();
                int z = (int) bedPos.feet().z();
                int blockState = game.getInstance().getBlock(x, y, z).stateId();
                recorder.record(new RecordableBlockChange(
                    x, y, z,
                    Block.AIR.stateId(), blockState
                ));
            }
            if (bedPos.head() != null) {
                int x = (int) bedPos.head().x();
                int y = (int) bedPos.head().y();
                int z = (int) bedPos.head().z();
                int blockState = game.getInstance().getBlock(x, y, z).stateId();
                recorder.record(new RecordableBlockChange(
                    x, y, z,
                    Block.AIR.stateId(), blockState
                ));
            }
        }
    }

    private byte mapDeathCause(BedWarsDeathType deathType) {
        return switch (deathType) {
            case GENERIC -> 0;
            case GENERIC_ASSISTED -> 1;
            case VOID -> 2;
            case VOID_ASSISTED -> 3;
            case BOW -> 4;
            case ENTITY -> 5;
        };
    }

    public void recordTeamElimination(TeamKey teamKey) {
        if (!recording) return;
        recorder.record(new RecordableTeamElimination((byte) teamKey.ordinal()));
    }

    /**
     * Records a generator upgrade event.
     *
     * @param generatorType // 0=diamond, 1=emerald
     * @param tier          // 1, 2, 3
     */
    public void recordGeneratorUpgrade(byte generatorType, byte tier) {
        if (!recording) return;
        recorder.record(new RecordableGeneratorUpgrade(generatorType, tier));
    }

    public void recordDroppedItem(ItemEntity itemEntity) {
        if (!recording) return;

        try {
            Pos pos = itemEntity.getPosition();
            Vec velocity = itemEntity.getVelocity();
            ItemStack itemStack = itemEntity.getItemStack();

            // Serialize item to NBT bytes
            byte[] itemNbt = serializeItemStack(itemStack);

            // Calculate despawn tick
            int despawnTick = recorder.getCurrentTick() + 6000;

            recorder.record(new RecordableDroppedItem(
                itemEntity.getEntityId(),
                itemEntity.getUuid(),
                pos.x(), pos.y(), pos.z(),
                (float) velocity.x(), (float) velocity.y(), (float) velocity.z(),
                itemNbt,
                10, // pickup delay in ticks
                despawnTick
            ));
        } catch (Exception e) {
            Logger.error(e, "Failed to record dropped item");
        }
    }

    public void recordItemPickup(int itemEntityId, int collectorEntityId) {
        if (!recording) return;
        recorder.record(new RecordableItemPickup(itemEntityId, collectorEntityId));
    }

    public void recordEntityDespawn(int entityId) {
        if (!recording) return;
        recorder.record(new RecordableEntityDespawn(entityId));
    }

    public void recordPlayerChat(BedWarsPlayer player, String message, boolean isShout) {
        if (!recording) return;
        recorder.record(new RecordablePlayerChat(
            player.getEntityId(),
            message,
            isShout
        ));
    }

    public void recordParticle(ParticlePacket particlePacket) {
        if (!recording) return;
        byte[] data = NetworkBuffer.makeArray(ParticlePacket.SERIALIZER, particlePacket, MinecraftServer.process().registries());
        recorder.record(new RecordableParticle(data));
    }

    public void recordSound(Sound sound, double x, double y, double z) {
        if (!recording) return;
        recorder.record(new RecordableSound(
            sound.name().asString(),
            (byte) sound.source().ordinal(),
            x, y, z,
            sound.volume(),
            sound.pitch()
        ));
    }

    public void recordEntityAnimation(EntityAnimationPacket packet) {
        if (!recording) return;
        RecordableEntityAnimation.AnimationType animationType = switch (packet.animation()) {
            case LEAVE_BED -> RecordableEntityAnimation.AnimationType.LEAVE_BED;
            case CRITICAL_EFFECT -> RecordableEntityAnimation.AnimationType.CRITICAL_EFFECT;
            case MAGICAL_CRITICAL_EFFECT -> RecordableEntityAnimation.AnimationType.MAGIC_CRITICAL_EFFECT;
        };
        recorder.record(new RecordableEntityAnimation(packet.entityId(), animationType));
    }

    public void recordSwingAnimation(SwingAnimationPacket packet) {
        if (!recording) return;
        recorder.record(new RecordableEntityAnimation(packet.entityId(), packet.hand() == PlayerHand.MAIN
                ? RecordableEntityAnimation.AnimationType.SWING_MAIN_HAND
                : RecordableEntityAnimation.AnimationType.SWING_OFFHAND));
    }

    public void recordDamageAnimation(DamageEventPacket packet) {
        if (!recording) return;
        recorder.record(new RecordableEntityAnimation(packet.targetEntityId(), RecordableEntityAnimation.AnimationType.TAKE_DAMAGE));
    }

    public void recordBlockBreakAnimation(BlockBreakAnimationPacket packet) {
        if (!recording) return;
        recorder.record(new RecordableBlockBreakAnimation(
            packet.entityId(),
            packet.blockPosition().blockX(),
            packet.blockPosition().blockY(),
            packet.blockPosition().blockZ(),
            packet.destroyStage()
        ));
    }

    public void recordPlayerInvisibility(BedWarsPlayer player, boolean invisible) {
        if (!recording) return;
        int effectId = PotionEffect.INVISIBILITY.id();
        if (invisible) {
            recorder.record(new RecordableEntityEffect(
                player.getEntityId(),
                effectId,
                (byte) 0,
                Integer.MAX_VALUE, // Infinite duration
                (byte) 0x06 // No particles, show icon
            ));
        }
    }

    public void recordGeneratorDisplay(int entityId, UUID entityUuid, Pos position,
                                       List<String> textLines, String displayType, String identifier) {
        if (!recording) return;
        recorder.record(new RecordableDynamicTextDisplay(
            entityId, entityUuid,
            position.x(), position.y(), position.z(),
            textLines, displayType, identifier
        ));
    }

    public void recordTextDisplayUpdate(int entityId, List<String> newTextLines, boolean replaceAll, int startIndex) {
        if (!recording) return;
        recorder.record(new RecordableTextDisplayUpdate(entityId, newTextLines, replaceAll, startIndex));
    }

    public void recordBelowNameTag(BedWarsPlayer player, int health) {
        if (!recording) return;
        recorder.record(new RecordablePlayerHealth(
            player.getEntityId(), health, 20.0f
        ));
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

        // Record NPC spawn
        recorder.record(new RecordableEntitySpawn(
            entityId,
            UUID.randomUUID(),
            replayEntityTypeId,
            position.x(), position.y(), position.z(),
            position.yaw(), position.pitch()
        ));

        if (replayEntityTypeId == EntityType.PLAYER.id()
            && replayTextureValue != null
            && !replayTextureValue.isBlank()) {
            recorder.record(new RecordablePlayerSkin(
                entityId,
                UUID.randomUUID(),
                replayTextureValue,
                replayTextureSignature == null ? "" : replayTextureSignature
            ));
        }

        // Record NPC display name (last line of holograms is typically the name)
        String displayName = holograms.length > 0 ? holograms[holograms.length - 1] : npcType;
        recorder.record(new RecordableNpcDisplayName(entityId, displayName, "", "", -1, true));

        // Record hologram text lines
        if (holograms.length > 1) {
            List<String> textLines = new ArrayList<>(Arrays.asList(holograms).subList(0, holograms.length - 1));
            recorder.record(new RecordableNpcTextLine(entityId, textLines, 2.5, 0));
        }
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
