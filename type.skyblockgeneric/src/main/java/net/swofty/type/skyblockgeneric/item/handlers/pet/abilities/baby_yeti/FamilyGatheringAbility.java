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

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BABY_YETI, minimumRarity = Rarity.MYTHIC, order = 0)
public final class FamilyGatheringAbility implements PetAbility {
    private static final double PER_LEVEL = 0.1;

    @Override
    public String getName() {
        return "Family Gathering";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Grants <d>" + decimalify(value, 2) + " <stat:tracking> <7>while on",
                "<c>Jerry's Workshop<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (!isOnJerrysWorkshop(player)) return ItemStatistics.empty();

        double value = PER_LEVEL * level;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.TRACKING, value)
                .build();
    }

    private static boolean isOnJerrysWorkshop(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        return region != null && region.getType() == RegionType.JERRYS_WORKSHOP;
    }
}
