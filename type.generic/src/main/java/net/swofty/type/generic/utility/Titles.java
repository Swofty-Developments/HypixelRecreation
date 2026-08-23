package net.swofty.type.generic.utility;

import net.kyori.adventure.title.Title;
import net.swofty.commons.text.Text;

/**
 * Markup-first factory for Adventure {@link Title}s.
 *
 * Use this only where the same title object has to be handed to several audiences (or to a raw
 * Minestom {@code Player}); a single {@code HypixelPlayer} should call
 * {@code player.showTitle(markup, subtitleMarkup, times)} instead.
 */
public final class Titles {

    private Titles() {
    }

    public static Title title(Text title, Text subtitle, Title.Times times) {
        return Title.title(title.asComponent(), subtitle.asComponent(), times);
    }

    public static Title title(Text title, Text subtitle) {
        return title(title, subtitle, Title.DEFAULT_TIMES);
    }

    public static Title title(String titleMarkup, String subtitleMarkup, Title.Times times, Object... arguments) {
        return title(Text.of(titleMarkup, arguments), Text.of(subtitleMarkup, arguments), times);
    }

    public static Title title(String titleMarkup, String subtitleMarkup) {
        return title(titleMarkup, subtitleMarkup, Title.DEFAULT_TIMES);
    }
}
