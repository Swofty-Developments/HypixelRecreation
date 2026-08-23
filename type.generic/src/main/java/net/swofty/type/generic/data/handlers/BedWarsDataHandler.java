package net.swofty.type.generic.data.handlers;

import io.sentry.Sentry;
import lombok.Getter;
import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.data.Datapoint;
import net.swofty.type.generic.data.GameDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsModeStats;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsQuickBuy;
import net.swofty.type.generic.data.datapoints.DatapointCollectibles;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardPreferences;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointMapStringLong;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.generic.user.HypixelPlayer;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.tinylog.Logger;
import tools.jackson.core.JacksonException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BedWarsDataHandler extends DataHandler implements GameDataHandler {
    public static final Map<UUID, BedWarsDataHandler> bedwarsCache = new HashMap<>();

    public BedWarsDataHandler() { super(); }
    public BedWarsDataHandler(UUID uuid) { super(uuid); }

    @Override
    public String getHandlerId() {
        return "bedwars";
    }

    @Override
    public Map<UUID, ? extends DataHandler> getCache() {
        return bedwarsCache;
    }

    @Override
    public DataHandler createFromDocument(UUID playerUuid, Document document) {
        BedWarsDataHandler handler = new BedWarsDataHandler(playerUuid);
        return handler.fromDocument(document);
    }

    @Override
    public DataHandler initWithDefaults(UUID playerUuid) {
        return initUserWithDefaultData(playerUuid);
    }

    @Override
    public DataHandler getHandler(UUID playerUuid) {
        return bedwarsCache.get(playerUuid);
    }

    @Override
    public void cacheHandler(UUID playerUuid, DataHandler handler) {
        bedwarsCache.put(playerUuid, (BedWarsDataHandler) handler);
    }

    @Override
    public void removeFromCache(UUID playerUuid) {
        bedwarsCache.remove(playerUuid);
    }

    public static BedWarsDataHandler getOfOfflinePlayer(UUID uuid) throws RuntimeException {
        BedWarsDataHandler handler = initUserWithDefaultData(uuid);
        handler.loadBackedData();
        return handler;
    }

    @Override
    public boolean hasDataInDocument(Document document) {
        if (document == null) return false;
        // Check if any BedWars data key exists in the document
        for (Data data : Data.values()) {
            if (document.containsKey(data.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static @NonNull BedWarsDataHandler getUser(UUID uuid) {
        if (!bedwarsCache.containsKey(uuid)) throw new RuntimeException("User " + uuid + " does not exist!");
        return bedwarsCache.get(uuid);
    }

    public static @Nullable BedWarsDataHandler getUser(HypixelPlayer player) {
        try { return getUser(player.getUuid()); } catch (Exception e) { return null; }
    }

    @Override
    public BedWarsDataHandler fromDocument(Document document) {
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
                // Use deepClone to preserve special datapoint properties (e.g., leaderboard keys)
                Datapoint<?> dp = data.getDefaultDatapoint().deepClone();
                dp.deserializeValue(jsonValue);
                this.datapoints.put(key, dp.setUser(this).setData(data));
            } catch (Exception e) {
                this.datapoints.put(key, data.getDefaultDatapoint().deepClone().setUser(this).setData(data));
                Logger.info("Issue with datapoint " + key + " for user " + this.uuid + " - defaulting");
                Sentry.captureException(e);
                e.printStackTrace();
            }
        }
        return this;
    }

    public static BedWarsDataHandler createFromDocument(Document document) {
        BedWarsDataHandler handler = new BedWarsDataHandler();
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

    /** Untyped convenience getter. */
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
                target.setFrom(produced); // no onChange during save
            }
        }
    }

    public static BedWarsDataHandler initUserWithDefaultData(UUID uuid) {
        BedWarsDataHandler handler = new BedWarsDataHandler();
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
                Sentry.captureException(e);
            }
        }
        return handler;
    }

    /** BedWars specific data enum. */
    public enum Data {
        // Leaderboard-tracked fields
        EXPERIENCE("bedwars_experience", DatapointLeaderboardLong.class,
            new DatapointLeaderboardLong("bedwars_experience", 0L, "bedwars:experience")),
        TOKENS("bedwars_tokens", DatapointLeaderboardLong.class,
            new DatapointLeaderboardLong("bedwars_tokens", 0L, "bedwars:tokens")),
        SLUMBER_TICKETS("bedwars_slumber_tickets", DatapointLeaderboardLong.class,
            new DatapointLeaderboardLong("bedwars_slumber_tickets", 0L, "bedwars:slumber_tickets")),
        FAVORITE_MAPS("bedwars_favorite_maps", DatapointStringList.class, new DatapointStringList("bedwars_favorite_maps")),
        QUICK_BUY("bedwars_quick_buy", DatapointBedWarsQuickBuy.class, new DatapointBedWarsQuickBuy("bedwars_quick_buy")),
        MAP_JOIN_COUNTS("bedwars_map_join_counts", DatapointMapStringLong.class, new DatapointMapStringLong("bedwars_map_join_counts")),
        HOTBAR_LAYOUT("bedwars_hotbar_layout", DatapointBedWarsHotbar.class, new DatapointBedWarsHotbar("bedwars_hotbar_layout")),
        COLLECTIBLES("bedwars_collectibles", DatapointCollectibles.class,
            new DatapointCollectibles("bedwars_collectibles", DatapointCollectibles.CollectiblesState.bedWarsDefaults())),

        // Cumulative lifetime stats (for quick access without DB aggregation)
        TOTAL_WINS("bedwars_total_wins", DatapointLong.class, new DatapointLong("bedwars_total_wins", 0L)),
        TOTAL_FINAL_KILLS("bedwars_total_final_kills", DatapointLong.class, new DatapointLong("bedwars_total_final_kills", 0L)),
        TOTAL_BEDS_BROKEN("bedwars_total_beds_broken", DatapointLong.class, new DatapointLong("bedwars_total_beds_broken", 0L)),

        // Per-mode and per-period stats (wins, final kills, beds broken)
        MODE_STATS("bedwars_mode_stats", DatapointBedWarsModeStats.class, new DatapointBedWarsModeStats("bedwars_mode_stats")),

        // Leaderboard hologram filter preferences (period, mode, view, text alignment)
        LEADERBOARD_PREFS("bedwars_lb_prefs", DatapointLeaderboardPreferences.class, new DatapointLeaderboardPreferences("bedwars_lb_prefs"));

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
