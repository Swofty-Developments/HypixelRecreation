package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ankylosaurus;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ANKYLOSAURUS, minimumRarity = Rarity.LEGENDARY, order = 0)
public final class ArmoredTankAbility implements PetAbility {
    private static final double PER_LEVEL = 0.5;

    @Override
    public String getName() {
        return "Armored Tank";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Gain <a>" + decimalify(value, 1) + "% <7>of your <a><stat:defense> <7>as <c><stat:strength><7>.",
                "<8>(Max +500)"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double defense = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.DEFENSE);
        double strength = Math.min(PER_LEVEL * level / 100 * defense, 500);

        return ItemStatistics.builder()
                .withBase(ItemStatistic.STRENGTH, strength)
                .build();
    }
}
