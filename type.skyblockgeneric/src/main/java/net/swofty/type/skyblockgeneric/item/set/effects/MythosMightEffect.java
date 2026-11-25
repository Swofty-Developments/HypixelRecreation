package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class MythosMightEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Mythos' Might"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.ABILITY; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Doubles this armor's stats in the Hub during the Mythological Ritual.");
    }

}
