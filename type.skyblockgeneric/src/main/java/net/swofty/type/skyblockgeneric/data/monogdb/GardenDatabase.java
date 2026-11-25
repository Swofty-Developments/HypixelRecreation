package net.swofty.type.skyblockgeneric.data.monogdb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.swofty.type.generic.data.mongodb.MongoDB;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public record GardenDatabase(String profileUuid) implements MongoDB {
    public static MongoDatabase database;
    public static MongoCollection<Document> collection;

    public static void connect(MongoClient client) {
        database = client.getDatabase("Minestom");
        collection = database.getCollection("garden");
    }

    @Override
    public void set(String key, Object value) {
        insertOrUpdate(key, value);
    }

    @Override
    public Object get(String key, Object def) {
        Document doc = collection.find(Filters.eq("_id", profileUuid)).first();
        if (doc == null) {
            return def;
        }
        return doc.get(key);
    }

    public boolean has(String key) {
        Document doc = collection.find(Filters.eq("_id", profileUuid)).first();
        return doc != null && doc.containsKey(key);
    }

    public List<Document> getAll() {
        FindIterable<Document> results = collection.find();
        List<Document> list = new ArrayList<>();
        for (Document doc : results) {
            list.add(doc);
        }
        return list;
    }

    public Document getDocument() {
        Document query = new Document("_id", profileUuid);
        return collection.find(query).first();
    }

    @Override
    public boolean remove(String id) {
        Document query = new Document("_id", id);
        Document found = collection.find(query).first();
        if (found == null) {
            return false;
        }
        collection.deleteOne(query);
        return true;
    }

    public void insertOrUpdate(String key, Object value) {
        if (exists()) {
            Document query = new Document("_id", profileUuid);
            Document found = collection.find(query).first();
            assert found != null;
            collection.updateOne(found, Updates.set(key, value));
            return;
        }

        Document document = new Document("_id", profileUuid);
        document.append(key, value);
        collection.insertOne(document);
    }

    public boolean exists() {
        Document query = new Document("_id", profileUuid);
        Document found = collection.find(query).first();
        return found != null;
    }
}
