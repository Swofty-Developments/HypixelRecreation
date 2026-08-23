package net.swofty.type.skyblockgeneric.region;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.Songs;
import net.swofty.type.skyblockgeneric.region.mining.configurations.*;
import net.swofty.type.skyblockgeneric.region.mining.configurations.deepmines.*;
import net.swofty.type.skyblockgeneric.region.mining.configurations.thepark.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Getter
public enum RegionType {
    PRIVATE_ISLAND("Your Island", NamedTextColor.GREEN),

    THE_GARDEN("The Garden", NamedTextColor.GREEN),

    // Hub
    VILLAGE("Village", WheatAndFlowersConfiguration.class),
    BANK("Bank", NamedTextColor.GOLD),
    LIBRARY("Library", NamedTextColor.DARK_GREEN),
    AUCTION_HOUSE("Auction House", NamedTextColor.GOLD),
    SHENS_AUCTION("Shen's Auction", NamedTextColor.GOLD),
    FLOWER_HOUSE("Flower House"),
    BAZAAR_ALLEY("Bazaar Alley", NamedTextColor.YELLOW),
    COMMUNITY_CENTER("Community Center"),
    WIZARD_TOWER("Wizard Tower", NamedTextColor.LIGHT_PURPLE),
    BUILDERS_HOUSE("Builder's House"),
    THAUMATURGIST("Thaumaturgist", NamedTextColor.GOLD),
    TRADE_CENTER("Trade Center"),
    ELECTION_ROOM("Election Room"),
    MOUNTAIN("Mountain"),
    WILDERNESS("Wilderness", NamedTextColor.DARK_GREEN, Songs.ABSTRACT_RINGING),
    PLAYER_MUSEUM("%s's Museum", NamedTextColor.DARK_AQUA),
    RUINS("Ruins", NamedTextColor.AQUA),
    RABBIT_HOUSE("Rabbit House", NamedTextColor.YELLOW),
    HEXATORUM("Hexatorum", NamedTextColor.LIGHT_PURPLE),
    UNINCORPORATED("Unincorporated", NamedTextColor.RED),
    COLOSSEUM("Colosseum"),
    CANVAS_ROOM("Canvas Room", NamedTextColor.AQUA),
    CARNIVAL("Carnival", NamedTextColor.YELLOW),
    COMBAT_SETTLEMENT("Combat Settlement", NamedTextColor.RED),
    MINING_DISTRICT("Mining District", NamedTextColor.GOLD),
    GRAVEYARD("Graveyard", NamedTextColor.RED),
    CRYPTS("Crypts", NamedTextColor.RED),
    TAVERN("Tavern", NamedTextColor.AQUA),
    COAL_MINE("Coal Mine", NamedTextColor.DARK_GRAY, MineCoalConfiguration.class),
    ARCHERY_RANGE("Archery Range", NamedTextColor.DARK_GREEN),
    BLACKSMITH("Blacksmith"),
    FARM("Farm", NamedTextColor.YELLOW, MineWheatConfiguration.class),
    FARMHOUSE("Farmhouse", NamedTextColor.AQUA),
    DARK_AUCTION("Dark Auction", NamedTextColor.DARK_PURPLE),
    FISHING_OUTPOST("Fishing Outpost"),
    FISHERMANS_HUT("Fisherman's Hut"),
    ARTISTS_ABODE("Artist's Abode", NamedTextColor.YELLOW),
    FOREST("Forest", NamedTextColor.DARK_GREEN, MineLogsConfiguration.class),
    FORAGING_CAMP("Foraging Camp", NamedTextColor.DARK_GREEN, MineLogsConfiguration.class), // TODO: you can't break everything here though
    REGALIA_ROOM("Regalia Room", NamedTextColor.GOLD),
    ABIPHONES_AND_CO("Abiphones & Co.", NamedTextColor.AQUA),
    FASHION_SHOP("Fashion Shop", NamedTextColor.LIGHT_PURPLE),
    PET_CARE("Pet Care", NamedTextColor.AQUA),
    SEWER("Sewer", NamedTextColor.RED),
    TAYLORS_SHOP("Taylor's Shop", NamedTextColor.AQUA),

