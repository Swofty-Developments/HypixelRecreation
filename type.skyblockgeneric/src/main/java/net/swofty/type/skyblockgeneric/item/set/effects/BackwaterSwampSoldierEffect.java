package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;

import java.util.List;

public final class BackwaterSwampSoldierEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Swamp Soldier"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Increases damage dealt to Sea Creatures by <green>+" + bonus(context) + "%</green>.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }

    @Override
    public float modifyOutgoingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return mob.getMobTypes().contains(MobType.AQUATIC) ? damage * (1F + bonus(context) / 100F) : damage;
    }

    private int bonus(ArmorSetContext context) {
        return switch (context.wornPieces()) {
            case 1 -> 5;
            case 2 -> 10;
            case 3 -> 20;
            default -> 40;
        };
    }
}
