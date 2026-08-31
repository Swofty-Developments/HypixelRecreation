package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.black_cat;

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

@PetAbilityRegistration(pet = PetHandler.BLACK_CAT, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class OmenAbility implements PetAbility {
    private static final RarityValue<Double> PET_LUCK_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.15, 0.15, 0.0);

    @Override
    public String getName() {
        return "Omen";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PET_LUCK_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Grants <a>" + decimalify(value, 2) + " <stat:pet_luck><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.PET_LUCK, PET_LUCK_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
