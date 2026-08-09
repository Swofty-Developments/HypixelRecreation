package net.swofty.type.skyblockgeneric.item.set.effects;

import net.minestom.server.instance.block.Block;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.TREASURE_CHANCE;

public final class DiverDeepSeaDiverEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Deep Sea Diver"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.TIERED; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants " + statistic(TREASURE_CHANCE, value(context)) + " while <blue>underwater</blue>.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        if (context.player() == null || context.player().getInstance() == null) return ItemStatistics.empty();
        Block block = context.player().getInstance().getBlock(context.player().getPosition());
        return block == Block.WATER || block == Block.BUBBLE_COLUMN
                ? baseStatistic(TREASURE_CHANCE, value(context)) : ItemStatistics.empty();
    }

    private double value(ArmorSetContext context) {
        return context.tierValue(0, 0.5, 1, 1.5);
    }
}
