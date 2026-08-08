package net.swofty.service.replay.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import net.swofty.commons.replay.protocol.ReplayChunk;
import org.bson.Document;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReplayDatabase {
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> replays;
    private MongoCollection<Document> replayData;
    private MongoCollection<Document> maps;

    public void connect(String mongoUri) {
        Logger.info("Connecting to MongoDB...");
        client = MongoClients.create(mongoUri);
        database = client.getDatabase("Minestom");

        replays = database.getCollection("replays_v4");
        replayData = database.getCollection("replay_chunks_v4");
        maps = database.getCollection("replay_maps");

        replays.createIndex(Indexes.ascending("replayId"), new IndexOptions().unique(true));
        replays.createIndex(Indexes.ascending("gameId"));
        replays.createIndex(Indexes.descending("startTime"));
        replays.createIndex(Indexes.ascending("players"));

        replayData.createIndex(Indexes.ascending("replayId", "section", "sequence"), new IndexOptions().unique(true));

        maps.createIndex(Indexes.ascending("hash"), new IndexOptions().unique(true));

        Logger.info("Connected to MongoDB, collections initialized");
    }

    public void saveReplayMetadata(Document metadata) {
        replays.replaceOne(
            Filters.eq("replayId", metadata.getString("replayId")),
            metadata,
            new ReplaceOptions().upsert(true)
        );
    }

    public Document getReplayMetadata(UUID replayId) {
        return replays.find(Filters.eq("replayId", replayId.toString())).first();
    }

    public List<Document> getReplaysByPlayer(UUID playerId, int limit) {
        return replays.find(Filters.eq("players", playerId.toString()))
            .sort(new Document("startTime", -1))
            .limit(limit)
            .into(new ArrayList<>());
    }

    public List<Document> getReplaysByGame(String gameId) {
        return replays.find(Filters.eq("gameId", gameId))
            .into(new ArrayList<>());
    }


    public void saveReplayChunk(UUID replayId, ReplayChunk chunk) {
        Document doc = new Document()
            .append("replayId", replayId.toString())
                .append("section", chunk.section().name())
                .append("sequence", chunk.sequence())
                .append("startTick", chunk.startTick())
                .append("endTick", chunk.endTick())
                .append("uncompressedLength", chunk.uncompressedLength())
                .append("recordCount", chunk.recordCount())
                .append("checksum", chunk.checksum())
                .append("data", chunk.compressedPayload())
                .append("size", chunk.compressedPayload().length);

        replayData.replaceOne(
            Filters.and(
                Filters.eq("replayId", replayId.toString()),
                    Filters.eq("section", chunk.section().name()),
                    Filters.eq("sequence", chunk.sequence())
            ),
            doc,
            new ReplaceOptions().upsert(true)
        );
    }

    public List<Document> getReplayDataChunks(UUID replayId) {
        return replayData.find(Filters.eq("replayId", replayId.toString()))
                .sort(new Document("section", 1).append("sequence", 1))
            .into(new ArrayList<>());
    }

    public boolean hasMap(String mapHash) {
        return maps.countDocuments(Filters.eq("hash", mapHash)) > 0;
    }

    public void saveMap(String mapHash, String mapName, byte[] compressedData) {
        Document doc = new Document()
            .append("hash", mapHash)
            .append("name", mapName)
            .append("data", compressedData)
            .append("size", compressedData.length)
            .append("uploadedAt", System.currentTimeMillis());

        maps.replaceOne(
            Filters.eq("hash", mapHash),
            doc,
            new ReplaceOptions().upsert(true)
        );
    }

    public byte[] getMapData(String mapHash) {
        Document doc = maps.find(Filters.eq("hash", mapHash)).first();
        if (doc == null) return null;
        return doc.get("data", org.bson.types.Binary.class).getData();
    }

    public Document getMapMetadata(String mapHash) {
        return maps.find(Filters.eq("hash", mapHash)).first();
    }

    public long getTotalReplays() {
        return replays.countDocuments();
    }

    public long getTotalMaps() {
        return maps.countDocuments();
    }

    public long getTotalDataSize() {
        // Approximate total size from replay_data collection
        Document result = database.runCommand(new Document("collStats", "replay_chunks_v4"));
        return result.getInteger("size", 0);
    }
}
