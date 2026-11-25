package net.swofty.commons.replay.protocol;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplayCompression {
    private static final int LEVEL = 3;
    public static final int DEFAULT_MAX_DECOMPRESSED_BYTES = 64 * 1024 * 1024;

    public static byte[] compress(byte[] data) {
        return Zstd.compress(data, LEVEL);
    }

    public static byte[] decompress(byte[] compressedData) throws IOException {
        return decompress(compressedData, DEFAULT_MAX_DECOMPRESSED_BYTES);
    }

    public static byte[] decompress(byte[] compressedData, int maxDecompressedBytes) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            throw new IOException("Replay payload is empty");
        }
        if (maxDecompressedBytes <= 0) {
            throw new IllegalArgumentException("maxDecompressedBytes must be positive");
        }
        long size = Zstd.getFrameContentSize(compressedData);
        if (size <= 0 || size > maxDecompressedBytes) {
            throw new IOException("Invalid zstd frame content size: " + size);
        }

        try {
            return Zstd.decompress(compressedData, (int) size);
        } catch (ZstdException e) {
            throw new IOException("Failed to decompress replay zstd payload", e);
        }
    }
}
