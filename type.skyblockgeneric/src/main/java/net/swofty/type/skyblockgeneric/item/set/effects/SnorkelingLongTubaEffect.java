package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class SnorkelingLongTubaEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Long Tuba"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants " + statistic(RESPIRATION, context.tierValue(0, 2, 5, 10)) + ".");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }
    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return baseStatistic(RESPIRATION, context.tierValue(0, 2, 5, 10));
    }
}
