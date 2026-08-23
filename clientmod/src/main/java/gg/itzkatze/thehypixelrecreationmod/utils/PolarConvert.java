package gg.itzkatze.thehypixelrecreationmod.utils;

import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.ChunkSelector;
import net.hollowcube.polar.PolarChunk;
import net.hollowcube.polar.PolarSection;
import net.hollowcube.polar.PolarWriter;
import net.hollowcube.polar.PolarWorld;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minestom.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

public final class PolarConvert {
    private PolarConvert() {
    }

    public static boolean convertWorldFolderToPolar(Path anvilPath, Path outputPath) throws IOException {
        convertWorldFolderToPolar(anvilPath, outputPath, null, List.of(), List.of());
        return true;
    }

    public static ConversionResult convertWorldFolderToPolar(Path anvilPath, Path outputPath, ClientLevel level) throws IOException {
        return convertWorldFolderToPolar(anvilPath, outputPath, level, List.of(), List.of());
    }

    public static ConversionResult convertWorldFolderToPolar(
        Path anvilPath,
        Path outputPath,
        ClientLevel level,
        Map<Long, CompoundTag> sourceChunks,
        Collection<CompoundTag> blockDisplays
    ) throws IOException {
        return convertWorldFolderToPolar(anvilPath, outputPath, level, sourceChunks.values(), blockDisplays);
    }

    private static ConversionResult convertWorldFolderToPolar(
        Path anvilPath,
        Path outputPath,
        ClientLevel level,
        Collection<CompoundTag> sourceChunks,
        Collection<CompoundTag> blockDisplays
    ) throws IOException {
        MinecraftServer.init();
        try {
            return convert(
                anvilPath,
                outputPath,
                level == null ? null : level.registryAccess(),
                sourceChunks,
                blockDisplays
            );
        } finally {
            MinecraftServer.stopCleanly();
        }
    }

    public static List<ConversionOutcome> convertAll(List<ConversionJob> jobs) {
        List<ConversionOutcome> outcomes = new ArrayList<>();
        if (jobs.isEmpty()) {
            return outcomes;
        }

        MinecraftServer.init();
        try {
            for (ConversionJob job : jobs) {
                try {
                    outcomes.add(new ConversionOutcome(job, convert(
                        job.anvilPath(),
                        job.outputPath(),
                        job.registries(),
                        job.sourceChunks().get(),
                        job.blockDisplays()
                    ), null));
                } catch (IOException | RuntimeException exception) {
                    outcomes.add(new ConversionOutcome(job, null, String.valueOf(exception.getMessage())));
                }
            }
        } finally {
            MinecraftServer.stopCleanly();
        }

        return outcomes;
    }

