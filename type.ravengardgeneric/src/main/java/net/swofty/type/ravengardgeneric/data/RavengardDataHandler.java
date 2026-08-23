package net.swofty.type.ravengardgeneric.data;

import lombok.Getter;
import net.swofty.PlayerField;
import net.swofty.codec.Codecs;
import net.swofty.commons.data.SwoftyData;
import net.swofty.type.generic.data.BackedField;
import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.data.DataWriteQueue;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.data.Datapoint;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.ravengardgeneric.data.datapoints.DatapointRavengardInteger;
import net.swofty.type.ravengardgeneric.data.datapoints.DatapointRavengardBoolean;
import net.swofty.type.ravengardgeneric.data.datapoints.DatapointRavengardString;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import tools.jackson.core.JacksonException;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class RavengardDataHandler extends DataHandler {

    @Getter
    private UUID currentProfileId;

    protected RavengardDataHandler() {
        super();
    }

    protected RavengardDataHandler(UUID uuid) {
        super(uuid);
    }

    public static RavengardDataHandler getUser(UUID uuid) {
        return PlayerDataService.get(RavengardDomain.KEY, uuid);
    }

    public static @Nullable RavengardDataHandler getUser(HypixelPlayer player) {
        return PlayerDataService.find(RavengardDomain.KEY, player.getUuid()).orElse(null);
    }

    public static RavengardDataHandler createFromDocument(UUID playerUuid, Document document) {
        RavengardDataHandler handler = new RavengardDataHandler(playerUuid);
        return handler.fromDocument(document);
    }

    @Override
    public RavengardDataHandler fromDocument(Document document) {
        if (document == null) {
            return initUserWithDefaultData(this.uuid);
        }

        if (document.containsKey("_id")) {
            this.uuid = UUID.fromString(document.getString("_id"));
        }

        for (Data data : Data.values()) {
            String key = data.getKey();
            if (!document.containsKey(key)) {
                this.datapoints.put(key, data.getDefaultDatapoint().deepClone().setUser(this).setData(data));
                continue;
            }

            String jsonValue = document.getString(key);
            try {
                Datapoint<?> datapoint = data.getDefaultDatapoint().deepClone();
                datapoint.deserializeValue(jsonValue);
                this.datapoints.put(key, datapoint.setUser(this).setData(data));
            } catch (Exception e) {
                this.datapoints.put(key, data.getDefaultDatapoint().deepClone().setUser(this).setData(data));
                Logger.warn(e, "Issue with Ravengard datapoint {} for user {} - defaulting", key, this.uuid);
            }
        }

        return this;
    }

    @Override
    public Document toDocument() {
        Document document = new Document();
        document.put("_owner", this.uuid.toString());
        if (currentProfileId != null) document.put("_id", currentProfileId.toString());

        for (Data data : Data.values()) {
            try {
                document.put(data.getKey(), getDatapoint(data.getKey()).getSerializedValue());
            } catch (JacksonException e) {
                Logger.error(e, "Failed to serialize Ravengard datapoint {} for user {}", data.getKey(), this.uuid);
            }
        }

        return document;
    }

    public Datapoint<?> get(Data datapoint) {
        Datapoint<?> datapointValue = this.datapoints.get(datapoint.key);
        return datapointValue != null ? datapointValue : datapoint.defaultDatapoint;
    }

    public <R extends Datapoint<?>> R get(Data datapoint, Class<R> type) {
        Datapoint<?> datapointValue = this.datapoints.get(datapoint.key);
        return (R) (datapointValue != null ? type.cast(datapointValue) : type.cast(datapoint.defaultDatapoint));
    }

    public void attachProfile(@Nullable UUID profileId) {
        DataWriteQueue.drain(getUuid());

        UUID previous = this.currentProfileId;
        this.currentProfileId = profileId;
        if (profileId != null) SwoftyData.profile().load(profileId);

        resetToDefaults();
        loadBackedData();

        if (previous != null && !previous.equals(profileId)) RavengardProfileStorage.release(previous);
    }

    private void resetToDefaults() {
        for (Data data : Data.values()) {
            try {
                this.datapoints.put(
                        data.getKey(),
                        data.getDefaultDatapoint().deepClone().setUser(this).setData(data)
                );
            } catch (Exception e) {
                Logger.error(e, "Issue with Ravengard datapoint {} for user {} - requires fixing", data.getKey(), uuid);
            }
        }
    }

    @Override
    public void runOnLoad(HypixelPlayer player) {
        for (Data data : Data.values()) {
            if (data.onLoad != null) {
                data.onLoad.accept(player, get(data));
            }
        }
    }

    @Override
    public void runOnSave(HypixelPlayer player) {
        for (Data data : Data.values()) {
            if (data.onQuit != null) {
                Datapoint<?> produced = data.onQuit.apply(player);
                Datapoint<?> target = get(data);
                target.setFrom(produced);
            }
        }
    }

    public static RavengardDataHandler initUserWithDefaultData(UUID uuid) {
        RavengardDataHandler handler = new RavengardDataHandler();
        handler.uuid = uuid;
        handler.resetToDefaults();
        return handler;
    }

    public static boolean hasDataInDocument(Document document) {
        if (document == null) {
            return false;
        }
        for (Data data : Data.values()) {
            if (document.containsKey(data.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static RavengardDataHandler getOfOfflinePlayer(UUID uuid) {
        RavengardDataHandler cached = PlayerDataService.find(RavengardDomain.KEY, uuid).orElse(null);
        if (cached != null) return cached;

        RavengardDataHandler handler = initUserWithDefaultData(uuid);
        handler.attachProfile(RavengardProfileIndex.read(uuid).selected());
        return handler;
    }

    public enum Data implements BackedField {
        DATA_VERSION(
                "ravengard_data_version",
                null,
                DatapointRavengardInteger.class,
                new DatapointRavengardInteger("ravengard_data_version", 1)
        ),
        CLASS(
                "ravengard_class",
                RavengardProfileFields.CLASS,
                DatapointRavengardString.class,
                new DatapointRavengardString("ravengard_class", "")
        ),
        LEVEL(
                "ravengard_level",
                RavengardProfileFields.LEVEL,
                DatapointRavengardInteger.class,
                new DatapointRavengardInteger("ravengard_level", 1)
        ),
        IS_TUTORIAL(
                "ravengard_is_tutorial",
                RavengardProfileFields.TUTORIAL,
                DatapointRavengardBoolean.class,
                new DatapointRavengardBoolean("ravengard_is_tutorial", true)
        ),
        CROWNS(
                "ravengard_crowns",
                RavengardProfileFields.CROWNS,
                DatapointRavengardInteger.class,
                new DatapointRavengardInteger("ravengard_crowns", 0)
        );

        @Getter
        private final String key;
        @Getter
        private final Class<? extends Datapoint<?>> type;
        @Getter
        private final Datapoint<?> defaultDatapoint;
        private final PlayerField<String> profileField;
        private final PlayerField<String> accountField;
        public final BiConsumer<HypixelPlayer, Datapoint<?>> onChange;
        public final BiConsumer<HypixelPlayer, Datapoint<?>> onLoad;
        public final Function<HypixelPlayer, Datapoint<?>> onQuit;

        Data(
                String key,
                PlayerField<String> profileField,
                Class<? extends Datapoint<?>> type,
                Datapoint<?> defaultDatapoint,
                BiConsumer<HypixelPlayer, Datapoint<?>> onChange,
                BiConsumer<HypixelPlayer, Datapoint<?>> onLoad,
                Function<HypixelPlayer, Datapoint<?>> onQuit
        ) {
            this.key = key;
            this.type = type;
            this.defaultDatapoint = defaultDatapoint;
            this.onChange = onChange;
            this.onLoad = onLoad;
            this.onQuit = onQuit;
            this.profileField = profileField;
            this.accountField = profileField != null ? null
                    : PlayerField.create(RavengardProfileFields.LEGACY_NAMESPACE, key, Codecs.STRING, null);
        }

        Data(String key, PlayerField<String> profileField, Class<? extends Datapoint<?>> type,
             Datapoint<?> defaultDatapoint) {
            this(key, profileField, type, defaultDatapoint, null, null, null);
        }

        @Override
        public String readData(DataHandler handler) {
            if (profileField == null) {
                return SwoftyData.account().get(handler.getUuid(), accountField);
            }
            UUID profileId = ((RavengardDataHandler) handler).getCurrentProfileId();
            return profileId == null ? null : SwoftyData.profile().get(profileId, profileField);
        }

        @Override
        public void writeData(DataHandler handler, String serialized) {
            if (profileField == null) {
                SwoftyData.account().set(handler.getUuid(), accountField, serialized);
                return;
            }
            UUID profileId = ((RavengardDataHandler) handler).getCurrentProfileId();
            if (profileId == null) return;
            SwoftyData.profile().set(profileId, profileField, serialized);
        }

        public boolean isProfileScoped() {
            return profileField != null;
        }

        public PlayerField<String> profileField() {
            return profileField;
        }

        public PlayerField<String> accountField() {
            return accountField;
        }

        public static Data fromKey(String key) {
            for (Data data : values()) {
                if (data.getKey().equals(key)) {
                    return data;
                }
            }
            return null;
        }
    }
}
