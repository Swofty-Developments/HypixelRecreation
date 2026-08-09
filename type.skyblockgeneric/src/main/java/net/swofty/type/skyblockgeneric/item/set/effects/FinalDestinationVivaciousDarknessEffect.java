package net.swofty.type.skyblockgeneric.item.set.effects;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class FinalDestinationVivaciousDarknessEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Vivacious Darkness"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("While sneaking in combat, consumes 2 Soulflow every 5 seconds to grant <sbstat:strength:+30>, <sbstat:bonus_attack_speed:+20%>, <sbstat:speed:+10>, 1.25x Intelligence, <sbstat:ferocity:+200> and +100% damage against Endermen.");
    }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null || !player.isSneaking()) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(STRENGTH, 30D).withBase(BONUS_ATTACK_SPEED, 20D)
                .withBase(SPEED, 10D).withMultiplicative(INTELLIGENCE, 1.25D).build();
    }
    @Override
    public float modifyOutgoingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return mob.getEntityType() == EntityType.ENDERMAN ? damage * 2F : damage;
    }
}