    private static ConversionResult convert(
        Path anvilPath,
        Path outputPath,
        RegistryAccess registries,
        Collection<CompoundTag> sourceChunks,
        Collection<CompoundTag> blockDisplays
    ) throws IOException {
        PolarWorld polarWorld = AnvilPolar.anvilToPolar(anvilPath, ChunkSelector.all());
        restoreSourceBiomePalettes(polarWorld, sourceChunks);
        Map<String, Tag> customBiomes = collectCustomBiomeDefinitions(polarWorld, registries, sourceChunks);
        if (!customBiomes.isEmpty() || !blockDisplays.isEmpty()) {
            polarWorld.userData(writeUserData(customBiomes, blockDisplays));
        }
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, PolarWriter.write(polarWorld));
        return new ConversionResult(outputPath, customBiomes.size(), blockDisplays.size());
    }

    public record ConversionJob(
        String worldId,
        Path anvilPath,
        Path outputPath,
        RegistryAccess registries,
        Supplier<Collection<CompoundTag>> sourceChunks,
        Collection<CompoundTag> blockDisplays
    ) {
    }

    public record ConversionOutcome(ConversionJob job, ConversionResult result, String error) {
    }

    private static Map<String, Tag> collectCustomBiomeDefinitions(
        PolarWorld polarWorld,
        RegistryAccess registries,
        Collection<CompoundTag> sourceChunks
    ) throws IOException {
        if (registries == null) {
            return Map.of();
        }

        Set<String> biomeReferences = collectBiomeReferences(sourceChunks);
        if (biomeReferences.isEmpty()) {
            biomeReferences = collectBiomeReferences(polarWorld);
        }

        if (biomeReferences.isEmpty()) {
            return Map.of();
        }

        var biomeRegistry = registries.lookupOrThrow(Registries.BIOME);
        var registryOps = RegistryOps.create(NbtOps.INSTANCE, registries);
        Map<String, Tag> customBiomes = new TreeMap<>();

        for (String biomeReference : biomeReferences) {
            Identifier id = Identifier.tryParse(biomeReference);
            if (id == null || "minecraft".equals(id.getNamespace())) {
                continue;
            }

            Biome biome = biomeRegistry.getValue(id);
            if (biome == null) {
                continue;
            }

            Tag encoded = Biome.DIRECT_CODEC.encodeStart(registryOps, biome)
                .getOrThrow(message -> new IOException("Failed to encode biome " + biomeReference + ": " + message));
            customBiomes.put(biomeReference, encoded);
        }

        return customBiomes;
    }

    private static void restoreSourceBiomePalettes(PolarWorld polarWorld, Collection<CompoundTag> sourceChunks) {
        if (sourceChunks.isEmpty()) {
            return;
        }

        Map<Long, Map<Integer, SourceBiomeSection>> sourceBiomeSections = collectSourceBiomeSections(sourceChunks);
        if (sourceBiomeSections.isEmpty()) {
            return;
        }

        for (PolarChunk chunk : List.copyOf(polarWorld.chunks())) {
            Map<Integer, SourceBiomeSection> chunkBiomeSections = sourceBiomeSections.get(packChunkPos(chunk.x(), chunk.z()));
            if (chunkBiomeSections == null) {
                continue;
            }

            PolarSection[] restoredSections = Arrays.copyOf(chunk.sections(), chunk.sections().length);
            boolean changed = false;

            for (Map.Entry<Integer, SourceBiomeSection> entry : chunkBiomeSections.entrySet()) {
                int sectionIndex = entry.getKey() - polarWorld.minSection();
                if (sectionIndex < 0 || sectionIndex >= restoredSections.length) {
                    continue;
                }

                PolarSection section = restoredSections[sectionIndex];
                SourceBiomeSection sourceBiomes = entry.getValue();
                String[] blockPalette = section.blockPalette();
                restoredSections[sectionIndex] = new PolarSection(
                    blockPalette,
                    blockPalette.length > 1 ? section.blockData() : null,
                    sourceBiomes.palette(),
                    sourceBiomes.data(),
                    section.blockLightContent(),
                    section.blockLight(),
                    section.skyLightContent(),
                    section.skyLight()
                );
                changed = true;
            }

            if (changed) {
                polarWorld.updateChunkAt(
                    chunk.x(),
                    chunk.z(),
                    new PolarChunk(
                        chunk.x(),
                        chunk.z(),
                        restoredSections,
                        chunk.blockEntities(),
                        chunk.heightmaps(),
                        chunk.userData()
                    )
                );
            }
        }
    }

    private static Map<Long, Map<Integer, SourceBiomeSection>> collectSourceBiomeSections(Collection<CompoundTag> sourceChunks) {
        Map<Long, Map<Integer, SourceBiomeSection>> sourceBiomeSections = new HashMap<>();

        for (CompoundTag chunk : sourceChunks) {
            int chunkX = chunk.getIntOr("xPos", Integer.MIN_VALUE);
            int chunkZ = chunk.getIntOr("zPos", Integer.MIN_VALUE);
            if (chunkX == Integer.MIN_VALUE || chunkZ == Integer.MIN_VALUE) {
                continue;
            }

            Map<Integer, SourceBiomeSection> sectionsByY = new HashMap<>();
            for (Tag sectionTag : chunk.getListOrEmpty("sections")) {
                sectionTag.asCompound().ifPresent(section -> {
                    int sectionY = section.getIntOr("Y", Integer.MIN_VALUE);
                    if (sectionY == Integer.MIN_VALUE) {
                        return;
                    }

                    SourceBiomeSection sourceBiomes = readSourceBiomeSection(section.getCompoundOrEmpty("biomes"));
                    if (sourceBiomes != null) {
                        sectionsByY.put(sectionY, sourceBiomes);
                    }
                });
            }

            if (!sectionsByY.isEmpty()) {
                sourceBiomeSections.put(packChunkPos(chunkX, chunkZ), sectionsByY);
            }
        }

        return sourceBiomeSections;
    }

    private static SourceBiomeSection readSourceBiomeSection(CompoundTag biomes) {
        ListTag paletteTag = biomes.getListOrEmpty("palette");
        if (paletteTag.isEmpty()) {
            return null;
        }

        String[] palette = new String[paletteTag.size()];
        for (int index = 0; index < paletteTag.size(); index++) {
            palette[index] = paletteTag.get(index).asString().orElse("minecraft:plains");
        }

        int[] data = null;
        if (palette.length > 1) {
            data = unpackPalettedContainerData(
                biomes.getLongArray("data").orElse(new long[0]),
                palette.length,
                PolarSection.BIOME_PALETTE_SIZE
            );
        }

        return new SourceBiomeSection(palette, data);
    }

    private static int[] unpackPalettedContainerData(long[] packedData, int paletteSize, int entryCount) {
        int[] unpacked = new int[entryCount];
        if (packedData.length == 0) {
            return unpacked;
        }

        int bitsPerEntry = Math.max(1, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
        int entriesPerLong = Long.SIZE / bitsPerEntry;
        long mask = (1L << bitsPerEntry) - 1L;

        for (int index = 0; index < entryCount; index++) {
            int packedIndex = index / entriesPerLong;
            if (packedIndex >= packedData.length) {
                break;
            }

            int bitOffset = (index - packedIndex * entriesPerLong) * bitsPerEntry;
            int paletteIndex = (int) ((packedData[packedIndex] >>> bitOffset) & mask);
            unpacked[index] = paletteIndex < paletteSize ? paletteIndex : 0;
        }

        return unpacked;
    }

    private static long packChunkPos(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }

    private static Set<String> collectBiomeReferences(Collection<CompoundTag> sourceChunks) {
        Set<String> biomeReferences = new TreeSet<>();
        for (CompoundTag chunk : sourceChunks) {
            for (Tag sectionTag : chunk.getListOrEmpty("sections")) {
                sectionTag.asCompound().ifPresent(section -> {
                    CompoundTag biomes = section.getCompoundOrEmpty("biomes");
                    for (Tag paletteEntry : biomes.getListOrEmpty("palette")) {
                        paletteEntry.asString().ifPresent(biomeReferences::add);
                    }
                });
            }
        }
        return biomeReferences;
    }

    private static Set<String> collectBiomeReferences(PolarWorld polarWorld) {
        Set<String> biomeReferences = new TreeSet<>();
        for (var chunk : polarWorld.chunks()) {
            for (var section : chunk.sections()) {
                biomeReferences.addAll(Arrays.asList(section.biomePalette()));
            }
        }
        return biomeReferences;
    }

    private static byte[] writeUserData(Map<String, Tag> customBiomes, Collection<CompoundTag> blockDisplays) throws IOException {
        CompoundTag root = new CompoundTag();
        root.putString("format", "hypixel:custom_biomes");
        root.putInt("version", 1);

        ListTag biomes = new ListTag();
        for (Map.Entry<String, Tag> entry : customBiomes.entrySet()) {
            CompoundTag biome = new CompoundTag();
            biome.putString("id", entry.getKey());
            biome.put("definition", entry.getValue());
            biomes.add(biome);
        }
        root.put("custom_biomes", biomes);

        ListTag displays = new ListTag();
        blockDisplays.stream().map(CompoundTag::copy).forEach(displays::add);
        root.put("block_displays", displays);

        ListTag registry = new ListTag();
        registry.add(StringTag.valueOf("minecraft:worldgen/biome"));
        root.put("registries", registry);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        NbtIo.writeCompressed(root, outputStream);
        return outputStream.toByteArray();
    }

    public record ConversionResult(Path path, int customBiomeCount, int blockDisplayCount) {
    }

    private record SourceBiomeSection(String[] palette, int[] data) {
    }
}
