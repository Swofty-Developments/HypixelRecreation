package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class AnglerDepthChampionEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Depth Champion"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Reduces damage taken from Sea Creatures by <green>" + reduction(context) + "%</green>.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }

    private int reduction(ArmorSetContext context) {
        return switch (context.wornPieces()) {
            case 1 -> 3;
            case 2 -> 6;
            case 3 -> 8;
            default -> 10;
        };
    }
    @Override
    public float modifyIncomingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        if (!mob.getMobTypes().contains(MobType.AQUATIC)) return damage;
        return damage * switch (context.wornPieces()) {
            case 1 -> 0.97F;
            case 2 -> 0.94F;
            case 3 -> 0.92F;
            default -> 0.9F;
        };
    }
}
