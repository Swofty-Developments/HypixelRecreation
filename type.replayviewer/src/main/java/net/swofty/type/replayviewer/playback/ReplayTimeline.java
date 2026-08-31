package net.swofty.type.replayviewer.playback;

import net.swofty.commons.replay.protocol.ReplayChunk;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayFormat;
import net.swofty.commons.replay.protocol.ReplaySection;
import net.swofty.type.game.replay.api.CoreReplayTypes;
import net.swofty.type.game.replay.api.ReplayEvent;
import net.swofty.type.game.replay.api.ReplayStateDelta;
import net.swofty.type.game.replay.api.ReplayTypeRegistry;
import net.swofty.type.game.replay.codec.ReplaySnapshotCodec;
import net.swofty.type.game.replay.model.ReplayBlockPosition;
import net.swofty.type.game.replay.model.ReplaySnapshot;

import java.io.IOException;
import java.util.*;

public final class ReplayTimeline {
    private final NavigableMap<Integer, ReplaySnapshot> snapshots = new TreeMap<>();
    private final NavigableMap<Integer, List<ReplayStateDelta>> stateDeltas = new TreeMap<>();
    private final NavigableMap<Integer, List<ReplayEvent>> transientEvents = new TreeMap<>();
    private final ReplayTypeRegistry<ReplayStateDelta> deltaTypes = CoreReplayTypes.deltas();
    private final ReplayTypeRegistry<ReplayEvent> eventTypes = CoreReplayTypes.events();
    private final NavigableSet<Integer> populatedTicks = new TreeSet<>();

    public void load(List<ReplayChunk> chunks, int durationTicks) throws IOException {
        if (durationTicks < 0 || durationTicks > ReplayFormat.MAX_TICKS)
            throw new IOException("Invalid replay duration: " + durationTicks);
        ReplayFormat.validateOrdered(chunks, ReplaySection.SNAPSHOT);
        ReplayFormat.validateOrdered(chunks, ReplaySection.DELTA);
        ReplayFormat.validateOrdered(chunks, ReplaySection.EVENT);
        for (ReplayChunk chunk : chunks) {
            for (byte[] entry : ReplayFormat.readChunk(chunk)) {
                if (chunk.section() == ReplaySection.SNAPSHOT) {
                    ReplaySnapshot snapshot = ReplaySnapshotCodec.read(entry);
                    if (snapshot.tick() < chunk.startTick() || snapshot.tick() > chunk.endTick()) {
                        throw new IOException("Snapshot tick is outside its chunk range");
                    }
                    if (snapshots.put(snapshot.tick(), snapshot) != null)
                        throw new IOException("Duplicate replay snapshot at tick " + snapshot.tick());
                } else {
                    readEntry(entry, chunk.section(), durationTicks);
                }
            }
        }
        if (snapshots.isEmpty() || snapshots.firstKey() != 0 || snapshots.lastKey() != durationTicks) {
            throw new IOException("Replay snapshot index is incomplete");
        }
    }

    private void readEntry(byte[] encoded, ReplaySection section, int durationTicks) throws IOException {
        try (ReplayDataReader reader = new ReplayDataReader(encoded)) {
            int tick = reader.readVarInt();
            int typeId = reader.readVarInt();
            byte[] payload = reader.readBytes(ReplayFormat.MAX_ENTRY_BYTES);
            if (reader.available() != 0 || tick < 0 || tick > durationTicks)
                throw new IOException("Invalid replay entry framing");
            if (section == ReplaySection.DELTA) {
                stateDeltas.computeIfAbsent(tick, ignored -> new ArrayList<>()).add(deltaTypes.read(typeId, payload));
            } else {
                transientEvents.computeIfAbsent(tick, ignored -> new ArrayList<>()).add(eventTypes.read(typeId, payload));
            }
            populatedTicks.add(tick);
        }
    }

    public ReplaySnapshot snapshotAtOrBefore(int tick) {
        var entry = snapshots.floorEntry(tick);
        if (entry == null) throw new IllegalArgumentException("No replay snapshot at or before tick " + tick);
        return entry.getValue();
    }

    public List<ReplayStateDelta> stateDeltasAt(int tick) {
        return stateDeltas.getOrDefault(tick, List.of());
    }

    public List<ReplayEvent> transientEventsAt(int tick) {
        return transientEvents.getOrDefault(tick, List.of());
    }

    public List<ReplayStateDelta> stateDeltasBetween(int startTick, int endTick) {
        if (startTick > endTick) return List.of();
        List<ReplayStateDelta> result = new ArrayList<>();
        stateDeltas.subMap(startTick, true, endTick, true).values().forEach(result::addAll);
        return List.copyOf(result);
    }

    public Set<Integer> getAllTicks() {
        return Collections.unmodifiableSet(populatedTicks);
    }

    public NavigableSet<Integer> snapshotTicks() {
        return Collections.unmodifiableNavigableSet(new TreeSet<>(snapshots.keySet()));
    }

    public Set<ReplayBlockPosition> overlayPositions() {
        Set<ReplayBlockPosition> result = new TreeSet<>();
        snapshots.values().forEach(snapshot -> result.addAll(snapshot.blockOverlay().keySet()));
        return Set.copyOf(result);
    }
}
