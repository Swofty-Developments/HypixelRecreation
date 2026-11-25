package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class FermentoFeastEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Feast"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Combines the lower farming armor bonuses, adds Helianthus drops, and grants " + statistic(FARMING_FORTUNE, context.tierValue(0, 25, 50, 75)) + ".");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }
    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return baseStatistic(FARMING_FORTUNE, context.tierValue(0, 25, 50, 75));
    }
}
