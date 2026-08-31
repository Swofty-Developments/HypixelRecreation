package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.flying_fish;

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

@PetAbilityRegistration(pet = PetHandler.FLYING_FISH, minimumRarity = Rarity.RARE, order = 0)
public final class QuickReelAbility implements PetAbility {
    private static final RarityValue<Double> FISHING_SPEED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.6, 0.75, 0.8, 0.8, 0.0);

    @Override
    public String getName() {
        return "Quick Reel";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Grants <b>+" + decimalify(value, 2) + " <stat:fishing_speed><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_SPEED, FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
