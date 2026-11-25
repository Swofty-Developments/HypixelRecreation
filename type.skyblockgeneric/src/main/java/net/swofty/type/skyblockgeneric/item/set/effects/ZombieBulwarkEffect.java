package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public abstract class ZombieBulwarkEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Zombie Bulwark"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.PIECE; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Kill Zombies to accumulate Defense against them on each armor piece.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }
}
