package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class AuroraArcaneEnergyEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Arcane Energy"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Magic damage grants Arcane Energy. At 10 stacks, sneak to fire homing missiles.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }
}
