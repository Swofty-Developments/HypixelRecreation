package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class AbyssalOneWithTheFishEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "One with the Fish"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants permanent underwater breathing outside Galatea and Backwater Bayou.");
    }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return baseStatistic(RESPIRATION, 100);
    }
}
