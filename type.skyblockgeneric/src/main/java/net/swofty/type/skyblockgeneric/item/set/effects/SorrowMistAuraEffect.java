package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class SorrowMistAuraEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Mist Aura"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants invisibility and multiplies damage taken from Ghosts by 0.6x.");
    }

    @Override
    public float modifyIncomingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return mob.getDisplayName().toLowerCase().contains("ghost") ? damage * 0.6F : damage;
    }
}
