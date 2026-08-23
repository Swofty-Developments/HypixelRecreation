package net.swofty.type.ravengardgeneric.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.TooltipDisplay;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.gui.RavengardFont;
import net.swofty.type.ravengardgeneric.data.monogdb.RavengardTrackedItemsDatabase;
import net.swofty.type.ravengardgeneric.item.attribute.attributes.ItemAttributeItemId;
import net.swofty.type.ravengardgeneric.item.attribute.attributes.ItemAttributeStatBoost;
import net.swofty.type.ravengardgeneric.item.attribute.attributes.ItemAttributeUniqueTrackedId;
import net.swofty.type.ravengardgeneric.item.components.PlaceholderSlotComponent;
import net.swofty.type.ravengardgeneric.item.components.StandardItemComponent;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the real stack for an item type, generating the captured tooltip: a rarity and type tag
 * line, the defense statistic behind its icon, and a tag line of every class that can use it.
 */
public final class RavengardItem {

    /**
     * Class tag order as the captured Old Boots tooltip lists them. Restricted items instead show
     * their tags in the order the item declares its classes, which is how the captured Weathered
     * Pants row reads.
     */
    private static final RavengardClass[] CLASS_TAG_ORDER = {
            RavengardClass.KNIGHT, RavengardClass.HUNTER,
            RavengardClass.ASSASSIN, RavengardClass.WARRIOR, RavengardClass.SORCERER};

    private static final Set<DataComponent<?>> HIDDEN = Set.of(
            DataComponents.PAINTING_VARIANT,
            DataComponents.FIREWORKS,
            DataComponents.ATTRIBUTE_MODIFIERS,
            DataComponents.ENCHANTMENTS,
            DataComponents.STORED_ENCHANTMENTS,
            DataComponents.TRIM,
            DataComponents.CHARGED_PROJECTILES,
            DataComponents.JUKEBOX_PLAYABLE,
            DataComponents.MAP_ID,
            DataComponents.UNBREAKABLE,
            DataComponents.WRITTEN_BOOK_CONTENT,
            DataComponents.BANNER_PATTERNS,
            DataComponents.POTION_CONTENTS,
            DataComponents.DYED_COLOR);

    private static final Pattern EFFECT_HIGHLIGHT = Pattern.compile(
            "(?<hp>[+-]?\\d+(?:\\.\\d+)? ?HP(?: over)?)|(?<seconds>\\d+ seconds?)");

    private static final Text BLANK = Text.empty();

    private RavengardItem() {
    }

    public static ItemStack of(String id) {
        return of(id, null);
    }

    public static ItemStack of(String id, RavengardPlayer owner) {
        RavengardItemType type = RavengardItemRegistry.get(id);
        return type == null ? ItemStack.AIR : of(type, owner);
    }

    /** A display copy for menus: identical tooltip, but no tracked uuid is minted. */
    public static ItemStack.Builder displayBuilder(RavengardItemType type) {
        return build(type, null, false, 1.0);
    }

    public static ItemStack of(RavengardItemType type, RavengardPlayer owner) {
        return of(type, owner, 1.0);
    }

    /**
     * A starred roll: every statistic is multiplied by the boost, the name gains the yellow star
     * and each boosted stat line shows the multiplier, exactly as the copied captures do.
     */
    public static ItemStack of(RavengardItemType type, RavengardPlayer owner, double boost) {
        return build(type, owner, true, boost).build();
    }

    public static List<Text> loreOf(RavengardItemType type) {
        return loreOf(type, false);
    }

    /** Shop menus include the crown value line; the item itself never carries it. */
    public static List<Text> loreOf(RavengardItemType type, boolean shopContext) {
        return type.component(PlaceholderSlotComponent.class) != null
                ? placeholderLore(type)
                : lore(type, shopContext, 1.0);
    }

