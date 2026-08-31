package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jade_dragon;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.JADE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class JadeScaleAbility implements PetAbility {
    private static final int FORAGING_FORTUNE_PER_DIGIT = 15;
    private static final int SWEEP_PER_DIGIT = 4;
    private static final int MAX_COLLECTION = 10_000_000;

    @Override
    public String getName() {
        return "Jade Scale";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Grants <6>+" + FORAGING_FORTUNE_PER_DIGIT + " <stat:foraging_fortune>",
                "<7>and <2>+" + SWEEP_PER_DIGIT + " <stat:sweep> <7>for every digit in your",
                "<a>Mangrove Collection<7>.",
                "<8>(Max 10M collection)"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        int mangrove = player.getCollection().get(ItemType.MANGROVE_LOG);
        if (mangrove <= 0) return ItemStatistics.empty();

        int digits = String.valueOf(Math.min(mangrove, MAX_COLLECTION)).length();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FORAGING_FORTUNE, (double) FORAGING_FORTUNE_PER_DIGIT * digits)
                .withBase(ItemStatistic.SWEEP, (double) SWEEP_PER_DIGIT * digits)
                .build();
    }
}
