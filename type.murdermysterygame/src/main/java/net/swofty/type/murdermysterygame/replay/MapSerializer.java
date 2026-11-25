package net.swofty.type.murdermysterygame.replay;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.replay.protocol.ReplayCompression;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public final class MapSerializer {
    private static final int SECTION_SIZE = 16;
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 320;

    private MapSerializer() {
    }

    public static SerializedMap serializeRegion(Instance instance, int centerChunkX, int centerChunkZ, int radius)
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeInt(centerChunkX);
        output.writeInt(centerChunkZ);
        output.writeInt(radius);
        output.writeInt(MIN_Y);
        output.writeInt(MAX_Y);

        List<ChunkData> chunks = new ArrayList<>();
        for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
            for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
                Chunk chunk = instance.getChunk(chunkX, chunkZ);
                if (chunk != null) chunks.add(serializeChunk(chunk, chunkX, chunkZ));
            }
        }
        output.writeInt(chunks.size());

        Map<Integer, Integer> palette = new HashMap<>();
        palette.put(0, 0);
        int nextPaletteId = 1;
        for (ChunkData chunk : chunks) {
            for (int stateId : chunk.blockStates) {
                if (stateId != 0 && !palette.containsKey(stateId)) palette.put(stateId, nextPaletteId++);
            }
        }

        output.writeInt(palette.size());
        for (var entry : palette.entrySet()) {
            output.writeInt(entry.getKey());
            output.writeInt(entry.getValue());
        }
        for (ChunkData chunk : chunks) {
            output.writeInt(chunk.chunkX);
            output.writeInt(chunk.chunkZ);
            int[] paletteIds = new int[chunk.blockStates.length];
            for (int index = 0; index < chunk.blockStates.length; index++) {
                paletteIds[index] = palette.getOrDefault(chunk.blockStates[index], 0);
            }
            int bitsPerBlock = Math.max(1, 32 - Integer.numberOfLeadingZeros(palette.size() - 1));
            output.writeByte(bitsPerBlock);
            writePackedBlocks(output, paletteIds, bitsPerBlock);
        }
        output.flush();

        byte[] uncompressed = bytes.toByteArray();
        byte[] compressed = ReplayCompression.compress(uncompressed);
        return new SerializedMap(hash(compressed), compressed, uncompressed.length, compressed.length);
    }

    private static ChunkData serializeChunk(Chunk chunk, int chunkX, int chunkZ) {
        int sectionCount = (MAX_Y - MIN_Y) / SECTION_SIZE;
        int[] blockStates = new int[SECTION_SIZE * SECTION_SIZE * sectionCount * SECTION_SIZE];
        int index = 0;
        for (int section = 0; section < sectionCount; section++) {
            int baseY = MIN_Y + section * SECTION_SIZE;
            for (int y = 0; y < SECTION_SIZE; y++) {
                for (int z = 0; z < SECTION_SIZE; z++) {
                    for (int x = 0; x < SECTION_SIZE; x++) {
                        Block block = chunk.getBlock(x, baseY + y, z);
                        blockStates[index++] = block.stateId();
                    }
                }
            }
        }
        return new ChunkData(chunkX, chunkZ, blockStates);
    }

    private static void writePackedBlocks(DataOutputStream output, int[] blocks, int bitsPerBlock) throws IOException {
        int blocksPerLong = 64 / bitsPerBlock;
        int longCount = (blocks.length + blocksPerLong - 1) / blocksPerLong;
        output.writeInt(longCount);
        int blockIndex = 0;
        for (int index = 0; index < longCount; index++) {
            long value = 0;
            int bitPosition = 0;
            for (int block = 0; block < blocksPerLong && blockIndex < blocks.length; block++) {
                value |= ((long) blocks[blockIndex++] & ((1L << bitsPerBlock) - 1)) << bitPosition;
                bitPosition += bitsPerBlock;
            }
            output.writeLong(value);
        }
    }

    private static String hash(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)).substring(0, 16);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record SerializedMap(String hash, byte[] compressedData, int uncompressedSize, int compressedSize) {
    }

    private record ChunkData(int chunkX, int chunkZ, int[] blockStates) {
    }
}
