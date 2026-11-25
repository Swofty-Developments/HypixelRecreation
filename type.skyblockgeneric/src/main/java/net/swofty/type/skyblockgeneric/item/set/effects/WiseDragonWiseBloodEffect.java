package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class WiseDragonWiseBloodEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Wise Blood"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Reduces ability Mana costs to two thirds.");
    }

    @Override
    public int modifyManaCost(ArmorSetContext context, int manaCost) {
        return (int) Math.ceil(manaCost * 2D / 3D);
    }
}
