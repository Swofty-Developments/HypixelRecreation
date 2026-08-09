package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class FairyOutfitEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Fairy's Outfit"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants <sbstat:health:+1> per Fairy Soul found and alerts you to nearby undiscovered Fairy Souls.");
    }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        SkyBlockPlayer player = context.player();
        return baseStatistic(HEALTH, player == null ? 0 : player.getFairySouls().getAllFairySouls().size());
    }
}
