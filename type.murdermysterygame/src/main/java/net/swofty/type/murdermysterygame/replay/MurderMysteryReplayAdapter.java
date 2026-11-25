package net.swofty.type.murdermysterygame.replay;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.TimedPotion;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.ReplayRecorder;
import net.swofty.type.game.replay.api.ReplayGameAdapter;
import net.swofty.type.game.replay.delta.ReplayGameStateDelta;
import net.swofty.type.game.replay.model.ReplayEntityState;
import net.swofty.type.game.replay.model.ReplayPotionEffectState;
import net.swofty.type.game.replay.model.ReplaySnapshot;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.role.GameRole;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MurderMysteryReplayAdapter implements ReplayGameAdapter<MurderMysteryReplayMetadata, MurderMysteryReplayState> {
    public static final String GAME_TYPE = "MURDER_MYSTERY";
    public static final int SCHEMA_VERSION = 1;
    public static final int DELTA_TYPE_ID = 1;

    private static final GsonComponentSerializer COMPONENTS = GsonComponentSerializer.gson();

    private final Game game;
    private final Set<UUID> eliminatedPlayers = new HashSet<>();
    private final Map<UUID, Integer> kills = new LinkedHashMap<>();
    private String winCondition;
    private UUID winner;

    public MurderMysteryReplayAdapter(Game game) {
        this.game = game;
    }

    @Override
    public String gameType() {
        return GAME_TYPE;
    }

    @Override
    public int metadataSchemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public MurderMysteryReplayMetadata captureMetadata() {
        Map<UUID, GameRole> roles = game.getRoleManager().getRoles();
        Map<UUID, UUID> targets = game.getRoleManager().getAssassinTargets();
        List<MurderMysteryReplayMetadata.PlayerDefinition> players = roles.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new MurderMysteryReplayMetadata.PlayerDefinition(
                        entry.getKey(), entry.getValue().name(), targets.get(entry.getKey())))
                .toList();
        return new MurderMysteryReplayMetadata(game.getGameType().name(), players);
    }

    @Override
    public MurderMysteryReplayState captureState() {
        Map<UUID, String> roles = new LinkedHashMap<>();
        game.getRoleManager().getRoles().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> roles.put(entry.getKey(), entry.getValue().name()));

        for (MurderMysteryPlayer player : game.getPlayers()) {
            if (player.isEliminated()) eliminatedPlayers.add(player.getUuid());
            kills.putIfAbsent(player.getUuid(), player.getKillsThisGame());
            kills.put(player.getUuid(), Math.max(kills.get(player.getUuid()), player.getKillsThisGame()));
        }

        return new MurderMysteryReplayState(
                roles,
                kills,
                eliminatedPlayers.stream().sorted().toList(),
                game.hasMurdererReceivedSword(),
                game.getDroppedDetectiveBow() != null,
                winCondition,
                winner
        );
    }

    @Override
    public void writeMetadata(ReplayDataWriter writer, MurderMysteryReplayMetadata metadata) throws IOException {
        writer.writeString(metadata.modeId());
        writer.writeVarInt(metadata.players().size());
        for (var player : metadata.players()) {
            writer.writeUUID(player.uuid());
            writer.writeString(player.role());
            writer.writeBoolean(player.target() != null);
            if (player.target() != null) writer.writeUUID(player.target());
        }
    }

    @Override
    public void writeState(ReplayDataWriter writer, MurderMysteryReplayState state) throws IOException {
        writer.writeVarInt(state.roles().size());
        for (var role : state.roles().entrySet()) {
            writer.writeUUID(role.getKey());
            writer.writeString(role.getValue());
        }
        writer.writeVarInt(state.kills().size());
        for (var kill : state.kills().entrySet()) {
            writer.writeUUID(kill.getKey());
            writer.writeVarInt(kill.getValue());
        }
        writer.writeVarInt(state.eliminatedPlayers().size());
        for (UUID uuid : state.eliminatedPlayers()) writer.writeUUID(uuid);
        writer.writeBoolean(state.murdererReceivedSword());
        writer.writeBoolean(state.detectiveBowAvailable());
        writeNullable(writer, state.winCondition());
        writer.writeBoolean(state.winner() != null);
        if (state.winner() != null) writer.writeUUID(state.winner());
    }

    public ReplaySnapshot captureSnapshot(ReplayRecorder recorder) {
        try {
            ReplayDataWriter stateWriter = new ReplayDataWriter();
            writeState(stateWriter, captureState());
            Map<Integer, ReplayEntityState> entities = new LinkedHashMap<>();
            for (Entity entity : game.getInstance().getEntities()) {
                if (isReplayVisible(entity)) entities.put(entity.getEntityId(), captureEntity(entity));
            }
            return new ReplaySnapshot(recorder.getCurrentTick(), recorder.snapshotBlockOverlay(), entities,
                    stateWriter.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to capture Murder Mystery replay snapshot", exception);
        }
    }

    public ReplayGameStateDelta killDelta(UUID killer, UUID victim, Game.KillType killType) {
        eliminatedPlayers.add(victim);
        kills.merge(killer, 1, Integer::sum);
        return gameDelta(writer -> {
            writer.writeByte(1);
            writer.writeUUID(killer);
            writer.writeUUID(victim);
            writer.writeString(killType.name());
        });
    }

    public ReplayGameStateDelta environmentalDeathDelta(UUID victim, String reason) {
        eliminatedPlayers.add(victim);
        return gameDelta(writer -> {
            writer.writeByte(2);
            writer.writeUUID(victim);
            writer.writeString(reason);
        });
    }

    public ReplayGameStateDelta gameEndDelta(String condition, UUID winner) {
        this.winCondition = condition;
        this.winner = winner;
        return gameDelta(writer -> {
            writer.writeByte(3);
            writer.writeString(condition);
            writer.writeBoolean(winner != null);
            if (winner != null) writer.writeUUID(winner);
        });
    }

    public ReplayEntityState captureEntity(Entity entity) {
        var position = entity.getPosition();
        var velocity = entity.getVelocity();
        Map<Integer, byte[]> equipment = new LinkedHashMap<>();
        List<ReplayPotionEffectState> effects = new ArrayList<>();
        float health = 0;
        float maximumHealth = 0;
        if (entity instanceof LivingEntity living) {
            health = living.getHealth();
            maximumHealth = (float) living.getAttribute(Attribute.MAX_HEALTH).getValue();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = living.getEquipment(slot);
                if (!item.isAir()) equipment.put(slot.ordinal(), MurderMysteryReplayManager.serializeItemStack(item));
            }
            for (TimedPotion timed : living.getActiveEffects()) {
                var potion = timed.potion();
                byte flags = (byte) ((potion.isAmbient() ? 1 : 0)
                        | (potion.hasParticles() ? 2 : 0)
                        | (potion.hasIcon() ? 4 : 0));
                effects.add(new ReplayPotionEffectState(potion.effect().id(), (byte) potion.amplifier(),
                        potion.duration(), flags));
            }
        }

        ReplayEntityState.PlayerState playerState = null;
        ReplayEntityState.Lifecycle lifecycle = ReplayEntityState.Lifecycle.ALIVE;
        if (entity instanceof Player player) {
            MurderMysteryPlayer murderMysteryPlayer = player instanceof MurderMysteryPlayer value ? value : null;
            boolean eliminated = murderMysteryPlayer != null && murderMysteryPlayer.isEliminated();
            boolean spectator = player.getGameMode() == GameMode.SPECTATOR;
            lifecycle = eliminated ? ReplayEntityState.Lifecycle.ELIMINATED
                    : spectator ? ReplayEntityState.Lifecycle.SPECTATOR
                    : player.isInvisible() ? ReplayEntityState.Lifecycle.DEAD_WAITING
                    : ReplayEntityState.Lifecycle.ALIVE;
            var skin = player.getSkin();
            GameRole role = murderMysteryPlayer == null ? null : game.getRoleManager().getRole(player.getUuid());
            playerState = new ReplayEntityState.PlayerState(
                    player.getUuid(), skin == null ? null : skin.textures(), skin == null ? null : skin.signature(),
                    COMPONENTS.serialize(player.getDisplayName()), role == null ? null : role.name(),
                    player.getGameMode().ordinal(), spectator && !eliminated,
                    MurderMysteryReplayManager.serializeItemStack(player.getItemInMainHand()));
        }

        byte[] typePayload = entity instanceof ItemEntity item
                ? MurderMysteryReplayManager.serializeItemStack(item.getItemStack())
                : new byte[0];
        int flags = (entity.isSneaking() ? 1 : 0) | (entity.isSprinting() ? 2 : 0);
        return new ReplayEntityState(entity.getEntityId(), entity.getUuid(), entity.getEntityType().id(),
                position.x(), position.y(), position.z(), position.yaw(), position.pitch(),
                velocity.x(), velocity.y(), velocity.z(), entity.getPose().ordinal(), !entity.isInvisible(),
                entity.isGlowing(), flags, lifecycle, equipment, health, maximumHealth, effects, playerState,
                typePayload);
    }

    public boolean isReplayVisible(Entity entity) {
        return entity instanceof Player || !entity.isInvisible();
    }

    public static MurderMysteryReplayMetadata readMetadata(ReplayDataReader reader) throws IOException {
        String mode = reader.readString();
        int count = checkedCount(reader.readVarInt(), 1024, "players");
        List<MurderMysteryReplayMetadata.PlayerDefinition> players = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            UUID uuid = reader.readUUID();
            String role = reader.readString();
            UUID target = reader.readBoolean() ? reader.readUUID() : null;
            players.add(new MurderMysteryReplayMetadata.PlayerDefinition(uuid, role, target));
        }
        return new MurderMysteryReplayMetadata(mode, players);
    }

    public static MurderMysteryReplayState readState(ReplayDataReader reader) throws IOException {
        int roleCount = checkedCount(reader.readVarInt(), 1024, "roles");
        Map<UUID, String> roles = new LinkedHashMap<>();
        for (int index = 0; index < roleCount; index++) roles.put(reader.readUUID(), reader.readString());
        int killCount = checkedCount(reader.readVarInt(), 1024, "kills");
        Map<UUID, Integer> kills = new LinkedHashMap<>();
        for (int index = 0; index < killCount; index++) kills.put(reader.readUUID(), reader.readVarInt());
        int eliminatedCount = checkedCount(reader.readVarInt(), 1024, "eliminated players");
        List<UUID> eliminated = new ArrayList<>(eliminatedCount);
        for (int index = 0; index < eliminatedCount; index++) eliminated.add(reader.readUUID());
        boolean sword = reader.readBoolean();
        boolean bow = reader.readBoolean();
        String condition = reader.readBoolean() ? reader.readString() : null;
        UUID winner = reader.readBoolean() ? reader.readUUID() : null;
        return new MurderMysteryReplayState(roles, kills, eliminated, sword, bow, condition, winner);
    }

    private ReplayGameStateDelta gameDelta(DeltaWriter value) {
        try {
            ReplayDataWriter writer = new ReplayDataWriter();
            value.write(writer);
            return new ReplayGameStateDelta(DELTA_TYPE_ID, writer.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to encode Murder Mystery replay delta", exception);
        }
    }

    private static void writeNullable(ReplayDataWriter writer, String value) throws IOException {
        writer.writeBoolean(value != null);
        if (value != null) writer.writeString(value);
    }

    private static int checkedCount(int value, int maximum, String name) throws IOException {
        if (value < 0 || value > maximum) throw new IOException("Invalid Murder Mystery " + name + " count");
        return value;
    }

    @FunctionalInterface
    private interface DeltaWriter {
        void write(ReplayDataWriter writer) throws IOException;
    }
}
