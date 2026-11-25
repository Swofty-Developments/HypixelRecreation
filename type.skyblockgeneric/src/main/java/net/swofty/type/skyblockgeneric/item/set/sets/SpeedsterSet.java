package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;

import java.util.ArrayList;
import java.util.List;

public class SpeedsterSet implements ArmorSet {
    @Override
    public String getName() {
        return "Bonus Speed";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "Increases your §aSpeed §7by §a+20§7."
        ));
    }

    @Override
    public ItemStatistics getStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.SPEED, 20D)
                .build();
    }
}
