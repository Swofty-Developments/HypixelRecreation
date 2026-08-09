package net.swofty.type.garden.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.hollowcube.schem.BlockEntityData;
import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.SpongeSchematic;
import net.hollowcube.schem.reader.SchematicReader;
import net.hollowcube.schem.util.BlockConsumer;
import net.hollowcube.schem.util.CoordinateUtil;
import net.hollowcube.schem.util.GameDataProvider;
import net.hollowcube.schem.util.Rotation;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.hollowcube.schem.util.CoordinateUtil.blockIndex;

/**
 * A modified version of Schem library's <a href="https://github.com/hollow-cube/schem/blob/main/src/main/java/net/hollowcube/schem/reader/LitematicaSchematicReader.java">LitematicaSchematicReader</a> which was licensed under
 * <a href="https://github.com/hollow-cube/schem/blob/main/LICENSE">MIT License</a>
 */
final class LitematicaSchematicReader implements SchematicReader {
    private final GameDataProvider gameData = GameDataProvider.provider();

    @Override
    public @NonNull Schematic read(byte @NonNull [] data) {
        try {
            return read(BinaryTagIO.reader().readNamed(
                new ByteArrayInputStream(data),
                BinaryTagIO.Compression.GZIP
            ));
        } catch (IOException e) {
            throw new RuntimeException("failed to read root compound", e);
        }
    }

    @ApiStatus.Internal
    public Schematic read(Map.Entry<String, CompoundBinaryTag> rootPair) {
        assertTrue("".equals(rootPair.getKey()), "root tag must be empty, was: '{0}'", rootPair.getKey());
        var root = rootPair.getValue();
        var dataVersion = getRequired(root, "MinecraftDataVersion", BinaryTagTypes.INT).value();
        var version = getRequired(root, "Version", BinaryTagTypes.INT).value();
        var subVersion = getRequired(root, "SubVersion", BinaryTagTypes.INT).value();
        // flexible version check - ARI
        assertTrue(version == 6 || version == 7, "unsupported version (only 6 and 7 are supported): {0}.{1}", version, subVersion);

        var metadata = getRequired(root, "Metadata", BinaryTagTypes.COMPOUND);
        var enclosingSize = getRequiredVec3(metadata, "EnclosingSize");
        assertTrue(enclosingSize.blockX() > 0, "invalid enclosing width: {0}", enclosingSize.blockX());
        assertTrue(enclosingSize.blockY() > 0, "invalid enclosing height: {0}", enclosingSize.blockY());
        assertTrue(enclosingSize.blockZ() > 0, "invalid enclosing length: {0}", enclosingSize.blockZ());

        var regions = new HashMap<String, Schematic>();
        for (var regionPair : getRequired(root, "Regions", BinaryTagTypes.COMPOUND)) {
            var region = loadRegion(dataVersion, (CompoundBinaryTag) regionPair.getValue());
            regions.put(regionPair.getKey(), region);
        }

        return new RegionedLitematicaSchematic(metadata, enclosingSize, regions);
    }

    private Schematic loadRegion(int dataVersion, CompoundBinaryTag region) {
        var rawPos = getRequiredVec3(region, "Position");
        var rawSize = getRequiredVec3(region, "Size")
            .withX(x -> x >= 0 ? x - 1 : x + 1)
            .withY(y -> y >= 0 ? y - 1 : y + 1)
            .withZ(z -> z >= 0 ? z - 1 : z + 1);
        var relativeEnd = rawSize.add(rawPos);
        var absoluteMin = CoordinateUtil.min(rawPos, relativeEnd);
        var absoluteMax = CoordinateUtil.max(rawPos, relativeEnd);
        var size = absoluteMax.sub(absoluteMin).add(1);

        // === Block Palette ===
        var paletteList = getRequired(region, "BlockStatePalette", BinaryTagTypes.LIST);
        var blockPalette = new Block[paletteList.size()];
        for (int i = 0; i < paletteList.size(); i++)
            blockPalette[i] = readBlockState(paletteList.getCompound(i));

        // Litematica doesn't include the block entity id in TileEntities, so we have to look up
        // the block IDs from the palette while creating blockData to get the block id.
        // We can use the Minestom registry to get the associated block entity and store that too.
        Int2ObjectMap<CompoundBinaryTag> blockEntityData = new Int2ObjectOpenHashMap<>();
        for (var blockEntityTag : region.getList("TileEntities", BinaryTagTypes.COMPOUND)) {
            var blockEntity = (CompoundBinaryTag) blockEntityTag;
            var pos = getRequiredVec3(blockEntity, "");
            var data = blockEntity.remove("x").remove("y").remove("z");

            var index = (int) (pos.blockX() + pos.blockZ() * size.x() + pos.blockY() * size.x() * size.z());
            blockEntityData.put(index, data);
        }

        var blockEntities = new Int2ObjectArrayMap<BlockEntityData>();
        var packedBlocks = getRequired(region, "BlockStates", BinaryTagTypes.LONG_ARRAY).value();
        var unpackedBlocks = new int[size.blockX() * size.blockY() * size.blockZ()];
        var bitsPerEntry = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(blockPalette.length - 1));
        unpackPaletteTight(unpackedBlocks, packedBlocks, bitsPerEntry);
        var blockData = ByteArrayBinaryTag.byteArrayBinaryTag(NetworkBuffer.makeArray(buffer -> {
            for (int index = 0; index < unpackedBlocks.length; index++) {
                var paletteIndex = unpackedBlocks[index];
                buffer.write(NetworkBuffer.VAR_INT, paletteIndex);

                // Try to find block entity for this block
                var blockEntityType = blockPalette[paletteIndex].registry().blockEntityType();
                if (blockEntityType != null) {
                    var blockEntity = blockEntityData.getOrDefault(index, CompoundBinaryTag.empty());
                    var blockPosition = new Vec(
                        index % size.x(),
                        (index / size.x()) % size.z(),
                        index / (size.x() * size.z())
                    );
                    if (dataVersion != gameData.dataVersion()) {
                        blockEntity = gameData.upgradeBlockEntity(dataVersion, gameData.dataVersion(), blockEntityType.name(), blockEntity);
                    }
                    blockEntities.put(blockIndex(size, blockPosition), new BlockEntityData(blockEntityType.name(), blockPosition, blockEntity));
                }
            }
        }));

