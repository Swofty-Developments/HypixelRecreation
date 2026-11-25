package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class PrimordialOctodexterityEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Octodexterity"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Every third strike deals +50% damage and applies Venom for 4 seconds.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }
}
