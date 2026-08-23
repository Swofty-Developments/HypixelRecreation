package net.swofty.type.generic.data.handlers;

import io.sentry.Sentry;
import lombok.Getter;
import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.data.Datapoint;
import net.swofty.type.generic.data.GameDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointBoolean;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsKitStats;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsLeaderboardPreferences;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsModeStats;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.datapoints.DatapointSoulWellUpgrades;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.generic.user.HypixelPlayer;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import tools.jackson.core.JacksonException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SkywarsDataHandler extends DataHandler implements GameDataHandler {
    public static final Map<UUID, SkywarsDataHandler> skywarsCache = new HashMap<>();

    public SkywarsDataHandler() { super(); }
    public SkywarsDataHandler(UUID uuid) { super(uuid); }

    @Override
    public String getHandlerId() {
        return "skywars";
    }

    @Override
    public Map<UUID, ? extends DataHandler> getCache() {
        return skywarsCache;
    }

    @Override
    public DataHandler createFromDocument(UUID playerUuid, Document document) {
        SkywarsDataHandler handler = new SkywarsDataHandler(playerUuid);
        return handler.fromDocument(document);
    }

    @Override
    public DataHandler initWithDefaults(UUID playerUuid) {
        return initUserWithDefaultData(playerUuid);
    }

    @Override
    public DataHandler getHandler(UUID playerUuid) {
        return skywarsCache.get(playerUuid);
    }

    @Override
    public void cacheHandler(UUID playerUuid, DataHandler handler) {
        skywarsCache.put(playerUuid, (SkywarsDataHandler) handler);
    }

    @Override
    public void removeFromCache(UUID playerUuid) {
        skywarsCache.remove(playerUuid);
    }

    public static SkywarsDataHandler getOfOfflinePlayer(UUID uuid) throws RuntimeException {
        SkywarsDataHandler handler = initUserWithDefaultData(uuid);
        handler.loadBackedData();
        return handler;
    }

    @Override
    public boolean hasDataInDocument(Document document) {
        if (document == null) return false;
        for (Data data : Data.values()) {
            if (document.containsKey(data.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static SkywarsDataHandler getUser(UUID uuid) {
        if (!skywarsCache.containsKey(uuid)) throw new RuntimeException("User " + uuid + " does not exist!");
        return skywarsCache.get(uuid);
    }

    public static @Nullable SkywarsDataHandler getUser(HypixelPlayer player) {
        try { return getUser(player.getUuid()); } catch (Exception e) { return null; }
    }

    @Override
    public SkywarsDataHandler fromDocument(Document document) {
        if (document == null) return initUserWithDefaultData(this.uuid);

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
                Datapoint<?> dp = data.getDefaultDatapoint().deepClone();
                dp.deserializeValue(jsonValue);
                this.datapoints.put(key, dp.setUser(this).setData(data));
            } catch (Exception e) {
                this.datapoints.put(key, data.getDefaultDatapoint().deepClone().setUser(this).setData(data));
                Logger.info("Issue with datapoint " + key + " for user " + this.uuid + " - defaulting");
                e.printStackTrace();
                Sentry.captureException(e);
            }
        }
        return this;
    }

    public static SkywarsDataHandler createFromDocument(Document document) {
        SkywarsDataHandler handler = new SkywarsDataHandler();
        return handler.fromDocument(document);
    }

    @Override
    public Document toDocument() {
        Document document = new Document();
        document.put("_owner", this.uuid.toString());
        for (Data data : Data.values()) {
            try {
                document.put(data.getKey(), getDatapoint(data.getKey()).getSerializedValue());
            } catch (JacksonException e) {
                e.printStackTrace();
            }
        }
        return document;
    }

    public Datapoint<?> get(Data datapoint) {
        Datapoint<?> dp = this.datapoints.get(datapoint.key);
        return dp != null ? dp : datapoint.defaultDatapoint;
    }

    public <R extends Datapoint<?>> R get(Data datapoint, Class<R> type) {
        Datapoint<?> dp = this.datapoints.get(datapoint.key);
        return (R) (dp != null ? type.cast(dp) : type.cast(datapoint.defaultDatapoint));
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

    public static SkywarsDataHandler initUserWithDefaultData(UUID uuid) {
        SkywarsDataHandler handler = new SkywarsDataHandler();
        handler.uuid = uuid;
        for (Data data : Data.values()) {
            try {
                handler.datapoints.put(
                        data.getKey(),
                        data.getDefaultDatapoint().deepClone().setUser(handler).setData(data)
                );
            } catch (Exception e) {
                Logger.error("Issue with datapoint " + data.getKey() + " for user " + uuid + " - fix");
                e.printStackTrace();
            }
        }
        return handler;
    }

    public enum Data {
        EXPERIENCE("skywars_experience", DatapointLong.class,
                new DatapointLong("skywars_experience", 0L)),
        SOULS("skywars_souls", DatapointLong.class,
                new DatapointLong("skywars_souls", 0L)),
        COINS("skywars_coins", DatapointLong.class,
                new DatapointLong("skywars_coins", 0L)),
        TOKENS("skywars_tokens", DatapointLong.class,
                new DatapointLong("skywars_tokens", 0L)),
        TOTAL_WINS("skywars_total_wins", DatapointLong.class,
                new DatapointLong("skywars_total_wins", 0L)),
        TOTAL_KILLS("skywars_total_kills", DatapointLong.class,
                new DatapointLong("skywars_total_kills", 0L)),
        MODE_STATS("skywars_mode_stats", DatapointSkywarsModeStats.class,
                new DatapointSkywarsModeStats("skywars_mode_stats")),
        UNLOCKS("skywars_unlocks", DatapointSkywarsUnlocks.class,
                new DatapointSkywarsUnlocks("skywars_unlocks")),
        LEADERBOARD_PREFS("skywars_leaderboard_prefs", DatapointSkywarsLeaderboardPreferences.class,
                new DatapointSkywarsLeaderboardPreferences("skywars_leaderboard_prefs")),
        HIDE_LEVEL("skywars_hide_level", DatapointBoolean.class,
                new DatapointBoolean("skywars_hide_level", false)),
        SOUL_WELL_UPGRADES("skywars_soul_well_upgrades", DatapointSoulWellUpgrades.class,
                new DatapointSoulWellUpgrades("skywars_soul_well_upgrades")),
        KIT_STATS("skywars_kit_stats", DatapointSkywarsKitStats.class,
                new DatapointSkywarsKitStats("skywars_kit_stats"));

        @Getter
        private final String key;
        @Getter private final Class<? extends Datapoint<?>> type;
        @Getter private final Datapoint<?> defaultDatapoint;
        public final BiConsumer<HypixelPlayer, Datapoint<?>> onChange;
        public final BiConsumer<HypixelPlayer, Datapoint<?>> onLoad;
        public final Function<HypixelPlayer, Datapoint<?>> onQuit;

        Data(String key,
             Class<? extends Datapoint<?>> type,
             Datapoint<?> defaultDatapoint,
             BiConsumer<HypixelPlayer, Datapoint<?>> onChange,
             BiConsumer<HypixelPlayer, Datapoint<?>> onLoad,
             Function<HypixelPlayer, Datapoint<?>> onQuit) {
            this.key = key; this.type = type; this.defaultDatapoint = defaultDatapoint;
            this.onChange = onChange; this.onLoad = onLoad; this.onQuit = onQuit;
        }

        Data(String key, Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint,
             BiConsumer<HypixelPlayer, Datapoint<?>> onChange, BiConsumer<HypixelPlayer, Datapoint<?>> onLoad) {
            this(key, type, defaultDatapoint, onChange, onLoad, null);
        }

        Data(String key, Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint,
             BiConsumer<HypixelPlayer, Datapoint<?>> onChange) {
            this(key, type, defaultDatapoint, onChange, null, null);
        }

        Data(String key, Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint) {
            this(key, type, defaultDatapoint, null, null, null);
        }

        public static Data fromKey(String key) {
            for (Data d : values()) if (d.getKey().equals(key)) return d;
            return null;
        }
    }
}
