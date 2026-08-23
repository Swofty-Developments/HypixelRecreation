package net.swofty.type.skyblockgeneric.text;

import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextBody;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link TextBody} pre-shaped into the canonical SkyBlock item lore layout and bound to the
 * {@link SkyBlockItem} it describes plus the optional {@link SkyBlockPlayer} looking at it.
 *
 * Every section named in {@link Sections} is declared up front in Hypixel's rendering order, so the code that
 * fills a section never has to know which of its neighbours exist. Sections that Hypixel separates from the
 * block above them with a blank line are declared {@link TextBody.Section#separated() separated}, which means
 * the blank appears only when both that section and something before it actually render.
 *
 * Sections that Hypixel renders flush against each other are declared as one parent holding children:
 * {@link Sections#GEMSTONES} hangs off {@link Sections#STATS}, and the whole footer run of
 * {@link Sections#REFORGEABLE}, {@link Sections#SOULBOUND}, {@link Sections#STATS_WHEN_SHOT},
 * {@link Sections#UNFINISHED} and {@link Sections#RARITY_FOOTER} hangs off a single separated group, so the
 * group takes one leading blank between it and the body while its members stay glued together.
 * {@link Sections#ABSOLUTE} sits ahead of the whole layout for items that replace the document wholesale.
 *
 * {@link #section(String)} resolves those nestings, so callers address every section by its flat id.
 */
public final class LoreText {

    public static final class Sections {
        public static final String ABSOLUTE = "absolute";
        public static final String UNDER_NAME = "under_name";
        public static final String BREAKING_POWER = "breaking_power";
        public static final String STATS = "stats";
        public static final String GEMSTONES = "gemstones";
        public static final String POTION = "potion";
        public static final String ENCHANTS = "enchants";
        public static final String RUNE = "rune";
        public static final String CUSTOM_BEFORE_ABILITY = "custom_before_ability";
        public static final String CONFIG_LORE = "config_lore";
        public static final String ABILITIES = "abilities";
        public static final String CUSTOM_AFTER_ABILITY = "custom_after_ability";
        public static final String FULL_SET_BONUS = "full_set_bonus";
        public static final String RECIPES = "recipes";
        public static final String REFORGEABLE = "reforgeable";
        public static final String SOULBOUND = "soulbound";
        public static final String STATS_WHEN_SHOT = "stats_when_shot";
        public static final String UNFINISHED = "unfinished";
        public static final String RARITY_FOOTER = "rarity_footer";

        private Sections() {
        }
    }

    private static final String FOOTER_GROUP = "footer";

    private static final int ABSOLUTE_ORDER = 0;
    private static final int UNDER_NAME_ORDER = 10;
    private static final int BREAKING_POWER_ORDER = 20;
    private static final int STATS_ORDER = 30;
    private static final int POTION_ORDER = 40;
    private static final int ENCHANTS_ORDER = 50;
    private static final int RUNE_ORDER = 60;
    private static final int CUSTOM_BEFORE_ABILITY_ORDER = 70;
    private static final int CONFIG_LORE_ORDER = 80;
    private static final int ABILITIES_ORDER = 90;
    private static final int CUSTOM_AFTER_ABILITY_ORDER = 100;
    private static final int FULL_SET_BONUS_ORDER = 110;
    private static final int RECIPES_ORDER = 120;
    private static final int FOOTER_ORDER = 130;

    private static final Map<String, String> NESTED = Map.of(
            Sections.GEMSTONES, Sections.STATS,
            Sections.REFORGEABLE, FOOTER_GROUP,
            Sections.SOULBOUND, FOOTER_GROUP,
            Sections.STATS_WHEN_SHOT, FOOTER_GROUP,
            Sections.UNFINISHED, FOOTER_GROUP,
            Sections.RARITY_FOOTER, FOOTER_GROUP);

    private final TextBody body = new TextBody();
    private final SkyBlockItem item;
    private final SkyBlockPlayer viewer;

    public LoreText(SkyBlockItem item) {
        this(item, null);
    }

    public LoreText(SkyBlockItem item, @Nullable SkyBlockPlayer viewer) {
        this.item = Objects.requireNonNull(item, "item");
        this.viewer = viewer;
        declare();
    }

    public static Text gray(String markup) {
        return Text.of("<7>{}", Text.parseLenient(markup));
    }

    public static Text darkGray(String markup) {
        return Text.of("<8>{}", Text.parseLenient(markup));
    }

    public SkyBlockItem item() {
        return item;
    }

    public @Nullable SkyBlockPlayer viewer() {
        return viewer;
    }

    public TextBody.Section section(String id) {
        String parent = NESTED.get(id);
        return parent == null ? body.section(id) : body.section(parent).child(id);
    }

    public TextBody.Section section(String id, int order) {
        String parent = NESTED.get(id);
        if (parent != null) {
            throw new IllegalArgumentException("Section " + id + " renders inside " + parent + " and cannot be reordered");
        }
        return body.section(id, order);
    }

    public List<Text> render() {
        return body.render();
    }

    private void declare() {
        body.section(Sections.ABSOLUTE, ABSOLUTE_ORDER);
        body.section(Sections.UNDER_NAME, UNDER_NAME_ORDER).separated();
        body.section(Sections.BREAKING_POWER, BREAKING_POWER_ORDER).separated();
        body.section(Sections.STATS, STATS_ORDER).separated().child(Sections.GEMSTONES);
        body.section(Sections.POTION, POTION_ORDER).separated();
        body.section(Sections.ENCHANTS, ENCHANTS_ORDER).separated();
        body.section(Sections.RUNE, RUNE_ORDER).separated();
        body.section(Sections.CUSTOM_BEFORE_ABILITY, CUSTOM_BEFORE_ABILITY_ORDER).separated();
        body.section(Sections.CONFIG_LORE, CONFIG_LORE_ORDER).separated();
        body.section(Sections.ABILITIES, ABILITIES_ORDER);
        body.section(Sections.CUSTOM_AFTER_ABILITY, CUSTOM_AFTER_ABILITY_ORDER).separated();
        body.section(Sections.FULL_SET_BONUS, FULL_SET_BONUS_ORDER).separated();
        body.section(Sections.RECIPES, RECIPES_ORDER).separated();

        TextBody.Section footer = body.section(FOOTER_GROUP, FOOTER_ORDER).separated();
        footer.child(Sections.REFORGEABLE);
        footer.child(Sections.SOULBOUND);
        footer.child(Sections.STATS_WHEN_SHOT);
        footer.child(Sections.UNFINISHED);
        footer.child(Sections.RARITY_FOOTER);
    }
}
