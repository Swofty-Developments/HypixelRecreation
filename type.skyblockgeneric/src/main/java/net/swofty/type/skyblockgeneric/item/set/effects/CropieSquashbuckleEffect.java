package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class CropieSquashbuckleEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Squashbuckle"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants a chance to harvest Squash and " + statistic(FARMING_FORTUNE, context.tierValue(0, 15, 30, 45)) + ".");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }
    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return baseStatistic(FARMING_FORTUNE, context.tierValue(0, 15, 30, 45));
    }
}
