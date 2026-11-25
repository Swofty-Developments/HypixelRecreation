package net.swofty.type.skyblockgeneric.tabwidgets;

import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class TablistSettingsStore {
    private TablistSettingsStore() {
    }

    public static TablistWidgetSettings get(SkyBlockPlayer p) {
        return TablistWidgetSettings.deserialize(p.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TABLIST_WIDGET_SETTINGS, DatapointString.class).getValue());
    }

    public static void save(SkyBlockPlayer p, TablistWidgetSettings s) {
        p.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TABLIST_WIDGET_SETTINGS, DatapointString.class).setValue(s.serialize());
    }
}
