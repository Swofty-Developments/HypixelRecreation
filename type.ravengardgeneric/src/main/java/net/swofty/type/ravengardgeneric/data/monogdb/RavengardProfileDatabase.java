package net.swofty.type.ravengardgeneric.data.monogdb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.swofty.type.ravengardgeneric.profile.RavengardProfile;
import org.bson.Document;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RavengardProfileDatabase {
    public static MongoDatabase database;
    public static MongoCollection<Document> collection;

    private RavengardProfileDatabase() {
    }

    public static void connect(MongoClient client) {
        database = client.getDatabase("Minestom");
        collection = database.getCollection("ravengard_profiles");
    }

    public static List<RavengardProfile> byOwner(UUID owner) {
        List<RavengardProfile> profiles = new ArrayList<>();
        if (collection == null) {
            Logger.warn("The legacy Ravengard profile collection is unavailable, {} may be handed a fresh profile",
                    owner);
            return profiles;
        }
        for (Document document : collection.find(new Document("_owner", owner.toString()))
                .sort(new Document("created", 1))) {
            profiles.add(RavengardProfile.fromDocument(document));
        }
        return profiles;
    }

    public static RavengardProfile byId(UUID id) {
        if (collection == null || id == null) {
            return null;
        }
        Document document = collection.find(new Document("_id", id.toString())).first();
        return document == null ? null : RavengardProfile.fromDocument(document);
    }
}
