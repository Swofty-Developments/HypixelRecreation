package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.golden_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.bank.PersonalBankService;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GOLDEN_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 3)
public final class LegendaryTreasureAbility implements PetAbility {
    private static final RarityValue<Double> PERCENT_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.00125, 0.0, 0.0);
    private static final RarityValue<Double> MAX_PERCENT_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.25, 0.0, 0.0);

    @Override
    public String getName() {
        return "Legendary Treasure";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(0.125 + PERCENT_PER_LEVEL.getForRarity(rarity) * level, 2);
        String max = decimalify(125 + MAX_PERCENT_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gain <c>" + percent + "% <stat:damage> <7>for every million",
                "<7>coins in your bank. <8>(Max " + max + "%)<7>"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double bank = PersonalBankService.data(player).getAmount();
        if (bank < 1_000_000) return ItemStatistics.empty();

        double percent = (0.125 + PERCENT_PER_LEVEL.getForRarity(rarity) * level) * Math.floor(bank / 1_000_000);
        double max = 125 + MAX_PERCENT_PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.DAMAGE, Math.min(percent, max))
                .build();
    }
}
