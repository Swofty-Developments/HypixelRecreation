package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class AbyssalDeepSeaDiverEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Deep Sea Diver"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants " + statistic(TREASURE_CHANCE, context.tierValue(0, 1.5, 2, 2.5)) + " while underwater.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }
    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return baseStatistic(TREASURE_CHANCE, context.tierValue(0, 1.5, 2, 2.5));
    }
}
