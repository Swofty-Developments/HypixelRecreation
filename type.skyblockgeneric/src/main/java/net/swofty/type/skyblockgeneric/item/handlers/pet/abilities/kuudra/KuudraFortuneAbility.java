package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.kuudra;

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

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.KUUDRA, minimumRarity = Rarity.EPIC)
public final class KuudraFortuneAbility implements PetAbility {
    private static final RarityValue<Double> MINING_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Kuudra Fortune";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String fortune = commaify(MINING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Gain <6>+" + fortune + " <stat:mining_fortune>",
                "<7>while on the Crimson Isle."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (player.getRegion() == null || player.getRegion().getType() != RegionType.CRIMSON_ISLE) {
            return ItemStatistics.empty();
        }

        return ItemStatistics.builder()
                .withBase(ItemStatistic.MINING_FORTUNE, MINING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