        // === Entities ===
        // disable entities - ARI
        /*var entities = new ArrayList<CompoundBinaryTag>();
        for (var entityTag : region.getList("Entities", BinaryTagTypes.COMPOUND)) {
            var entity = (CompoundBinaryTag) entityTag;
            if (dataVersion != gameData.dataVersion())
                entity = gameData.upgradeEntity(dataVersion, gameData.dataVersion(), entity);
            entities.add(entity);
        }*/

        return new SpongeSchematic(
            CompoundBinaryTag.empty(), size, absoluteMin,
            List.of(blockPalette), blockData,
            List.of(), SpongeSchematic.EMPTY_BYTE_ARRAY,
            blockEntities, List.of()
        );
    }

    private static void assertTrue(boolean condition, String message, Object... args) {
        if (condition) {
            return;
        }
        throw new IllegalArgumentException(MessageFormat.format(message, args));
    }

    private static <T extends BinaryTag> T getRequired(CompoundBinaryTag tag, String key, BinaryTagType<T> type) {
        var value = tag.get(key);
        if (value == null) {
            throw new IllegalArgumentException("missing required field '" + key + "'");
        }
        if (value.type() != type) {
            throw new IllegalArgumentException("expected field '" + key + "' to be a " + type);
        }
        @SuppressWarnings("unchecked")
        var castValue = (T) value;
        return castValue;
    }

    private static Point getRequiredVec3(CompoundBinaryTag tag, String key) {
        var vec = key.isEmpty() ? tag : getRequired(tag, key, BinaryTagTypes.COMPOUND);
        var x = getRequired(vec, "x", BinaryTagTypes.INT).value();
        var y = getRequired(vec, "y", BinaryTagTypes.INT).value();
        var z = getRequired(vec, "z", BinaryTagTypes.INT).value();
        return new Vec(x, y, z);
    }

    private static Block readBlockState(CompoundBinaryTag tag) {
        var name = getRequired(tag, "Name", BinaryTagTypes.STRING).value();
        var block = Block.fromKey(name);
        assertTrue(block != null, "unknown block: {0}", name);

        var propsTag = tag.getCompound("Properties");
        if (propsTag.isEmpty()) {
            return block;
        }

        var properties = new HashMap<String, String>();
        for (var entry : propsTag) {
            assertTrue(entry.getValue().type() == BinaryTagTypes.STRING, "expected property value to be a string");
            properties.put(entry.getKey(), ((StringBinaryTag) entry.getValue()).value());
        }

        try {
            return block.withProperties(properties);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("failed to parse block properties", e);
        }
    }

    private static void unpackPaletteTight(int[] out, long[] in, int bitsPerEntry) {
        assert in.length != 0 : "unpack input array is zero";

        long maxEntryValue = (1L << bitsPerEntry) - 1L;
        for (int i = 0; i < out.length; i++) {
            long startOffset = i * (long) bitsPerEntry;
            int startArrIndex = (int) (startOffset >> 6);
            int endArrIndex = (int) (((i + 1L) * bitsPerEntry - 1L) >> 6);
            int subIndex = (int) (startOffset & 0x3F);

            if (startArrIndex == endArrIndex) {
                out[i] = (int) ((in[startArrIndex] >>> subIndex) & maxEntryValue);
            } else {
                out[i] = (int) ((in[startArrIndex] >>> subIndex | in[endArrIndex] << (64 - subIndex)) & maxEntryValue);
            }
        }
    }

    record RegionedLitematicaSchematic(
        CompoundBinaryTag metadata,
        Point size,
        Map<String, Schematic> regions
    ) implements Schematic {
        @Override
        public void forEachBlock(@NonNull Rotation rotation, @NonNull BlockConsumer consumer) {
            for (var region : regions.values()) {
                region.forEachBlock(rotation, consumer);
            }
        }

        @Override
        public String name() {
            var name = metadata.get("Name");
            if (name != null && name.type() == BinaryTagTypes.STRING) {
                return ((StringBinaryTag) name).value();
            }
            return null;
        }

        @Override
        public String author() {
            var author = metadata.get("Author");
            if (author != null && author.type() == BinaryTagTypes.STRING) {
                return ((StringBinaryTag) author).value();
            }
            return null;
        }

        @Override
        public Instant createdAt() {
            var createdAt = metadata.get("TimeCreated");
            if (createdAt != null && createdAt.type() == BinaryTagTypes.LONG) {
                return Instant.ofEpochMilli(((LongBinaryTag) createdAt).value());
            }
            return null;
        }

        @Override
        public boolean hasBlockData() {
            return regions.values().stream().anyMatch(Schematic::hasBlockData);
        }

        @Override
        public @NonNull List<BlockEntityData> blockEntities() {
            var blockEntities = new ArrayList<BlockEntityData>();
            for (var region : regions.values()) {
                blockEntities.addAll(region.blockEntities());
            }
            return blockEntities;
        }

        @Override
        public @NonNull List<CompoundBinaryTag> entities() {
            var entities = new ArrayList<CompoundBinaryTag>();
            for (var region : regions.values()) {
                entities.addAll(region.entities());
            }
            return entities;
        }
    }
}
