package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.bal;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BAL, minimumRarity = Rarity.EPIC, order = 0)
public final class FurnaceAbility implements PetAbility {
    private static final RarityValue<Double> PRISTINE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.02, 0.03, 0.0, 0.0);

    @Override
    public String getName() {
        return "Furnace";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PRISTINE_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Grants <5>+" + decimalify(value, 2) + " <stat:pristine> <7>while in the",
                "<c>Magma Fields<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (player.getRegion() == null || player.getRegion().getType() != RegionType.MAGMA_FIELDS)
            return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.PRISTINE, PRISTINE_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
