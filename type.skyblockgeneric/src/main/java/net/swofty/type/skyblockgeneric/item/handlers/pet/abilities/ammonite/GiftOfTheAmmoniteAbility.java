package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ammonite;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.AMMONITE, minimumRarity = Rarity.LEGENDARY, order = 2)
public final class GiftOfTheAmmoniteAbility implements PetAbility {
    private static final double FISHING_SPEED_PER_LEVEL = 0.005;
    private static final double SPEED_PER_LEVEL = 0.02;
    private static final double DEFENSE_PER_LEVEL = 0.02;

    @Override
    public String getName() {
        return "Gift of the Ammonite";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {

        return List.of(
                "<7>Each Mining and Fishing level grants",
                "<b>+" + decimalify(FISHING_SPEED_PER_LEVEL * level, 2) + " <stat:fishing_speed><7>,",
                "<f>+" + decimalify(SPEED_PER_LEVEL * level, 2) + " <stat:speed> <7>and",
                "<a>+" + decimalify(DEFENSE_PER_LEVEL * level, 2) + " <stat:defense><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        int skillLevels = player.getSkills().getCurrentLevel(SkillCategories.MINING)
                + player.getSkills().getCurrentLevel(SkillCategories.FISHING);

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_SPEED, FISHING_SPEED_PER_LEVEL * level * skillLevels)
                .withBase(ItemStatistic.SPEED, SPEED_PER_LEVEL * level * skillLevels)
                .withBase(ItemStatistic.DEFENSE, DEFENSE_PER_LEVEL * level * skillLevels)
                .build();
    }
}
