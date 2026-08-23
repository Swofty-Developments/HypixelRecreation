package net.swofty.type.generic.data.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public final class MongoConnection {
    public static final String DATABASE = "Minestom";

    private static volatile MongoClient client;

    private MongoConnection() {}

    public static void connect(MongoClient mongoClient) {
        client = mongoClient;
    }

    public static MongoClient client() {
        return client;
    }

    public static MongoCollection<Document> collection(String name) {
        MongoClient mongoClient = client;
        return mongoClient == null ? null : mongoClient.getDatabase(DATABASE).getCollection(name);
    }
}
