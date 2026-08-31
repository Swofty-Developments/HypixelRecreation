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

@PetAbilityRegistration(pet = PetHandler.ELEPHANT, minimumRarity = Rarity.COMMON)
public final class StompAbility implements PetAbility {
    private static final RarityValue<Double> DEFENSE_PER_LEVEL = new RarityValue<>(0.15, 0.15, 0.15, 0.2, 0.2, 0.2, 0.0);

    @Override
    public String getName() {
        return "Stomp";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = DEFENSE_PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>Gain <stat:defense:+" + decimalify(value, 2) + "> <7>for every",
                "<7>100<stat:speed><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double defensePerSpeed = DEFENSE_PER_LEVEL.getForRarity(rarity) * level;
        double speed = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.SPEED);
        double granted = defensePerSpeed * Math.floor(speed / 100.0);

        return ItemStatistics.builder()
                .withBase(ItemStatistic.DEFENSE, granted)
                .build();
    }
}
