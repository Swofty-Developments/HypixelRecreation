package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.giraffe;

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

@PetAbilityRegistration(pet = PetHandler.GIRAFFE, minimumRarity = Rarity.COMMON)
public final class GoodHeartAbility implements PetAbility {
    private static final RarityValue<Double> HEALTH_REGEN_BASE = new RarityValue<>(1.0, 35.0, 35.0, 50.0, 50.0, 0.0, 0.0);
    private static final RarityValue<Double> HEALTH_REGEN_PER_LEVEL = new RarityValue<>(0.49, 0.35, 0.35, 0.5, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Good Heart";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = commaify(HEALTH_REGEN_BASE.getForRarity(rarity) + HEALTH_REGEN_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Grants <c>+" + value + " <stat:health_regeneration><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double value = HEALTH_REGEN_BASE.getForRarity(rarity) + HEALTH_REGEN_PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH_REGENERATION, value)
                .build();
    }
}
