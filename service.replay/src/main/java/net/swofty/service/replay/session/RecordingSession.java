package net.swofty.service.replay.session;

import lombok.Getter;
import lombok.Setter;
import net.swofty.commons.protocol.objects.replay.ReplayProtocolDto;
import net.swofty.commons.replay.protocol.ReplayChunk;
import net.swofty.commons.replay.protocol.ReplayFormat;
import net.swofty.commons.replay.protocol.ReplaySection;
import net.swofty.type.game.replay.codec.ReplaySnapshotCodec;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

@Getter
public class RecordingSession {
    private final ReplayProtocolDto.Metadata metadata;
    private final Map<ReplaySection, ConcurrentSkipListMap<Integer, ReplayChunk>> chunks = new EnumMap<>(ReplaySection.class);
    private volatile long lastDataTime = System.currentTimeMillis();
    @Setter
    private long endTime;
    @Setter
    private int durationTicks;

    public RecordingSession(ReplayProtocolDto.Metadata metadata) {
        this.metadata = metadata;
        for (ReplaySection section : ReplaySection.values()) chunks.put(section, new ConcurrentSkipListMap<>());
    }

    public void addChunk(ReplayChunk chunk) throws IOException {
        ReplayFormat.readChunk(chunk);
        ReplayChunk existing = chunks.get(chunk.section()).putIfAbsent(chunk.sequence(), chunk);
        if (existing != null && !existing.equals(chunk)) {
            throw new IOException("Conflicting replay chunk " + chunk.section() + "/" + chunk.sequence());
        }
        lastDataTime = System.currentTimeMillis();
    }

    public List<ReplayChunk> getOrderedChunks() {
        List<ReplayChunk> result = new ArrayList<>();
        chunks.values().forEach(values -> result.addAll(values.values()));
        result.sort(Comparator.comparing(ReplayChunk::section).thenComparingInt(ReplayChunk::sequence));
        return List.copyOf(result);
    }

    public void validateComplete() throws IOException {
        if (metadata.descriptor().formatVersion() != ReplayFormat.MAJOR_VERSION) {
            throw new IOException("Unsupported replay format version: " + metadata.descriptor().formatVersion());
        }
        if (durationTicks < 0 || durationTicks > ReplayFormat.MAX_TICKS)
            throw new IOException("Invalid replay duration: " + durationTicks);
        List<ReplayChunk> ordered = getOrderedChunks();
        ReplayFormat.validateOrdered(ordered, ReplaySection.SNAPSHOT);
        ReplayFormat.validateOrdered(ordered, ReplaySection.DELTA);
        ReplayFormat.validateOrdered(ordered, ReplaySection.EVENT);
        List<ReplayChunk> snapshots = chunks.get(ReplaySection.SNAPSHOT).values().stream().toList();
        if (snapshots.isEmpty() || snapshots.getFirst().startTick() != 0)
            throw new IOException("Replay is missing the tick zero snapshot");
        if (snapshots.getLast().endTick() != durationTicks)
            throw new IOException("Replay is missing the final snapshot");
        int previousSnapshotTick = -1;
        for (ReplayChunk chunk : snapshots) {
            for (byte[] entry : ReplayFormat.readChunk(chunk)) {
                int snapshotTick = ReplaySnapshotCodec.read(entry).tick();
                if (snapshotTick <= previousSnapshotTick || snapshotTick < chunk.startTick() || snapshotTick > chunk.endTick()) {
                    throw new IOException("Invalid replay snapshot index at tick " + snapshotTick);
                }
                if (previousSnapshotTick >= 0 && snapshotTick - previousSnapshotTick > 200) {
                    throw new IOException("Replay snapshot interval exceeds 200 ticks");
                }
                previousSnapshotTick = snapshotTick;
            }
        }
        if (previousSnapshotTick != durationTicks)
            throw new IOException("Replay final snapshot tick does not match duration");
    }

    public long getTotalBytesReceived() {
        return getOrderedChunks().stream().mapToLong(chunk -> chunk.compressedPayload().length).sum();
    }
}
