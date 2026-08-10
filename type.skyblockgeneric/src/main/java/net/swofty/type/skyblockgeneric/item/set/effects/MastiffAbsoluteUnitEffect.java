package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class MastiffAbsoluteUnitEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Absolute Unit"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Reduces damage from animals by 20%, converts Crit Damage into Health, heals when hit, and caps Defense at 300.");
    }

    @Override
    public float modifyIncomingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return mob.getMobTypes().contains(MobType.ANIMAL) ? damage * 0.8F : damage;
    }
}
