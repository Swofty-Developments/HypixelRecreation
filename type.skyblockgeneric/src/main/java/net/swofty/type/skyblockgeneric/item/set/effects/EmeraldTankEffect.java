package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class EmeraldTankEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Tank"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants <sbstat:health:+1> and <sbstat:defense:+1> per 3,000 Emeralds in the collection, up to +350 each.");
    }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        SkyBlockPlayer player = context.player();
        double value = player == null ? 0 : Math.min(350, player.getCollection().get(ItemType.EMERALD) / 3_000);
        return ItemStatistics.builder().withBase(HEALTH, value).withBase(DEFENSE, value).build();
    }
}
