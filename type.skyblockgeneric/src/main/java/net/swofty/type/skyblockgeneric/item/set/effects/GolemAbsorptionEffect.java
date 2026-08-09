package net.swofty.type.skyblockgeneric.item.set.effects;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class GolemAbsorptionEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Absorption"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants Absorption III for 20 seconds after killing an enemy.");
    }

    @Override
    public void onMobKill(ArmorSetContext context, SkyBlockMob mob) {
        context.player().addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 2, 20 * 20));
    }
}
