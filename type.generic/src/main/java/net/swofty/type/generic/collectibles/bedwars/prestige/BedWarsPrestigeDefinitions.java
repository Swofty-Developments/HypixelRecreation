package net.swofty.type.generic.collectibles.bedwars.prestige;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BedWarsPrestigeDefinitions {

    public static final String DEFAULT_SCHEME_ID = "prestige_scheme_diamond";
    public static final String DEFAULT_STAR_ID = "prestige_star_default";
    public static final String DEFAULT_BRACKET_ID = "prestige_bracket_none";

    public static final List<Scheme> SCHEMES = List.of(
            new Scheme("none", "None", Material.NAME_TAG, 0, style("§7", List.of("§7", "", ""), "§7", "§7")),
        new Scheme("iron", "Iron", Material.IRON_INGOT, 100, style("§f", List.of("§f", "§f", "§f"), "§f", "§f")),
        new Scheme("gold", "Gold", Material.GOLD_INGOT, 200, style("§6", List.of("§6", "§6", "§6"), "§6", "§6")),
        new Scheme("diamond", "Diamond", Material.DIAMOND, 300, style("§b", List.of("§b", "§b", "§b"), "§b", "§b")),
        new Scheme("emerald", "Emerald", Material.EMERALD, 400, style("§2", List.of("§2", "§2", "§2"), "§2", "§2")),
        new Scheme("sapphire", "Sapphire", Material.CYAN_DYE, 500, style("§3", List.of("§3", "§3", "§3"), "§3", "§3")),
        new Scheme("ruby", "Ruby", Material.RED_DYE, 600, style("§4", List.of("§4", "§4", "§4"), "§4", "§4")),
        new Scheme("crystal", "Crystal", Material.QUARTZ, 700, style("§d", List.of("§d", "§d", "§d"), "§d", "§d")),
        new Scheme("opal", "Opal", Material.LAPIS_LAZULI, 800, style("§9", List.of("§9", "§9", "§9"), "§9", "§9")),
        new Scheme("amethyst", "Amethyst", Material.PURPLE_DYE, 900, style("§5", List.of("§5", "§5", "§5"), "§5", "§5")),
        new Scheme("rainbow", "Rainbow", Material.NETHER_STAR, 1000, style("§c", List.of("§6", "§e", "§a"), "§b", "§d")),
        new Scheme("iron_prime", "Iron Prime", Material.IRON_BLOCK, 1100, style("", List.of("§f", "§f", "§f"), "§7", "§7")),
        new Scheme("gold_prime", "Gold Prime", Material.GOLD_BLOCK, 1200, style("", List.of("§e", "§e", "§e"), "§6", "§7")),
        new Scheme("diamond_prime", "Diamond Prime", Material.DIAMOND_BLOCK, 1300, style("", List.of("§b", "§b", "§b"), "§3", "§7")),
        new Scheme("emerald_prime", "Emerald Prime", Material.EMERALD_BLOCK, 1400, style("", List.of("§a", "§a", "§a"), "§2", "§7")),
        new Scheme("sapphire_prime", "Sapphire Prime", Material.CYAN_WOOL, 1500, style("", List.of("§3", "§3", "§3"), "§9", "§7")),
        new Scheme("ruby_prime", "Ruby Prime", Material.RED_WOOL, 1600, style("", List.of("§c", "§c", "§c"), "§4", "§7")),
        new Scheme("crystal_prime", "Crystal Prime", Material.QUARTZ_BLOCK, 1700, style("", List.of("§d", "§d", "§d"), "§5", "§7")),
        new Scheme("opal_prime", "Opal Prime", Material.LAPIS_BLOCK, 1800, style("", List.of("§9", "§9", "§9"), "§1", "§7")),
        new Scheme("amethyst_prime", "Amethyst Prime", Material.PURPLE_WOOL, 1900, style("", List.of("§5", "§5", "§5"), "§8", "§7")),
        new Scheme("dawn", "Dawn", Material.SUNFLOWER, 2200, style("§6", List.of("§6", "§f", "§f"), "§b", "§3")),
        new Scheme("dusk", "Dusk", Material.PEONY, 2300, style("§5", List.of("§5", "§d", "§d"), "§6", "§e")),
        new Scheme("air", "Air", Material.GLASS_BOTTLE, 2400, style("§b", List.of("§b", "§f", "§f"), "§7", "§7")),
        new Scheme("wind", "Wind", Material.SADDLE, 2500, style("§f", List.of("§f", "§a", "§a"), "§2", "§2")),
        new Scheme("nebula", "Nebula", Material.FERMENTED_SPIDER_EYE, 2600, style("§4", List.of("§4", "§c", "§c"), "§d", "§d")),
        new Scheme("thunder", "Thunder", Material.OAK_SAPLING, 2700, style("§e", List.of("§e", "§f", "§f"), "§8", "§8")),
        new Scheme("earth", "Earth", Material.GRASS_BLOCK, 2800, style("§a", List.of("§a", "§2", "§2"), "§6", "§6")),
        new Scheme("water", "Water", Material.WATER_BUCKET, 2900, style("§b", List.of("§b", "§3", "§3"), "§9", "§9")),
        new Scheme("fire", "Fire", Material.BLAZE_POWDER, 3000, style("§e", List.of("§e", "§6", "§6"), "§c", "§c")),
        new Scheme("sunrise", "Sunrise", Material.RED_BED, 3100, style("§9", List.of("§9", "§3", "§3"), "§6", "§6")),
        new Scheme("eclipse", "Eclipse", Material.ENDER_PEARL, 3200, style("§c", List.of("§4", "§7", "§7"), "§4", "§c")),
        new Scheme("gamma", "Gamma", Material.BREWING_STAND, 3300, style("§9", List.of("§9", "§9", "§d"), "§c", "§c")),
        new Scheme("majestic", "Majestic", Material.DIAMOND_HORSE_ARMOR, 3400, style("§2", List.of("§a", "§d", "§d"), "§5", "§5")),
        new Scheme("andesine", "Andesine", Material.BEEF, 3500, style("§c", List.of("§c", "§4", "§4"), "§2", "§a")),
        new Scheme("marine", "Marine", Material.COD, 3600, style("§a", List.of("§a", "§a", "§b"), "§9", "§9")),
        new Scheme("element", "Element", Material.ENCHANTING_TABLE, 3700, style("§4", List.of("§4", "§c", "§c"), "§b", "§3")),
        new Scheme("galaxy", "Galaxy", Material.END_STONE, 3800, style("§1", List.of("§1", "§9", "§5"), "§5", "§d")),
        new Scheme("atomic", "Atomic", Material.EGG, 3900, style("§c", List.of("§c", "§a", "§a"), "§3", "§9")),
        new Scheme("sunset", "Sunset", Material.DAYLIGHT_DETECTOR, 4000, style("§5", List.of("§5", "§c", "§c"), "§6", "§6")),
        new Scheme("obsidian", "Obsidian", Material.OBSIDIAN, 4300, style("§0", List.of("§5", "§8", "§8"), "§5", "§5")),
        new Scheme("spring", "Spring", Material.LIME_TERRACOTTA, 4400, style("§2", List.of("§2", "§a", "§e"), "§6", "§5")),
        new Scheme("ice", "Ice", Material.ICE, 4500, style("§f", List.of("§f", "§b", "§b"), "§3", "§3")),
        new Scheme("summer", "Summer", Material.YELLOW_TERRACOTTA, 4600, style("§3", List.of("§b", "§e", "§6"), "§6", "§d")),
        new Scheme("spinel", "Spinel", Material.FLOWER_POT, 4700, style("§f", List.of("§4", "§c", "§c"), "§9", "§1")),
        new Scheme("autumn", "Autumn", Material.ORANGE_TERRACOTTA, 4800, style("§5", List.of("§5", "§c", "§6"), "§6", "§b")),
        new Scheme("mystic", "Mystic", Material.BIRCH_SAPLING, 4900, style("§2", List.of("§a", "§f", "§f"), "§f", "§a")),
        new Scheme("eternal", "Eternal", Material.INK_SAC, 5000, style("§4", List.of("§4", "§5", "§9"), "§9", "§1")),
        new Scheme("burnout", "Burnout", Material.FLINT_AND_STEEL, 5100, style("§4", List.of("§c", "§c", "§6"), "§e", "§f")),
        new Scheme("cooldown", "Cooldown", Material.PACKED_ICE, 5200, style("§1", List.of("§9", "§3", "§b"), "§f", "§e")),
        new Scheme("obliteration", "Obliteration", Material.TNT_MINECART, 5300, style("§5", List.of("§d", "§e", "§f"), "§e", "§d")),
        new Scheme("ender", "Ender", Material.ENDER_EYE, 5400, style("§3", List.of("§a", "§2", "§8"), "§2", "§a")),
        new Scheme("brust", "Brust", Material.MAGMA_CREAM, 5500, style("§2", List.of("§a", "§e", "§f"), "§b", "§d")),
        new Scheme("comical", "Comical", Material.TNT, 5600, style("§4", List.of("§c", "§e", "§f"), "§e", "§c")),
        new Scheme("lusterlost", "Lusterlost", Material.GRAY_DYE, 5700, style("§4", List.of("§6", "§2", "§3"), "§9", "§5")),
        new Scheme("maelstrom", "Maelstrom", Material.MUSIC_DISC_MELLOHI, 5800, style("§5", List.of("§c", "§6", "§f"), "§b", "§3")),
        new Scheme("time_undone", "Time Undone", Material.SKELETON_SKULL, 5900, style("", List.of("§0", "§8", "§7"), "§f", "§f")),
        new Scheme("umbrella", "Umbrella", Material.RED_MUSHROOM, 6000, style("§c", List.of("§f", "§f", "§f"), "§c", "§f")),
        new Scheme("luminous", "Luminous", Material.GLOWSTONE, 6100, style("§6", List.of("§e", "§f", "§f"), "§f", "§b")),
        new Scheme("bittersweet", "Bittersweet", Material.MELON_SLICE, 6400, style("§b", List.of("§b", "§c", "§c"), "§c", "§a")),
        new Scheme("sweetsour", "Sweetsour", Material.GLISTERING_MELON_SLICE, 6500, style("§3", List.of("§3", "§a", "§a"), "§f", "§a")),
        new Scheme("pop", "Pop", Material.PEONY, 6600, style("§9", List.of("§d", "§d", "§d"), "§b", "§9")),
        new Scheme("bubblegum", "Bubblegum", Material.PINK_DYE, 6700, style("§5", List.of("§d", "§d", "§d"), "§f", "§5")),
        new Scheme("contrast", "Contrast", Material.PUFFERFISH, 6800, style("§0", List.of("§6", "§6", "§e"), "§e", "§f")),
        new Scheme("blended", "Blended", Material.SUGAR_CANE, 6900, style("§a", List.of("§a", "§a", "§a"), "§2", "§2")),
        new Scheme("allay", "Allay", Material.NOTE_BLOCK, 7000, style("§3", List.of("§b", "§b", "§b"), "§f", "§3")),
        new Scheme("blaze", "Blaze", Material.BLAZE_ROD, 7100, style("§4", List.of("§c", "§6", "§e"), "§c", "§6")),
        new Scheme("creeper", "Creeper", Material.GUNPOWDER, 7200, style("§2", List.of("§a", "§f", "§2"), "§a", "§f")),
        new Scheme("drowned", "Drowned", Material.SAND, 7300, style("§2", List.of("§3", "§3", "§b"), "§b", "§a")),
        new Scheme("enderman", "Enderman", Material.END_STONE, 7400, style("§8", List.of("§8", "§8", "§8"), "§d", "§8")),
        new Scheme("frog", "Frog", Material.LILY_PAD, 7500, style("§6", List.of("§6", "§2", "§2"), "§f", "§f")),
        new Scheme("ghast", "Ghast", Material.GHAST_TEAR, 7600, style("§f", List.of("§f", "§f", "§7"), "§7", "§c")),
        new Scheme("hoglin", "Hoglin", Material.PORKCHOP, 7700, style("§d", List.of("§c", "§c", "§c"), "§6", "§d")),
        new Scheme("iron_golem", "Iron Golem", Material.POPPY, 7800, style("§8", List.of("§7", "§f", "§f"), "§f", "§e")),
        new Scheme("jerry", "Jerry", Material.VILLAGER_SPAWN_EGG, 7900, style("§6", List.of("§f", "§2", "§6"), "§2", "§f")),
        new Scheme("kringle", "Kringle", Material.COOKIE, 8000, style("§2", List.of("§a", "§a", "§a"), "§c", "§4")),
        new Scheme("liquid", "Liquid", Material.MILK_BUCKET, 8100, style("§8", List.of("§7", "§f", "§b"), "§3", "§9")),
        new Scheme("mint", "Mint", Material.LARGE_FERN, 8200, style("§f", List.of("§f", "§f", "§f"), "§a", "§f")),
        new Scheme("poser", "Poser", Material.SLIME_BALL, 8500, style("§3", List.of("§6", "§6", "§6"), "§e", "§3")),
        new Scheme("quartz", "Quartz", Material.QUARTZ, 8600, style("§d", List.of("§f", "§f", "§f"), "§e", "§d")),
        new Scheme("rich", "Rich", Material.GOLD_NUGGET, 8700, style("§8", List.of("§6", "§6", "§6"), "§6", "§8")),
        new Scheme("sanguine", "Sanguine", Material.FLOWER_POT, 8800, style("§4", List.of("§4", "§4", "§c"), "§c", "§f")),
        new Scheme("titanic", "Titanic", Material.OAK_BOAT, 8900, style("§9", List.of("§b", "§b", "§b"), "§3", "§3")),
        new Scheme("unorthodox", "Unorthodox", Material.ALLIUM, 9000, style("§d", List.of("§d", "§d", "§d"), "§5", "§8")),
        new Scheme("volcanic", "Volcanic", Material.NETHERRACK, 9100, style("§0", List.of("§c", "§6", "§6"), "§c", "§c")),
        new Scheme("weeping_cherry", "Weeping Cherry", Material.LILAC, 9200, style("§2", List.of("§d", "§d", "§d"), "§a", "§2")),
        new Scheme("x_ray", "X-Ray", Material.SKELETON_SPAWN_EGG, 9300, style("§f", List.of("§8", "§8", "§8"), "§f", "§f")),
        new Scheme("yearn", "Yearn", Material.FIRE_CHARGE, 9400, style("§e", List.of("§6", "§4", "§8"), "§8", "§8")),
        new Scheme("zebra", "Zebra", Material.GHAST_SPAWN_EGG, 9500, style("§0", List.of("§0", "§8", "§8"), "§7", "§7")),
        new Scheme("caution", "Caution", Material.YELLOW_DYE, 9600, style("§e", List.of("§e", "§e", "§0"), "§0", "§e")),
        new Scheme("indescribable", "Indescribable", Material.END_PORTAL_FRAME, 9700, style("§d", List.of("§d", "§d", "§e"), "§e", "§b")),
        new Scheme("forgotten", "Forgotten", Material.GRAY_STAINED_GLASS_PANE, 9800, style("§0", List.of("§8", "§8", "§8"), "§8", "§0")),
        new Scheme("fuse", "Fuse", Material.REPEATER, 9900, style("§8", List.of("§7", "§f", "§f"), "§f", "§e")),
        new Scheme("prestigious", "Prestigious", Material.FIREWORK_ROCKET, 10000, style("§9", List.of("§b", "§f", "§f"), "§f", "§c"))
    );

    public static final List<Star> STARS = List.of(
        new Star("default", "Default", Material.GREEN_DYE, 0, "✫"),
        new Star("1000", "1000+", Material.PINK_DYE, 1000, "✪"),
        new Star("2000", "2000+", Material.PURPLE_DYE, 2000, "⚝"),
        new Star("3000", "3000+", Material.RED_DYE, 3000, "✥"),
        new Star("4000", "4000+", Material.LAPIS_LAZULI, 4000, "✭"),
        new Star("four_pointed", "Four-Pointed", Material.PRISMARINE_SHARD, 0, "✦"),
        new Star("pinwheel", "Pinwheel", Material.NETHER_QUARTZ_ORE, 0, "✵"),
        new Star("hollow", "Hollow", Material.HOPPER, 0, "✰"),
        new Star("nautical", "Nautical", Material.OAK_BOAT, 0, "✯")
    );

    public static final List<Bracket> BRACKETS = List.of(
        new Bracket("none", "None", Material.NAME_TAG, 0, "[", "]"),
        new Bracket("curly_brace", "Curly Brace", Material.OAK_FENCE, 0, "{", "}"),
        new Bracket("parenthesis", "Parenthesis", Material.SPRUCE_FENCE, 0, "(", ")"),
        new Bracket("angled", "Angled", Material.BIRCH_FENCE, 0, "<", ">"),
        new Bracket("double_angle_quotation_mark", "Double Angle Quotation Mark", Material.ACACIA_FENCE, 0, "«", "»")
    );

    public static Scheme scheme(String id) {
        return SCHEMES.stream().filter(s -> s.id().equals(normalizeScheme(id))).findFirst().orElse(SCHEMES.stream().filter(s -> s.id().equals("diamond")).findFirst().orElse(SCHEMES.getFirst()));
    }

    public static Star star(String id) {
        return STARS.stream().filter(s -> s.id().equals(normalizeStar(id))).findFirst().orElse(STARS.getFirst());
    }

    public static Bracket bracket(String id) {
        return BRACKETS.stream().filter(b -> b.id().equals(normalizeBracket(id))).findFirst().orElse(BRACKETS.getFirst());
    }

    public static String schemeCollectibleId(String id) {
        return "prestige_scheme_" + normalizeScheme(id);
    }

    public static String starCollectibleId(String id) {
        return "prestige_star_" + normalizeStar(id);
    }

    public static String bracketCollectibleId(String id) {
        return "prestige_bracket_" + normalizeBracket(id);
    }

    private static String normalizeScheme(String id) {
        return stripPrefix(normalize(id), "prestige_scheme_");
    }

    private static String normalizeStar(String id) {
        return stripPrefix(normalize(id), "prestige_star_");
    }

    private static String normalizeBracket(String id) {
        return stripPrefix(normalize(id), "prestige_bracket_");
    }

    private static String stripPrefix(String id, String prefix) {
        return id.startsWith(prefix) ? id.substring(prefix.length()) : id;
    }

    private static String normalize(String id) {
        return id == null || id.isBlank() ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private static BedWarsPrestigeStyle style(String openColor, List<String> digitColors, String starColor, String closeColor) {
        return BedWarsPrestigeStyle.colors(openColor, digitColors, starColor, closeColor);
    }

    public record Scheme(String id, String name, Material material, int requiredLevel, BedWarsPrestigeStyle style) {
    }

    public record Star(String id, String name, Material material, int requiredLevel, String symbol) {
    }

    public record Bracket(String id, String name, Material material, int requiredLevel, String open, String close) {
    }
}
