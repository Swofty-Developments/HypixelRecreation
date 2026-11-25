package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rose_dragon;

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

@PetAbilityRegistration(pet = PetHandler.ROSE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 0)
public final class GardenPowerAbility implements PetAbility {
    private static final double BASE_FORTUNE = 1.5;
    private static final double FORTUNE_PER_LEVEL = 0.015;

    @Override
    public String getName() {
        return "Garden Power";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String fortune = decimalify(BASE_FORTUNE + FORTUNE_PER_LEVEL * level, 2);

        return List.of(
                "<7>Grants <6>+" + fortune + " <stat:farming_fortune> <7>per",
                "<a>Farming <7>level."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        int farmingLevel = player.getSkills().getCurrentLevel(SkillCategories.FARMING);

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FARMING_FORTUNE, (BASE_FORTUNE + FORTUNE_PER_LEVEL * level) * farmingLevel)
                .build();
    }
}
