package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.HEALTH;

public final class AnglerDeepnessWithinEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Deepness Within"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants " + statistic(HEALTH, healthPerLevel(context)) + " per Fishing Level.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        if (context.player() == null) return ItemStatistics.empty();
        int level = context.player().getSkills().getCurrentLevel(SkillCategories.FISHING);
        return baseStatistic(HEALTH, healthPerLevel(context) * level);
    }

    private int healthPerLevel(ArmorSetContext context) {
        return switch (context.wornPieces()) {
            case 2 -> 6;
            case 3 -> 8;
            default -> 10;
        };
    }
}
