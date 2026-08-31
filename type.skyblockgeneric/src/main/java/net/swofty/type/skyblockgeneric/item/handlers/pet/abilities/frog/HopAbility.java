package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.frog;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryConditionalStatistic;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.FROG, minimumRarity = Rarity.EPIC)
public final class HopAbility implements PetAbility {
    private static final long BUFF_DURATION_MILLIS = 20_000;
    private long effectGeneration;
    private static final RarityValue<Double> FORAGING_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.79, 0.79, 0.79, 0.0);

    @Override
    public String getName() {
        return "Hop";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = commaify(1 + FORAGING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Grants <6>" + value + " <stat:foraging_fortune> <7>for",
                "<e>20 <7>seconds every time you jump."
        );
    }

    @PetEventHandler
    public void onJump(PetEvent.Jump event) {
        var sourcePet = event.pet();
        Rarity rarity = sourcePet.getAttributeHandler().getRarity();
        int level = sourcePet.getAttributeHandler().getPetData().getAsLevel(rarity);
        ItemStatistics snapshot = ItemStatistics.builder()
                .withBase(ItemStatistic.FORAGING_FORTUNE,
                        1 + FORAGING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level)
                .build();
        long generation = ++effectGeneration;
        long expiresAt = System.currentTimeMillis() + BUFF_DURATION_MILLIS;

        event.player().getStatistics().boostStatistic(
                TemporaryConditionalStatistic.builder()
                        .withStatistics(player -> snapshot)
                        .withExpiry(player -> player.getPetData().isActive(sourcePet)
                                && effectGeneration == generation
                                && System.currentTimeMillis() < expiresAt)
                        .build()
        );
    }
}