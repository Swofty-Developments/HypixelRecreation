package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class TerrorHydraStrikeEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Hydra Strike"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Arrow attacks grant Hydra Strike, increasing arrow damage and speed and firing two extra arrows at 10 stacks.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }
}
