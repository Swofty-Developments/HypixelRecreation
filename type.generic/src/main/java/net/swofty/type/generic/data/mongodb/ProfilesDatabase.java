package net.swofty.type.generic.data.mongodb;

import com.mongodb.client.MongoClient;
import net.swofty.PlayerField;
import net.swofty.commons.data.NameIndex;
import net.swofty.commons.data.SwoftyData;
import net.swofty.commons.skyblock.SkyBlockProfileFields;
import org.bson.Document;
import org.tinylog.Logger;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public record ProfilesDatabase(String id) implements MongoDB {
    public static final PlayerField<String> DOCUMENT = SkyBlockProfileFields.DOCUMENT;

    private static volatile Predicate<UUID> hostedProfiles = profileId -> false;

    public static void connect(MongoClient client) {
    }

    public static void setHostedProfileCheck(Predicate<UUID> check) {
        hostedProfiles = check == null ? profileId -> false : check;
    }

    private Document read() {
        return fetchDocument(id);
    }

    @Override
    public void set(String key, Object value) {
        insertOrUpdate(key, value);
    }

    @Override
    public Object get(String key, Object def) {
        Document doc = read();
        return doc == null ? def : doc.getOrDefault(key, def);
    }

    public void insertOrUpdate(String key, Object value) {
        Document doc = read();
        if (doc == null) doc = new Document("_id", id);
        doc.put(key, value);
        saveDocument(doc);
    }

    public Document getDocument() {
        return read();
    }

    public void saveDocument(Document document) {
        writeDocument(UUID.fromString(id), document.toJson());
    }

    public boolean exists() {
        return readDocument(UUID.fromString(id)) != null;
    }

    @Override
    public boolean remove(String uniqueId) {
        writeDocument(UUID.fromString(uniqueId), null);
        return true;
    }

    public List<Document> getAll() {
        return List.of();
    }

    public static void replaceDocument(String uniqueId, Document document) {
        writeDocument(UUID.fromString(uniqueId), document.toJson());
    }

    public static UUID fetchUUID(String username) {
        return NameIndex.lookup(username);
    }

    public static Document fetchDocument(String uniqueId) {
        String json = readDocument(UUID.fromString(uniqueId));
        return json == null ? null : Document.parse(json);
    }

    public static Document fetchDocument(UUID uniqueId) {
        return fetchDocument(uniqueId.toString());
    }

    public static void deleteDocument(String uniqueId) {
        writeDocument(UUID.fromString(uniqueId), null);
    }

    private static String readDocument(UUID profileId) {
        try {
            return SwoftyData.profile().get(profileId, DOCUMENT);
        } finally {
            release(profileId);
        }
    }

    private static void writeDocument(UUID profileId, String json) {
        try {
            SwoftyData.profile().set(profileId, DOCUMENT, json);
        } finally {
            release(profileId);
        }
    }

    private static void release(UUID profileId) {
        if (hostedProfiles.test(profileId)) return;
        try {
            SwoftyData.profile().unload(profileId);
        } catch (Exception e) {
            Logger.error(e, "Failed to release offline profile container {}", profileId);
        }
    }
}
