package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class HollowSpiritEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Spirit"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Kills by you or recently supported players grant Spirit charges for the Hollow Wand.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }
}
