package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.blaze;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BLAZE, minimumRarity = Rarity.COMMON, order = 0)
public final class NetherEmbodimentAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.05, 0.075, 0.075, 0.1, 0.1, 0.0, 0.0);

    @Override
    public String getName() {
        return "Nether Embodiment";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Increases <c>Combat <7>and <d>Miscellaneous",
                "<7>stats by <a>" + decimalify(value, 2) + "% <7>while on the <c>Crimson",
                "<c>Isle<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        SkyBlockRegion region = player.getRegion();
        if (region == null || region.getType() != RegionType.CRIMSON_ISLE) return ItemStatistics.empty();

        double percent = PER_LEVEL.getForRarity(rarity) * level;

        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic stat : ItemStatistic.COMBAT_STATS) {
            builder.withAdditivePercentage(stat, percent);
        }
        for (ItemStatistic stat : ItemStatistic.MISC_STATS) {
            builder.withAdditivePercentage(stat, percent);
        }
        return builder.build();
    }
}
