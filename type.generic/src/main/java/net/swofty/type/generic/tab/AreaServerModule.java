package net.swofty.type.generic.tab;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generic 'Server Info' tablist module that displays:
 * <pre>
 *   {area name}
 *   Server: {server identifier}
 * </pre>
 * Most type.* loaders previously held a hand-written subclass that differed
 * only in the i18n area key (spiders_den, crimson_isle, gold_mine, ...).
 * Those have been collapsed to a single shared module; callers pass the
 * area key when registering.
 *
 * <p>Modules that need extra rows (e.g. private-island minion summary) still
 * extend {@link TablistModule} directly; this is only for the plain
 * area/server pair.
 */
public class AreaServerModule extends TablistModule {

    private final String areaI18nKey;

    public AreaServerModule(String areaI18nKey) {
        this.areaI18nKey = areaI18nKey;
    }

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer player) {
        Locale l = player.getLocale();
        ArrayList<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.server_info"), l, TablistSkinRegistry.CYAN)
        ));

        entries.add(new TablistEntry(Text.key(areaI18nKey), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(
                Text.key("tablist.server_info.server_label", HypixelConst.getServerName()),
                TablistSkinRegistry.GRAY
        ));

        fillRestWithGray(entries);
        return entries;
    }
}
