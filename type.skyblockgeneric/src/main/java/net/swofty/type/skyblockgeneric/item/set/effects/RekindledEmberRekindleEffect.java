package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class RekindledEmberRekindleEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Rekindle"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Increases outgoing burning damage and grows stronger while you are on fire.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }
}
