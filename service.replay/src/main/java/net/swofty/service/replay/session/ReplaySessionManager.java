package net.swofty.service.replay.session;

import net.swofty.commons.protocol.objects.replay.ReplayProtocolDto;
import net.swofty.commons.replay.protocol.ReplayChunk;
import net.swofty.commons.replay.protocol.ReplayFormat;
import net.swofty.commons.replay.protocol.ReplaySection;
import net.swofty.service.replay.storage.ReplayDatabase;
import net.swofty.type.game.replay.codec.ReplayHeaderCodec;
import net.swofty.type.game.replay.codec.ReplaySnapshotCodec;
import net.swofty.type.game.replay.model.ReplayDescriptor;
import net.swofty.type.game.replay.model.ReplayGameMetadataEnvelope;
import net.swofty.type.game.replay.model.ReplayHeader;
import net.swofty.type.game.replay.model.ReplayMetadata;
import net.swofty.type.game.replay.model.ReplayParticipant;
import org.bson.Document;
import org.bson.types.Binary;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReplaySessionManager {
    private static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000;
    private final ReplayDatabase database;
    private final Map<UUID, RecordingSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<EndResult>> finalizations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService finalizationExecutor = Executors.newFixedThreadPool(4);

    public ReplaySessionManager(ReplayDatabase database) {
        this.database = database;
    }

    public RecordingSession startSession(ReplayProtocolDto.Metadata metadata) {
        UUID replayId = metadata.descriptor().replayId();
        RecordingSession session = new RecordingSession(metadata);
        RecordingSession existing = activeSessions.putIfAbsent(replayId, session);
        if (existing != null) {
            if (!sameMetadata(existing.getMetadata(), metadata)) {
                throw new IllegalStateException("Replay session already exists with different metadata: " + replayId);
            }
            return existing;
        }
        Logger.info("Started replay recording session {} for game {}", replayId, metadata.descriptor().gameId());
        return session;
    }

    private boolean sameMetadata(ReplayProtocolDto.Metadata first, ReplayProtocolDto.Metadata second) {
        if (!first.descriptor().equals(second.descriptor()) || !first.participants().equals(second.participants())) {
            return false;
        }
        var firstGameMetadata = first.gameMetadata();
        var secondGameMetadata = second.gameMetadata();
        return firstGameMetadata.gameType().equals(secondGameMetadata.gameType())
                && firstGameMetadata.schemaVersion() == secondGameMetadata.schemaVersion()
                && Arrays.equals(firstGameMetadata.payload(), secondGameMetadata.payload());
    }

    public RecordingSession getSession(UUID replayId) {
        return activeSessions.get(replayId);
    }

    public boolean isFinalized(UUID replayId) {
        Document metadata = database.getReplayMetadata(replayId);
        return metadata != null && metadata.getBoolean("complete", false);
    }

    public void receiveChunk(UUID replayId, ReplayChunk chunk) throws Exception {
        RecordingSession session = activeSessions.get(replayId);
        if (session == null) throw new IllegalStateException("Unknown replay session: " + replayId);
        session.addChunk(chunk);
    }

    public CompletableFuture<EndResult> endSession(UUID replayId, long endTime, int durationTicks) {
        RecordingSession session = activeSessions.get(replayId);
        if (session == null) {
            Document metadata = database.getReplayMetadata(replayId);
            if (metadata != null && metadata.getBoolean("complete", false)) {
                long dataSize = metadata.getLong("dataSize");
                return CompletableFuture.completedFuture(new EndResult(true, dataSize, dataSize));
            }
            return CompletableFuture.completedFuture(new EndResult(false, 0, 0));
        }
        session.setEndTime(endTime);
        session.setDurationTicks(durationTicks);
        CompletableFuture<EndResult> finalization = finalizations.computeIfAbsent(replayId,
                ignored -> CompletableFuture.supplyAsync(() -> finalizeSession(replayId, session), finalizationExecutor));
        finalization.whenComplete((result, error) -> finalizations.remove(replayId, finalization));
        return finalization;
    }

    private EndResult finalizeSession(UUID replayId, RecordingSession session) {
        try {
            session.validateComplete();
            List<ReplayChunk> chunks = session.getOrderedChunks();
            long compressedBytes = 0;
            for (ReplayChunk chunk : chunks) {
                database.saveReplayChunk(replayId, chunk);
                compressedBytes += chunk.compressedPayload().length;
            }
            database.saveReplayMetadata(createMetadataDocument(session, compressedBytes));
            activeSessions.remove(replayId, session);
            Logger.info("Replay {} finalized with {} validated chunks", replayId, chunks.size());
            return new EndResult(true, compressedBytes, compressedBytes);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to finalize replay {}", replayId);
            return new EndResult(false, 0, 0);
        }
    }

    private Document createMetadataDocument(RecordingSession session, long dataSize) throws Exception {
        ReplayProtocolDto.Metadata metadata = session.getMetadata();
        ReplayProtocolDto.Descriptor descriptor = metadata.descriptor();
        List<Document> participants = metadata.participants().stream().map(participant -> new Document()
                .append("uuid", participant.uuid().toString())
                .append("entityId", participant.entityId())
                .append("username", participant.username())
                .append("textureValue", participant.textureValue())
                .append("textureSignature", participant.textureSignature())
                .append("displayNameJson", participant.displayNameJson())
                .append("prefixJson", participant.prefixJson())
                .append("suffixJson", participant.suffixJson())).toList();
        ReplayDescriptor replayDescriptor = new ReplayDescriptor(
                descriptor.replayId(), descriptor.gameId(), descriptor.gameType(), descriptor.serverType(), descriptor.serverId(),
                descriptor.mapName(), descriptor.mapHash(), descriptor.mapCenterX(), descriptor.mapCenterZ(), descriptor.formatVersion(),
                descriptor.startTime(), session.getEndTime(), session.getDurationTicks(), dataSize);
        List<ReplayParticipant> replayParticipants = metadata.participants().stream().map(participant -> new ReplayParticipant(
                participant.uuid(), participant.entityId(), participant.username(), participant.textureValue(), participant.textureSignature(),
                participant.displayNameJson(), participant.prefixJson(), participant.suffixJson())).toList();
        ReplayMetadata replayMetadata = new ReplayMetadata(replayDescriptor, replayParticipants,
                new ReplayGameMetadataEnvelope(metadata.gameMetadata().gameType(), metadata.gameMetadata().schemaVersion(), metadata.gameMetadata().payload()));
        List<Integer> snapshotIndex = new ArrayList<>();
        for (ReplayChunk chunk : session.getOrderedChunks()) {
            if (chunk.section() != ReplaySection.SNAPSHOT) continue;
            for (byte[] entry : ReplayFormat.readChunk(chunk))
                snapshotIndex.add(ReplaySnapshotCodec.read(entry).tick());
        }
        byte[] header = ReplayHeaderCodec.write(new ReplayHeader(replayMetadata, snapshotIndex));
        return new Document()
                .append("replayId", descriptor.replayId().toString())
                .append("gameId", descriptor.gameId())
                .append("gameType", descriptor.gameType())
                .append("serverType", descriptor.serverType().name())
                .append("serverId", descriptor.serverId())
                .append("mapName", descriptor.mapName())
                .append("mapHash", descriptor.mapHash())
                .append("mapCenterX", descriptor.mapCenterX())
                .append("mapCenterZ", descriptor.mapCenterZ())
                .append("formatVersion", descriptor.formatVersion())
                .append("startTime", descriptor.startTime())
                .append("endTime", session.getEndTime())
                .append("durationTicks", session.getDurationTicks())
                .append("dataSize", dataSize)
                .append("players", metadata.participants().stream().map(value -> value.uuid().toString()).toList())
                .append("participants", participants)
                .append("metadataGameType", metadata.gameMetadata().gameType())
                .append("metadataSchemaVersion", metadata.gameMetadata().schemaVersion())
                .append("metadataPayload", new Binary(metadata.gameMetadata().payload()))
                .append("header", new Binary(header))
                .append("complete", true);
    }

    public void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            activeSessions.entrySet().removeIf(entry -> now - entry.getValue().getLastDataTime() > SESSION_TIMEOUT_MS);
        }, 1, 1, TimeUnit.MINUTES);
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    public record EndResult(boolean success, long totalBytes, long compressedBytes) {
    }
}
