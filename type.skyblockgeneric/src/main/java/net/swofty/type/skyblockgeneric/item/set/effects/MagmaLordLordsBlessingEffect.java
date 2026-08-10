package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.SEA_CREATURE_CHANCE;

public final class MagmaLordLordsBlessingEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Lord's Blessing"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants <sbstat:sea_creature_chance:+1> and <sbstat:magic_find:+20> on Magmatic mobs.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 4; }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return baseStatistic(SEA_CREATURE_CHANCE, 1);
    }
}
