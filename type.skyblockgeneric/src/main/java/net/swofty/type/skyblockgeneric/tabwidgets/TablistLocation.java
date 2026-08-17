package net.swofty.type.skyblockgeneric.tabwidgets;

import net.swofty.type.generic.HypixelConst;

public enum TablistLocation {
    PRIVATE_ISLAND("Private Islands"), HUB("the Hub"), DUNGEON_HUB("the Dungeon Hub"),
    FARMING_ISLANDS("The Farming Islands"), GARDEN("Garden"), THE_PARK("The Park"),
    GOLD_MINE("the Gold Mine"), DEEP_CAVERNS("Deep Caverns"), DWARVEN_MINES("Dwarven Mines"),
    CRYSTAL_HOLLOWS("Crystal Hollows"), MINESHAFT("the Mineshaft"), SPIDERS_DEN("Spider's Den"),
    THE_END("The End"), CRIMSON_ISLE("Crimson Isle"), KUUDRA("Kuudra"), THE_RIFT("The Rift"),
    JERRYS_WORKSHOP("Jerry's Workshop"), GALATEA("Galatea"), BACKWATER_BAYOU("Backwater Bayou"),
    LOTUS_ATOLL("Lotus Atoll");

    private final String display;

    TablistLocation(String display) {
        this.display = display;
    }

    public String display() {
        return display;
    }

    public String texture() {
        return switch (this) {
            case PRIVATE_ISLAND -> "35f4b40cef9e017cd4112d26b62557f8c1d5b189da2e99534222bc8cec7d9196";
            case HUB -> "d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8";
            case DUNGEON_HUB -> "9b56895b9659896ad647f58599238af532d46db9c1b0389b8bbeb70999dab33d";
            case FARMING_ISLANDS -> "4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b";
            case GARDEN -> "8a4ff17e84583b4a62005a3e81fbc22f6aee594efe5294b06f3eeffcdf3af282";
            case THE_PARK -> "a221f813dacee0fef8c59f76894dbb26415478d9ddfc44c2e708a6d3b7549b";
            case GOLD_MINE -> "d8573ed917444316b0b28dd9927fd68e56f6625fcfa73ad80b8770d5139891b1";
            case DEEP_CAVERNS -> "74213dc6dc4b1641defd333f4a4732cc714dd677718fa10f140a6939c12aa32b";
            case DWARVEN_MINES -> "6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5";
            case CRYSTAL_HOLLOWS -> "21dbe30b027acbceb612563bd877cd7ebb719ea6ed1399027dcee58bb9049d4a";
            case SPIDERS_DEN -> "2ff708485e8630e6a54da44c995461619d378dfcf5cf714bc22a069a386a3df2";
            case THE_END -> "1cab25554cc4fb74518073f0a96c59905c014e5cf638420e46f3ec47f1cfae68";
            case CRIMSON_ISLE -> "64457b2d7acf522d1912f0d6c6139440ba9d333a6bf61a2e9440e7fb3788939e";
            case THE_RIFT -> "f26192609d6c46ade73e807fc40dbc3a1a1afbb456ae165785b0fe834dd1cb57";
            case JERRYS_WORKSHOP -> "6dd663136cafa11806fdbca6b596afd85166b4ec02142c8d5ac8941d89ab7";
            case GALATEA -> "a211ac81698c229d8ef2fae89f62a6a961b30d8b82b97161863090e90bff02a5";
            case BACKWATER_BAYOU -> "1c0cd33590f64d346d98cdd01606938742e715dda6737353306a44f81c8ba426";
            default -> "f769bbb9fb2316808131e6c2d02ce14e3aab674deb4b558bb265c3141d199b08";
        };
    }

    public static TablistLocation current() {
        String name = HypixelConst.getTypeLoader().getType().name();
        if (name.equals("SKYBLOCK_ISLAND")) return PRIVATE_ISLAND;
        for (TablistLocation value : values()) {
            if (name.contains(value.name())) return value;
        }
        return HUB;
    }
}
