package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class StrongDragonStrongBloodEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Strong Blood"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants the Aspect of the End and Aspect of the Void <sbstat:damage:+75>, +2 teleport range, +3 seconds of Speed, and <sbstat:strength:+5> on cast.");
    }

}
