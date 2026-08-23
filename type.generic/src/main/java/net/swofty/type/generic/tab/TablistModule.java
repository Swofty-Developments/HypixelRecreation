package net.swofty.type.generic.tab;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;
import java.util.Locale;

public abstract class TablistModule {

    public abstract List<TablistEntry> getEntries(HypixelPlayer player);

    public record TablistEntry(Text content, TablistSkin registry) {
        public TablistEntry(String markup, TablistSkin registry) {
            this(Text.of(markup), registry);
        }
    }

    public static TablistEntry getGrayEntry() {
        return new TablistEntry(Text.empty(), TablistSkinRegistry.GRAY);
    }

    public static TablistEntry centered(Text content, Locale locale, TablistSkin registry) {
        return new TablistEntry(getCentered(content, locale), registry);
    }

    public static TablistEntry centered(String markup, Locale locale, TablistSkin registry) {
        return centered(Text.of(markup), locale, registry);
    }

    public static List<TablistEntry> fillRestWithGray(List<TablistEntry> entries) {
        for (int i = entries.size(); i < 20; i++) {
            entries.add(getGrayEntry());
        }

        return entries;
    }

    public static Text getCentered(Text text, Locale locale) {
        String plain = text.plain(locale);

        if (plain.length() > 30) {
            return Text.literal(plain.substring(0, 30));
        }

        int spaces = (30 - plain.length()) / 2;
        return spaces == 0 ? text : Text.literal(" ".repeat(spaces)).append(text);
    }
}
