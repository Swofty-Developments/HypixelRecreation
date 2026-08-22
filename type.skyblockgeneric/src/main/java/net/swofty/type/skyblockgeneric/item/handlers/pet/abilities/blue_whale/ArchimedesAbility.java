package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.blue_whale;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BLUE_WHALE, minimumRarity = Rarity.LEGENDARY)
public final class ArchimedesAbility implements PetAbility {
    @Override
    public String getName() {
        return "Archimedes";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double maxHealth = level * 0.2;

        return List.of(
                "<7>Gain <stat:health:+" + decimalify(maxHealth, 2) + "% Max><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double maxHealth = level * 0.2;

        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.HEALTH, maxHealth)
                .build();
    }
}
