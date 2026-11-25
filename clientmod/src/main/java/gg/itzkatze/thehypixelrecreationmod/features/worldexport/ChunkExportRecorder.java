package gg.itzkatze.thehypixelrecreationmod.features.worldexport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gg.itzkatze.thehypixelrecreationmod.utils.PolarConvert;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.storage.TagValueOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public final class ChunkExportRecorder {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int CAPTURE_INTERVAL_TICKS = 20;

    private static final Map<Long, CompoundTag> RECORDED_CHUNKS = new LinkedHashMap<>();
    private static final Map<UUID, CapturedEntity> RECORDED_BLOCK_DISPLAYS = new LinkedHashMap<>();
    private static final Map<UUID, CompoundTag> RECORDED_RAVENGARD_OBJECTS = new LinkedHashMap<>();
    private static final Set<UUID> MOVING_ENTITIES = new HashSet<>();
    private static final Map<UUID, BedWarsSprayPosition> RECORDED_SPRAY_POSITIONS = new LinkedHashMap<>();

    private static LoadedChunkExporter.SessionContext sessionContext;
    private static Instant startedAt;
    private static int ticksUntilCapture;
    private static CaptureMode captureMode;
    private static String stitchedSessionName;
    private static boolean resumedSession;

    private ChunkExportRecorder() {
    }

    public static StartResult start() {
        return start(CaptureMode.CHUNKS);
    }

    public static StartResult start(CaptureMode mode) {
        return start(mode, null);
    }

    public static StartResult start(CaptureMode mode, String sessionName) {
        if (isActive()) {
            throw new IllegalStateException("A chunk export session is already active.");
        }

        Minecraft client = Minecraft.getInstance();
        sessionContext = LoadedChunkExporter.captureCurrentContext(client);
        startedAt = Instant.now();
        RECORDED_CHUNKS.clear();
        RECORDED_BLOCK_DISPLAYS.clear();
        RECORDED_RAVENGARD_OBJECTS.clear();
        MOVING_ENTITIES.clear();
        RECORDED_SPRAY_POSITIONS.clear();
        captureMode = mode;
        stitchedSessionName = sessionName == null ? null : LoadedChunkExporter.sanitizeSessionName(sessionName);
        try {
            resumedSession = stitchedSessionName != null && loadCheckpoint(stitchedSessionName);
        } catch (RuntimeException exception) {
            clearSession();
            throw exception;
        }
        ticksUntilCapture = CAPTURE_INTERVAL_TICKS;

        captureCurrentWorld(client.level, true);
        return new StartResult(sessionContext.dimension(), RECORDED_CHUNKS.size(), RECORDED_BLOCK_DISPLAYS.size(), mode);
    }

    public static StopResult stop(String sessionName) throws IOException {
        if (!isActive()) {
            throw new IllegalStateException("No chunk export session is active.");
        }

        Minecraft client = Minecraft.getInstance();
        captureCurrentWorld(client.level, false);
        String sanitizedName = LoadedChunkExporter.sanitizeSessionName(sessionName);
        if (stitchedSessionName != null && !stitchedSessionName.equals(sanitizedName)) {
            throw new IllegalStateException("A stitched session must be stopped with its original name: " + stitchedSessionName);
        }

        LoadedChunkExporter.ExportResult exportResult = LoadedChunkExporter.writeRecordedChunks(
                sessionContext,
                sessionName,
                startedAt,
                Instant.now(),
                RECORDED_CHUNKS,
                resumedSession
        );
        PolarConvert.ConversionResult polarResult = PolarConvert.convertWorldFolderToPolar(
                exportResult.path(),
                exportResult.path().resolveSibling(exportResult.path().getFileName() + ".polar"),
                client.level,
                RECORDED_CHUNKS,
                RECORDED_BLOCK_DISPLAYS.values().stream().map(CapturedEntity::tag).toList()
        );

        int ravengardObjectCount = RECORDED_RAVENGARD_OBJECTS.size();
        if (captureMode == CaptureMode.RAVENGARD) {
            writeRavengardConfiguration(polarResult.path(), RECORDED_RAVENGARD_OBJECTS.values());
        }
        if (captureMode == CaptureMode.BEDWARS) {
            writeBedWarsConfiguration(polarResult.path());
        }
        if (stitchedSessionName != null) saveCheckpoint(stitchedSessionName);
        clearSession();
        return new StopResult(
                sanitizedName,
                exportResult.path(),
                polarResult.path(),
                exportResult.chunkCount(),
                exportResult.sectionCount(),
                exportResult.blockEntityCount(),
                polarResult.customBiomeCount(),
                polarResult.blockDisplayCount(),
                ravengardObjectCount
        );
    }

    public static void tick() {
        if (!isActive()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null || client.player == null) {
            return;
        }

        if (!sessionContext.matches(client, level)) {
            return;
        }

        if (--ticksUntilCapture > 0) {
            return;
        }

        ticksUntilCapture = CAPTURE_INTERVAL_TICKS;
        captureCurrentWorld(level, false);
    }

    public static boolean isActive() {
        return sessionContext != null;
    }

    public static Status getStatus() {
        if (!isActive()) {
            throw new IllegalStateException("No chunk export session is active.");
        }

        return new Status(sessionContext.dimension(), RECORDED_CHUNKS.size(), RECORDED_BLOCK_DISPLAYS.size(),
                RECORDED_RAVENGARD_OBJECTS.size(), MOVING_ENTITIES.size(), captureMode);
    }

    private static void captureCurrentWorld(ClientLevel level, boolean requireMatchingContext) {
        if (level == null) {
            return;
        }

        if (requireMatchingContext || sessionContext.matches(Minecraft.getInstance(), level)) {
            List<LoadedChunkExporter.CapturedChunk> snapshots = LoadedChunkExporter.captureLoadedChunks(level);
            for (LoadedChunkExporter.CapturedChunk snapshot : snapshots) {
                RECORDED_CHUNKS.put(snapshot.packedPos(), snapshot.chunkTag().copy());
            }
            if (captureMode == CaptureMode.BLOCK_DISPLAYS || captureMode == CaptureMode.RAVENGARD) {
                captureBlockDisplays(level);
            }
            if (captureMode == CaptureMode.BEDWARS) captureSprayPositions(level);
        }
    }

    private static void captureSprayPositions(ClientLevel level) {
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemFrame frame)) continue;
            RECORDED_SPRAY_POSITIONS.put(frame.getUUID(), new BedWarsSprayPosition(
                    frame.getBlockX(), frame.getBlockY(), frame.getBlockZ(), frame.getDirection().getName()));
        }
    }

    private static void captureBlockDisplays(ClientLevel level) {
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof Display.BlockDisplay) && !(entity instanceof Display.ItemDisplay)) {
                continue;
            }

            UUID uuid = entity.getUUID();
            if (captureMode == CaptureMode.RAVENGARD && entity instanceof Display.ItemDisplay itemDisplay) {
                if (RavengardMetadataCapture.isExcluded(itemDisplay)) {
                    RECORDED_RAVENGARD_OBJECTS.remove(uuid);
                    RECORDED_BLOCK_DISPLAYS.remove(uuid);
                    continue;
                }
                Optional<RavengardMetadataCapture.CapturedObject> object = RavengardMetadataCapture.capture(itemDisplay);
                if (object.isPresent()) {
                    RECORDED_RAVENGARD_OBJECTS.put(uuid, object.get().data());
                    RECORDED_BLOCK_DISPLAYS.remove(uuid);
                    continue;
                }
            }
            if (MOVING_ENTITIES.contains(uuid)) {
                continue;
            }

            // servers send no-op move and motion packets for stationary displays, so only real
            // displacement counts as movement: anything under a hundredth of a block is noise
            if (entity.getDeltaMovement().lengthSqr() > 1.0E-6) {
                RECORDED_BLOCK_DISPLAYS.remove(uuid);
                MOVING_ENTITIES.add(uuid);
                continue;
            }

            EntityPosition position = new EntityPosition(entity.getX(), entity.getY(), entity.getZ());
            CapturedEntity previous = RECORDED_BLOCK_DISPLAYS.get(uuid);
            if (previous != null && previous.position().distanceSqr(position) > 1.0E-4) {
                RECORDED_BLOCK_DISPLAYS.remove(uuid);
                MOVING_ENTITIES.add(uuid);
                continue;
            }

            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            if (entity.save(output)) {
                RECORDED_BLOCK_DISPLAYS.put(uuid, new CapturedEntity(position, output.buildResult()));
            }
        }
    }

    private static void clearSession() {
        RECORDED_CHUNKS.clear();
        RECORDED_BLOCK_DISPLAYS.clear();
        RECORDED_RAVENGARD_OBJECTS.clear();
        MOVING_ENTITIES.clear();
        RECORDED_SPRAY_POSITIONS.clear();
        sessionContext = null;
        startedAt = null;
        ticksUntilCapture = 0;
        captureMode = null;
        stitchedSessionName = null;
        resumedSession = false;
    }

    private static void writeRavengardConfiguration(Path polarPath, Collection<CompoundTag> objects) throws IOException {
        Path configurationPath = polarPath.resolveSibling(polarPath.getFileName().toString().replaceFirst("\\.polar$", "") + ".json");
        List<RavengardDungeonObject> values = objects.stream()
                .map(object -> new RavengardDungeonObject(
                        object.getStringOr("category", ""),
                        object.getStringOr("type", ""),
                        object.getDoubleOr("x", 0),
                        object.getDoubleOr("y", 0),
                        object.getDoubleOr("z", 0),
                        object.getFloatOr("yaw", 0),
                        object.getFloatOr("pitch", 0)))
                .toList();
        String polarFile = polarPath.getFileName().toString();
        String id = polarFile.replaceFirst("\\.polar$", "").replaceFirst("\\.nbt$", "");
        Files.writeString(configurationPath, GSON.toJson(new RavengardDungeonConfiguration(
                id, id, polarFile, new RavengardPosition(0.5, 65, 0.5, 0, 0), values)));
    }

    private static void writeBedWarsConfiguration(Path polarPath) throws IOException {
        Path configurationPath = polarPath.resolveSibling(
                polarPath.getFileName().toString().replaceFirst("\\.polar$", "") + "-bedwars.json");
        Files.writeString(configurationPath, GSON.toJson(Map.of(
                "sprays", RECORDED_SPRAY_POSITIONS.values())));
    }

    private static Path checkpointPath(String name) {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("chunkexporter_sessions").resolve(name + ".nbt");
    }

    private static boolean loadCheckpoint(String name) {
        Path path = checkpointPath(name);
        if (!Files.isRegularFile(path)) return false;
        try (InputStream input = Files.newInputStream(path)) {
            CompoundTag root = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            String dimension = root.getStringOr("dimension", "");
            String source = root.getStringOr("source", "");
            if (!dimension.equals(sessionContext.dimension()) || !source.equals(sessionContext.source())) {
                throw new IllegalStateException("Stitched session belongs to a different server or dimension.");
            }
            for (var value : root.getListOrEmpty("chunks"))
                value.asCompound().ifPresent(chunk ->
                        RECORDED_CHUNKS.put(packChunk(chunk), chunk.copy()));
            for (var value : root.getListOrEmpty("displays"))
                value.asCompound().ifPresent(entry -> {
                    UUID uuid = UUID.fromString(entry.getStringOr("uuid", UUID.randomUUID().toString()));
                    CompoundTag tag = entry.getCompoundOrEmpty("data").copy();
                    RECORDED_BLOCK_DISPLAYS.put(uuid, new CapturedEntity(readPosition(tag), tag));
                });
            for (var value : root.getListOrEmpty("objects"))
                value.asCompound().ifPresent(entry -> {
                    UUID uuid = UUID.fromString(entry.getStringOr("uuid", UUID.randomUUID().toString()));
                    RECORDED_RAVENGARD_OBJECTS.put(uuid, entry.getCompoundOrEmpty("data").copy());
                });
            return true;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read stitched session " + name, exception);
        }
    }

    private static void saveCheckpoint(String name) throws IOException {
        CompoundTag root = new CompoundTag();
        root.putInt("version", 1);
        root.putString("dimension", sessionContext.dimension());
        root.putString("source", sessionContext.source());
        ListTag chunks = new ListTag();
        RECORDED_CHUNKS.values().stream().map(CompoundTag::copy).forEach(chunks::add);
        root.put("chunks", chunks);
        root.put("displays", entries(RECORDED_BLOCK_DISPLAYS.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().tag())).toList()));
        root.put("objects", entries(RECORDED_RAVENGARD_OBJECTS.entrySet()));
        Path path = checkpointPath(name);
        Files.createDirectories(path.getParent());
        try (OutputStream output = Files.newOutputStream(path)) {
            NbtIo.writeCompressed(root, output);
        }
    }

    private static ListTag entries(Collection<? extends Map.Entry<UUID, CompoundTag>> values) {
        ListTag result = new ListTag();
        for (Map.Entry<UUID, CompoundTag> value : values) {
            CompoundTag entry = new CompoundTag();
            entry.putString("uuid", value.getKey().toString());
            entry.put("data", value.getValue().copy());
            result.add(entry);
        }
        return result;
    }

    private static long packChunk(CompoundTag chunk) {
        return ((long) chunk.getIntOr("xPos", 0) << 32) | (chunk.getIntOr("zPos", 0) & 0xffffffffL);
    }

    private static EntityPosition readPosition(CompoundTag tag) {
        ListTag pos = tag.getListOrEmpty("Pos");
        return new EntityPosition(number(pos, 0), number(pos, 1), number(pos, 2));
    }

    private static double number(ListTag values, int index) {
        return index < values.size() ? values.get(index).asDouble().orElse(0d) : 0d;
    }

    public enum CaptureMode {
        CHUNKS,
        BLOCK_DISPLAYS,
        RAVENGARD,
        BEDWARS
    }

    public record StartResult(String dimension, int initialChunkCount, int initialBlockDisplayCount, CaptureMode mode) {
    }

    public record StopResult(
            String sessionName,
            Path path,
            Path polarPath,
            int chunkCount,
            int sectionCount,
            int blockEntityCount,
            int customBiomeCount,
            int blockDisplayCount,
            int ravengardObjectCount
    ) {
    }

    public record Status(String dimension, int chunkCount, int blockDisplayCount, int ravengardObjectCount,
                         int movingEntityCount, CaptureMode mode) {
    }

    private record CapturedEntity(EntityPosition position, CompoundTag tag) {
    }

    private record RavengardDungeonConfiguration(String id, String name, String polar, RavengardPosition spawn,
                                                 List<RavengardDungeonObject> objects) {
    }

    private record RavengardPosition(double x, double y, double z, float yaw, float pitch) {
    }

    private record RavengardDungeonObject(String category, String type, double x, double y, double z,
                                          float yaw, float pitch) {
    }

    private record BedWarsSprayPosition(int x, int y, int z, String facing) {
    }

    private record EntityPosition(double x, double y, double z) {
        double distanceSqr(EntityPosition other) {
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
