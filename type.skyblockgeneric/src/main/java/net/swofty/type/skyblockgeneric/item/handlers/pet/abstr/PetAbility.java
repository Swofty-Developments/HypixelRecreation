package net.swofty.type.skyblockgeneric.item.handlers.pet.abstr;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public interface PetAbility {
    String getName();

    List<String> getDescription(SkyBlockItem instance);

    default ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet) {
        return ItemStatistics.empty();
    }

    default double getManaRegenerationPercent(SkyBlockPlayer player, SkyBlockItem pet) {
        return 0;
    }

    default double getSuperpairsUltraRareBookMultiplier(SkyBlockPlayer player, SkyBlockItem pet) {
        return 1;
    }

    default void onEvent(PetEvent event) {
    }
}
