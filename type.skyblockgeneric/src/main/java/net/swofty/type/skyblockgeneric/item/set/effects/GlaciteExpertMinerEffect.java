package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class GlaciteExpertMinerEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Expert Miner"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants <sbstat:mining_speed:+2> per Mining level.");
    }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        SkyBlockPlayer player = context.player();
        return baseStatistic(MINING_SPEED, player == null ? 0 : player.getSkills().getCurrentLevel(SkillCategories.MINING) * 2D);
    }
}
