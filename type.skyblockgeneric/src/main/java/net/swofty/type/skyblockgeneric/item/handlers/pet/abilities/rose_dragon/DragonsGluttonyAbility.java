package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rose_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ROSE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 2)
public final class DragonsGluttonyAbility implements PetAbility {
    private static final double OVERBLOOM_BASE = 0.05;
    private static final double OVERBLOOM_PER_LEVEL = 0.0005;

    @Override
    public String getName() {
        return "Dragon's Gluttony";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String overbloom = decimalify(OVERBLOOM_BASE + OVERBLOOM_PER_LEVEL * level, 2);

        return List.of(
                "<7>Grants <e>+" + overbloom + " <stat:overbloom><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.OVERBLOOM, OVERBLOOM_BASE + OVERBLOOM_PER_LEVEL * level)
                .build();
    }
}
