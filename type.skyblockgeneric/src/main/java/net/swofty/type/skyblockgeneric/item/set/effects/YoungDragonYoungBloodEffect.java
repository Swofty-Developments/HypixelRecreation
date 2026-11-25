package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class YoungDragonYoungBloodEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Young Blood"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants <sbstat:speed:+150> while above 50% Health and increases the Speed cap by 100.");
    }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        SkyBlockPlayer player = context.player();
        return player != null && player.getHealth() > player.getMaxHealth() / 2
                ? baseStatistic(SPEED, 150) : ItemStatistics.empty();
    }
}
