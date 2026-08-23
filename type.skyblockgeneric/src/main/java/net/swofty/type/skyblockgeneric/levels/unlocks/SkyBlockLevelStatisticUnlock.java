package net.swofty.type.skyblockgeneric.levels.unlocks;

import lombok.Getter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelUnlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SkyBlockLevelStatisticUnlock extends SkyBlockLevelUnlock {
    private final ItemStatistics statistics;

    public SkyBlockLevelStatisticUnlock(ItemStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public UnlockType type() {
        return UnlockType.STATISTIC;
    }

    @Override
    public ItemStack.Builder getItemDisplay(SkyBlockPlayer player, int level) {
        List<String> statisticsDisplay = new ArrayList<>();
        statistics.getStatisticsAdditive().forEach((key, value) -> {
            if (value > 0)
                statisticsDisplay.add("<8> +<a>" + value + key.getSuffix() + " <stat:" + key.name().toLowerCase() + ">");
        });

        if (statisticsDisplay.isEmpty()) {
            statisticsDisplay.add("<8>No statistics unlocked");
        }

        List<Text> lore = new ArrayList<>();
        for (int i = 1; i < statisticsDisplay.size(); i++) {
            lore.add(Text.of(statisticsDisplay.get(i)));
        }
        lore.add(Text.of("<8>Level {}", level));

        return ItemStacks.item(Material.APPLE, 1, Text.of(statisticsDisplay.getFirst()), lore);
    }

    @Override
    public List<Text> getDisplay(SkyBlockPlayer player, int level) {
        ArrayList<Text> lore = new ArrayList<>();
        statistics.getStatisticsAdditive().forEach((key, value) -> {
            if (value > 0)
                lore.add(Text.of("<8> +<a>" + value + key.getSuffix() + " <stat:" + key.name().toLowerCase() + ">"));
        });
        return lore;
    }
}