    // The Park
    BIRCH_PARK("Birch Park", NamedTextColor.GREEN, BirchParkConfiguration.class),
    HOWLING_CAVE("Howling Cave"),
    SPRUCE_WOODS("Spruce Woods", NamedTextColor.GREEN, SpruceWoodsConfiguration.class),
    VIKING_LONGHOUSE("Viking Longhouse", NamedTextColor.AQUA, SpruceWoodsConfiguration.class),
    DARK_THICKET("Dark Thicket", NamedTextColor.GREEN, DarkOakConfiguration.class),
    TRIALS_OF_FIRE("Trials of Fire", NamedTextColor.RED),
    SAVANNA_WOODLAND("Savanna Woodland", NamedTextColor.GREEN, SavannaWoodlandConfiguration.class),
    SOUL_CAVE("Soul Cave", NamedTextColor.GREEN, SavannaWoodlandConfiguration.class),
    MELODY_PLATEAU("Melody's Plateau", NamedTextColor.LIGHT_PURPLE, SavannaWoodlandConfiguration.class),
    JUNGLE_ISLAND("Jungle Island", NamedTextColor.GREEN, JungleIslandConfiguration.class),
    SPIRIT_CAVE("Spirit Cave", NamedTextColor.AQUA, JungleIslandConfiguration.class),

    // Jerry's Workshop
    JERRYS_WORKSHOP("Jerry's Workshop", NamedTextColor.RED),
    JERRY_POND("Jerry Pond", NamedTextColor.AQUA),
    SUNKEN_JERRY_POND("Sunken Jerry Pond", NamedTextColor.AQUA),
    TERRYS_SHACK("Terry's Shack", NamedTextColor.AQUA),
    MOUNT_JERRY("Mount Jerry", NamedTextColor.RED),
    HOT_SPRINGS("Hot Springs", NamedTextColor.DARK_RED),
    GLACIAL_CAVE("Glacial Cave", NamedTextColor.DARK_AQUA, GlacialCaveConfiguration.class),
    GARYS_SHACK("Gary's Shack", NamedTextColor.AQUA),
    SHERRYS_SHOWROOM("Sherry's Showroom", NamedTextColor.YELLOW),
    EINARYS_EMPORIUM("Einary's Emporium", NamedTextColor.GOLD),
    REFLECTIVE_POND("Reflective Pond"),

    // The Barn
    THE_BARN("The Barn", NamedTextColor.AQUA, BarnConfiguration.class),
    WINDMILL("Windmill", NamedTextColor.AQUA, BarnConfiguration.class),

    // Mushroom Desert
    MUSHROOM_DESERT("Mushroom Desert"),
    DESERT_MOUNTAIN("Desert Mountain"),
    DESERT_SETTLEMENT("Desert Settlement", NamedTextColor.YELLOW),
    OASIS("Oasis"),
    SHEPHERD_KEEP("Shepherd's Keep"),
    TRAPPERS_DEN("Trapper's Den"),
    JAKE_HOUSE("Jake's House"),
    MUSHROOM_GORGE("Mushroom Gorge"),
    OVERGROWN_MUSHROOM_CAVE("Overgrown Mushroom Cave", NamedTextColor.DARK_GREEN),
    GLOWING_MUSHROOM_CAVE("Glowing Mushroom Cave", NamedTextColor.DARK_AQUA),
    ARCHAEOLOGICAL_SITE("Archaeological Site", NamedTextColor.GREEN),

