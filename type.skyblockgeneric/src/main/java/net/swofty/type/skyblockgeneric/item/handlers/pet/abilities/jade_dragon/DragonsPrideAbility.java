package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jade_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.JADE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 2)
public final class DragonsPrideAbility implements PetAbility {
    private static final double FORAGING_FORTUNE_PER_SWEEP = 1.0 / 5.0;

    @Override
    public String getName() {
        return "Dragon's Pride";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Grants <6>+1 <stat:foraging_fortune> <7>per",
                "<2>5 <stat:sweep><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double sweep = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.SWEEP);
        double foragingFortune = sweep * FORAGING_FORTUNE_PER_SWEEP;
        if (foragingFortune <= 0) return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FORAGING_FORTUNE, foragingFortune)
                .build();
    }
}
