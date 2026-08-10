package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class WerewolfRegenerativeHowlEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Regenerative Howl"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Ferocity strikes heal players within 25 blocks for 1% of your Defense and grant <sbstat:defense:+50> for 5 seconds, stacking up to 10 times.");
    }

}
