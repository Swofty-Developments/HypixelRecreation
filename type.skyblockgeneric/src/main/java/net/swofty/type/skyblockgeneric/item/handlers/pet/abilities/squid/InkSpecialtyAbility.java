package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.squid;

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

@PetAbilityRegistration(pet = PetHandler.SQUID, minimumRarity = Rarity.RARE)
public final class InkSpecialtyAbility implements PetAbility {
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.3, 0.4, 0.4, 0.0, 0.0);
    private static final RarityValue<Double> STRENGTH_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.1, 0.2, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Ink Specialty";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double damage = DAMAGE_PER_LEVEL.getForRarity(rarity) * level;
        double strength = STRENGTH_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Buffs the <5>Ink Wand <7>by <a>" + decimalify(damage, 1) + " <stat:damage>",
                "<7>and <a>" + decimalify(strength, 1) + " <stat:strength><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (!isHoldingInkWand(player)) return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, DAMAGE_PER_LEVEL.getForRarity(rarity) * level)
                .withBase(ItemStatistic.STRENGTH, STRENGTH_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }

    private static boolean isHoldingInkWand(SkyBlockPlayer player) {
        if (player.getItemInMainHand().isAir()) return false;
        return new SkyBlockItem(player.getItemInMainHand()).getItemType() == ItemType.INK_WAND;
    }
}
