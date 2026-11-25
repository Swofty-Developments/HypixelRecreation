package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class CrimsonDominusEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Dominus"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Melee attacks grant Dominus stacks. At 10 stacks, melee attacks also swipe through nearby enemies.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }
}
