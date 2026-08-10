package net.swofty.type.skyblockgeneric.item.set.effects;

import net.minestom.server.instance.block.Block;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.skyblock.statistics.ItemStatistic.*;

public final class SharkScaleAbsorbEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Absorb"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.FULL_SET; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Doubles Defense while in water.");
    }

    @Override
    public ItemStatistics getStatistics(ArmorSetContext context) {
        return waterDefense(context);
    }
    private ItemStatistics waterDefense(ArmorSetContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null || player.getInstance() == null) return ItemStatistics.empty();
        Block block = player.getInstance().getBlock(player.getPosition());
        return block == Block.WATER || block == Block.BUBBLE_COLUMN
                ? ItemStatistics.builder().withMultiplicative(DEFENSE, 2D).build()
                : ItemStatistics.empty();
    }
    private String tier(ArmorSetContext context, String... values) {
        return values[Math.clamp(context.wornPieces() - 2, 0, values.length - 1)];
    }
}
