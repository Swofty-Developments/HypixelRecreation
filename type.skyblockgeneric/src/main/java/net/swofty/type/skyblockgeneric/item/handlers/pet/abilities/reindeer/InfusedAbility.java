package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.reindeer;

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

@PetAbilityRegistration(pet = PetHandler.REINDEER, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class InfusedAbility implements PetAbility {
    private static final double TREASURE_CHANCE_BASE = 5;
    private static final RarityValue<Double> FISHING_SPEED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.75, 0.0, 0.0);

    @Override
    public String getName() {
        return "Infused";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String speed = decimalify(FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gives <b>+" + speed + " <stat:fishing_speed> <7>and",
                "<6>+" + (int) TREASURE_CHANCE_BASE + " <stat:treasure_chance> <7>while on",
                "<c>Jerry's Workshop<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (player.getRegion() == null || player.getRegion().getType() != RegionType.JERRYS_WORKSHOP)
            return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_SPEED, FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level)
                .withBase(ItemStatistic.TREASURE_CHANCE, TREASURE_CHANCE_BASE)
                .build();
    }
}
