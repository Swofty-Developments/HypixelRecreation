package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.pigman;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.PIGMAN, minimumRarity = Rarity.RARE)
public final class PorkMasterAbility implements PetAbility {
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.3, 0.4, 0.4, 0.0, 0.0);
    private static final RarityValue<Double> STRENGTH_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.15, 0.25, 0.25, 0.0, 0.0);

    @Override
    public String getName() {
        return "Pork Master";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String damage = decimalify(DAMAGE_PER_LEVEL.getForRarity(rarity) * level, 1);
        String strength = decimalify(STRENGTH_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Buffs the <6>Pigman Sword <7>by <c>+" + damage,
                "<stat:damage> <7>and <c>+" + strength + " <stat:strength>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (player.getItemInMainHand().isAir() || new SkyBlockItem(player.getItemInMainHand()).getItemType() != ItemType.PIGMAN_SWORD)
            return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, DAMAGE_PER_LEVEL.getForRarity(rarity) * level)
                .withBase(ItemStatistic.STRENGTH, STRENGTH_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
