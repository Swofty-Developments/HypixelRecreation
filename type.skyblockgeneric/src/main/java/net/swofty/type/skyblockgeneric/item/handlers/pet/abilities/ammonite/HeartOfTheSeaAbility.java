package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ammonite;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.AMMONITE, minimumRarity = Rarity.LEGENDARY, order = 0)
public final class HeartOfTheSeaAbility implements PetAbility {
    private static final double PER_LEVEL = 0.01;

    @Override
    public String getName() {
        return "Heart of the Sea";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Grants <3>+" + decimalify(value, 2) + " <stat:sea_creature_chance> <7>to your pet for each",
                "<5>Heart of the Mountain <7>level."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        int hotmTier = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class)
                .getValue().getTier();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.SEA_CREATURE_CHANCE, PER_LEVEL * level * hotmTier)
                .build();
    }
}
