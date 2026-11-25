package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.slug;

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

@PetAbilityRegistration(pet = PetHandler.SLUG, minimumRarity = Rarity.EPIC, order = 1)
public final class PestFriendsAbility implements PetAbility {
    private static final RarityValue<Double> PEST_CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.4, 0.4, 0.0, 0.0);

    @Override
    public String getName() {
        return "Pest Friends";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String pestChance = decimalify(PEST_CHANCE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <2>+" + pestChance + " <stat:bonus_pest_chance><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {

        return ItemStatistics.builder()
                .withBase(ItemStatistic.BONUS_PEST_CHANCE, PEST_CHANCE_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
