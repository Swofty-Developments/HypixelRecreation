package net.swofty.type.generic.data;

import io.sentry.Sentry;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import net.swofty.PlayerField;
import net.swofty.codec.Codecs;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.TeamColorUtil;
import net.swofty.commons.data.NameIndex;
import net.swofty.commons.data.SwoftyData;
import net.swofty.type.generic.data.domain.AccountDomain;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.*;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.utility.ScheduleUtility;
import org.bson.Document;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class HypixelDataHandler extends DataHandler {

    protected HypixelDataHandler() { super(); }
    public HypixelDataHandler(UUID uuid) { super(uuid); }

    public static HypixelDataHandler getUser(UUID uuid) {
        return PlayerDataService.get(AccountDomain.KEY, uuid);
    }

    public static @Nullable HypixelDataHandler getUser(Player player) {
        return PlayerDataService.find(AccountDomain.KEY, player.getUuid()).orElse(null);
    }

    public static java.util.Optional<HypixelDataHandler> findHypixelUser(UUID uuid) {
        return PlayerDataService.find(AccountDomain.KEY, uuid);
    }

    public static java.util.Optional<HypixelDataHandler> awaitHypixelUser(UUID uuid, long timeoutMillis) {
        return awaitUser(uuid, timeoutMillis).map(h -> (HypixelDataHandler) h);
    }

    @Override
    public HypixelDataHandler fromDocument(Document document) {
        if (document == null) return initUserWithDefaultData(this.uuid);

        String owner = document.getString("_id") != null ? document.getString("_id") : document.getString("_owner");
        if (owner != null) this.uuid = UUID.fromString(owner);
        for (Data data : Data.values()) {
            String key = data.getKey();
            if (!document.containsKey(key)) {
                this.datapoints.put(key, data.getDefaultDatapoint().deepClone().setUser(this).setData(data));
                continue;
            }
            String jsonValue = document.getString(key);
            try {
                Datapoint<?> dp = data.getDefaultDatapoint().getClass()
                        .getDeclaredConstructor(String.class).newInstance(key);
                dp.deserializeValue(jsonValue);
                this.datapoints.put(key, dp.setUser(this).setData(data));
            } catch (Exception e) {
                this.datapoints.put(key, data.getDefaultDatapoint().deepClone().setUser(this).setData(data));
                Logger.error(e, "Issue with datapoint {} for user {} - defaulting to default value", key, this.uuid);
                Sentry.captureException(e);
            }
        }
        return this;
    }

    public static HypixelDataHandler createFromDocument(Document document) {
        HypixelDataHandler h = new HypixelDataHandler();
        return h.fromDocument(document);
    }

    @Override
    public Document toDocument() {
        Document document = new Document();
        document.put("_owner", this.uuid.toString());
        for (Data data : Data.values()) {
            try {
                document.put(data.getKey(), getDatapoint(data.getKey()).getSerializedValue());
            } catch (Exception e) {
                Logger.error(e, "Failed to serialize datapoint {} for user {}", data.getKey(), this.uuid);
                Sentry.captureException(e);
            }
        }
        return document;
    }

    /** Untyped convenience getter. */
    public Datapoint<?> get(Data datapoint) {
        Datapoint<?> dp = this.datapoints.get(datapoint.key);
        return dp != null ? dp : datapoint.defaultDatapoint;
    }

    /** Optionally typed getter (casts to the class you pass). */
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
        Thread.startVirtualThread(this::indexName);
    }

    @Override
    public void runOnSave(HypixelPlayer player) {
        for (Data data : Data.values()) {
            if (data.onQuit != null) {
                Datapoint<?> produced = data.onQuit.apply(player);
                if (produced != null) {
                    Datapoint<?> target = get(data);
                    target.setFrom(produced); // no onChange during save
                }
            }
        }
    }

    public static HypixelDataHandler initUserWithDefaultData(UUID uuid) {
        HypixelDataHandler h = new HypixelDataHandler();
        h.uuid = uuid;
        for (Data data : Data.values()) {
            try {
                h.datapoints.put(
                        data.getKey(),
                        data.getDefaultDatapoint().deepClone().setUser(h).setData(data)
                );
            } catch (Exception e) {
                Logger.error(e, "Issue with datapoint {} for user {} - requires fixing", data.getKey(), uuid);
            }
        }
        return h;
    }

    public void loadFromApi() {
        SwoftyData.account().load(uuid);
        loadBackedData();
        indexName();
    }

    public void loadFromTransferDocument(Document document) {
        SwoftyData.account().load(uuid);
        fromDocument(document);
        saveBackedData();
        indexName();
    }

    public void saveToApi() {
        saveBackedData();
    }

    private void indexName() {
        String ign = get(Data.IGN, DatapointString.class).getValue();
        if (ign != null && !ign.equals("null")) NameIndex.index(ign, uuid);
    }

    public static HypixelDataHandler getOfOfflinePlayer(UUID uuid) throws RuntimeException {
        HypixelDataHandler cached = PlayerDataService.find(AccountDomain.KEY, uuid).orElse(null);
        if (cached != null) return cached;

        HypixelDataHandler handler = initUserWithDefaultData(uuid);
        try {
            handler.loadFromApi();
        } finally {
            releaseOfflineAccount(uuid);
        }
        return handler;
    }

    private static void releaseOfflineAccount(UUID uuid) {
        if (PlayerDataService.isLoaded(AccountDomain.KEY, uuid)) return;
        try {
            SwoftyData.account().unload(uuid);
        } catch (Exception e) {
            Logger.error(e, "Failed to release offline account container for user {}", uuid);
        }
    }

    @Blocking
    public static String getPotentialIGNFromUUID(UUID uuid) {
        HypixelDataHandler cached = PlayerDataService.find(AccountDomain.KEY, uuid).orElse(null);
        if (cached != null) return cached.get(Data.IGN, DatapointString.class).getValue();

        String stored;
        try {
            stored = SwoftyData.account().get(uuid, Data.IGN.field);
        } finally {
            releaseOfflineAccount(uuid);
        }
        if (stored == null) return "null";
        DatapointString dp = new DatapointString("ign");
        dp.deserializeValue(stored);
        return dp.getValue();
    }

    @Blocking
    public static @Nullable UUID getPotentialUUIDFromName(String name) throws RuntimeException {
        return NameIndex.lookup(name);
    }

    public enum Data implements BackedField {
        RANK("rank", DatapointRank.class, new DatapointRank("rank", Rank.DEFAULT),
                (player, datapoint) -> {
            player.sendPacket(MinecraftServer.getCommandManager().createDeclareCommandsPacket(player));

            Rank rank = (Rank) datapoint.getValue();

            // Delay this as player needs to be loaded
            ScheduleUtility.delay(() -> {
                if (!player.isOnline()) return;
                if (HypixelConst.getTypeLoader().getType().isSkyBlock()) return;
                if (HypixelConst.getTypeLoader().getType() == ServerType.BEDWARS_GAME) return;

                String teamName = StringUtility.limitStringLength(rank.getPriorityCharacter() + player.getUsername(), 15);
                Team team = new TeamBuilder("H" + teamName, MinecraftServer.getTeamManager())
                    .prefix(((HypixelPlayer) player).getRankPrefix())
                        .teamColor(TeamColorUtil.fromNamedColor(rank.getTextColor()))
                        .build();
                player.setTeam(team);
                player.getTeam().sendUpdatePacket();
            }, 5);
        }, (player, datapoint) -> {
            player.sendPacket(MinecraftServer.getCommandManager().createDeclareCommandsPacket(player));
            if (HypixelConst.getTypeLoader().getType().isSkyBlock()) return;
            if (HypixelConst.getTypeLoader().getType() == ServerType.BEDWARS_GAME) return;

            Rank rank = (Rank) datapoint.getValue();
            String teamName = StringUtility.limitStringLength(rank.getPriorityCharacter() + player.getUsername(), 15);
            player.setTeam(new TeamBuilder("H" + teamName, MinecraftServer.getTeamManager())
                .prefix(((HypixelPlayer) player).getRankPrefix())
                    .teamColor(TeamColorUtil.fromNamedColor(rank.getTextColor()))
                    .build());
            player.getTeam().sendUpdatePacket();
        }),

        IGN_LOWER("ignLowercase", DatapointString.class, new DatapointString("ignLowercase", "null"),
                null, (player, datapoint) -> ((DatapointString) datapoint).setValue(player.getUsername().toLowerCase())),

        IGN("ign", DatapointString.class, new DatapointString("ign", "null"),
                null, (player, datapoint) -> ((DatapointString) datapoint).setValue(player.getUsername())),

        CHAT_TYPE("chat_type", DatapointChatType.class,
                new DatapointChatType("chat_type", new DatapointChatType.ChatType(DatapointChatType.Chats.ALL))),

        LOCALE("locale",
            DatapointLocale.class,
            new DatapointLocale(
                "locale",
                new DatapointLocale.LocaleType(DatapointLocale.SupportedLocale.UNSET)
            ),
            (player, datapoint) -> {
                DatapointLocale localeDatapoint = (DatapointLocale) datapoint;
                DatapointLocale.LocaleType localeType = localeDatapoint.getValue();

                if (localeType.getCurrentLocale() == DatapointLocale.SupportedLocale.UNSET) {
                    return;
                }

                player.setLocale(localeType.getCurrentLocale().getLocale());
            },
            (player, datapoint) -> {
                DatapointLocale localeDatapoint = (DatapointLocale) datapoint;
                DatapointLocale.LocaleType localeType = localeDatapoint.getValue();

                if (localeType.getCurrentLocale() == DatapointLocale.SupportedLocale.UNSET) {
                    localeDatapoint.setValue(
                        new DatapointLocale.LocaleType(
                            DatapointLocale.SupportedLocale.fromLocale(player.getLocale())
                        )
                    );
                    return;
                }

                player.setLocale(localeType.getCurrentLocale().getLocale());
            },
            player -> new DatapointLocale(
                "locale",
                new DatapointLocale.LocaleType(
                    DatapointLocale.SupportedLocale.fromLocale(player.getLocale())
                )
            )
        ),

        TOGGLES("toggles", DatapointToggles.class, new DatapointToggles("toggles")),

        GAMEMODE("gamemode", DatapointGamemode.class, new DatapointGamemode("gamemode", GameMode.SURVIVAL),
                (player, datapoint) -> {
                    if (HypixelConst.getTypeLoader().getType().isSkyBlock()) {
                        player.setGameMode((GameMode) datapoint.getValue());
                    }
                },
                (player, datapoint) -> {
                    if (HypixelConst.getTypeLoader().getType().isSkyBlock()) {
                        // Reset to SURVIVAL if coming from non-SkyBlock server
                        HypixelPlayer hypixelPlayer = (HypixelPlayer) player;
                        if (hypixelPlayer.getOriginServer() == null || !hypixelPlayer.getOriginServer().isSkyBlock()) {
                            player.setGameMode(GameMode.SURVIVAL);
                        } else {
                            player.setGameMode((GameMode) datapoint.getValue());
                        }
                    }
                },
                (player) -> {
                    if (HypixelConst.getTypeLoader().getType().isSkyBlock()) {
                        return new DatapointGamemode("gamemode", player.getGameMode());
                    }
                    return null; // Don't update gamemode for non-SkyBlock servers
                }),

        // combine these for a HypixelSkin? could use the same in tab then and other places
        SKIN_SIGNATURE("skin_signature",
                DatapointString.class, new DatapointString("skin_signature", "null"),
                (player, datapoint) -> {},
                (player, datapoint) -> ((DatapointString) datapoint).setValue(player.getSkin().signature())),

        SKIN_TEXTURE("skin_texture",
                DatapointString.class, new DatapointString("skin_texture", "null"),
                (player, datapoint) -> {},
                (player, datapoint) -> ((DatapointString) datapoint).setValue(player.getSkin().textures())),

        HYPIXEL_EXPERIENCE("hypixel_experience",
                DatapointHypixelExperience.class, new DatapointHypixelExperience("hypixel_experience", 0L)),

        RANK_COLOR("rank_color", DatapointString.class, new DatapointString("rank_color", "RED")),
        MVP_PLUS_PLUS_AQUA("mvp_plus_plus_aqua", DatapointBoolean.class, new DatapointBoolean("mvp_plus_plus_aqua", false)),
        RANKS_GIFTED("ranks_gifted", DatapointInteger.class, new DatapointInteger("ranks_gifted", 0)),

        ACHIEVEMENT_DATA("achievement_data",
                DatapointAchievementData.class, new DatapointAchievementData("achievement_data")),

        QUEST_DATA("quest_data",
                DatapointQuestData.class, new DatapointQuestData("quest_data")),

        PARKOUR_DATA("parkour_data", DatapointParkourData.class, new DatapointParkourData("parkour_data")),

        FRIEND_SORT("friend_sort", DatapointFriendSort.class,
                new DatapointFriendSort("friend_sort",
                        new DatapointFriendSort.FriendSortData(DatapointFriendSort.SortType.DEFAULT, false)))
        ;

        @Getter private final String key;
        @Getter private final Class<? extends Datapoint<?>> type;
        @Getter private final Datapoint<?> defaultDatapoint;
        private final PlayerField<String> field;
        public final BiConsumer<Player, Datapoint<?>> onChange;
        public final BiConsumer<Player, Datapoint<?>> onLoad;
        public final Function<Player, Datapoint<?>> onQuit;

        Data(String key,
             Class<? extends Datapoint<?>> type,
             Datapoint<?> defaultDatapoint,
             BiConsumer<Player, Datapoint<?>> onChange,
             BiConsumer<Player, Datapoint<?>> onLoad,
             Function<Player, Datapoint<?>> onQuit) {
            this.key = key; this.type = type; this.defaultDatapoint = defaultDatapoint;
            this.field = PlayerField.create("hypixel", key, Codecs.STRING, null);
            this.onChange = onChange; this.onLoad = onLoad; this.onQuit = onQuit;
        }

        @Override
        public String readData(DataHandler handler) {
            return SwoftyData.account().get(handler.getUuid(), field);
        }

        @Override
        public void writeData(DataHandler handler, String serialized) {
            SwoftyData.account().set(handler.getUuid(), field, serialized);
        }

        public PlayerField<String> accountField() {
            return field;
        }
        Data(String key, Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint,
             BiConsumer<Player, Datapoint<?>> onChange, BiConsumer<Player, Datapoint<?>> onLoad) {
            this(key, type, defaultDatapoint, onChange, onLoad, null);
        }
        Data(String key, Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint,
             BiConsumer<Player, Datapoint<?>> onChange) {
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
