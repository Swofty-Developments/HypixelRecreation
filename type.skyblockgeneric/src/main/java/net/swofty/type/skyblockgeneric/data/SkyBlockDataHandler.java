package net.swofty.type.skyblockgeneric.data;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.PlayerShopData;
import net.swofty.commons.skyblock.SkyBlockPlayerProfiles;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.data.Datapoint;
import net.swofty.type.generic.data.datapoints.*;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.datapoints.*;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockInventory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import tools.jackson.core.JacksonException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SkyBlockDataHandler extends DataHandler {

    // SkyBlock specific cache
    public static final Map<UUID, SkyBlockDataHandler> skyBlockCache = new ConcurrentHashMap<>();

    @Getter
    private UUID currentProfileId;

    protected SkyBlockDataHandler() {
        super();
    }

    protected SkyBlockDataHandler(UUID uuid, UUID profileId) {
        super(uuid);
        this.currentProfileId = profileId;
    }

    public static SkyBlockDataHandler getUser(UUID uuid) {
        if (!skyBlockCache.containsKey(uuid)) throw new RuntimeException("User " + uuid + " does not exist!");
        return skyBlockCache.get(uuid);
    }

    public static @Nullable SkyBlockDataHandler getUser(HypixelPlayer player) {
        try {
            return getUser(player.getUuid());
        } catch (Exception e) {
            return null;
        }
    }

    public static SkyBlockDataHandler createFromProfileOnly(Document profileDoc) {
        if (profileDoc == null) {
            throw new IllegalArgumentException("Cannot create SkyBlock data from a missing profile document");
        }
        UUID owner = UUID.fromString(profileDoc.getString("_owner"));
        UUID profileId = UUID.fromString(profileDoc.getString("_id"));
        SkyBlockDataHandler sb = new SkyBlockDataHandler(owner, profileId);
        sb.loadSkyBlock(profileDoc);
        return sb;
    }

    public static SkyBlockDataHandler createFromProfile(UUID playerUuid, UUID profileId, Document profileDoc) {
        SkyBlockDataHandler sb = new SkyBlockDataHandler(playerUuid, profileId);
        sb.loadSkyBlock(profileDoc);
        return sb;
    }

    private void loadSkyBlock(Document document) {
        if (document == null) {
            initSkyBlockDefaults();
            return;
        }
        this.uuid = UUID.fromString(document.getString("_owner"));
        this.currentProfileId = UUID.fromString(document.getString("_id"));

        for (Data data : Data.values()) {
            String key = data.getKey();
            if (!document.containsKey(key)) {
                datapoints.put(key, data.getDefaultDatapoint().setUser(this).setData(data));
                continue;
            }
            String jsonValue = document.getString(key);
            try {
                Datapoint<?> dp = data.getDefaultDatapoint().getClass()
                        .getDeclaredConstructor(String.class).newInstance(key);
                dp.deserializeValue(jsonValue);
                datapoints.put(key, dp.setUser(this).setData(data));
            } catch (Exception e) {
                datapoints.put(key, data.getDefaultDatapoint().setUser(this).setData(data));
                Logger.error(e, "Issue with SkyBlock datapoint {} for user {} - defaulting to default value", key, this.uuid);
            }
        }
    }

    private void initSkyBlockDefaults() {
        for (Data data : Data.values()) {
            try {
                datapoints.put(
                        data.getKey(),
                        data.getDefaultDatapoint().deepClone().setUser(this).setData(data)
                );
            } catch (Exception e) {
                Logger.error(e, "Issue with SkyBlock datapoint {} for user {} - requires fixing", data.getKey(), uuid);
            }
        }
    }

    public Document toProfileDocument() {
        Document document = new Document();
        document.put("_owner", this.uuid.toString());
        document.put("_id", currentProfileId.toString());

        for (Data data : Data.values()) {
            try {
                Datapoint<?> datapoint = getDatapoint(data.getKey());
                if (datapoint == null) {
                    datapoint = data.getDefaultDatapoint().deepClone().setUser(this).setData(data);
                    datapoints.put(data.getKey(), datapoint);
                }
                document.put(data.getKey(), datapoint.getSerializedValue());
            } catch (JacksonException e) {
                Logger.error(e, "Failed to serialize SkyBlock datapoint {} for user {}", data.getKey(), this.uuid);
            }
        }
        return document;
    }

    @Override
    public SkyBlockDataHandler fromDocument(Document document) {
        throw new UnsupportedOperationException("Use SkyBlockDataHandler.createFromProfile(...) instead");
    }

    @Override
    public Document toDocument() {
        return toProfileDocument();
    }

    public SkyBlockDatapoint<?> getSkyBlockDatapoint(String key) {
        return (SkyBlockDatapoint<?>) getDatapoint(key);
    }

    public static SkyBlockDataHandler getProfileOfOfflinePlayer(UUID uuid, UUID profileUUID) throws RuntimeException {
        if (skyBlockCache.containsKey(uuid))
            return skyBlockCache.get(uuid);

        if (profileUUID == null)
            throw new RuntimeException("No profile selected for user " + uuid.toString());

        Document profile = ProfilesDatabase.fetchDocument(profileUUID);
        return createFromProfileOnly(profile);
    }

    /**
     * SB datapoint by enum (no generic param).
     */
    public Datapoint<?> get(Data datapoint) {
        Datapoint<?> dp = datapoints.get(datapoint.key);
        return dp != null ? dp : datapoint.defaultDatapoint;
    }

    /**
     * Optionally typed getter (casts to the class you pass).
     */
    public <R extends Datapoint<?>> R get(Data datapoint, Class<R> type) {
        Datapoint<?> dp = datapoints.get(datapoint.key);
        return (R) (dp != null ? type.cast(dp) : type.cast(datapoint.defaultDatapoint));
    }

    @Override
    public void runOnLoad(HypixelPlayer player) {
        for (Data data : Data.values()) {
            if (data.onLoad != null) data.onLoad.accept((SkyBlockPlayer) player, get(data));
        }
    }

    @SneakyThrows
    @Override
    public void runOnSave(HypixelPlayer player) {
        if (player instanceof SkyBlockPlayer sbp) {
            for (Data data : Data.values()) {
                if (data.onQuit != null) {
                    Datapoint<?> target = get(data);
                    Datapoint<?> produced = data.onQuit.apply(sbp);
                    target.setFrom(produced); // no onChange during save
                }
            }
        }
    }

    public static SkyBlockDataHandler initUserWithDefaultData(UUID uuid, UUID profileId) {
        SkyBlockDataHandler h = new SkyBlockDataHandler(uuid, profileId);
        h.initSkyBlockDefaults();
        return h;
    }

    public Map<String, Object> getCoopValues() {
        Map<String, Object> values = new HashMap<>();
        Arrays.stream(Data.values()).forEach(data -> {
            if (data.isCoopPersistent) {
                values.put(data.getKey(), getDatapoint(data.getKey()).getValue());
            }
        });
        return values;
    }

    public static @Nullable UUID getPotentialUUIDFromName(String name) throws RuntimeException {
        Document doc = UserDatabase.collection.find(new Document("ignLowercase", name.toLowerCase())).first();
        if (doc == null) return null;
        String id = doc.getString("_id");
        return id != null ? UUID.fromString(id) : null;
    }

    // Same Data enum as before - unchanged
    public enum Data {
        EXPERIENCED_STATISTICS("experienced_statistics", false, false, false,
                DatapointStringList.class, new DatapointStringList("experienced_statistics")),
        PROFILE_NAME("profile_name", false, true, false,
                DatapointString.class, new DatapointString("profile_name", "null"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    if (Objects.equals(datapoint.getValue(), "null")) {
                        ((DatapointString) datapoint).setValue(SkyBlockPlayerProfiles.getRandomName());
                    }
                }),

        PROFILE_MODE("profile_mode", false, true, false,
                DatapointString.class, new DatapointString("profile_mode", "CLASSIC")),

        COINS("coins", false, false, false,
                DatapointDouble.class, new DatapointDouble("coins", 0.0)),

        INVENTORY("inventory", false, false, false,
                DatapointInventory.class, new DatapointInventory("inventory", new SkyBlockInventory()),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    SkyBlockInventory inv = (SkyBlockInventory) datapoint.getValue();

                    player.setHelmet(new SkyBlockItem(inv.getHelmet()).getItemStack());
                    player.setChestplate(new SkyBlockItem(inv.getChestplate()).getItemStack());
                    player.setLeggings(new SkyBlockItem(inv.getLeggings()).getItemStack());
                    player.setBoots(new SkyBlockItem(inv.getBoots()).getItemStack());

                    inv.getItems().forEach((slot, item) -> {
                        PlayerItemOrigin origin = PlayerItemOrigin.INVENTORY_SLOT;
                        origin.setData(slot);

                        ItemStack updated = PlayerItemUpdater.playerUpdate(
                                player,
                                new SkyBlockItem(item).getItemStack(),
                                true
                        ).build();

                        origin.setStack(player, updated);
                    });

                    player.getInventory().setItemStack(8,
                            new NonPlayerItemUpdater(new SkyBlockItem(ItemType.SKYBLOCK_MENU).getItemStack())
                                    .getUpdatedItem().build());
                },
                (player) -> {
                    SkyBlockInventory inv = new SkyBlockInventory();
                    ItemStack helmet = player.getHelmet();
                    if (SkyBlockItem.isSkyBlockItem(helmet)) inv.setHelmet(new SkyBlockItem(helmet).toUnderstandable());
                    ItemStack chestplate = player.getChestplate();
                    if (SkyBlockItem.isSkyBlockItem(chestplate))
                        inv.setChestplate(new SkyBlockItem(chestplate).toUnderstandable());
                    ItemStack leggings = player.getLeggings();
                    if (SkyBlockItem.isSkyBlockItem(leggings))
                        inv.setLeggings(new SkyBlockItem(leggings).toUnderstandable());
                    ItemStack boots = player.getBoots();
                    if (SkyBlockItem.isSkyBlockItem(boots)) inv.setBoots(new SkyBlockItem(boots).toUnderstandable());

                    for (int i = 0; i < 36; i++) {
                        ItemStack stack = player.getInventory().getItemStack(i);
                        if (SkyBlockItem.isSkyBlockItem(stack)) {
                            inv.getItems().put(i, new SkyBlockItem(stack).toUnderstandable());
                        }
                    }
                    return new DatapointInventory("inventory", inv);
                }),

        SKILLS("skills", false, false, false,
                DatapointSkills.class, new DatapointSkills("skills")),

        EXPERIENCE("experience", false, false, false,
                DatapointLong.class, new DatapointLong("experience", 0L),
                (player, datapoint) -> {
                },
                (player, datapoint) -> player.setExperience((Long) datapoint.getValue()),
                (player) -> new DatapointLong("experience", player.getExperience())),

        COMPLETED_BAZAAR_TRANSACTIONS("completed_bazaar_transactions", false, false, false,
                DatapointCompletedBazaarTransactions.class, new DatapointCompletedBazaarTransactions("completed_bazaar_transactions")),

        MISSION_DATA("mission_data", false, false, false,
                DatapointMissionData.class, new DatapointMissionData("mission_data", new MissionData()),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    MissionData data = (MissionData) datapoint.getValue();
                    data.setSkyBlockPlayer(player);
                    ((DatapointMissionData) datapoint).setValue(data);
                }),

        SHOPPING_DATA("shopping_data", false, false, true,
                DatapointShopData.class, new DatapointShopData("shopping_data", new PlayerShopData()),
                (player, datapoint) -> {
                }),

        FAIRY_SOULS("player_fairy_souls", false, false, false,
                DatapointFairySouls.class, new DatapointFairySouls("player_fairy_souls")),

        CREATED("created", false, true, false,
                DatapointLong.class, new DatapointLong("created", 0L),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    if (Objects.equals(datapoint.getValue(), 0L)) {
                        ((DatapointLong) datapoint).setValue(System.currentTimeMillis());
                    }
                }),

        LAST_EDITED_SKILL("last_edited_skill", false, false, false,
                DatapointSkillCategory.class, new DatapointSkillCategory("last_edited_skill", SkillCategories.FORAGING)),

        ISLAND_UUID("island_uuid", false, true, false,
                DatapointUUID.class, new DatapointUUID("island_uuid", null),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    if (player.getSkyBlockIsland() != null) {
                        ((DatapointUUID) datapoint).setValue(player.getSkyBlockIsland().getIslandID());
                    }
                }),

        IS_COOP("is_coop", false, true, false,
                DatapointBoolean.class, new DatapointBoolean("is_coop", false)),

        COLLECTION("collection", false, true, false,
                DatapointCollection.class, new DatapointCollection("collection")),

        HUNTING("hunting", false, false, false,
                DatapointHunting.class, new DatapointHunting("hunting")),

        FOREST_ESSENCE("forest_essence", false, false, false,
                DatapointLong.class, new DatapointLong("forest_essence", 0L)),

        FOREST_WHISPERS("forest_whispers", false, false, false,
                DatapointLong.class, new DatapointLong("forest_whispers", 0L)),

        HOTF_EXPERIENCE("hotf_experience", false, false, false,
                DatapointLong.class, new DatapointLong("hotf_experience", 0L)),

        MINION_DATA("minions", false, true, false,
                DatapointMinionData.class, new DatapointMinionData("minions")),

        STORAGE("storage", false, false, false,
                DatapointStorage.class, new DatapointStorage("storage")),

        WARDROBE("wardrobe", false, false, false,
                DatapointWardrobe.class, new DatapointWardrobe("wardrobe")),

        LOADOUTS("loadouts", false, false, false,
                DatapointLoadouts.class, new DatapointLoadouts("loadouts")),

        BACKPACKS("backpacks", false, false, false,
                DatapointBackpacks.class, new DatapointBackpacks("backpacks")),

        VISITED_REGIONS("visited_regions", false, false, false,
                DatapointStringList.class, new DatapointStringList("visited_regions")),

        AUCTION_STATISTICS("auction_statistics", false, false, false,
                DatapointAuctionStatistics.class, new DatapointAuctionStatistics("auction_statistics")),

        AUCTION_ACTIVE_BIDS("auction_bids", false, false, false,
                DatapointUUIDList.class, new DatapointUUIDList("auction_active_bids")),

        AUCTION_INACTIVE_BIDS("auction_inactive_bids", false, false, false,
                DatapointUUIDList.class, new DatapointUUIDList("auction_inactive_bids")),

        AUCTION_ACTIVE_OWNED("auction_owned", false, false, false,
                DatapointUUIDList.class, new DatapointUUIDList("auction_active_owned")),

        AUCTION_INACTIVE_OWNED("auction_inactive_owned", false, false, false,
                DatapointUUIDList.class, new DatapointUUIDList("auction_inactive_owned")),

        AUCTION_ESCROW("auction_escrow", false, false, false,
                DatapointAuctionEscrow.class, new DatapointAuctionEscrow("auction_escrow")),

        BANK_DATA("bank_data", false, true, false,
                DatapointBankData.class, new DatapointBankData("bank_data"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> Thread.startVirtualThread(() ->
                        ((DatapointBankData) datapoint).setValue(((DatapointBankData) datapoint).getValue())
                )),

        PET_DATA("pet_data", false, false, false,
                DatapointPetData.class, new DatapointPetData("pet_data")),

        QUIVER("quiver", false, false, false,
                DatapointQuiver.class, new DatapointQuiver("quiver")),

        SHIP_STATE("ship_state", false, false, false,
                DatapointShipState.class, new DatapointShipState("ship_state")),

        TROPHY_FISH("trophy_fish", false, false, false,
                DatapointTrophyFish.class, new DatapointTrophyFish("trophy_fish")),

        SLAYER("slayer", false, false, false,
                DatapointSlayer.class, new DatapointSlayer("slayer")),

        RACE_BEST_TIME("race_best_time", false, false, false, DatapointMapStringLong.class,
                new DatapointMapStringLong("race_best_time")),

        ACCESSORY_BAG("accessory_bag", false, false, false,
                DatapointAccessoryBag.class, new DatapointAccessoryBag("accessory_bag")),

        SACK_OF_SACKS("sack_of_sacks", false, false, false,
                DatapointSackOfSacks.class, new DatapointSackOfSacks("sack_of_sacks")),

        ITEMS_IN_SACKS("items_in_sacks", false, false, false,
                DatapointItemsInSacks.class, new DatapointItemsInSacks("items_in_sacks")),

        BESTIARY("bestiary", false, false, false,
                DatapointBestiary.class, new DatapointBestiary("bestiary")),

        DEATHS("deaths", false, false, false,
                DatapointDeaths.class, new DatapointDeaths("deaths")),

        ARCHERY_PRACTICE("archery_practice", false, false, false,
                DatapointArcheryPractice.class, new DatapointArcheryPractice("archery_practice")),

        SKYBLOCK_EXPERIENCE("skyblock_experience", false, false, false,
                DatapointSkyBlockExperience.class, new DatapointSkyBlockExperience("skyblock_experience")),

        BITS("bits", false, false, false,
                DatapointInteger.class, new DatapointInteger("bits", 0)),

        GEMS("gems", false, false, false,
                DatapointInteger.class, new DatapointInteger("gems", 0)),

        MUSEUM_DATA("museum_data", false, false, false,
                DatapointMuseum.class, new DatapointMuseum("museum_data"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    DatapointMuseum.MuseumData data = (DatapointMuseum.MuseumData) datapoint.getValue();
                    data.setCurrentlyViewing(new DatapointMuseum.ViewingInfo(
                            player.getUuid(),
                            player.getProfiles().getCurrentlySelected()
                    ));
                }),

        BUILD_MODE("build_mode", true, false, false, DatapointBoolean.class,
                new DatapointBoolean("build_mode", false),
                (player, datapoint) -> ((SkyBlockPlayer) player).setBypassBuild((Boolean) datapoint.getValue()),
                (player, datapoint) -> player.setBypassBuild((Boolean) datapoint.getValue()),
                (player) -> new DatapointBoolean("build_mode", player.isBypassBuild())),

        SKIN_SIGNATURE("skin_signature", false, false, false,
                DatapointString.class, new DatapointString("skin_signature", "null"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> ((DatapointString) datapoint).setValue(player.getSkin().signature())),

        SKIN_TEXTURE("skin_texture", false, false, false,
                DatapointString.class, new DatapointString("skin_texture", "null"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> ((DatapointString) datapoint).setValue(player.getSkin().textures())),

        VISITED_ISLANDS("visited_islands", false, false, false,
                DatapointStringList.class, new DatapointStringList("visited_islands")),

        USED_SCROLLS("used_scrolls", false, false, false,
                DatapointStringList.class, new DatapointStringList("used_scrolls")),

        BOOSTER_COOKIE_EXPIRATION_DATE("booster_cookie_expiration_date", false, false, false,
                DatapointLong.class, new DatapointLong("booster_cookie_expiration_date", 1L)),

        TRIAL_OF_FIRE_LEVEL("trial_of_fire_level", false, false, false,
                DatapointInteger.class, new DatapointInteger("trial_of_fire_level", 0)),

        LATEST_NEW_YEAR_CAKE_YEAR("latest_new_year_cake_year", false, false, false,
                DatapointInteger.class, new DatapointInteger("latest_new_year_cake_year", 0)),

        LATEST_YEAR_PRESENT_PICKUP("latest_year_pickup_present", false, false, false,
                DatapointPresentYear.class, new DatapointPresentYear("latest_year_pickup_present")),

        CHOCOLATE_FACTORY("chocolate_factory", false, false, false,
                DatapointChocolateFactory.class, new DatapointChocolateFactory("chocolate_factory")),

        SOULFLOW("soulflow", false, false, false,
                DatapointInteger.class, new DatapointInteger("soulflow", 0)),

        COMMISSIONS_COMPLETED("commissions_completed", false, false, false,
                DatapointInteger.class, new DatapointInteger("commissions_completed", 0)),

        COMMISSIONS("commissions", false, false, false,
                DatapointCommissions.class, new DatapointCommissions("commissions")),

        HOTM("hotm", false, false, false,
                DatapointHOTM.class, new DatapointHOTM("hotm")),

        KAT("kat", false, false, false,
                DatapointKat.class, new DatapointKat("kat")),

        GARDEN_CORE("garden_core", false, true, false,
                DatapointGardenCore.class, new DatapointGardenCore("garden_core")),

        GARDEN_VISITORS("garden_visitors", false, true, false,
                DatapointGardenVisitors.class, new DatapointGardenVisitors("garden_visitors")),

        GARDEN_PESTS("garden_pests", false, true, false,
                DatapointGardenPests.class, new DatapointGardenPests("garden_pests")),

        GARDEN_COMPOSTER("garden_composter", false, true, false,
                DatapointGardenComposter.class, new DatapointGardenComposter("garden_composter")),

        GARDEN_GREENHOUSE("garden_greenhouse", false, true, false,
                DatapointGardenGreenhouse.class, new DatapointGardenGreenhouse("garden_greenhouse")),

        GARDEN_PERSONAL("garden_personal", false, false, false,
                DatapointGardenPersonal.class, new DatapointGardenPersonal("garden_personal")),

        STASH("stash", false, false, false,
                DatapointStash.class, new DatapointStash("stash")),

        COLLECTED_MOB_TYPE_REWARDS("collected_mob_type_rewards", false, false, false,
                DatapointCollectedMobTypeRewards.class, new DatapointCollectedMobTypeRewards("collected_mob_type_rewards")),
        ;

        @Getter
        private final String key;
        @Getter
        private final Boolean isProfilePersistent;
        @Getter
        private final Boolean isCoopPersistent;
        @Getter
        private final Boolean repeatSetValue;
        @Getter
        private final Class<? extends Datapoint<?>> type;
        @Getter
        private final Datapoint<?> defaultDatapoint;
        public final BiConsumer<Player, Datapoint<?>> onChange;
        public final BiConsumer<SkyBlockPlayer, Datapoint<?>> onLoad;
        public final Function<SkyBlockPlayer, Datapoint<?>> onQuit;

        Data(String key, Boolean isProfilePersistent, Boolean isCoopPersistent, Boolean repeatSetValue,
             Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint,
             BiConsumer<Player, Datapoint<?>> onChange, BiConsumer<SkyBlockPlayer, Datapoint<?>> onLoad, Function<SkyBlockPlayer, Datapoint<?>> onQuit) {
            this.key = key;
            this.isProfilePersistent = isProfilePersistent;
            this.isCoopPersistent = isCoopPersistent;
            this.repeatSetValue = repeatSetValue;
            this.type = type;
            this.defaultDatapoint = defaultDatapoint;
            this.onChange = onChange;
            this.onLoad = onLoad;
            this.onQuit = onQuit;
        }

        Data(String key, Boolean isProfilePersistent, Boolean isCoopPersistent, Boolean repeatSetValue,
             Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint,
             BiConsumer<Player, Datapoint<?>> onChange, BiConsumer<SkyBlockPlayer, Datapoint<?>> onLoad) {
            this(key, isProfilePersistent, isCoopPersistent, repeatSetValue, type, defaultDatapoint, onChange, onLoad, null);
        }

        Data(String key, Boolean isProfilePersistent, Boolean isCoopPersistent, Boolean repeatSetValue,
             Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint, BiConsumer<Player, Datapoint<?>> onChange) {
            this(key, isProfilePersistent, isCoopPersistent, repeatSetValue, type, defaultDatapoint, onChange, null, null);
        }

        Data(String key, Boolean isProfilePersistent, Boolean isCoopPersistent, Boolean repeatSetValue,
             Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint) {
            this(key, isProfilePersistent, isCoopPersistent, repeatSetValue, type, defaultDatapoint, null, null, null);
        }

        public static Data fromKey(String key) {
            for (Data data : values()) if (data.getKey().equals(key)) return data;
            return null;
        }
    }

    public static void startRepeatSetValueLoop() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                SkyBlockDataHandler h = player.getSkyblockDataHandler();
                for (Data data : Data.values()) {
                    if (data.repeatSetValue) {
                        Datapoint<?> dp = h.get(data);
                        @SuppressWarnings("unchecked")
                        Datapoint<Object> any = (Datapoint<Object>) dp;
                        any.setValue(any.getValue());
                    }
                }
            });
            return TaskSchedule.nextTick();
        });
    }
}
