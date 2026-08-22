package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.bee;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BEE, minimumRarity = Rarity.MYTHIC)
public final class PoweredByPollenAbility implements PetAbility {
    private static final double PER_LEVEL = 1.6;

    @Override
    public String getName() {
        return "Powered by Pollen";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double fortune = PER_LEVEL * level;

        return List.of(
                "<7>Grants <6>+" + decimalify(fortune, 1) + "☘ Sunflower<7>,",
                "<6>Moonflower<7>, and <6>Wild Rose Fortune",
                "<7>while in <a>The Garden<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (player.getRegion() == null || player.getRegion().getType() != RegionType.THE_GARDEN)
            return ItemStatistics.empty();

        double fortune = PER_LEVEL * level;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.SUNFLOWER_FORTUNE, fortune)
                .withBase(ItemStatistic.MOONFLOWER_FORTUNE, fortune)
                .withBase(ItemStatistic.WILD_ROSE_FORTUNE, fortune)
                .build();
    }
}
