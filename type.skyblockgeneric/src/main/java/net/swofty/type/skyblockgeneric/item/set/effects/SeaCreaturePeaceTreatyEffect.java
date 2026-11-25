package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public abstract class SeaCreaturePeaceTreatyEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Peace Treaty"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Gain immunity to Sea Creatures, but they are immune to you and you can no longer catch them.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }

    @Override
    public float modifyIncomingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return mob.getMobTypes().contains(MobType.AQUATIC) ? 0 : damage;
    }

    @Override
    public float modifyOutgoingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return mob.getMobTypes().contains(MobType.AQUATIC) ? 0 : damage;
    }
}
