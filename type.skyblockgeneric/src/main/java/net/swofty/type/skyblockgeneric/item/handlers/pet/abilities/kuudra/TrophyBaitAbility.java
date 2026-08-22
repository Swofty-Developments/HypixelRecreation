package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.kuudra;

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

@PetAbilityRegistration(pet = PetHandler.KUUDRA, minimumRarity = Rarity.UNCOMMON)
public final class TrophyBaitAbility implements PetAbility {
    private static final RarityValue<Double> TROPHY_CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.15, 0.15, 0.2, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Trophy Bait";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(TROPHY_CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Grants <6>+" + chance + " <stat:trophy_fish_chance> <7>while",
                "<7>on the <c>Crimson Isle<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (player.getRegion() == null || player.getRegion().getType() != RegionType.CRIMSON_ISLE) {
            return ItemStatistics.empty();
        }

        return ItemStatistics.builder()
                .withBase(ItemStatistic.TROPHY_FISH_CHANCE, TROPHY_CHANCE_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
