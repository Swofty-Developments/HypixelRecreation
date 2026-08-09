package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class SnowSuitColdThumbEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Cold Thumb"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.PIECE; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }
}
