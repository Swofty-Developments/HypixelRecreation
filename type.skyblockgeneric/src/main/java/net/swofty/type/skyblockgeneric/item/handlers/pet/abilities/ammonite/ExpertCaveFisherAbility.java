package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ammonite;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.AMMONITE, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class ExpertCaveFisherAbility implements PetAbility {
    private static final double PER_LEVEL = 0.005;

    @Override
    public String getName() {
        return "Expert Cave Fisher";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Grants <9>+" + decimalify(value, 2) + " <stat:double_hook_chance> <7>for each",
                "<5>Heart of the Mountain <7>level",
                "<7>while in the <5>Crystal Hollows<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (player.getRegion() == null || player.getRegion().getType() != RegionType.CRYSTAL_HOLLOWS)
            return ItemStatistics.empty();

        int hotmTier = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class)
                .getValue().getTier();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.DOUBLE_HOOK_CHANCE, PER_LEVEL * level * hotmTier)
                .build();
    }
}
