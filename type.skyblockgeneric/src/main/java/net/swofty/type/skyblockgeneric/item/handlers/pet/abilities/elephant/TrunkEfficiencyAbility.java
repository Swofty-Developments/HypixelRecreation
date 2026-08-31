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

@PetAbilityRegistration(pet = PetHandler.ELEPHANT, minimumRarity = Rarity.LEGENDARY)
public final class TrunkEfficiencyAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.5, 1.5, 0.0);

    @Override
    public String getName() {
        return "Trunk Efficiency";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>Grants <stat:farming_fortune:+" + decimalify(value, 1) + ">,",
                "<7>which increases your",
                "<7>chance for multiple drops."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FARMING_FORTUNE, value)
                .build();
    }
}
