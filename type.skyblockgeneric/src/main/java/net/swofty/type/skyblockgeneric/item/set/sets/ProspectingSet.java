package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;

import java.util.ArrayList;
import java.util.List;

public class ProspectingSet implements ArmorSet {
    @Override
    public String getName() {
        return "Beginner's Boost";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "Grants §a+40 §7Mining Speed."
        ));
    }

    @Override
    public ItemStatistics getStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.MINING_SPEED, 40D)
                .build();
    }
}
