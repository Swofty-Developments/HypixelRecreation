package net.swofty.type.game.replay.codec;

import net.swofty.commons.ServerType;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.commons.replay.protocol.ReplayFormat;
import net.swofty.type.game.replay.model.*;

import java.io.IOException;
import java.util.ArrayList;

public final class ReplayHeaderCodec {
    public static final int MAX_PARTICIPANTS = 10_000;
    public static final int MAX_SNAPSHOTS = ReplayFormat.MAX_TICKS / 20 + 2;

    private ReplayHeaderCodec() {
    }

    public static byte[] write(ReplayHeader header) throws IOException {
        ReplayDescriptor descriptor = header.metadata().descriptor();
        if (descriptor.formatVersion() != ReplayFormat.MAJOR_VERSION) {
            throw new IOException("Unsupported replay format version: " + descriptor.formatVersion());
        }
        if (descriptor.durationTicks() > ReplayFormat.MAX_TICKS || header.metadata().participants().size() > MAX_PARTICIPANTS) {
            throw new IOException("Replay header exceeds configured limits");
        }
        if (!descriptor.gameType().equals(header.metadata().gameMetadata().gameType())) {
            throw new IOException("Replay metadata game type mismatch");
        }
        validateSnapshotIndex(header.snapshotIndex(), descriptor.durationTicks());
        ReplayDataWriter writer = new ReplayDataWriter();
        writer.writeInt(ReplayFormat.MAGIC);
        writer.writeVarInt(ReplayFormat.MAJOR_VERSION);
        writer.writeUUID(descriptor.replayId());
        writer.writeString(descriptor.gameId());
        writer.writeString(descriptor.gameType());
        writer.writeString(descriptor.serverType().name());
        writer.writeString(descriptor.serverId());
        writer.writeString(descriptor.mapName());
        writer.writeString(descriptor.mapHash());
        writer.writeDouble(descriptor.mapCenterX());
        writer.writeDouble(descriptor.mapCenterZ());
        writer.writeLong(descriptor.startTime());
        writer.writeLong(descriptor.endTime());
        writer.writeVarInt(descriptor.durationTicks());
        writer.writeVarLong(descriptor.dataSize());
        writer.writeVarInt(header.metadata().participants().size());
        for (ReplayParticipant participant : header.metadata().participants()) {
            writer.writeUUID(participant.uuid());
            writer.writeVarInt(participant.entityId());
            writer.writeString(participant.username());
            writeNullable(writer, participant.textureValue());
            writeNullable(writer, participant.textureSignature());
            writer.writeString(participant.displayNameJson());
            writer.writeString(participant.prefixJson());
            writer.writeString(participant.suffixJson());
        }
        ReplayGameMetadataEnvelope envelope = header.metadata().gameMetadata();
        writer.writeString(envelope.gameType());
        writer.writeVarInt(envelope.schemaVersion());
        writer.writeBytes(envelope.payload());
        writer.writeVarInt(header.snapshotIndex().size());
        for (int tick : header.snapshotIndex()) writer.writeVarInt(tick);
        return writer.toByteArray();
    }

    public static ReplayHeader read(byte[] bytes) throws IOException {
        try (ReplayDataReader reader = new ReplayDataReader(bytes)) {
            if (reader.readInt() != ReplayFormat.MAGIC) throw new IOException("Invalid replay magic");
            int version = reader.readVarInt();
            if (version != ReplayFormat.MAJOR_VERSION)
                throw new IOException("Unsupported replay format version: " + version);
            var replayId = reader.readUUID();
            String gameId = reader.readString();
            String gameType = reader.readString();
            ServerType serverType;
            try {
                serverType = ServerType.valueOf(reader.readString());
            } catch (IllegalArgumentException exception) {
                throw new IOException("Unknown replay server type", exception);
            }
            String serverId = reader.readString();
            String mapName = reader.readString();
            String mapHash = reader.readString();
            double mapCenterX = reader.readDouble();
            double mapCenterZ = reader.readDouble();
            long startTime = reader.readLong();
            long endTime = reader.readLong();
            int duration = reader.readVarInt();
            long dataSize = reader.readVarLong();
            if (duration > ReplayFormat.MAX_TICKS || dataSize < 0)
                throw new IOException("Invalid replay duration or size");
            int participantCount = checked(reader.readVarInt(), MAX_PARTICIPANTS, "participants");
            var participants = new ArrayList<ReplayParticipant>(participantCount);
            for (int index = 0; index < participantCount; index++) {
                participants.add(new ReplayParticipant(reader.readUUID(), reader.readVarInt(), reader.readString(),
                        readNullable(reader), readNullable(reader), reader.readString(), reader.readString(), reader.readString()));
            }
            String metadataGameType = reader.readString();
            int schemaVersion = reader.readVarInt();
            byte[] payload = reader.readBytes(ReplaySnapshotCodec.MAX_GAME_STATE_PAYLOAD);
            int snapshotCount = checked(reader.readVarInt(), MAX_SNAPSHOTS, "snapshots");
            var snapshotIndex = new ArrayList<Integer>(snapshotCount);
            int previous = -1;
            for (int index = 0; index < snapshotCount; index++) {
                int tick = reader.readVarInt();
                if (tick <= previous || tick > duration) throw new IOException("Invalid replay snapshot index");
                snapshotIndex.add(tick);
                previous = tick;
            }
            if (snapshotIndex.isEmpty() || snapshotIndex.getFirst() != 0 || snapshotIndex.getLast() != duration) {
                throw new IOException("Incomplete replay snapshot index");
            }
            if (!gameType.equals(metadataGameType)) throw new IOException("Replay metadata game type mismatch");
            if (reader.available() != 0) throw new IOException("Trailing replay header data");
            ReplayDescriptor descriptor = new ReplayDescriptor(replayId, gameId, gameType, serverType, serverId, mapName, mapHash,
                    mapCenterX, mapCenterZ, version, startTime, endTime, duration, dataSize);
            ReplayMetadata metadata = new ReplayMetadata(descriptor, participants,
                    new ReplayGameMetadataEnvelope(metadataGameType, schemaVersion, payload));
            return new ReplayHeader(metadata, snapshotIndex);
        }
    }

    private static void writeNullable(ReplayDataWriter writer, String value) throws IOException {
        writer.writeBoolean(value != null);
        if (value != null) writer.writeString(value);
    }

    private static String readNullable(ReplayDataReader reader) throws IOException {
        return reader.readBoolean() ? reader.readString() : null;
    }

    private static int checked(int value, int maximum, String name) throws IOException {
        if (value < 0 || value > maximum) throw new IOException("Invalid replay " + name + " count");
        return value;
    }

    private static void validateSnapshotIndex(java.util.List<Integer> snapshots, int duration) throws IOException {
        if (snapshots.isEmpty() || snapshots.size() > MAX_SNAPSHOTS || snapshots.getFirst() != 0
                || snapshots.getLast() != duration) throw new IOException("Incomplete replay snapshot index");
        int previous = -1;
        for (int tick : snapshots) {
            if (tick <= previous || tick > duration) throw new IOException("Invalid replay snapshot index");
            previous = tick;
        }
    }
}
