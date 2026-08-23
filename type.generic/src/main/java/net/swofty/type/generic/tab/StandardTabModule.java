package net.swofty.type.generic.tab;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointRank;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StandardTabModule extends TablistModule{
    public int page;

    public StandardTabModule(int page) {
            this.page = page;
    }

    public List<TablistEntry> getEntries(HypixelPlayer player) {
        List<HypixelPlayer> players = HypixelGenericLoader.getLoadedPlayers();

        ArrayList<TablistEntry> entries = new ArrayList<>();

        List<HypixelPlayer> toShow = new ArrayList<>();

        // Sort players by their rank ordinal in reverse
        players.sort((o1, o2) -> o2.getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().ordinal()
                - o1.getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().ordinal());
        Collections.reverse(players);

        // In chunks of 20, load the players into the toShow list.
        // If the page is 1, then use the first 20, if the page is 2, then use the second set of 20, etc.
        for (int i = 0; i < players.size(); i++) {
            if (i >= (page - 1) * 20 && i < page * 20) {
                toShow.add(players.get(i));
            }
        }

        for (int x = 0; x < 20; x++) {
            if (x >= toShow.size()) {
                continue;
            }

            entries.add(new TablistEntry(Text.of("{}", toShow.get(x).getRankDisplayName()), TablistSkinRegistry.GRAY));
        }

        return entries;
    }
}