    // Spider's Den
    SPIDERS_DEN("Spider's Den", NamedTextColor.DARK_RED),
    ARACHNES_BURROW("Arachne's Burrow", NamedTextColor.DARK_RED),
    ARACHNES_SANCTUARY("Arachne's Sanctuary", NamedTextColor.DARK_RED),
    ARCHAEOLOGISTS_CAMP("Archaeologist's Camp", NamedTextColor.AQUA),
    GRANDMAS_HOUSE("Grandma's House", NamedTextColor.RED),
    GRAVEL_MINES("Gravel Mines", NamedTextColor.DARK_GRAY),
    SPIDER_MOUND("Spider Mound", NamedTextColor.RED),

    // The End
    THE_END("The End", NamedTextColor.LIGHT_PURPLE),
    THE_END_NEST("The End", NamedTextColor.LIGHT_PURPLE),
    VOID_SEPULTURE("Void Sepulture", NamedTextColor.LIGHT_PURPLE),
    DRAGONS_NEST("Dragon's Nest", NamedTextColor.DARK_PURPLE),
    VOID_SLATE("Void Slate"),
    ZEALOT_BRUISER_HIDEOUT("Zealot Bruiser Hideout"),

    // Gold Mine
    GOLD_MINE("Gold Mine", NamedTextColor.GOLD, GoldMineConfiguration.class),

    // Deep Caverns
    DEEP_CAVERNS("Deep Caverns", NamedTextColor.AQUA, GunpowderMinesConfiguration.class, null, Songs.AMBIENT_CAVES),
    GUNPOWDER_MINES("Gunpowder Mines", NamedTextColor.AQUA, GunpowderMinesConfiguration.class, null, Songs.AMBIENT_CAVES),
    LAPIS_QUARRY("Lapis Quarry", LapisQuarryConfiguration.class),
    PIGMENS_DEN("Pigmen's Den", PigmensDenConfiguration.class),
    SLIMEHILL("Slimehill", SlimehillConfiguration.class),
    DIAMOND_RESERVE("Diamond Reserve", DiamondReserveConfiguration.class),
    OBSIDIAN_SANCTUARY("Obsidian Sanctuary", ObsidianSanctuaryConfiguration.class),

    // Dwarven Mines
    DWARVEN_MINES("Dwarven Mines", NamedTextColor.DARK_GREEN, DwarvenMinesConfiguration.class),
    ABANDONED_QUARRY("Abandoned Quarry", DwarvenMinesConfiguration.class),
    CLIFFSIDE_VEINS("Cliffside Veins", DwarvenMinesConfiguration.class),
    DIVANS_GATEWAY("Divan's Gateway", DwarvenMinesConfiguration.class),
    DWARVEN_BASE_CAMP("Dwarven Base Camp", DwarvenMinesConfiguration.class),
    DWARVEN_VILLAGE("Dwarven Village", DwarvenMinesConfiguration.class),
    DWARVEN_TAVERN("Dwarven Tavern", DwarvenMinesConfiguration.class),
    FAR_RESERVE("Far Reserve", DwarvenMinesConfiguration.class),
    FOSSIL_RESEARCH_CENTER("Fossil Research Center", DwarvenMinesConfiguration.class),
    GATES_TO_THE_MINES("Gates to the Mines", DwarvenMinesConfiguration.class),
    GOBLIN_BURROWS("Goblin Burrows", DwarvenMinesConfiguration.class),
    GLACITE_TUNNELS("Glacite Tunnels", DwarvenMinesConfiguration.class),
    GRANDPA_WOLFS_CAVE("Grandpa Wolf's Cave", DwarvenMinesConfiguration.class),
    GREAT_GLACITE_LAKE("Great Glacite Lake", DwarvenMinesConfiguration.class),
    GREAT_ICE_WALL("Great Ice Wall", DwarvenMinesConfiguration.class),
    RAMPARTS_QUARRY("Rampart's Quarry", DwarvenMinesConfiguration.class),
    IRONMANS_GUILD("Ironman's Guild", DwarvenMinesConfiguration.class),
    ROYAL_MINES("Royal Mines", DwarvenMinesConfiguration.class),
    ROYAL_PALACE("Royal Palace", DwarvenMinesConfiguration.class),
    ARISTOCRAT_PASSAGE("Aristocrat's Passage", DwarvenMinesConfiguration.class),
    BARRACKS_OF_HEROES("Barracks of Heroes", DwarvenMinesConfiguration.class),
    GRAND_LIBRARY("Grand Library", DwarvenMinesConfiguration.class),
    HANGING_COURT("Hanging Court", DwarvenMinesConfiguration.class),
    PALACE_BRIDGE("Palace Bridge", DwarvenMinesConfiguration.class),
    ROYAL_QUARTERS("Royal Quarters", DwarvenMinesConfiguration.class),
    THE_FORGE("The Forge", DwarvenMinesConfiguration.class),
    FORGE_BASIN("Forge Basin", DwarvenMinesConfiguration.class),
    THE_LIFT("The Lift", DwarvenMinesConfiguration.class),
    THE_MIST("The Mist", NamedTextColor.DARK_GRAY, DwarvenMinesConfiguration.class),
    UPPER_MINES("Upper Mines", DwarvenMinesConfiguration.class),
    LAVA_SPRINGS("Lava Springs", DwarvenMinesConfiguration.class),
    FAR_REACH("Far Reserve", DwarvenMinesConfiguration.class),

