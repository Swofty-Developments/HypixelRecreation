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

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.BLACK_CAT, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a modifiable Speed cap; speed stat itself is granted")
public final class HunterAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0);

    @Override
    public String getName() {
        return "Hunter";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Increases your speed and",
                "<7>speed cap by +<a>" + commaify(value) + "<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.SPEED, PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
