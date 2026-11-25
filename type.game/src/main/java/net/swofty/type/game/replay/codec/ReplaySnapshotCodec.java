package net.swofty.type.game.replay.codec;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.model.ReplayBlockPosition;
import net.swofty.type.game.replay.model.ReplayEntityState;
import net.swofty.type.game.replay.model.ReplayPotionEffectState;
import net.swofty.type.game.replay.model.ReplaySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReplaySnapshotCodec {
    public static final int MAX_BLOCKS = 2_000_000;
    public static final int MAX_ENTITIES = 100_000;
    public static final int MAX_EQUIPMENT_SLOTS = 16;
    public static final int MAX_EFFECTS = 128;
    public static final int MAX_ENTITY_PAYLOAD = 1_048_576;
    public static final int MAX_GAME_STATE_PAYLOAD = 16 * 1024 * 1024;

    private ReplaySnapshotCodec() {
    }

    public static byte[] write(ReplaySnapshot snapshot) throws IOException {
        if (snapshot.blockOverlay().size() > MAX_BLOCKS || snapshot.entities().size() > MAX_ENTITIES
                || snapshot.gameStatePayload().length > MAX_GAME_STATE_PAYLOAD) {
            throw new IOException("Replay snapshot exceeds configured limits");
        }
        ReplayDataWriter writer = new ReplayDataWriter();
        writer.writeVarInt(snapshot.tick());
        writer.writeVarInt(snapshot.blockOverlay().size());
        for (var entry : snapshot.blockOverlay().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            writer.writeBlockCoords(entry.getKey().x(), entry.getKey().y(), entry.getKey().z());
            writer.writeVarInt(entry.getValue());
        }
        writer.writeVarInt(snapshot.entities().size());
        for (var entry : snapshot.entities().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            writeEntity(writer, entry.getValue());
        }
        writer.writeBytes(snapshot.gameStatePayload());
        return writer.toByteArray();
    }

    public static ReplaySnapshot read(byte[] data) throws IOException {
        try (ReplayDataReader reader = new ReplayDataReader(data)) {
            int tick = reader.readVarInt();
            int blockCount = checkedCount(reader.readVarInt(), MAX_BLOCKS, "snapshot blocks");
            Map<ReplayBlockPosition, Integer> blocks = new LinkedHashMap<>(blockCount);
            for (int index = 0; index < blockCount; index++) {
                int[] position = reader.readBlockCoords();
                blocks.put(new ReplayBlockPosition(position[0], position[1], position[2]), reader.readVarInt());
            }
            int entityCount = checkedCount(reader.readVarInt(), MAX_ENTITIES, "snapshot entities");
            Map<Integer, ReplayEntityState> entities = new LinkedHashMap<>(entityCount);
            for (int index = 0; index < entityCount; index++) {
                ReplayEntityState entity = readEntity(reader);
                if (entities.put(entity.replayEntityId(), entity) != null) {
                    throw new IOException("Duplicate replay entity ID: " + entity.replayEntityId());
                }
            }
            byte[] gameState = reader.readBytes(MAX_GAME_STATE_PAYLOAD);
            if (reader.available() != 0) throw new IOException("Trailing snapshot data");
            return new ReplaySnapshot(tick, blocks, entities, gameState);
        }
    }

    private static void writeEntity(ReplayDataWriter writer, ReplayEntityState entity) throws IOException {
        if (entity.equipment().size() > MAX_EQUIPMENT_SLOTS || entity.effects().size() > MAX_EFFECTS
                || entity.typePayload().length > MAX_ENTITY_PAYLOAD
                || entity.equipment().values().stream().anyMatch(value -> value.length > MAX_ENTITY_PAYLOAD)
                || entity.player() != null && entity.player().heldItem().length > MAX_ENTITY_PAYLOAD) {
            throw new IOException("Replay entity exceeds configured limits: " + entity.replayEntityId());
        }
        writer.writeVarInt(entity.replayEntityId());
        writer.writeBoolean(entity.uuid() != null);
        if (entity.uuid() != null) writer.writeUUID(entity.uuid());
        writer.writeVarInt(entity.entityTypeId());
        writer.writeDouble(entity.x());
        writer.writeDouble(entity.y());
        writer.writeDouble(entity.z());
        writer.writeFloat(entity.yaw());
        writer.writeFloat(entity.pitch());
        writer.writeDouble(entity.velocityX());
        writer.writeDouble(entity.velocityY());
        writer.writeDouble(entity.velocityZ());
        writer.writeVarInt(entity.poseId());
        writer.writeBoolean(entity.visible());
        writer.writeBoolean(entity.glowing());
        writer.writeVarInt(entity.flags());
        writer.writeByte(entity.lifecycle().ordinal());
        writer.writeVarInt(entity.equipment().size());
        for (var equipment : entity.equipment().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            writer.writeVarInt(equipment.getKey());
            writer.writeBytes(equipment.getValue());
        }
        writer.writeFloat(entity.health());
        writer.writeFloat(entity.maximumHealth());
        writer.writeVarInt(entity.effects().size());
        for (ReplayPotionEffectState effect : entity.effects()) {
            writer.writeVarInt(effect.effectId());
            writer.writeByte(effect.amplifier());
            writer.writeVarInt(effect.remainingTicks());
            writer.writeByte(effect.flags());
        }
        writer.writeBoolean(entity.player() != null);
        if (entity.player() != null) writePlayer(writer, entity.player());
        writer.writeBytes(entity.typePayload());
    }

    private static ReplayEntityState readEntity(ReplayDataReader reader) throws IOException {
        int entityId = reader.readVarInt();
        var uuid = reader.readBoolean() ? reader.readUUID() : null;
        int entityTypeId = reader.readVarInt();
        double x = reader.readDouble();
        double y = reader.readDouble();
        double z = reader.readDouble();
        float yaw = reader.readFloat();
        float pitch = reader.readFloat();
        double velocityX = reader.readDouble();
        double velocityY = reader.readDouble();
        double velocityZ = reader.readDouble();
        int poseId = reader.readVarInt();
        boolean visible = reader.readBoolean();
        boolean glowing = reader.readBoolean();
        int flags = reader.readVarInt();
        int lifecycleId = reader.readUnsignedByte();
        if (lifecycleId >= ReplayEntityState.Lifecycle.values().length)
            throw new IOException("Unknown entity lifecycle: " + lifecycleId);
        int equipmentCount = checkedCount(reader.readVarInt(), MAX_EQUIPMENT_SLOTS, "equipment slots");
        Map<Integer, byte[]> equipment = new LinkedHashMap<>(equipmentCount);
        for (int index = 0; index < equipmentCount; index++) {
            equipment.put(reader.readVarInt(), reader.readBytes(MAX_ENTITY_PAYLOAD));
        }
        float health = reader.readFloat();
        float maximumHealth = reader.readFloat();
        int effectCount = checkedCount(reader.readVarInt(), MAX_EFFECTS, "potion effects");
        var effects = new ArrayList<ReplayPotionEffectState>(effectCount);
        for (int index = 0; index < effectCount; index++) {
            effects.add(new ReplayPotionEffectState(reader.readVarInt(), (byte) reader.readByte(), reader.readVarInt(), (byte) reader.readByte()));
        }
        ReplayEntityState.PlayerState player = reader.readBoolean() ? readPlayer(reader) : null;
        byte[] payload = reader.readBytes(MAX_ENTITY_PAYLOAD);
        return new ReplayEntityState(entityId, uuid, entityTypeId, x, y, z, yaw, pitch,
                velocityX, velocityY, velocityZ, poseId, visible, glowing, flags,
                ReplayEntityState.Lifecycle.values()[lifecycleId], equipment, health, maximumHealth, effects, player, payload);
    }

    private static void writePlayer(ReplayDataWriter writer, ReplayEntityState.PlayerState player) throws IOException {
        writer.writeUUID(player.participantUuid());
        writeNullable(writer, player.textureValue());
        writeNullable(writer, player.textureSignature());
        writer.writeString(player.displayJson());
        writeNullable(writer, player.teamId());
        writer.writeVarInt(player.gameMode());
        writer.writeBoolean(player.legitimateSpectator());
        writer.writeBytes(player.heldItem());
    }

    private static ReplayEntityState.PlayerState readPlayer(ReplayDataReader reader) throws IOException {
        return new ReplayEntityState.PlayerState(
                reader.readUUID(), readNullable(reader), readNullable(reader), reader.readString(), readNullable(reader),
                reader.readVarInt(), reader.readBoolean(), reader.readBytes(MAX_ENTITY_PAYLOAD));
    }

    private static void writeNullable(ReplayDataWriter writer, String value) throws IOException {
        writer.writeBoolean(value != null);
        if (value != null) writer.writeString(value);
    }

    private static String readNullable(ReplayDataReader reader) throws IOException {
        return reader.readBoolean() ? reader.readString() : null;
    }

    private static int checkedCount(int count, int maximum, String name) throws IOException {
        if (count < 0 || count > maximum)
            throw new IOException("Invalid " + name + " count: " + Integer.toUnsignedLong(count));
        return count;
    }
}