    // Crystal Hollows
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    CRYSTAL_NUCLEUS("Crystal Nucleus"),
    DRAGONS_LAIR("Dragon's Lair"),
    FAIRY_GROTTO("Fairy Grotto"),
    GOBLIN_HOLDOUT("Goblin Holdout"),
    GOBLIN_QUEENS_DEN("Goblin Queen's Den"),
    JUNGLE("Jungle"),
    JUNGLE_TEMPLE("Jungle Temple"),
    MAGMA_FIELDS("Magma Fields"),
    KHAZAD_DUM("Khazad-dûm"),
    MITHRIL_DEPOSITS("Mithril Deposits"),
    MINES_OF_DIVAN("Mines of Divan"),
    PRECURSOR_REMNANTS("Precursor Remnants"),
    LOST_PRECURSOR_CITY("Lost Precursor City"),

    // Crimson Isle
    CRIMSON_ISLE("Crimson Isle", NamedTextColor.RED),
    AURAS_LAB("Aura's Lab"),
    BARBARIAN_OUTPOST("Barbarian Outpost"),
    BELLY_OF_THE_BEAST("Belly of the Beast"),
    BLAZING_VOLCANO("Blazing Volcano", NamedTextColor.DARK_RED),
    BURNING_DESERT("Burning Desert", NamedTextColor.GOLD),
    COURTYARD("Courtyard"),
    CRIMSON_FIELDS("Crimson Fields"),
    DOJO("Dojo", NamedTextColor.GOLD),
    DRAGONTAIL("Dragontail", NamedTextColor.DARK_RED),
    DRAGONTAIL_AUCTION_HOUSE("Dragontail Auction House"),
    DRAGONTAIL_BANK("Dragontail Bank"),
    DRAGONTAIL_BAZAAR("Dragontail Bazaar"),
    DRAGONTAIL_BLACKSMITH("Dragontail Blacksmith"),
    DRAGONTAIL_TOWNSQUARE("Dragontail Townsquare"),
    CHIEFS_HUT("Chief's Hut"),
    MINION_SHOP("Minion Shop"),
    FORGOTTEN_SKULL("Forgotten Skull"),
    MAGE_OUTPOST("Mage Outpost"),
    MAGMA_CHAMBER("Magma Chamber"),
    MATRIARCHS_LAIR("Matriarch's Lair"),
    MYSTIC_MARSH("Mystic Marsh", NamedTextColor.DARK_GREEN),
    ODGERS_HUT("Odger's Hut"),
    PLHLEGBLAST_POOL("Plhlegblast Pool"),
    RUINS_OF_ASHFANG("Ruins of Ashfang"),
    SCARLETON("Scarleton", NamedTextColor.RED),
    SCARLETON_AUCTION_HOUSE("Scarleton Auction House"),
    SCARLETON_BANK("Scarleton Bank"),
    SCARLETON_BAZAAR("Scarleton Bazaar"),
    SCARLETON_BLACKSMITH("Scarleton Blacksmith"),
    SCARLETON_MINION_SHOP("Scarleton Minion Shop"),
    SCARLETON_PLAZA("Scarleton Plaza"),
    CATHEDRAL("Cathedral"),
    SCARLETON_COMMUNITY_CENTER("Community Center"),
    THRONE_ROOM("Throne Room"),
    MAGE_COUNCIL("Mage Council"),
    IGRUPANS_HOUSE("Igrupan's House"),
    IGRUPANS_CHICKEN_COOP("Igrupan's Chicken Coop"),
    SMOLDERING_TOMB("Smoldering Tomb"),
    STRONGHOLD("Stronghold", NamedTextColor.DARK_RED),
    THE_BASTION("The Bastion"),
    THE_DUKEDOM("The Dukedom"),
    THE_WASTELAND("The Wasteland"),

