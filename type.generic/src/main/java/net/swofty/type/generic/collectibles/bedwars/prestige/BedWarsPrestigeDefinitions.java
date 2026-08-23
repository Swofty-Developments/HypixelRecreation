package net.swofty.type.generic.collectibles.bedwars.prestige;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Locale;

import static net.kyori.adventure.text.format.NamedTextColor.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BedWarsPrestigeDefinitions {

    public static final String DEFAULT_SCHEME_ID = "prestige_scheme_diamond";
    public static final String DEFAULT_STAR_ID = "prestige_star_default";
    public static final String DEFAULT_BRACKET_ID = "prestige_bracket_none";

    public static final List<Scheme> SCHEMES = List.of(
        new Scheme("none", "None", Material.NAME_TAG, 0, plain()),
        new Scheme("iron", "Iron", Material.IRON_INGOT, 100, style(WHITE, List.of(WHITE, WHITE, WHITE), WHITE, WHITE)),
        new Scheme("gold", "Gold", Material.GOLD_INGOT, 200, style(GOLD, List.of(GOLD, GOLD, GOLD), GOLD, GOLD)),
        new Scheme("diamond", "Diamond", Material.DIAMOND, 300, style(AQUA, List.of(AQUA, AQUA, AQUA), AQUA, AQUA)),
        new Scheme("emerald", "Emerald", Material.EMERALD, 400, style(DARK_GREEN, List.of(DARK_GREEN, DARK_GREEN, DARK_GREEN), DARK_GREEN, DARK_GREEN)),
        new Scheme("sapphire", "Sapphire", Material.CYAN_DYE, 500, style(DARK_AQUA, List.of(DARK_AQUA, DARK_AQUA, DARK_AQUA), DARK_AQUA, DARK_AQUA)),
        new Scheme("ruby", "Ruby", Material.RED_DYE, 600, style(DARK_RED, List.of(DARK_RED, DARK_RED, DARK_RED), DARK_RED, DARK_RED)),
        new Scheme("crystal", "Crystal", Material.QUARTZ, 700, style(LIGHT_PURPLE, List.of(LIGHT_PURPLE, LIGHT_PURPLE, LIGHT_PURPLE), LIGHT_PURPLE, LIGHT_PURPLE)),
        new Scheme("opal", "Opal", Material.LAPIS_LAZULI, 800, style(BLUE, List.of(BLUE, BLUE, BLUE), BLUE, BLUE)),
        new Scheme("amethyst", "Amethyst", Material.PURPLE_DYE, 900, style(DARK_PURPLE, List.of(DARK_PURPLE, DARK_PURPLE, DARK_PURPLE), DARK_PURPLE, DARK_PURPLE)),
        new Scheme("rainbow", "Rainbow", Material.NETHER_STAR, 1000, style(RED, List.of(GOLD, YELLOW, GREEN), AQUA, LIGHT_PURPLE)),
        new Scheme("iron_prime", "Iron Prime", Material.IRON_BLOCK, 1100, style(null, List.of(WHITE, WHITE, WHITE), GRAY, GRAY)),
        new Scheme("gold_prime", "Gold Prime", Material.GOLD_BLOCK, 1200, style(null, List.of(YELLOW, YELLOW, YELLOW), GOLD, GRAY)),
        new Scheme("diamond_prime", "Diamond Prime", Material.DIAMOND_BLOCK, 1300, style(null, List.of(AQUA, AQUA, AQUA), DARK_AQUA, GRAY)),
        new Scheme("emerald_prime", "Emerald Prime", Material.EMERALD_BLOCK, 1400, style(null, List.of(GREEN, GREEN, GREEN), DARK_GREEN, GRAY)),
        new Scheme("sapphire_prime", "Sapphire Prime", Material.CYAN_WOOL, 1500, style(null, List.of(DARK_AQUA, DARK_AQUA, DARK_AQUA), BLUE, GRAY)),
        new Scheme("ruby_prime", "Ruby Prime", Material.RED_WOOL, 1600, style(null, List.of(RED, RED, RED), DARK_RED, GRAY)),
        new Scheme("crystal_prime", "Crystal Prime", Material.QUARTZ_BLOCK, 1700, style(null, List.of(LIGHT_PURPLE, LIGHT_PURPLE, LIGHT_PURPLE), DARK_PURPLE, GRAY)),
        new Scheme("opal_prime", "Opal Prime", Material.LAPIS_BLOCK, 1800, style(null, List.of(BLUE, BLUE, BLUE), DARK_BLUE, GRAY)),
        new Scheme("amethyst_prime", "Amethyst Prime", Material.PURPLE_WOOL, 1900, style(null, List.of(DARK_PURPLE, DARK_PURPLE, DARK_PURPLE), DARK_GRAY, GRAY)),
        new Scheme("dawn", "Dawn", Material.SUNFLOWER, 2200, style(GOLD, List.of(GOLD, WHITE, WHITE), AQUA, DARK_AQUA)),
        new Scheme("dusk", "Dusk", Material.PEONY, 2300, style(DARK_PURPLE, List.of(DARK_PURPLE, LIGHT_PURPLE, LIGHT_PURPLE), GOLD, YELLOW)),
        new Scheme("air", "Air", Material.GLASS_BOTTLE, 2400, style(AQUA, List.of(AQUA, WHITE, WHITE), GRAY, GRAY)),
        new Scheme("wind", "Wind", Material.SADDLE, 2500, style(WHITE, List.of(WHITE, GREEN, GREEN), DARK_GREEN, DARK_GREEN)),
        new Scheme("nebula", "Nebula", Material.FERMENTED_SPIDER_EYE, 2600, style(DARK_RED, List.of(DARK_RED, RED, RED), LIGHT_PURPLE, LIGHT_PURPLE)),
        new Scheme("thunder", "Thunder", Material.OAK_SAPLING, 2700, style(YELLOW, List.of(YELLOW, WHITE, WHITE), DARK_GRAY, DARK_GRAY)),
        new Scheme("earth", "Earth", Material.GRASS_BLOCK, 2800, style(GREEN, List.of(GREEN, DARK_GREEN, DARK_GREEN), GOLD, GOLD)),
        new Scheme("water", "Water", Material.WATER_BUCKET, 2900, style(AQUA, List.of(AQUA, DARK_AQUA, DARK_AQUA), BLUE, BLUE)),
        new Scheme("fire", "Fire", Material.BLAZE_POWDER, 3000, style(YELLOW, List.of(YELLOW, GOLD, GOLD), RED, RED)),
        new Scheme("sunrise", "Sunrise", Material.RED_BED, 3100, style(BLUE, List.of(BLUE, DARK_AQUA, DARK_AQUA), GOLD, GOLD)),
        new Scheme("eclipse", "Eclipse", Material.ENDER_PEARL, 3200, style(RED, List.of(DARK_RED, GRAY, GRAY), DARK_RED, RED)),
        new Scheme("gamma", "Gamma", Material.BREWING_STAND, 3300, style(BLUE, List.of(BLUE, BLUE, LIGHT_PURPLE), RED, RED)),
        new Scheme("majestic", "Majestic", Material.DIAMOND_HORSE_ARMOR, 3400, style(DARK_GREEN, List.of(GREEN, LIGHT_PURPLE, LIGHT_PURPLE), DARK_PURPLE, DARK_PURPLE)),
        new Scheme("andesine", "Andesine", Material.BEEF, 3500, style(RED, List.of(RED, DARK_RED, DARK_RED), DARK_GREEN, GREEN)),
        new Scheme("marine", "Marine", Material.COD, 3600, style(GREEN, List.of(GREEN, GREEN, AQUA), BLUE, BLUE)),
        new Scheme("element", "Element", Material.ENCHANTING_TABLE, 3700, style(DARK_RED, List.of(DARK_RED, RED, RED), AQUA, DARK_AQUA)),
        new Scheme("galaxy", "Galaxy", Material.END_STONE, 3800, style(DARK_BLUE, List.of(DARK_BLUE, BLUE, DARK_PURPLE), DARK_PURPLE, LIGHT_PURPLE)),
        new Scheme("atomic", "Atomic", Material.EGG, 3900, style(RED, List.of(RED, GREEN, GREEN), DARK_AQUA, BLUE)),
        new Scheme("sunset", "Sunset", Material.DAYLIGHT_DETECTOR, 4000, style(DARK_PURPLE, List.of(DARK_PURPLE, RED, RED), GOLD, GOLD)),
        new Scheme("obsidian", "Obsidian", Material.OBSIDIAN, 4300, style(BLACK, List.of(DARK_PURPLE, DARK_GRAY, DARK_GRAY), DARK_PURPLE, DARK_PURPLE)),
        new Scheme("spring", "Spring", Material.LIME_TERRACOTTA, 4400, style(DARK_GREEN, List.of(DARK_GREEN, GREEN, YELLOW), GOLD, DARK_PURPLE)),
        new Scheme("ice", "Ice", Material.ICE, 4500, style(WHITE, List.of(WHITE, AQUA, AQUA), DARK_AQUA, DARK_AQUA)),
        new Scheme("summer", "Summer", Material.YELLOW_TERRACOTTA, 4600, style(DARK_AQUA, List.of(AQUA, YELLOW, GOLD), GOLD, LIGHT_PURPLE)),
        new Scheme("spinel", "Spinel", Material.FLOWER_POT, 4700, style(WHITE, List.of(DARK_RED, RED, RED), BLUE, DARK_BLUE)),
        new Scheme("autumn", "Autumn", Material.ORANGE_TERRACOTTA, 4800, style(DARK_PURPLE, List.of(DARK_PURPLE, RED, GOLD), GOLD, AQUA)),
        new Scheme("mystic", "Mystic", Material.BIRCH_SAPLING, 4900, style(DARK_GREEN, List.of(GREEN, WHITE, WHITE), WHITE, GREEN)),
        new Scheme("eternal", "Eternal", Material.INK_SAC, 5000, style(DARK_RED, List.of(DARK_RED, DARK_PURPLE, BLUE), BLUE, DARK_BLUE)),
        new Scheme("burnout", "Burnout", Material.FLINT_AND_STEEL, 5100, style(DARK_RED, List.of(RED, RED, GOLD), YELLOW, WHITE)),
        new Scheme("cooldown", "Cooldown", Material.PACKED_ICE, 5200, style(DARK_BLUE, List.of(BLUE, DARK_AQUA, AQUA), WHITE, YELLOW)),
        new Scheme("obliteration", "Obliteration", Material.TNT_MINECART, 5300, style(DARK_PURPLE, List.of(LIGHT_PURPLE, YELLOW, WHITE), YELLOW, LIGHT_PURPLE)),
        new Scheme("ender", "Ender", Material.ENDER_EYE, 5400, style(DARK_AQUA, List.of(GREEN, DARK_GREEN, DARK_GRAY), DARK_GREEN, GREEN)),
        new Scheme("brust", "Brust", Material.MAGMA_CREAM, 5500, style(DARK_GREEN, List.of(GREEN, YELLOW, WHITE), AQUA, LIGHT_PURPLE)),
        new Scheme("comical", "Comical", Material.TNT, 5600, style(DARK_RED, List.of(RED, YELLOW, WHITE), YELLOW, RED)),
        new Scheme("lusterlost", "Lusterlost", Material.GRAY_DYE, 5700, style(DARK_RED, List.of(GOLD, DARK_GREEN, DARK_AQUA), BLUE, DARK_PURPLE)),
        new Scheme("maelstrom", "Maelstrom", Material.MUSIC_DISC_MELLOHI, 5800, style(DARK_PURPLE, List.of(RED, GOLD, WHITE), AQUA, DARK_AQUA)),
        new Scheme("time_undone", "Time Undone", Material.SKELETON_SKULL, 5900, style(null, List.of(BLACK, DARK_GRAY, GRAY), WHITE, WHITE)),
        new Scheme("umbrella", "Umbrella", Material.RED_MUSHROOM, 6000, style(RED, List.of(WHITE, WHITE, WHITE), RED, WHITE)),
        new Scheme("luminous", "Luminous", Material.GLOWSTONE, 6100, style(GOLD, List.of(YELLOW, WHITE, WHITE), WHITE, AQUA)),
        new Scheme("bittersweet", "Bittersweet", Material.MELON_SLICE, 6400, style(AQUA, List.of(AQUA, RED, RED), RED, GREEN)),
        new Scheme("sweetsour", "Sweetsour", Material.GLISTERING_MELON_SLICE, 6500, style(DARK_AQUA, List.of(DARK_AQUA, GREEN, GREEN), WHITE, GREEN)),
        new Scheme("pop", "Pop", Material.PEONY, 6600, style(BLUE, List.of(LIGHT_PURPLE, LIGHT_PURPLE, LIGHT_PURPLE), AQUA, BLUE)),
        new Scheme("bubblegum", "Bubblegum", Material.PINK_DYE, 6700, style(DARK_PURPLE, List.of(LIGHT_PURPLE, LIGHT_PURPLE, LIGHT_PURPLE), WHITE, DARK_PURPLE)),
        new Scheme("contrast", "Contrast", Material.PUFFERFISH, 6800, style(BLACK, List.of(GOLD, GOLD, YELLOW), YELLOW, WHITE)),
        new Scheme("blended", "Blended", Material.SUGAR_CANE, 6900, style(GREEN, List.of(GREEN, GREEN, GREEN), DARK_GREEN, DARK_GREEN)),
        new Scheme("allay", "Allay", Material.NOTE_BLOCK, 7000, style(DARK_AQUA, List.of(AQUA, AQUA, AQUA), WHITE, DARK_AQUA)),
        new Scheme("blaze", "Blaze", Material.BLAZE_ROD, 7100, style(DARK_RED, List.of(RED, GOLD, YELLOW), RED, GOLD)),
        new Scheme("creeper", "Creeper", Material.GUNPOWDER, 7200, style(DARK_GREEN, List.of(GREEN, WHITE, DARK_GREEN), GREEN, WHITE)),
        new Scheme("drowned", "Drowned", Material.SAND, 7300, style(DARK_GREEN, List.of(DARK_AQUA, DARK_AQUA, AQUA), AQUA, GREEN)),
        new Scheme("enderman", "Enderman", Material.END_STONE, 7400, style(DARK_GRAY, List.of(DARK_GRAY, DARK_GRAY, DARK_GRAY), LIGHT_PURPLE, DARK_GRAY)),
        new Scheme("frog", "Frog", Material.LILY_PAD, 7500, style(GOLD, List.of(GOLD, DARK_GREEN, DARK_GREEN), WHITE, WHITE)),
        new Scheme("ghast", "Ghast", Material.GHAST_TEAR, 7600, style(WHITE, List.of(WHITE, WHITE, GRAY), GRAY, RED)),
        new Scheme("hoglin", "Hoglin", Material.PORKCHOP, 7700, style(LIGHT_PURPLE, List.of(RED, RED, RED), GOLD, LIGHT_PURPLE)),
        new Scheme("iron_golem", "Iron Golem", Material.POPPY, 7800, style(DARK_GRAY, List.of(GRAY, WHITE, WHITE), WHITE, YELLOW)),
        new Scheme("jerry", "Jerry", Material.VILLAGER_SPAWN_EGG, 7900, style(GOLD, List.of(WHITE, DARK_GREEN, GOLD), DARK_GREEN, WHITE)),
        new Scheme("kringle", "Kringle", Material.COOKIE, 8000, style(DARK_GREEN, List.of(GREEN, GREEN, GREEN), RED, DARK_RED)),
        new Scheme("liquid", "Liquid", Material.MILK_BUCKET, 8100, style(DARK_GRAY, List.of(GRAY, WHITE, AQUA), DARK_AQUA, BLUE)),
        new Scheme("mint", "Mint", Material.LARGE_FERN, 8200, style(WHITE, List.of(WHITE, WHITE, WHITE), GREEN, WHITE)),
        new Scheme("poser", "Poser", Material.SLIME_BALL, 8500, style(DARK_AQUA, List.of(GOLD, GOLD, GOLD), YELLOW, DARK_AQUA)),
        new Scheme("quartz", "Quartz", Material.QUARTZ, 8600, style(LIGHT_PURPLE, List.of(WHITE, WHITE, WHITE), YELLOW, LIGHT_PURPLE)),
        new Scheme("rich", "Rich", Material.GOLD_NUGGET, 8700, style(DARK_GRAY, List.of(GOLD, GOLD, GOLD), GOLD, DARK_GRAY)),
        new Scheme("sanguine", "Sanguine", Material.FLOWER_POT, 8800, style(DARK_RED, List.of(DARK_RED, DARK_RED, RED), RED, WHITE)),
        new Scheme("titanic", "Titanic", Material.OAK_BOAT, 8900, style(BLUE, List.of(AQUA, AQUA, AQUA), DARK_AQUA, DARK_AQUA)),
        new Scheme("unorthodox", "Unorthodox", Material.ALLIUM, 9000, style(LIGHT_PURPLE, List.of(LIGHT_PURPLE, LIGHT_PURPLE, LIGHT_PURPLE), DARK_PURPLE, DARK_GRAY)),
        new Scheme("volcanic", "Volcanic", Material.NETHERRACK, 9100, style(BLACK, List.of(RED, GOLD, GOLD), RED, RED)),
        new Scheme("weeping_cherry", "Weeping Cherry", Material.LILAC, 9200, style(DARK_GREEN, List.of(LIGHT_PURPLE, LIGHT_PURPLE, LIGHT_PURPLE), GREEN, DARK_GREEN)),
        new Scheme("x_ray", "X-Ray", Material.SKELETON_SPAWN_EGG, 9300, style(WHITE, List.of(DARK_GRAY, DARK_GRAY, DARK_GRAY), WHITE, WHITE)),
        new Scheme("yearn", "Yearn", Material.FIRE_CHARGE, 9400, style(YELLOW, List.of(GOLD, DARK_RED, DARK_GRAY), DARK_GRAY, DARK_GRAY)),
        new Scheme("zebra", "Zebra", Material.GHAST_SPAWN_EGG, 9500, style(BLACK, List.of(BLACK, DARK_GRAY, DARK_GRAY), GRAY, GRAY)),
        new Scheme("caution", "Caution", Material.YELLOW_DYE, 9600, style(YELLOW, List.of(YELLOW, YELLOW, BLACK), BLACK, YELLOW)),
        new Scheme("indescribable", "Indescribable", Material.END_PORTAL_FRAME, 9700, style(LIGHT_PURPLE, List.of(LIGHT_PURPLE, LIGHT_PURPLE, YELLOW), YELLOW, AQUA)),
        new Scheme("forgotten", "Forgotten", Material.GRAY_STAINED_GLASS_PANE, 9800, style(BLACK, List.of(DARK_GRAY, DARK_GRAY, DARK_GRAY), DARK_GRAY, BLACK)),
        new Scheme("fuse", "Fuse", Material.REPEATER, 9900, style(DARK_GRAY, List.of(GRAY, WHITE, WHITE), WHITE, YELLOW)),
        new Scheme("prestigious", "Prestigious", Material.FIREWORK_ROCKET, 10000, style(BLUE, List.of(AQUA, WHITE, WHITE), WHITE, RED))
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

    private static BedWarsPrestigeStyle plain() {
        return BedWarsPrestigeStyle.builder().build();
    }

    private static BedWarsPrestigeStyle style(TextColor openColor, List<TextColor> digitColors, TextColor starColor, TextColor closeColor) {
        return BedWarsPrestigeStyle.colors(openColor, digitColors, starColor, closeColor);
    }

    public record Scheme(String id, String name, Material material, int requiredLevel, BedWarsPrestigeStyle style) {
    }

    public record Star(String id, String name, Material material, int requiredLevel, String symbol) {
    }

    public record Bracket(String id, String name, Material material, int requiredLevel, String open, String close) {
    }
}
