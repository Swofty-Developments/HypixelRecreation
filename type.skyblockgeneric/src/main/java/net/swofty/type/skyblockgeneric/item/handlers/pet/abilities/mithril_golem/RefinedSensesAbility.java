package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mithril_golem;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MITHRIL_GOLEM, minimumRarity = Rarity.MYTHIC)
public final class RefinedSensesAbility implements PetAbility {
    private static final RarityValue<Double> MAGIC_FIND_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0);

    @Override
    public String getName() {
        return "Refined Senses";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(MAGIC_FIND_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <b>+" + percent + "% <stat:magic_find> <7>while on",
                "<b>Mining Islands<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (!isOnMiningIsland()) return ItemStatistics.empty();

        double percent = MAGIC_FIND_PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.MAGIC_FIND, percent)
                .build();
    }

    private boolean isOnMiningIsland() {
        ServerType type = HypixelConst.getTypeLoader().getType();
        return type == ServerType.SKYBLOCK_GOLD_MINE
                || type == ServerType.SKYBLOCK_DEEP_CAVERNS
                || type == ServerType.SKYBLOCK_DWARVEN_MINES;
    }
}
