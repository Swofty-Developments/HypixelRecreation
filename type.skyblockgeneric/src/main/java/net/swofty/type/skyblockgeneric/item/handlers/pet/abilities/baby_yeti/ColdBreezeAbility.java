package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.baby_yeti;

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

@PetAbilityRegistration(pet = PetHandler.BABY_YETI, minimumRarity = Rarity.RARE, order = 0)
public final class ColdBreezeAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.1, 0.2, 0.2, 0.2, 0.0);

    @Override
    public String getName() {
        return "Cold Breeze";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Increases <c>Combat Stats <7>and <b>Fishing Stats <7>by",
                "<a>" + decimalify(value, 2) + "% <7>while on <c>Jerry's Workshop<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (!isOnJerrysWorkshop(player)) return ItemStatistics.empty();

        double percent = PER_LEVEL.getForRarity(rarity) * level;

        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic stat : ItemStatistic.COMBAT_STATS) {
            builder.withAdditivePercentage(stat, percent);
        }
        for (ItemStatistic stat : ItemStatistic.FISHING_STATS) {
            builder.withAdditivePercentage(stat, percent);
        }
        return builder.build();
    }

    private static boolean isOnJerrysWorkshop(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        return region != null && region.getType() == RegionType.JERRYS_WORKSHOP;
    }
}
