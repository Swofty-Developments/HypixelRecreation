package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.elephant;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ELEPHANT, minimumRarity = Rarity.RARE)
public final class WalkingFortressAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.01, 0.01, 0.01, 0.01, 0.0);

    @Override
    public String getName() {
        return "Walking Fortress";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>Gain <stat:health:+" + decimalify(value, 2) + "> <7>for every",
                "<7>10<stat:defense><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double healthPerDefense = PER_LEVEL.getForRarity(rarity) * level;
        double defense = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.DEFENSE);
        double granted = healthPerDefense * Math.floor(defense / 10.0);

        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, granted)
                .build();
    }
}
