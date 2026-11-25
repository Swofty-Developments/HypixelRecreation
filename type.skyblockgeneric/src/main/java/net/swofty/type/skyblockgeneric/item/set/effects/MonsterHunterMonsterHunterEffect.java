package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class MonsterHunterMonsterHunterEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Monster Hunter"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Deals 25% more damage to monsters and takes 25% less damage from them.");
    }

    @Override
    public float modifyOutgoingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) { return damage * 1.25F; }

    @Override
    public float modifyIncomingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) { return damage * 0.75F; }
}
