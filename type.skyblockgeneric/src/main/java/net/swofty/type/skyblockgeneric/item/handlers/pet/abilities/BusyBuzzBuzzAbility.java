package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

public final class BusyBuzzBuzzAbility implements PetAbility {
    @Override
    public String getName() {
        return "Busy Buzz Buzz";
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        ItemAttributePetData.PetData petData = instance.getAttributeHandler().getPetData();
        var rarity = instance.getAttributeHandler().getRarity();
        int level = petData.getAsLevel(rarity);
        double bonus = rarity.isAtLeast(Rarity.EPIC) ? level * 0.3 : level * 0.2;

        return Arrays.asList(
                "<7>Grants <a>+" + decimalify(bonus, 1) + " <7>of each to your pet:",
                "<stat:farming_fortune>",
                "<stat:foraging_fortune>",
                "<stat:mining_fortune>"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet) {
        ItemAttributePetData.PetData petData = pet.getAttributeHandler().getPetData();
        var rarity = pet.getAttributeHandler().getRarity();
        int level = petData.getAsLevel(rarity);
        double bonus = rarity.isAtLeast(Rarity.EPIC) ? level * 0.3 : level * 0.2;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FARMING_FORTUNE, bonus)
                .withBase(ItemStatistic.FORAGING_FORTUNE, bonus)
                .withBase(ItemStatistic.MINING_FORTUNE, bonus)
                .build();
    }
}
