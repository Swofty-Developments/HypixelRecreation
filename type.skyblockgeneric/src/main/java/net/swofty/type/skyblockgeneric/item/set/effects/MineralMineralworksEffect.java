package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class MineralMineralworksEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Mineralworks"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants " + statistic(MINING_SPREAD, context.tierValue(100, 200, 300, 400)) + " on Ores and Blocks.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }
    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return baseStatistic(MINING_SPREAD, context.tierValue(100, 200, 300, 400));
    }
}
