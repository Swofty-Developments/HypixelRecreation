package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.penguin;

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

@PetAbilityRegistration(pet = PetHandler.PENGUIN, minimumRarity = Rarity.LEGENDARY, order = 2)
public final class SubzeroHeroAbility implements PetAbility {
    private static final RarityValue<Double> FISHING_SPEED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.75, 0.0, 0.0);

    @Override
    public String getName() {
        return "Subzero Hero";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String speed = decimalify(FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gain <b>+" + speed + " <stat:fishing_speed> <7>while",
                "<7>in the <b>Glacite Tunnels<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (player.getRegion() == null || player.getRegion().getType() != RegionType.GLACITE_TUNNELS)
            return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_SPEED, FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
