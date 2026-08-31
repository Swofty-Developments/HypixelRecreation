package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ender_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ENDER_DRAGON, minimumRarity = Rarity.LEGENDARY)
public final class SuperiorAbility implements PetAbility {
    private static final double PER_LEVEL = 0.1;

    @Override
    public String getName() {
        return "Superior";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return Arrays.asList(
                "<7>Increases all <c>Combat <7>stats and ",
                "<stat:magic_find> <7>by <a>" + decimalify(value, 1) + "%<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double percent = PER_LEVEL * level;

        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic stat : ItemStatistic.COMBAT_STATS) {
            builder.withAdditivePercentage(stat, percent);
        }
        builder.withAdditivePercentage(ItemStatistic.MAGIC_FIND, percent);

        return builder.build();
    }
}
