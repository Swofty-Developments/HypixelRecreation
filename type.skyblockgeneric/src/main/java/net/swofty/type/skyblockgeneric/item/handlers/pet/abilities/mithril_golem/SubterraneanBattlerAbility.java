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

@PetAbilityRegistration(pet = PetHandler.MITHRIL_GOLEM, minimumRarity = Rarity.RARE)
public final class SubterraneanBattlerAbility implements PetAbility {
    private static final RarityValue<Double> COMBAT_STATS_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.1, 0.2, 0.2, 0.2, 0.0);

    @Override
    public String getName() {
        return "Subterranean Battler";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(COMBAT_STATS_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Increases all <c>Combat Stats <7>by <a>+" + percent + "%",
                "<7>on <b>Mining Islands<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (!isOnMiningIsland()) return ItemStatistics.empty();

        double percent = COMBAT_STATS_PER_LEVEL.getForRarity(rarity) * level;

        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic stat : ItemStatistic.COMBAT_STATS) {
            builder.withAdditivePercentage(stat, percent);
        }
        return builder.build();
    }

    private boolean isOnMiningIsland() {
        ServerType type = HypixelConst.getTypeLoader().getType();
        return type == ServerType.SKYBLOCK_GOLD_MINE
                || type == ServerType.SKYBLOCK_DEEP_CAVERNS
                || type == ServerType.SKYBLOCK_DWARVEN_MINES;
    }
}
