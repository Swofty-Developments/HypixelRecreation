package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class OldDragonOldBloodEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Old Blood"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Improves Growth, Protection, Feather Falling, Sugar Rush, and True Protection enchantments.");
    }

}
