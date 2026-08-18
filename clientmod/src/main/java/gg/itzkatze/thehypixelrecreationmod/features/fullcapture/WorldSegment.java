package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.itzkatze.thehypixelrecreationmod.features.worldexport.LoadedChunkExporter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class WorldSegment {
    private final String id;
    private final int index;
    private final LoadedChunkExporter.SessionContext context;
    private final RegistryAccess registries;
    private final long startedMs;
    private final String enterReason;
    private final Map<Long, CompoundTag> chunks = new LinkedHashMap<>();
    private final Map<Long, Integer> fingerprints = new LinkedHashMap<>();
    private final List<String> notes = new ArrayList<>();

    private JsonObject entryPosition;
    private long endedMs = -1L;
    private String exitReason;
    private int rejoins;
    private Path directory;
    private String polarFile;
    private int sectionCount;
    private int blockEntityCount;
    private String error;
    private String duplicateOf;
    private boolean mergeForbidden;
    private double duplicateOverlap;

    WorldSegment(int index, LoadedChunkExporter.SessionContext context, RegistryAccess registries,
                 long startedMs, String enterReason) {
        this.index = index;
        this.id = String.format("world_%03d", index);
        this.context = context;
        this.registries = registries;
        this.startedMs = startedMs;
        this.enterReason = enterReason;
    }

    String id() {
        return id;
    }

    int index() {
        return index;
    }

    LoadedChunkExporter.SessionContext context() {
        return context;
    }

    String dimension() {
        return context.dimension();
    }

    String source() {
        return context.source();
    }

    RegistryAccess registries() {
        return registries;
    }

    long startedMs() {
        return startedMs;
    }

    int chunkCount() {
        return fingerprints.size();
    }

    boolean isClosed() {
        return endedMs >= 0L;
    }

    Path directory() {
        return directory;
    }

    void setEntryPosition(JsonObject position) {
        if (entryPosition == null) {
            entryPosition = position;
        }
    }

    void addNote(String note) {
        notes.add(note);
    }

    void forbidMerge() {
        mergeForbidden = true;
    }

    boolean mergeForbidden() {
        return mergeForbidden;
    }

    void markDuplicate(String otherId, double overlap) {
        duplicateOf = otherId;
        duplicateOverlap = overlap;
    }

    int add(List<LoadedChunkExporter.CapturedChunk> captured) {
        int added = 0;
        for (LoadedChunkExporter.CapturedChunk chunk : captured) {
            if (add(chunk.packedPos(), chunk.chunkTag())) {
                added++;
            }
        }
        return added;
    }

    boolean add(long packedPos, CompoundTag chunkTag) {
        if (fingerprints.containsKey(packedPos)) {
            return false;
        }
        chunks.put(packedPos, chunkTag.copy());
        fingerprints.put(packedPos, fingerprint(chunkTag));
        return true;
    }

    double overlapWith(WorldSegment other) {
        if (fingerprints.isEmpty() || other.fingerprints.isEmpty()) {
            return 0.0;
        }
        if (!context.dimension().equals(other.context.dimension())
                || !context.source().equals(other.context.source())) {
            return 0.0;
        }

        int matched = 0;
        for (Map.Entry<Long, Integer> entry : fingerprints.entrySet()) {
            if (entry.getValue().equals(other.fingerprints.get(entry.getKey()))) {
                matched++;
            }
        }
        return (double) matched / fingerprints.size();
    }

    void absorb(WorldSegment other) {
        for (Map.Entry<Long, CompoundTag> entry : other.chunks.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        notes.addAll(other.notes);
        rejoins++;
        endedMs = -1L;
        exitReason = null;
    }

    void close(long ms, String reason) {
        if (endedMs < 0L) {
            endedMs = ms;
            exitReason = reason;
        }
    }

    void write(Path worldsDirectory) {
        directory = worldsDirectory.resolve(id);
        try {
            LoadedChunkExporter.ExportResult result =
                    LoadedChunkExporter.writeChunksTo(directory, context, chunks, true);
            sectionCount = result.sectionCount();
            blockEntityCount = result.blockEntityCount();
        } catch (IOException | RuntimeException exception) {
            error = String.valueOf(exception.getMessage());
        }
        chunks.clear();
    }

    Map<Long, CompoundTag> reloadChunks() throws IOException {
        if (directory == null) {
            return Map.of();
        }
        return LoadedChunkExporter.readChunks(directory, context.dimension(), fingerprints.keySet());
    }

    void setPolarFile(String polarFile) {
        this.polarFile = polarFile;
    }

    void setError(String error) {
        this.error = error;
    }

    JsonObject describe() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("dimension", context.dimension());
        json.addProperty("server", context.source());
        json.addProperty("minY", context.minY());
        json.addProperty("height", context.height());
        json.addProperty("enteredMs", startedMs);
        json.addProperty("exitedMs", endedMs);
        json.addProperty("enterReason", enterReason);
        json.addProperty("exitReason", exitReason);
        json.addProperty("rejoins", rejoins);
        json.addProperty("chunks", fingerprints.size());
        json.addProperty("sections", sectionCount);
        json.addProperty("blockEntities", blockEntityCount);
        json.add("entryPosition", entryPosition);
        json.addProperty("region", directory == null ? null : id + "/region");
        json.addProperty("polar", polarFile);
        if (duplicateOf != null) {
            JsonObject duplicate = new JsonObject();
            duplicate.addProperty("world", duplicateOf);
            duplicate.addProperty("overlap", CaptureJson.round(duplicateOverlap));
            json.add("resemblesEarlierWorld", duplicate);
        }
        if (!notes.isEmpty()) {
            JsonArray noteArray = new JsonArray();
            notes.forEach(noteArray::add);
            json.add("notes", noteArray);
        }
        json.addProperty("error", error);
        return json;
    }

    private static int fingerprint(CompoundTag chunkTag) {
        int hash = 1;
        for (Tag sectionTag : chunkTag.getListOrEmpty("sections")) {
            CompoundTag section = sectionTag.asCompound().orElse(null);
            if (section == null) {
                continue;
            }
            hash = hash * 31 + section.getIntOr("Y", 0);
            hash = hash * 31 + section.getCompoundOrEmpty("block_states").hashCode();
        }
        return hash;
    }
}
