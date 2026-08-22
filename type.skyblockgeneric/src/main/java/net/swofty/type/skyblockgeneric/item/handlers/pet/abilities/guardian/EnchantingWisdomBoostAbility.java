package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.guardian;

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

@PetAbilityRegistration(pet = PetHandler.GUARDIAN, minimumRarity = Rarity.RARE)
public final class EnchantingWisdomBoostAbility implements PetAbility {
    private static final RarityValue<Double> WISDOM_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.25, 0.3, 0.3, 0.3, 0.0);

    @Override
    public String getName() {
        return "Enchanting Wisdom Boost";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String wisdom = decimalify(WISDOM_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Grants <3>+" + wisdom + " <stat:enchanting_wisdom><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.ENCHANTING_WISDOM, WISDOM_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
