package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class ReaperTrollingTheReaperEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Trolling the Reaper"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Healing Wands are 50% more effective, grants <sbstat:defense:+100> and +100% damage against Undead, but reduces damage against other mobs to 1%.");
    }

    @Override
    public float modifyOutgoingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return damage * (mob.getMobTypes().contains(MobType.UNDEAD) ? 2F : 0.01F);
    }
}
