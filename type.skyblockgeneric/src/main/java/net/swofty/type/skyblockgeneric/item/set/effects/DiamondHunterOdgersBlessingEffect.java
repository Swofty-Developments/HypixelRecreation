package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.TROPHY_FISH_CHANCE;

public final class DiamondHunterOdgersBlessingEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Odger's Blessing"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants " + statistic(TROPHY_FISH_CHANCE, value(context)) + ".");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return baseStatistic(TROPHY_FISH_CHANCE, value(context));
    }

    private double value(ArmorSetContext context) {
        return context.tierValue(0, 20, 25, 30);
    }
}
