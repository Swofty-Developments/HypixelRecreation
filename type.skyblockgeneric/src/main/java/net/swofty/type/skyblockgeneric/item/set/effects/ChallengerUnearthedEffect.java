package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class ChallengerUnearthedEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Unearthed"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Reduces damage taken from Mythological mobs by up to 25%.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }
}
