package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.golden_dragon;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.GOLDEN_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class ShiningScalesAbility implements PetAbility {
    private static final double STRENGTH_PER_DIGIT = 100 / 9.0;
    private static final double MAGIC_FIND_PER_DIGIT = 20 / 9.0;
    private static final int MAX_COLLECTION = 100_000_000;

    @Override
    public String getName() {
        return "Shining Scales";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Grants <c>+11.1 <stat:strength> <7>and <b>+2.2 <stat:magic_find>",
                "<7>to your pet for each digit in your <6>Gold Collection<7>.",
                "<8>(Max 100M collection)<7>"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        int gold = player.getCollection().get(ItemType.GOLD_INGOT);
        if (gold <= 0) return ItemStatistics.empty();

        int digits = String.valueOf(Math.min(gold, MAX_COLLECTION)).length();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.STRENGTH, STRENGTH_PER_DIGIT * digits)
                .withBase(ItemStatistic.MAGIC_FIND, MAGIC_FIND_PER_DIGIT * digits)
                .build();
    }
}
