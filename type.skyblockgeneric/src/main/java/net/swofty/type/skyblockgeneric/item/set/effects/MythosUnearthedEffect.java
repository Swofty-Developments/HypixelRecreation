package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;

import java.util.List;

public final class MythosUnearthedEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Unearthed"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Reduces damage taken from Mythological mobs by <green>" + reduction(context) + "%</green>.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }

    @Override
    public float modifyIncomingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return mob.getMobTypes().contains(MobType.MYTHOLOGICAL)
                ? damage * (1F - (float) reduction(context) / 100F) : damage;
    }

    private double reduction(ArmorSetContext context) {
        return switch (context.wornPieces()) {
            case 2 -> 10;
            case 3 -> 12.5;
            default -> 15;
        };
    }
}
