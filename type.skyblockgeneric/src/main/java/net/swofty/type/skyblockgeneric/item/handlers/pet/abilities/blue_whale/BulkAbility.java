package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.blue_whale;

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

@PetAbilityRegistration(pet = PetHandler.BLUE_WHALE, minimumRarity = Rarity.RARE)
public final class BulkAbility implements PetAbility {
    private static final RarityValue<Integer> MAX_HEALTH_THRESHOLD = new RarityValue<>(30, 30, 30, 25, 20, 20, 20);

    @Override
    public String getName() {
        return "Bulk";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double defense = level * 0.01;
        int threshold = MAX_HEALTH_THRESHOLD.getForRarity(rarity);

        return Arrays.asList(
                "<7>Gain <stat:defense:+" + decimalify(defense, 2) + "> <7>per",
                "<stat:health:" + threshold + " Max><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double defense = level * 0.01;
        int threshold = MAX_HEALTH_THRESHOLD.getForRarity(rarity);

        double maxHealth = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.HEALTH);
        double granted = Math.floor(maxHealth / threshold) * defense;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.DEFENSE, granted)
                .build();
    }
}