package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.monkey;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MONKEY, minimumRarity = Rarity.LEGENDARY)
public final class EvolvedAxesAbility implements PetAbility {
    private static final RarityValue<Double> SWEEP_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.0);

    @Override
    public String getName() {
        return "Evolved Axes";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(SWEEP_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <2>" + value + " <stat:sweep> <7>while in <a>The Park"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_THE_PARK)
            return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.SWEEP, SWEEP_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
