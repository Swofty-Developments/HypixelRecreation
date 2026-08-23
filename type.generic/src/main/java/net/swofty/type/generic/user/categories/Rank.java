package net.swofty.type.generic.user.categories;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.swofty.commons.StringUtility;

import java.util.Locale;

@Getter
public enum Rank {
    STAFF("ዞ", true, NamedTextColor.RED),
    YOUTUBE("YOUTUBE", false, NamedTextColor.RED),
    MVP_PLUS_PLUS("MVP++", false, NamedTextColor.GOLD),
    MVP_PLUS("MVP+", false, NamedTextColor.AQUA),
    MVP("MVP", false, NamedTextColor.AQUA),
    VIP_PLUS("VIP+", false, NamedTextColor.GREEN),
    VIP("VIP", false, NamedTextColor.GREEN),
    DEFAULT("Default", false, NamedTextColor.GRAY),
    ;

    public static final String TAG = "rank";

    private final String title;
    private final boolean isStaff;
    private final NamedTextColor textColor;

    Rank(String title, boolean isStaff, NamedTextColor textColor) {
        this.title = title;
        this.isStaff = isStaff;
        this.textColor = textColor;

    }

    public boolean isEqualOrHigherThan(Rank rank) {
        return this.ordinal() <= rank.ordinal();
    }

    public char getPriorityCharacter() {
        return StringUtility.ALPHABET[ordinal()];
    }

    public Component titleComponent(RankColor plusColor, NamedTextColor mvpPlusPlusColor) {
        return switch (this) {
            case STAFF -> Component.text("ዞ", NamedTextColor.GOLD);
            case YOUTUBE -> Component.text("YOUTUBE", NamedTextColor.WHITE);
            case MVP_PLUS_PLUS ->
                Component.text("MVP", mvpPlusPlusColor).append(Component.text("++", plusColor.getColor()));
            case MVP_PLUS ->
                Component.text("MVP", NamedTextColor.AQUA).append(Component.text("+", plusColor.getColor()));
            case MVP -> Component.text("MVP", NamedTextColor.AQUA);
            case VIP_PLUS ->
                Component.text("VIP", NamedTextColor.GREEN).append(Component.text("+", NamedTextColor.GOLD));
            case VIP -> Component.text("VIP", NamedTextColor.GREEN);
            case DEFAULT -> Component.text("Default", NamedTextColor.GRAY);
        };
    }

    public Component prefixComponent(RankColor plusColor, NamedTextColor mvpPlusPlusColor) {
        if (this == DEFAULT) return Component.empty();
        NamedTextColor bracketColor = this == MVP_PLUS || this == MVP_PLUS_PLUS ? mvpPlusPlusColor : textColor;
        return Component.text("[", bracketColor).append(titleComponent(plusColor, mvpPlusPlusColor))
            .append(Component.text("] ", bracketColor));
    }

    public Component prefixComponent() {
        return prefixComponent(RankColor.RED, NamedTextColor.GOLD);
    }

    public Component displayName(RankColor plusColor, NamedTextColor mvpPlusPlusColor, String username) {
        if (this == DEFAULT) return Component.text(username);
        NamedTextColor bracketColor = this == MVP_PLUS || this == MVP_PLUS_PLUS ? mvpPlusPlusColor : textColor;
        return prefixComponent(plusColor, mvpPlusPlusColor).append(Component.text(username, bracketColor));
    }

    public Component displayName(String username) {
        return displayName(RankColor.RED, NamedTextColor.GOLD, username);
    }

    public String prefixMarkup() {
        return "<" + TAG + ":" + name().toLowerCase(Locale.ROOT) + ">";
    }

    public static Component resolveTag(String name) {
        Rank rank = byTag(name);
        return rank == null ? null : rank.prefixComponent();
    }

    private static Rank byTag(String name) {
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
