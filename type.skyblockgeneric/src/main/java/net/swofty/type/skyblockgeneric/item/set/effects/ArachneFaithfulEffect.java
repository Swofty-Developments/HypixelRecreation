package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

import java.util.List;

public final class ArachneFaithfulEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Arachne's Faithful"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        double value = value(context);
        return List.of("Grants " + statistic(HEALTH, value) + " and " + statistic(DEFENSE, value) + ".");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        double value = value(context);
        return ItemStatistics.builder().withBase(HEALTH, value).withBase(DEFENSE, value).build();
    }

    private double value(ArmorSetContext context) {
        return switch (context.wornPieces()) {
            case 2 -> 5;
            case 3 -> 10;
            default -> 20;
        };
    }
}
