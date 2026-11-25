package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class SuperiorDragonSuperiorBloodEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Superior Blood"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Increases Combat stats and Magic Find by 5%, and increases Aspect of the Dragons ability damage by 50%.");
    }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic statistic : List.of(HEALTH, DEFENSE, STRENGTH, INTELLIGENCE, CRITICAL_CHANCE,
                CRITICAL_DAMAGE, BONUS_ATTACK_SPEED, ABILITY_DAMAGE, TRUE_DEFENSE, FEROCITY, HEALTH_REGENERATION,
                VITALITY, MENDING, SWING_RANGE, SPEED, MAGIC_FIND)) {
            builder.withMultiplicativePercentage(statistic, 5D);
        }
        return builder.build();
    }
}
