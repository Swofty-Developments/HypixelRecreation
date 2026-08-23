package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

final class CaptureStreams implements AutoCloseable {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    private final Path directory;
    private final Map<String, BufferedWriter> writers = new LinkedHashMap<>();
    private final Map<String, Integer> counts = new LinkedHashMap<>();
    private boolean dirty;
    private String lastError;

    CaptureStreams(Path directory) throws IOException {
        Files.createDirectories(directory);
        this.directory = directory;
    }

    void write(String stream, JsonObject event) {
        BufferedWriter writer = writers.get(stream);
        if (writer == null) {
            try {
                writer = Files.newBufferedWriter(directory.resolve(stream + ".jsonl"), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (IOException exception) {
                lastError = exception.getMessage();
                return;
            }
            writers.put(stream, writer);
        }

        try {
            writer.write(GSON.toJson(event));
            writer.newLine();
            counts.merge(stream, 1, Integer::sum);
            dirty = true;
        } catch (IOException exception) {
            lastError = exception.getMessage();
        }
    }

    void flush() {
        if (!dirty) {
            return;
        }
        dirty = false;
        for (BufferedWriter writer : writers.values()) {
            try {
                writer.flush();
            } catch (IOException exception) {
                lastError = exception.getMessage();
            }
        }
    }

    Map<String, Integer> counts() {
        return Map.copyOf(counts);
    }

    int total() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    String lastError() {
        return lastError;
    }

    @Override
    public void close() {
        for (BufferedWriter writer : writers.values()) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException exception) {
                lastError = exception.getMessage();
            }
        }
        writers.clear();
    }
}
