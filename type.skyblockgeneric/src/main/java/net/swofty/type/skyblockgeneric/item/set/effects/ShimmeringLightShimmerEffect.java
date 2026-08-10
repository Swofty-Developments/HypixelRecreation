package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

public final class ShimmeringLightShimmerEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Shimmer"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Increases Experience Orbs from monsters and ores by " + tier(context, "200%", "200%", "300%") + ".");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 2; }
    private String tier(ArmorSetContext context, String... values) {
        return "<green>" + values[Math.clamp(context.wornPieces() - 2, 0, values.length - 1)] + "</green>";
    }
}