    // Galatea
    ANCIENT_RUINS("Ancient Ruins"),
    BUBBLEBOOST_COLUMN("Bubbleboost Column"),
    DIVE_EMBER_PASS("Dive-Ember Pass"),
    DRIPTOAD_DELVE("Driptoad Delve"),
    DRIPTOAD_PASS("Driptoad Pass"),
    DROWNED_RELIQUARY("Drowned Reliquary"),
    FOREST_TEMPLE("Forest Temple"),
    FUSION_HOUSE("Fusion House"),
    KELPWOVEN_TUNNELS("Kelpwoven Tunnels"),
    MURKWATER_DEPTHS("Murkwater Depths"),
    MURKWATER_OUTPOST("Murkwater Outpost"),
    NORTH_REACHES("North Reaches"),
    RED_HOUSE("Red House"),
    REEFGUARD_PASS("Reefguard Pass"),
    SIDE_EMBER_WAY("Side-Ember Way"),
    SQUID_CAVE("Squid Cave"),
    STRIDE_EMBER_FISSURE("Stride-Ember Fissure"),
    SOUTH_WETLANDS("South Wetlands"),
    SWAMPCUT_INC("SwampCut Inc."),
    TANGLEBURG_LIBRARY("Tangleburg Library"),
    TOMB_FLOODWAY("Tomb Floodway"),
    TRANQUIL_PASS("Tranquil Pass"),
    TRANQUILITY_SANCTUM("Tranquility Sanctum"),
    VERDANT_SUMMIT("Verdant Summit"),
    WEST_REACHES("West Reaches"),
    WESTBOUND_WETLANDS("Westbound Wetlands"),
    WYRMGROVE_TOMB("Wyrmgrove Tomb"),
    TANGLEBURGS_PATH("Tangleburg's Path", GalateaForagingConfiguration.class),
    TANGLEBURG("Tangleburg", GalateaForagingConfiguration.class),
    TANGLEBURG_BANK("Tangleburg Bank", NamedTextColor.GOLD),
    EVERGREEN_PLATEAU("Evergreen Plateau", NamedTextColor.GREEN, GalateaForagingConfiguration.class),
    SOUTH_REACHES("South Reaches", NamedTextColor.GREEN, GalateaForagingConfiguration.class),
    MOONGLADES_EDGE("Moonglade's Edge", NamedTextColor.DARK_GREEN, GalateaForagingConfiguration.class),
    MOONGLADE_MARSH("Moonglade Marsh", NamedTextColor.DARK_GREEN, GalateaForagingConfiguration.class),
    MURKWATER_LOCH("Murkwater Loch", NamedTextColor.DARK_GREEN, GalateaForagingConfiguration.class),
    MURKWATER_SHALLOWS("Murkwater Shallows", NamedTextColor.DARK_AQUA, GalateaForagingConfiguration.class),
    NORTH_WETLANDS("North Wetlands", NamedTextColor.DARK_GREEN, GalateaForagingConfiguration.class),

