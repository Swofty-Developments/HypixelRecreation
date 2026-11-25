package net.swofty.service.replay.endpoints;

import net.swofty.commons.protocol.objects.replay.ReplayLoadProtocolObject;
import net.swofty.commons.protocol.objects.replay.ReplayProtocolDto;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.replay.protocol.ReplayChunk;
import net.swofty.commons.replay.protocol.ReplayFormat;
import net.swofty.commons.replay.protocol.ReplaySection;
import net.swofty.service.replay.ReplayService;
import net.swofty.type.game.replay.codec.ReplayHeaderCodec;
import net.swofty.type.game.replay.codec.ReplaySnapshotCodec;
import net.swofty.type.game.replay.model.ReplayHeader;
import org.bson.Document;
import org.bson.types.Binary;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public class ReplayLoadEndpoint implements RedisMessageHandler<ReplayLoadProtocolObject.LoadRequest, ReplayLoadProtocolObject.LoadResponse> {
    @Override
    public ReplayLoadProtocolObject protocol() {
        return new ReplayLoadProtocolObject();
    }

    @Override
    public ReplayLoadProtocolObject.LoadResponse handle(ReplayLoadProtocolObject.LoadRequest msg, RedisMessageContext context) {
        try {
            Document document = ReplayService.getDatabase().getReplayMetadata(msg.replayId());
            if (document == null || !document.getBoolean("complete", false))
                return failure("Replay is unavailable or unfinished");
            ReplayHeader header = parseHeader(document);
            ReplayProtocolDto.Metadata metadata = toDto(header);
            if (metadata.descriptor().formatVersion() != ReplayFormat.MAJOR_VERSION) {
                return failure("Unsupported replay format version: " + metadata.descriptor().formatVersion());
            }
            List<ReplayChunk> chunks = new ArrayList<>();
            for (Document chunk : ReplayService.getDatabase().getReplayDataChunks(msg.replayId())) {
                chunks.add(new ReplayChunk(
                        ReplaySection.valueOf(chunk.getString("section")), chunk.getInteger("sequence"),
                        chunk.getInteger("startTick"), chunk.getInteger("endTick"), chunk.getInteger("uncompressedLength"),
                        chunk.getInteger("recordCount"), chunk.getInteger("checksum"), chunk.get("data", Binary.class).getData()));
            }
            ReplayFormat.validateOrdered(chunks, ReplaySection.SNAPSHOT);
            ReplayFormat.validateOrdered(chunks, ReplaySection.DELTA);
            ReplayFormat.validateOrdered(chunks, ReplaySection.EVENT);
            List<ReplayChunk> snapshots = chunks.stream().filter(value -> value.section() == ReplaySection.SNAPSHOT).toList();
            if (snapshots.isEmpty() || snapshots.getFirst().startTick() != 0
                    || snapshots.getLast().endTick() != metadata.descriptor().durationTicks()) {
                return failure("Replay snapshot index is incomplete");
            }
            int previousSnapshotTick = -1;
            List<Integer> actualSnapshotIndex = new ArrayList<>();
            for (ReplayChunk snapshotChunk : snapshots) {
                for (byte[] entry : ReplayFormat.readChunk(snapshotChunk)) {
                    int tick = ReplaySnapshotCodec.read(entry).tick();
                    if (tick <= previousSnapshotTick || tick < snapshotChunk.startTick() || tick > snapshotChunk.endTick()) {
                        return failure("Replay snapshot index is corrupt");
                    }
                    if (previousSnapshotTick >= 0 && tick - previousSnapshotTick > 200) {
                        return failure("Replay snapshot interval is corrupt");
                    }
                    actualSnapshotIndex.add(tick);
                    previousSnapshotTick = tick;
                }
            }
            if (previousSnapshotTick != metadata.descriptor().durationTicks())
                return failure("Replay final snapshot is missing");
            if (!actualSnapshotIndex.equals(header.snapshotIndex()))
                return failure("Replay snapshot index does not match stored data");
            return new ReplayLoadProtocolObject.LoadResponse(true, null, metadata, List.copyOf(chunks));
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load replay {}", msg.replayId());
            return failure("Replay data is corrupt or incomplete");
        }
    }

    private ReplayLoadProtocolObject.LoadResponse failure(String message) {
        return new ReplayLoadProtocolObject.LoadResponse(false, message, null, null);
    }

    private ReplayHeader parseHeader(Document document) throws Exception {
        Binary headerData = document.get("header", Binary.class);
        if (headerData == null) throw new IllegalArgumentException("Replay header is missing");
        return ReplayHeaderCodec.read(headerData.getData());
    }

    private ReplayProtocolDto.Metadata toDto(ReplayHeader header) {
        var descriptor = header.metadata().descriptor();
        ReplayProtocolDto.Descriptor descriptorDto = new ReplayProtocolDto.Descriptor(
                descriptor.replayId(), descriptor.gameId(), descriptor.gameType(), descriptor.serverType(), descriptor.serverId(),
                descriptor.mapName(), descriptor.mapHash(), descriptor.mapCenterX(), descriptor.mapCenterZ(), descriptor.formatVersion(),
                descriptor.startTime(), descriptor.endTime(), descriptor.durationTicks(), descriptor.dataSize());
        var participants = header.metadata().participants().stream().map(participant -> new ReplayProtocolDto.Participant(
                participant.uuid(), participant.entityId(), participant.username(), participant.textureValue(), participant.textureSignature(),
                participant.displayNameJson(), participant.prefixJson(), participant.suffixJson())).toList();
        var envelope = header.metadata().gameMetadata();
        return new ReplayProtocolDto.Metadata(descriptorDto, participants,
                new ReplayProtocolDto.GameMetadataEnvelope(envelope.gameType(), envelope.schemaVersion(), envelope.payload()));
    }
}
