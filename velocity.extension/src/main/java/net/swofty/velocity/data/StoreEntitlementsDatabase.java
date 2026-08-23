package net.swofty.velocity.data;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class StoreEntitlementsDatabase {
    private static final String COLLECTION = "store-player-entitlements";

    private static MongoClient client;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;

    private StoreEntitlementsDatabase() {}

    public static void connect(String connectionString) {
        ConnectionString cs = new ConnectionString(connectionString);
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs).build();
        client = MongoClients.create(settings);
        database = client.getDatabase("Minestom");
        collection = database.getCollection(COLLECTION);
    }

    public static MongoCollection<Document> collection() {
        return collection;
    }
}