    ANTS_CAVE("Ant's Cave"),
    DESERT_TEMPLE("Desert Temple"),
    HOTSPOT_HAVEN("Hotspot Haven"),
    MIRIAS_HUT("Miria's Hut"),
    CRITTER_SAFARI_ENTRANCE("Critter Safari Entrance"),
    CRITTER_SAFARI("Critter Safari"),
    SPRING_DEPTHS("Spring Depths"),
    SPRING_PATH("Spring Path"),
    SPRING_SHALLOWS("Spring Shallows"),
    TORRHUS_HEIGHTS("Torrhus Heights"),
    TORRHUS_SPRINGS("Torrhus Springs"),
    PANGOLIN_HIDEAWAY("Pangolin Hideaway"),

    // Ship Navigator
    BACKWATER_BAYOU("Backwater Bayou", NamedTextColor.DARK_GREEN),
    LOTUS_ATOLL("Lotus Atoll"),
    LOTUS_EATERS_CAVE("Lotus Eater's Cave"),
    LOTUS_HIGHLANDS("Lotus Highlands"),
    TEWTIL_TUNNEL("Tewtil Tunnel"),

    // Dungeons
    DUNGEON_HUB("Dungeon Hub"),
    CATACOMBS("Catacombs"),
    KUUDRA_HOLLOW("Kuudra's Hollow");

    private final String name;
    private final TextColor color;
    private final SkyBlockRegenConfiguration miningHandler;
    private final SkyBlockBiomeConfiguration biomeHandler;
    private final List<Songs> songs;

    RegionType(String name, TextColor color, Class<? extends SkyBlockRegenConfiguration> miningHandler, Class<? extends SkyBlockBiomeConfiguration> biomeHandler, Songs... songs) {
        this.name = name;
        this.color = color;

        if (miningHandler != null) {
            try {
                this.miningHandler = miningHandler.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else
            this.miningHandler = null;

        if (biomeHandler != null) {
            try {
                this.biomeHandler = biomeHandler.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else
            this.biomeHandler = null;

        this.songs = new ArrayList<>(List.of(songs));
    }

    RegionType(String name, TextColor color, Class<? extends SkyBlockRegenConfiguration> miningHandler) {
        this(name, color, miningHandler, null, new Songs[0]);
    }

    RegionType(String name, Class<? extends SkyBlockRegenConfiguration> miningHandler) {
        this(name, NamedTextColor.AQUA, miningHandler);
    }


    RegionType(String name, Class<? extends SkyBlockRegenConfiguration> miningHandler, Class<? extends SkyBlockBiomeConfiguration> biomeHandler) {
        this(name, NamedTextColor.AQUA, miningHandler, biomeHandler);
    }

    RegionType(String name, TextColor color) {
        this(name, color, new Songs[0]);
    }

    RegionType(String name, TextColor color, Songs... songs) {
        this.name = name;
        this.color = color;
        this.miningHandler = null;
        this.songs = new ArrayList<>(List.of(songs));
        this.biomeHandler = null;
    }

    RegionType(String name) {
        this(name, NamedTextColor.AQUA, new Songs[0]);
    }

    public static RegionType getByID(int id) {
        return RegionType.values()[id];
    }

    @SneakyThrows
    public SkyBlockRegenConfiguration getMiningHandler() {
        return miningHandler;
    }

    @SneakyThrows
    public SkyBlockBiomeConfiguration getBiomeHandler() {
        return biomeHandler;
    }

    @Override
    public String toString() {
        return name;
    }
}
