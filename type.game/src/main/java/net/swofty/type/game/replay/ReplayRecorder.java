package net.swofty.type.game.replay;

import lombok.Getter;
import net.minestom.server.entity.Entity;
import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.replay.*;
import net.swofty.commons.replay.protocol.ReplayChunk;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.commons.replay.protocol.ReplayFormat;
import net.swofty.commons.replay.protocol.ReplaySection;
import net.swofty.type.game.replay.api.*;
import net.swofty.type.game.replay.codec.ReplaySnapshotCodec;
import net.swofty.type.game.replay.delta.ReplayBlockDelta;
import net.swofty.type.game.replay.delta.ReplayEntityUpsertDelta;
import net.swofty.type.game.replay.model.*;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReplayRecorder {
    public static final int SNAPSHOT_INTERVAL_TICKS = 200;
    private static final int BATCH_SIZE = 1000;
    private static final int BATCH_INTERVAL_TICKS = 200;

    @Getter
    private final UUID replayId;
    private final String gameId;
    private final ServerType serverType;
    private final Consumer<Object> serviceSender;
    private final Map<ReplaySection, ConcurrentLinkedQueue<PendingEntry>> buffers = new EnumMap<>(ReplaySection.class);
    private final Map<ReplaySection, AtomicInteger> sequences = new EnumMap<>(ReplaySection.class);
    private final AtomicInteger bufferedCount = new AtomicInteger();
    private final Map<ReplayBlockPosition, Integer> blockOverlay = new java.util.concurrent.ConcurrentHashMap<>();

    @Getter
    private volatile int currentTick;
    private volatile int lastBatchTick;
    private volatile boolean recording;
    private volatile boolean finished;
    private volatile Supplier<ReplaySnapshot> snapshotSupplier;
    private volatile int lastSnapshotTick = -1;
    private volatile Function<Entity, net.swofty.type.game.replay.model.ReplayEntityState> entityCapture;
    private volatile ReplayEntityVisibilityPolicy entityVisibility = entity -> true;

    public ReplayRecorder(String gameId, ServerType serverType, Consumer<Object> serviceSender) {
        this.replayId = UUID.randomUUID();
        this.gameId = gameId;
        this.serverType = serverType;
        this.serviceSender = serviceSender;
        for (ReplaySection section : ReplaySection.values()) {
            buffers.put(section, new ConcurrentLinkedQueue<>());
            sequences.put(section, new AtomicInteger());
        }
    }

    public <M extends net.swofty.type.game.replay.api.ReplayGameMetadata,
            S extends net.swofty.type.game.replay.api.ReplayGameState> void start(
            ReplayDescriptor descriptor,
            List<ReplayParticipant> participants,
            ReplayGameAdapter<M, S> adapter,
            Supplier<ReplaySnapshot> snapshotSupplier
    ) {
        try {
            ReplayDataWriter metadataWriter = new ReplayDataWriter();
            adapter.writeMetadata(metadataWriter, adapter.captureMetadata());
            start(new ReplayMetadata(
                    descriptor,
                    participants,
                    new net.swofty.type.game.replay.model.ReplayGameMetadataEnvelope(
                            adapter.gameType(), adapter.metadataSchemaVersion(), metadataWriter.toByteArray())
            ), snapshotSupplier);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to encode replay metadata", exception);
        }
    }

    public void start(ReplayMetadata metadata, Supplier<ReplaySnapshot> snapshotSupplier) {
        if (recording || finished) throw new IllegalStateException("Replay recorder has already been started");
        ReplayDescriptor descriptor = metadata.descriptor();
        if (!descriptor.replayId().equals(replayId))
            throw new IllegalArgumentException("Replay descriptor ID does not match recorder ID");
        if (descriptor.formatVersion() != ReplayVersion.CURRENT_VERSION) {
            throw new IllegalArgumentException("Replay descriptor format must be " + ReplayVersion.CURRENT_VERSION);
        }
        this.snapshotSupplier = snapshotSupplier;
        recording = true;
        currentTick = 0;
        lastBatchTick = 0;
        serviceSender.accept(new ReplayStartProtocolObject.StartMessage(toDto(metadata)));
        captureSnapshot();
        Logger.info("Started replay recording {} for game {}", replayId, gameId);
    }

    public void tick() {
        if (!isRecording()) return;
        currentTick++;
        if (currentTick % SNAPSHOT_INTERVAL_TICKS == 0) captureSnapshot();
        if (bufferedCount.get() >= BATCH_SIZE || currentTick - lastBatchTick >= BATCH_INTERVAL_TICKS) flushAll();
    }

    public void recordDelta(ReplayStateDelta delta) {
        if (delta instanceof ReplayBlockDelta block) {
            blockOverlay.put(block.position(), block.blockStateId());
        }
        record(ReplaySection.DELTA, delta.typeId(), delta::write);
    }

    public void recordEvent(ReplayEvent event) {
        record(ReplaySection.EVENT, event.typeId(), event::write);
    }

    public void configureEntityCapture(Function<Entity, ReplayEntityState> capture,
                                       ReplayEntityVisibilityPolicy visibility) {
        this.entityCapture = java.util.Objects.requireNonNull(capture);
        this.entityVisibility = java.util.Objects.requireNonNull(visibility);
    }

    public void recordEntityState(Entity entity) {
        if (!isEntityReplayVisible(entity)) return;
        Function<Entity, ReplayEntityState> capture = entityCapture;
        if (capture == null) throw new IllegalStateException("Replay entity capture is not configured");
        recordDelta(new ReplayEntityUpsertDelta(capture.apply(entity)));
    }

    public boolean isEntityReplayVisible(Entity entity) {
        return entityVisibility.isReplayVisible(entity);
    }

    public void recordSnapshot(ReplaySnapshot snapshot) {
        if (!isRecording()) return;
        if (snapshot.tick() != currentTick)
            throw new IllegalArgumentException("Snapshot tick does not match recorder tick");
        try {
            buffers.get(ReplaySection.SNAPSHOT).offer(new PendingEntry(currentTick, ReplaySnapshotCodec.write(snapshot)));
            bufferedCount.incrementAndGet();
            lastSnapshotTick = currentTick;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to encode replay snapshot at tick " + currentTick, exception);
        }
    }

    private void record(ReplaySection section, int typeId, ReplayTypeRegistry.ReplayEntryWriter writer) {
        if (!isRecording()) return;
        try {
            buffers.get(section).offer(new PendingEntry(currentTick, ReplayTypeRegistry.encode(currentTick, typeId, writer)));
            if (bufferedCount.incrementAndGet() >= BATCH_SIZE) flushAll();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to encode replay entry at tick " + currentTick + " type " + typeId, exception);
        }
    }

    private void captureSnapshot() {
        if (snapshotSupplier == null) return;
        ReplaySnapshot snapshot = snapshotSupplier.get();
        if (snapshot == null) throw new IllegalStateException("Replay snapshot supplier returned null");
        recordSnapshot(snapshot);
    }

    private synchronized void flushAll() {
        for (ReplaySection section : ReplaySection.values()) flush(section);
        lastBatchTick = currentTick;
    }

    private void flush(ReplaySection section) {
        ConcurrentLinkedQueue<PendingEntry> buffer = buffers.get(section);
        if (buffer.isEmpty()) return;
        List<PendingEntry> pending = new ArrayList<>();
        PendingEntry entry;
        while ((entry = buffer.poll()) != null) {
            pending.add(entry);
            bufferedCount.decrementAndGet();
        }
        try {
            ReplayChunk chunk = ReplayFormat.createChunk(
                    section,
                    sequences.get(section).getAndIncrement(),
                    pending.getFirst().tick(),
                    pending.getLast().tick(),
                    pending.stream().map(PendingEntry::data).toList()
            );
            serviceSender.accept(new ReplayDataBatchProtocolObject.BatchMessage(
                    replayId, chunk.section(), chunk.sequence(), chunk.startTick(), chunk.endTick(),
                    chunk.uncompressedLength(), chunk.recordCount(), chunk.checksum(), chunk.compressedPayload()));
        } catch (Exception exception) {
            pending.forEach(buffer::offer);
            bufferedCount.addAndGet(pending.size());
            throw new IllegalStateException("Failed to flush replay " + section + " chunk", exception);
        }
    }

    public synchronized void finish() {
        if (finished) return;
        if (!recording) throw new IllegalStateException("Replay recorder has not been started");
        if (lastSnapshotTick != currentTick) captureSnapshot();
        flushAll();
        recording = false;
        finished = true;
        serviceSender.accept(new ReplayEndProtocolObject.EndMessage(replayId, System.currentTimeMillis(), currentTick));
        Logger.info("Finished replay recording {} ({} ticks)", replayId, currentTick);
    }

    public void uploadMapIfNeeded(String mapHash, String mapName, byte[] compressedData) {
        serviceSender.accept(new ReplayMapUploadProtocolObject.MapUploadMessage(mapHash, mapName, compressedData));
    }

    public Map<ReplayBlockPosition, Integer> snapshotBlockOverlay() {
        return Map.copyOf(blockOverlay);
    }

    public boolean isRecording() {
        return recording && !finished;
    }

    private ReplayProtocolDto.Metadata toDto(ReplayMetadata metadata) {
        ReplayDescriptor descriptor = metadata.descriptor();
        ReplayProtocolDto.Descriptor descriptorDto = new ReplayProtocolDto.Descriptor(
                descriptor.replayId(), descriptor.gameId(), descriptor.gameType(), descriptor.serverType(), descriptor.serverId(),
                descriptor.mapName(), descriptor.mapHash(), descriptor.mapCenterX(), descriptor.mapCenterZ(),
                descriptor.formatVersion(), descriptor.startTime(), descriptor.endTime(), descriptor.durationTicks(), descriptor.dataSize());
        List<ReplayProtocolDto.Participant> participants = metadata.participants().stream()
                .map(participant -> new ReplayProtocolDto.Participant(
                        participant.uuid(), participant.entityId(), participant.username(), participant.textureValue(),
                        participant.textureSignature(), participant.displayNameJson(), participant.prefixJson(), participant.suffixJson()))
                .toList();
        var gameMetadata = metadata.gameMetadata();
        return new ReplayProtocolDto.Metadata(descriptorDto, participants,
                new ReplayProtocolDto.GameMetadataEnvelope(gameMetadata.gameType(), gameMetadata.schemaVersion(), gameMetadata.payload()));
    }

    private record PendingEntry(int tick, byte[] data) {
    }
}
