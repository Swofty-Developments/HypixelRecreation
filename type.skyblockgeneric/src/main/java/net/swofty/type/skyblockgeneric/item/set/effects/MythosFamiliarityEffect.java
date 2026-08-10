package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.MAGIC_FIND;

public final class MythosFamiliarityEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Familiarity"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants " + statistic(MAGIC_FIND, value(context)) + " on Mythological mobs.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }

    private int value(ArmorSetContext context) {
        return switch (context.wornPieces()) {
            case 2 -> 10;
            case 3 -> 15;
            default -> 20;
        };
    }
}
