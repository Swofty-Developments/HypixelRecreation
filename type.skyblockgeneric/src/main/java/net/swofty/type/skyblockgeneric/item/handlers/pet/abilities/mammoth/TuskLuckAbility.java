package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mammoth;

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

@PetAbilityRegistration(pet = PetHandler.MAMMOTH, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class TuskLuckAbility implements PetAbility {
    private static final double MINING_FORTUNE_PER_MAGIC_FIND = 100;
    private static final RarityValue<Double> MAGIC_FIND_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.005, 0.0, 0.0);

    @Override
    public String getName() {
        return "Tusk Luck";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(MAGIC_FIND_PER_LEVEL.getForRarity(rarity) * level, 3);

        return List.of(
                "<7>Gain <b>+" + value + " <stat:magic_find> <7>for every",
                "<7><6>100 <stat:mining_fortune><7>, doubled in the",
                "<b>Glacite Tunnels <7>and <b>Glacite",
                "<b>Mineshafts<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double miningFortune = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.MINING_FORTUNE);
        double magicFind = (miningFortune / MINING_FORTUNE_PER_MAGIC_FIND) * MAGIC_FIND_PER_LEVEL.getForRarity(rarity) * level;
        if (magicFind <= 0) return ItemStatistics.empty();

        double multiplier = isInGlacite(player) ? 2 : 1;
        return ItemStatistics.builder()
                .withBase(ItemStatistic.MAGIC_FIND, magicFind * multiplier)
                .build();
    }

    private boolean isInGlacite(SkyBlockPlayer player) {
        return player.getRegion() != null && player.getRegion().getType() == RegionType.GLACITE_TUNNELS;
    }
}
