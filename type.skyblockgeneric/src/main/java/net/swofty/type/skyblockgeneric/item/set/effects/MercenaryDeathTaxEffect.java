package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class MercenaryDeathTaxEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Death Tax"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Killing a level 10 or higher enemy grants 5 Coins and heals 20 Health.");
    }

    @Override
    public void onMobKill(ArmorSetContext context, SkyBlockMob mob) {
        if (mob.getLevel() < 10) return;
        context.player().addCoins(5);
        context.player().setHealth(Math.min(context.player().getMaxHealth(), context.player().getHealth() + 20));
    }
}
