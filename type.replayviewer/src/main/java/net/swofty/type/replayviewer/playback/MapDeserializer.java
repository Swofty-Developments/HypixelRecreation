package net.swofty.type.replayviewer.playback;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.replay.protocol.ReplayCompression;
import org.tinylog.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public final class MapDeserializer {

    private static final int CHUNK_SECTION_SIZE = 16;
    private static final int BLOCKS_PER_LAYER = CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE;
    private static final int MAX_CHUNKS = 65_536;
    private static final int MAX_PALETTE_SIZE = 1 << 16;

    private MapDeserializer() {}

    /**
     * Loads map data into an instance.
     *
     * @param instance The instance to load the map into
     * @param compressedData The compressed map data
     * @throws IOException If deserialization fails
     */
    public static CompletableFuture<Void> loadMap(
            Instance instance,
            byte[] compressedData
    ) throws IOException {
        byte[] decompressed = ReplayCompression.decompress(compressedData);

        AbsoluteBlockBatch batch = new AbsoluteBlockBatch();

        try (DataInputStream dis = new DataInputStream(
                new ByteArrayInputStream(decompressed))) {

            int centerChunkX = dis.readInt();
            int centerChunkZ = dis.readInt();
            int radius = dis.readInt();
            int minY = dis.readInt();
            int maxY = dis.readInt();
            int chunkCount = dis.readInt();

            validateHeader(radius, minY, maxY, chunkCount);

            int paletteSize = dis.readInt();
            if (paletteSize <= 0 || paletteSize > MAX_PALETTE_SIZE) {
                throw new IOException("Invalid palette size: " + paletteSize);
            }
            Block[] palette = new Block[paletteSize];

            for (int i = 0; i < paletteSize; i++) {
                int stateId = dis.readInt();
                int paletteId = dis.readInt();
                if (paletteId < 0 || paletteId >= paletteSize || palette[paletteId] != null) {
                    throw new IOException("Invalid or duplicate palette ID: " + paletteId);
                }
                Block block = Block.fromStateId(stateId);
                if (block == null) {
                    throw new IOException("Unknown block state ID: " + stateId);
                }
                palette[paletteId] = block;
            }

            int blockCount = Math.multiplyExact(maxY - minY, BLOCKS_PER_LAYER);

            for (int c = 0; c < chunkCount; c++) {
                int chunkX = dis.readInt();
                int chunkZ = dis.readInt();
                int bitsPerBlock = dis.readUnsignedByte();
                readChunk(dis, batch, palette, bitsPerBlock, blockCount, chunkX, chunkZ, minY);
            }

            Logger.debug(
                    "Loading map: center=({}, {}), radius={}, chunks={}, palette={}",
                    centerChunkX,
                    centerChunkZ,
                    radius,
                    chunkCount,
                    paletteSize);
        }

        CompletableFuture<Void> result = new CompletableFuture<>();
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            try {
                batch.apply(instance, ignored -> {
                    Logger.info("Map loaded successfully");
                    result.complete(null);
                });
            } catch (Throwable throwable) {
                result.completeExceptionally(throwable);
            }
        });

        return result;
    }

    private static void validateHeader(int radius, int minY, int maxY, int chunkCount) throws IOException {
        if (radius < 0 || maxY <= minY || maxY - minY > 4096) {
            throw new IOException("Invalid map bounds: radius=" + radius + ", y=" + minY + ".." + maxY);
        }
        long diameter = (long) radius * 2 + 1;
        long maximumRegionChunks = diameter * diameter;
        if (chunkCount < 0 || chunkCount > MAX_CHUNKS || chunkCount > maximumRegionChunks) {
            throw new IOException("Invalid chunk count: " + chunkCount);
        }
    }

    private static void readChunk(
            DataInputStream dis,
            AbsoluteBlockBatch batch,
            Block[] palette,
            int bitsPerBlock,
            int blockCount,
            int chunkX,
            int chunkZ,
            int minY
    ) throws IOException {
        int requiredBits = Math.max(1, 32 - Integer.numberOfLeadingZeros(palette.length - 1));
        if (bitsPerBlock < requiredBits || bitsPerBlock > 32) {
            throw new IOException("Invalid bits per block: " + bitsPerBlock);
        }

        int numLongs = dis.readInt();
        int blocksPerLong = 64 / bitsPerBlock;
        int expectedLongs = Math.ceilDiv(blockCount, blocksPerLong);
        if (numLongs != expectedLongs) {
            throw new IOException("Invalid packed block length: " + numLongs + ", expected " + expectedLongs);
        }
        long mask = (1L << bitsPerBlock) - 1;
        int blockIndex = 0;
        int worldX = chunkX << 4;
        int worldZ = chunkZ << 4;

        for (int i = 0; i < numLongs; i++) {
            long packed = dis.readLong();
            int entries = Math.min(blocksPerLong, blockCount - blockIndex);
            for (int j = 0; j < entries; j++, blockIndex++) {
                int paletteId = (int) ((packed >>> (j * bitsPerBlock)) & mask);
                if (paletteId >= palette.length || palette[paletteId] == null) {
                    throw new IOException("Unknown palette ID: " + paletteId);
                }
                Block block = palette[paletteId];
                if (!block.air()) {
                    int layerIndex = blockIndex & (BLOCKS_PER_LAYER - 1);
                    batch.setBlock(
                            worldX + (layerIndex & 15),
                            minY + (blockIndex >>> 8),
                            worldZ + (layerIndex >>> 4),
                            block);
                }
            }
        }
    }
}
