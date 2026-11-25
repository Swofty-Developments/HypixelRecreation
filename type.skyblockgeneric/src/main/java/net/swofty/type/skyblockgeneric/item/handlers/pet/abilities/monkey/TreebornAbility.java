package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.monkey;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MONKEY, minimumRarity = Rarity.COMMON)
public final class TreebornAbility implements PetAbility {
    private static final RarityValue<Double> FORAGING_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.4, 0.5, 0.5, 0.6, 0.6, 0.0, 0.0);

    @Override
    public String getName() {
        return "Treeborn";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(FORAGING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <a>+" + value + " <stat:foraging_fortune><7>, which",
                "<7>increases your chance at double logs."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FORAGING_FORTUNE, FORAGING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