    private static ItemStack.Builder build(RavengardItemType type, RavengardPlayer owner,
                                           boolean tracked, double boost) {
        ItemStack.Builder builder = ItemStack.builder(type.getMaterial());

        if (type.getItemModel() != null) {
            builder.set(DataComponents.ITEM_MODEL, type.getItemModel());
        }

        boolean placeholder = type.component(PlaceholderSlotComponent.class) != null;
        Component name = Component.text(displayName(type))
                .color(placeholder ? NamedTextColor.GRAY
                        : TextColor.color(type.getRarity().getNameColor()))
                .decoration(TextDecoration.ITALIC, false);
        if (boosted(boost)) {
            name = name.append(Text.of(" <#f0f05c>⭐</color>"));
        }
        builder.set(DataComponents.CUSTOM_NAME, name);

        if (!placeholder) {
            builder.set(DataComponents.UNBREAKABLE, net.minestom.server.utils.Unit.INSTANCE);
            builder.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, HIDDEN));
            builder.set(DataComponents.TOOLTIP_STYLE, type.getRarity().tooltipStyle());
            if (tracked) {
                applyTrackedAttributes(builder, type, owner, boost);
            }
        }

        for (RavengardItemComponent component : type.getComponents()) {
            builder = component.apply(builder, type);
        }

        List<Text> lore = placeholder ? placeholderLore(type) : lore(type, false, boost);
        if (!lore.isEmpty()) {
            ItemStacks.lines(builder, lore);
        }
        return builder;
    }

    private static boolean boosted(double boost) {
        return boost > 1.0001;
    }

    private static String boostText(double boost) {
        String text = String.valueOf(boost);
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }

    /**
     * Every non-placeholder item is unique: it carries the rarity-prefixed id plus a freshly
     * minted uuid, recorded to the tracker so the item has a history. The distinct uuid is also
     * what stops two of them stacking.
     */
    private static void applyTrackedAttributes(ItemStack.Builder builder, RavengardItemType type,
                                               RavengardPlayer owner, double boost) {
        UUID itemUuid = UUID.randomUUID();
        String trackedId = type.getTrackedId() != null
                ? type.getTrackedId()
                : type.getRarity().name() + "_" + type.getId();
        RavengardTrackedItemsDatabase.record(itemUuid, trackedId,
                owner == null ? null : owner.getUuid(),
                owner == null ? null : owner.getSelectedProfile());
        new ItemAttributeItemId().apply(builder, trackedId);
        new ItemAttributeUniqueTrackedId().apply(builder, itemUuid.toString());
        if (boosted(boost)) {
            new ItemAttributeStatBoost().apply(builder, boost);
        }
    }

    private static String displayName(RavengardItemType type) {
        if (type.getDisplayName() != null) {
            return type.getDisplayName();
        }
        String[] words = type.getId().toLowerCase().split("_");
        StringBuilder name = new StringBuilder();
        for (String word : words) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    private static List<Text> lore(RavengardItemType type, boolean shopContext, double boost) {
        List<Text> lore = new ArrayList<>();

        Component tags = glyph(type.getRarity().getTagGlyph());
        StandardItemComponent standard = type.component(StandardItemComponent.class);
        boolean consumable = standard != null && "CONSUMABLE".equals(standard.getStandardItemType());
        if (standard != null && standard.tagGlyph() != 0) {
            tags = tags.append(Component.text(" ").font(RavengardFont.DEFAULT)
                    .append(glyph(standard.tagGlyph())));
        }
        lore.add(Text.of("<f>{}", tags));
        lore.add(BLANK);

        boolean stats = false;
        for (var statistic : net.swofty.type.ravengardgeneric.item.statistics.RavengardItemStatistic.values()) {
            boolean scaled = statistic.isBoostable() && boosted(boost);
            stats |= stat(lore, statistic,
                    type.statistic(statistic) * (scaled ? boost : 1.0),
                    scaled ? boost : 0);
        }
        if (stats) {
            lore.add(BLANK);
        }

        if (!type.getDescription().isEmpty()) {
            type.getDescription().forEach(line -> lore.add(Text.of("<7>" + highlightNumbers(line))));
            lore.add(BLANK);
        }
        if (type.getEffect() != null) {
            lore.add(highlightEffect(type.getEffect()));
            lore.add(BLANK);
        }

        if (type.getValue() > 0) {
            lore.add(crowns(type.getValue(), type.getValue() == 1 ? " Crown" : " Crowns"));
            lore.add(BLANK);
        }

        if (!consumable) {
            Iterable<RavengardClass> order = type.getRestrictedTo().isEmpty()
                    ? List.of(CLASS_TAG_ORDER) : type.getRestrictedTo();
            Component classes = Component.empty()
                    .color(NamedTextColor.WHITE)
                    .shadowColor(net.kyori.adventure.text.format.ShadowColor.shadowColor(0));
            for (RavengardClass tag : order) {
                classes = classes.append(glyph(tag.getTagGlyph())).append(Component.text(" "));
            }
            lore.add(Text.of("{}", classes));
        }
        while (!lore.isEmpty() && lore.getLast() == BLANK) {
            lore.removeLast();
        }
        return lore;
    }

    private static boolean stat(List<Text> lore,
                                net.swofty.type.ravengardgeneric.item.statistics.RavengardItemStatistic statistic,
                                double amount, double boost) {
        if (amount <= 0) {
            return false;
        }
        double rounded = Math.round(amount * 100.0) / 100.0;
        String value = rounded == Math.floor(rounded)
                ? String.valueOf((int) rounded) : String.valueOf(rounded);
        if (statistic.isPercent()) {
            value = "+" + value + "%";
        }
        Component iconValue = glyph(statistic.getIconGlyph())
                .append(Component.text(value).font(RavengardFont.DEFAULT));
        String label = statistic.getDisplayName();
        lore.add(boost > 0
                ? Text.of("<#5fec7b>{}</color> <f>{}</f> <#a3a3c2>(+{}x)</color>", iconValue, label, boostText(boost))
                : Text.of("<#5fec7b>{}</color> <f>{}</f>", iconValue, label));
        return true;
    }

    private static Component glyph(int codePoint) {
        return Component.text(new String(Character.toChars(codePoint)))
                .font(RavengardFont.RAVENGARD);
    }

    public static Text crowns(int amount, String suffix) {
        return Text.of("<f>👑</f><#ffce47>{}{}</color>", amount, suffix);
    }

    private static String highlightNumbers(String line) {
        return line
                .replaceAll("([+-]?\\d+(?:\\.\\d+)? ?HP)", "<c>$1</c>")
                .replaceAll("(\\d+(?:\\.\\d+)?%)", "<a>$1</a>")
                .replaceAll("(\\d+ seconds?)", "<e>$1</e>");
    }

    private static Text highlightEffect(String effect) {
        Matcher matcher = EFFECT_HIGHLIGHT.matcher(effect);
        StringBuilder markup = new StringBuilder("<7>");
        List<Object> arguments = new ArrayList<>();
        int last = 0;
        while (matcher.find()) {
            if (matcher.start() > last) {
                markup.append("{}");
                arguments.add(effect.substring(last, matcher.start()));
            }
            markup.append(matcher.group("hp") != null ? "<c>{}</c>" : "<e>{}</e>");
            arguments.add(matcher.group());
            last = matcher.end();
        }
        if (last < effect.length()) {
            markup.append("{}");
            arguments.add(effect.substring(last));
        }
        return Text.of(markup.toString(), arguments.toArray());
    }

    private static List<Text> placeholderLore(RavengardItemType type) {
        PlaceholderSlotComponent slot = type.component(PlaceholderSlotComponent.class);
        if (slot == null) {
            return List.of();
        }
        return List.of(BLANK, white(slot.getLoreOne()), white(slot.getLoreTwo()));
    }

    private static Text white(String text) {
        return Text.of("<f>{}", text);
    }
}
