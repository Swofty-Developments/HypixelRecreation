package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class HolyDragonHolyBloodEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Holy Blood"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants <sbstat:health_regeneration:+75> to you and players within 10 blocks.");
    }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return baseStatistic(HEALTH_REGENERATION, 75);
    }
}
