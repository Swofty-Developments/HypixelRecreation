package gg.itzkatze.thehypixelrecreationmod.features.worldexport;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import it.unimi.dsi.fastutil.shorts.ShortList;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class LoadedChunkExporter {
    private static final Path EXPORT_BASE_DIR =
        FabricLoader.getInstance()
            .getGameDir()
            .resolve("saves");

    private LoadedChunkExporter() {
    }

    public static ExportResult exportLoadedChunks() throws IOException {
        Minecraft client = Minecraft.getInstance();
        SessionContext context = captureCurrentContext(client);
        Map<Long, CompoundTag> chunks = new LinkedHashMap<>();
        for (CapturedChunk capturedChunk : captureLoadedChunks(client.level)) {
            chunks.put(capturedChunk.packedPos(), capturedChunk.chunkTag().copy());
        }

        return writeRecordedChunks(context, "loaded_chunks", Instant.now(), Instant.now(), chunks);
    }

    public static SessionContext captureCurrentContext(Minecraft client) {
        ClientLevel level = client.level;
        if (client.player == null || level == null) {
            throw new IllegalStateException("No world is currently loaded.");
        }

        return new SessionContext(
            level.dimension().identifier().toString(),
            describeSource(client),
            level.getMinY(),
            level.getHeight()
        );
    }

    public static List<CapturedChunk> captureLoadedChunks(ClientLevel level) {
        List<CapturedChunk> chunkSnapshots = new ArrayList<>();
        for (LevelChunk chunk : collectLoadedChunks(Minecraft.getInstance(), level, level.getChunkSource())) {
            long chunkKey = ((long) chunk.getPos().x() << 32) | (chunk.getPos().z() & 0xFFFFFFFFL);
            chunkSnapshots.add(new CapturedChunk(chunkKey, serializeChunk(level, chunk)));
        }
        return chunkSnapshots;
    }

    public static Optional<CapturedChunk> captureChunk(ClientLevel level, int chunkX, int chunkZ) {
        if (level == null || !level.hasChunk(chunkX, chunkZ)) {
            return Optional.empty();
        }

        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        if (chunk == null) {
            return Optional.empty();
        }

        long chunkKey = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        return Optional.of(new CapturedChunk(chunkKey, serializeChunk(level, chunk)));
    }

    public static ExportResult writeChunksTo(
        Path outputPath,
        SessionContext context,
        Map<Long, CompoundTag> chunks,
        boolean replaceExisting
    ) throws IOException {
        ExportAccumulator accumulator = new ExportAccumulator();
        prepareOutputDirectory(outputPath, replaceExisting);
        writeVanillaWorld(outputPath, context, chunks, accumulator);
        return new ExportResult(outputPath, chunks.size(), accumulator.sectionCount, accumulator.blockEntityCount);
    }

    public static Map<Long, CompoundTag> readChunks(
        Path worldPath,
        String dimension,
        Collection<Long> packedPositions
    ) throws IOException {
        Map<Long, CompoundTag> chunks = new LinkedHashMap<>();
        RegionStorageInfo storageInfo = new RegionStorageInfo(
            worldPath.getFileName().toString(),
            toMinecraftDimension(dimension),
            "region"
        );

        try (RegionFileStorage storage = new RegionFileStorage(storageInfo, worldPath.resolve("region"), false)) {
            for (long packedPosition : packedPositions) {
                ChunkPos chunkPos = new ChunkPos((int) (packedPosition >> 32), (int) packedPosition);
                CompoundTag chunkTag = storage.read(chunkPos);
                if (chunkTag != null) {
                    chunks.put(packedPosition, chunkTag);
                }
            }
        }

        return chunks;
    }

    public static ExportResult writeRecordedChunks(
        SessionContext context,
        String sessionName,
        Instant startedAt,
        Instant stoppedAt,
        Map<Long, CompoundTag> chunks
    ) throws IOException {
        return writeRecordedChunks(context, sessionName, startedAt, stoppedAt, chunks, false);
    }

    public static ExportResult writeRecordedChunks(
            SessionContext context,
            String sessionName,
            Instant startedAt,
            Instant stoppedAt,
            Map<Long, CompoundTag> chunks,
            boolean replaceExisting
    ) throws IOException {
        String safeSessionName = sanitizeSessionName(sessionName);
        ExportAccumulator accumulator = new ExportAccumulator();
        Path outputPath = EXPORT_BASE_DIR.resolve(safeSessionName);
        prepareOutputDirectory(outputPath, replaceExisting);
        writeVanillaWorld(outputPath, context, chunks, accumulator);
        return new ExportResult(outputPath, chunks.size(), accumulator.sectionCount, accumulator.blockEntityCount);
    }

    public static String sanitizeSessionName(String sessionName) {
        String sanitized = sessionName
            .replaceAll("[/\\\\]", "_")
            .replaceAll("[^a-zA-Z0-9._-]", "_")
            .replaceAll("_+", "_")
            .trim();

        if (sanitized.isBlank()) {
            throw new IllegalStateException("Session name must contain at least one usable character.");
        }

        return sanitized;
    }

    private static CompoundTag serializeChunk(ClientLevel level, LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        List<SerializableChunkData.SectionData> sectionData = new ArrayList<>();
        LevelChunkSection[] sections = chunk.getSections();
        int minSectionY = SectionPos.blockToSectionCoord(chunk.getMinY());
        int maxSectionY = minSectionY + sections.length - 1;
        LevelLightEngine lightEngine = level.getLightEngine();
        int minSerializedSectionY = Math.min(minSectionY, lightEngine.getMinLightSection());
        int maxSerializedSectionY = Math.max(maxSectionY, lightEngine.getMaxLightSection() - 1);
        for (int sectionY = minSerializedSectionY; sectionY <= maxSerializedSectionY; sectionY++) {
            int index = sectionY - minSectionY;
            LevelChunkSection section = index >= 0 && index < sections.length ? sections[index] : null;
            SectionPos sectionPos = SectionPos.of(pos, sectionY);
            DataLayer blockLight = copyLightData(lightEngine, LightLayer.BLOCK, sectionPos);
            DataLayer skyLight = copyLightData(lightEngine, LightLayer.SKY, sectionPos);

            if (section != null || blockLight != null || skyLight != null) {
                sectionData.add(new SerializableChunkData.SectionData(
                    sectionY,
                    section == null ? null : section.copy(),
                    blockLight,
                    skyLight
                ));
            }
        }

        List<CompoundTag> blockEntityTags = new ArrayList<>();
        for (var blockEntity : chunk.getBlockEntities().values()) {
            blockEntityTags.add(blockEntity.saveWithFullMetadata(level.registryAccess()));
        }

        SerializableChunkData serializableChunkData = new SerializableChunkData(
            level.palettedContainerFactory(),
            pos,
            chunk.getMinSectionY(),
            0L,
            chunk.getInhabitedTime(),
            chunk.getPersistedStatus(),
            null,
            chunk.getBelowZeroRetrogen(),
            chunk.getUpgradeData(),
            null,
            copyHeightmaps(chunk),
            chunk.getTicksForSerialization(0L),
            copyPostProcessing(chunk.getPostProcessing()),
            chunk.isLightCorrect(),
            sectionData,
            List.of(),
            blockEntityTags,
            new CompoundTag()
        );

        return serializableChunkData.write();
    }

    private static DataLayer copyLightData(LevelLightEngine lightEngine, LightLayer layer, SectionPos sectionPos) {
        DataLayer data = lightEngine.getLayerListener(layer).getDataLayerData(sectionPos);
        return data == null ? null : data.copy();
    }

    private static Map<Heightmap.Types, long[]> copyHeightmaps(ChunkAccess chunk) {
        Map<Heightmap.Types, long[]> heightmaps = new EnumMap<>(Heightmap.Types.class);
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps()) {
            heightmaps.put(entry.getKey(), entry.getValue().getRawData().clone());
        }
        return heightmaps;
    }

    private static List<LevelChunk> collectLoadedChunks(Minecraft client, ClientLevel level, ClientChunkCache chunkCache) {
        Map<Long, LevelChunk> uniqueChunks = new LinkedHashMap<>();
        if (client.player == null) {
            return List.of();
        }

        int centerChunkX = client.player.chunkPosition().x();
        int centerChunkZ = client.player.chunkPosition().z();
        int radius = Math.max(2, client.options.getEffectiveRenderDistance()) + 2;

        for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
            for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                LevelChunk chunk = chunkCache.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk != null) {
                    long chunkKey = ((long) chunk.getPos().x() << 32) | (chunk.getPos().z() & 0xFFFFFFFFL);
                    uniqueChunks.putIfAbsent(chunkKey, chunk);
                }
            }
        }

        return new ArrayList<>(uniqueChunks.values());
    }

    private static String describeSource(Minecraft client) {
        if (client.hasSingleplayerServer()) {
            return "singleplayer";
        }

        ServerData serverData = client.getCurrentServer();
        if (serverData == null) {
            return "unknown";
        }

        return serverData.ip;
    }

    private static void writeVanillaWorld(
        Path outputPath,
        SessionContext context,
        Map<Long, CompoundTag> chunks,
        ExportAccumulator accumulator
    ) throws IOException {
        Path regionPath = outputPath.resolve("region");
        Map<Long, RegionFile> regionFiles = new LinkedHashMap<>();
        try {
            RegionStorageInfo storageInfo = new RegionStorageInfo(
                outputPath.getFileName().toString(),
                toMinecraftDimension(context.dimension()),
                "region"
            );

            for (CompoundTag chunkTag : chunks.values()) {
                ChunkPos chunkPos = new ChunkPos(
                    chunkTag.getIntOr("xPos", 0),
                    chunkTag.getIntOr("zPos", 0)
                );

                RegionFile regionFile = regionFiles.computeIfAbsent(regionIndex(chunkPos), ignored -> {
                    try {
                        return new RegionFile(
                            storageInfo,
                            regionPath.resolve(regionFileName(chunkPos)),
                            regionPath,
                            false
                        );
                    } catch (IOException exception) {
                        throw new RuntimeIOException(exception);
                    }
                });

                try (DataOutputStream outputStream = regionFile.getChunkDataOutputStream(chunkPos)) {
                    NbtIo.write(chunkTag, outputStream);
                }

                accumulator.sectionCount += chunkTag.getListOrEmpty("sections").size();
                accumulator.blockEntityCount += chunkTag.getListOrEmpty("block_entities").size();
            }
        } catch (RuntimeIOException exception) {
            throw exception.ioCause();
        } finally {
            IOException closeFailure = null;
            for (RegionFile regionFile : regionFiles.values()) {
                try {
                    regionFile.close();
                } catch (IOException exception) {
                    if (closeFailure == null) {
                        closeFailure = exception;
                    } else {
                        closeFailure.addSuppressed(exception);
                    }
                }
            }
            if (closeFailure != null) {
                throw closeFailure;
            }
        }
    }

    private static void prepareOutputDirectory(Path outputPath, boolean replaceExisting) throws IOException {
        Path regionPath = outputPath.resolve("region");
        if (Files.exists(regionPath)) {
            try (Stream<Path> files = Files.list(regionPath)) {
                List<Path> existing = files.toList();
                if (!existing.isEmpty() && !replaceExisting) {
                    throw new IOException("Target save already contains region data: " + outputPath.getFileName());
                }
                if (replaceExisting) {
                    for (Path file : existing) Files.delete(file);
                }
            }
        }

        Files.createDirectories(regionPath);
    }

    private static ShortList[] copyPostProcessing(ShortList[] source) {
        ShortList[] copy = new ShortList[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    private static long regionIndex(ChunkPos chunkPos) {
        return ((long) chunkPos.getRegionX() << 32) | (chunkPos.getRegionZ() & 0xFFFFFFFFL);
    }

    private static String regionFileName(ChunkPos chunkPos) {
        return "r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + ".mca";
    }

    private static ResourceKey<Level> toMinecraftDimension(String dimension) {
        return switch (dimension) {
            case "minecraft:the_nether" -> Level.NETHER;
            case "minecraft:the_end" -> Level.END;
            default -> Level.OVERWORLD;
        };
    }

    private static final class ExportAccumulator {
        private int sectionCount;
        private int blockEntityCount;
    }

    public record SessionContext(String dimension, String source, int minY, int height) {
        public boolean matches(Minecraft client, ClientLevel level) {
            return source.equals(describeSource(client))
                && dimension.equals(level.dimension().identifier().toString());
        }
    }

    public record CapturedChunk(long packedPos, CompoundTag chunkTag) {
    }

    public record ExportResult(Path path, int chunkCount, int sectionCount, int blockEntityCount) {
    }

    private static final class RuntimeIOException extends RuntimeException {
        private RuntimeIOException(IOException cause) {
            super(cause);
        }

        private IOException ioCause() {
            return (IOException) super.getCause();
        }
    }
}
