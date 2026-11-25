package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.chicken;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.CHICKEN, minimumRarity = Rarity.COMMON)
public final class FreeRangeAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL = new RarityValue<>(0.5, 0.75, 1.0, 1.0, 1.0, 1.0, 0.0);

    @Override
    public String getName() {
        return "Free Range";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double ff = PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>Grants <6>+" + decimalify(ff, 1) + "<stat:farming_fortune><7>while",
                "<7>on <b>Public Islands<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (!isPublicIsland(player)) return ItemStatistics.empty();
        double ff = PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FARMING_FORTUNE, ff)
                .build();
    }

    private boolean isPublicIsland(SkyBlockPlayer player) {
        return player.getRegion() != null && player.getRegion().getType() != RegionType.PRIVATE_ISLAND;
    }

}
