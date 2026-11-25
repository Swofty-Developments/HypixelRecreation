package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.goblin;

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

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.GOBLIN, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class FetidThiefAbility implements PetAbility {
    private static final RarityValue<Double> MINING_SPREAD_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Fetid Thief";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = commaify(MINING_SPREAD_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Gain <e>+" + value + " <stat:mining_spread> <7>while in the",
                "<2>Mines of Divan<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (player.getRegion() == null || player.getRegion().getType() != RegionType.MINES_OF_DIVAN)
            return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.MINING_SPREAD, MINING_SPREAD_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
