package net.swofty.type.generic.data;

import lombok.Getter;
import lombok.NonNull;
import net.swofty.PlayerField;
import net.swofty.codec.Codecs;
import net.swofty.commons.data.SwoftyData;
import net.swofty.type.generic.data.domain.AccountDomain;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.user.HypixelPlayer;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Getter
public abstract class DataHandler {
    protected UUID uuid;
    protected final Map<String, Datapoint<?>> datapoints = new ConcurrentHashMap<>();

    protected DataHandler() {}
    protected DataHandler(UUID uuid) { this.uuid = uuid; }

    public Datapoint<?> getDatapoint(String key) { return this.datapoints.get(key); }

    private static final Map<String, PlayerField<String>> gameFields = new ConcurrentHashMap<>();

    private static PlayerField<String> gameField(String key) {
        return gameFields.computeIfAbsent(key, k -> PlayerField.create("game", k, Codecs.STRING, null));
    }

    public void loadBackedData() {
        for (Datapoint<?> datapoint : datapoints.values()) {
            String stored = datapoint.getData() instanceof BackedField field
                    ? field.readData(this)
                    : SwoftyData.account().get(getUuid(), gameField(datapoint.getKey()));
            if (stored != null) datapoint.deserializeValue(stored);
        }
    }

    public void saveBackedData() {
        saveBackedData(datapoint -> true);
    }

    public void saveBackedData(Predicate<Datapoint<?>> filter) {
        DataWriteQueue.drain(getUuid());
        for (Datapoint<?> datapoint : datapoints.values()) {
            if (!filter.test(datapoint)) continue;
            try {
                String serialized = datapoint.getSerializedValue();
                writeBackedValue(datapoint.getData(), datapoint.getKey(), serialized);
                datapoint.markPersisted(serialized);
            } catch (Exception e) {
                Logger.error(e, "Failed to save datapoint {} for user {}", datapoint.getKey(), getUuid());
            }
        }
    }

    public void writeBackedValue(Enum<?> data, String key, String serialized) {
        if (data instanceof BackedField field) {
            field.writeData(this, serialized);
        } else {
            SwoftyData.account().set(getUuid(), gameField(key), serialized);
        }
    }

    public static @NonNull DataHandler getUser(UUID uuid) {
        return PlayerDataService.get(AccountDomain.KEY, uuid);
    }

    public static DataHandler getUser(HypixelPlayer player) { return getUser(player.getUuid()); }

    public static Optional<DataHandler> findUser(UUID uuid) {
        return PlayerDataService.find(AccountDomain.KEY, uuid).map(handler -> (DataHandler) handler);
    }

    public static Optional<DataHandler> findUser(HypixelPlayer player) {
        return findUser(player.getUuid());
    }

    public static Optional<DataHandler> awaitUser(UUID uuid, long timeoutMillis) {
        return Optional.ofNullable(PlayerDataService.await(AccountDomain.KEY, uuid, timeoutMillis));
    }

    public abstract DataHandler fromDocument(Document document);
    public abstract Document toDocument();
    public abstract void runOnLoad(HypixelPlayer player);
    public abstract void runOnSave(HypixelPlayer player);

    public static DataHandler initUserWithDefaultData(UUID uuid) {
        throw new UnsupportedOperationException("Must be implemented by subclass");
    }

    public static @Nullable UUID getPotentialUUIDFromName(String name) throws RuntimeException {
        throw new UnsupportedOperationException("Implement if name lookup is supported");
    }
}
