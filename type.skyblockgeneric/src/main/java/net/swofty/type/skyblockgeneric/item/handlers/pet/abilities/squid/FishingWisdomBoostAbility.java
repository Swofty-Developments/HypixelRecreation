package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.squid;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SQUID, minimumRarity = Rarity.LEGENDARY)
public final class FishingWisdomBoostAbility implements PetAbility {
    private static final double WISDOM_PER_LEVEL = 0.3;

    @Override
    public String getName() {
        return "Fishing Wisdom Boost";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String wisdom = decimalify(WISDOM_PER_LEVEL * level, 2);

        return List.of(
                "<7>Grants <3>+" + wisdom + " <stat:fishing_wisdom><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_WISDOM, WISDOM_PER_LEVEL * level)
                .build();
    }
}
