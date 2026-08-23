package net.swofty.type.generic.collectibles.bedwars.prestige;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.collectibles.CollectibleCategory;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleStateService;
import net.swofty.type.generic.data.datapoints.DatapointCollectibles;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BedWarsPrestigeRenderer {

    public static Text renderBrackets(HypixelPlayer player) {
        return render(player, level(player), true);
    }

    public static Text renderString(HypixelPlayer player) {
        return render(player, level(player), false);
    }

    public static Text renderBrackets(HypixelPlayer player, int level) {
        return render(player, level, true);
    }

    public static Text renderString(HypixelPlayer player, int level) {
        return render(player, level, false);
    }

    public static Text renderPreview(HypixelPlayer player, int level, String schemeId, String starId, String bracketId) {
        return render(level, schemeId, starId, bracketId, true);
    }

    public static Text renderForData(BedWarsDataHandler dataHandler, int level, boolean brackets) {
        DatapointCollectibles.CollectiblesState state = dataHandler
            .get(BedWarsDataHandler.Data.COLLECTIBLES, DatapointCollectibles.class)
            .getValue();

        return render(
            level,
            state.getSelected(CollectibleCategory.PRESTIGE_SCHEMES),
            state.getSelected(CollectibleCategory.PRESTIGE_STARS),
            state.getSelected(CollectibleCategory.PRESTIGE_BRACKETS),
            brackets
        );
    }

    private static Text render(HypixelPlayer player, int level, boolean brackets) {
        BedWarsCollectibleStateService.reconcileSelected(player, CollectibleCategory.PRESTIGE_SCHEMES);
        BedWarsCollectibleStateService.reconcileSelected(player, CollectibleCategory.PRESTIGE_STARS);
        BedWarsCollectibleStateService.reconcileSelected(player, CollectibleCategory.PRESTIGE_BRACKETS);

        String schemeId = BedWarsCollectibleStateService.getSelectedId(player, CollectibleCategory.PRESTIGE_SCHEMES);
        String starId = BedWarsCollectibleStateService.getSelectedId(player, CollectibleCategory.PRESTIGE_STARS);
        String bracketId = BedWarsCollectibleStateService.getSelectedId(player, CollectibleCategory.PRESTIGE_BRACKETS);
        return render(level, schemeId, starId, bracketId, brackets);
    }

    private static Text render(int level, String schemeId, String starId, String bracketId, boolean brackets) {
        BedWarsPrestigeDefinitions.Scheme scheme = BedWarsPrestigeDefinitions.scheme(schemeId);
        BedWarsPrestigeDefinitions.Star star = BedWarsPrestigeDefinitions.star(starId);
        BedWarsPrestigeDefinitions.Bracket bracket = BedWarsPrestigeDefinitions.bracket(bracketId);
        return Text.parse(scheme.style().render(String.valueOf(level), star.symbol(), bracket, brackets));
    }

    private static int level(HypixelPlayer player) {
        BedWarsDataHandler handler = BedWarsDataHandler.getUser(player);
        if (handler == null) {
            return 0;
        }
        long experience = handler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class).getValue();
        return BedwarsLevelUtil.calculateLevel(experience);
    }

}
