package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.horse;

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

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.HORSE, minimumRarity = Rarity.RARE)
public final class StampedeAbility implements PetAbility {
    private static final int MAX_STACKS = 20;
    private static final long DURATION_MILLIS = 5_000L;
    private static final RarityValue<Double> SPEED_BASE = new RarityValue<>(0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0);
    private static final RarityValue<Double> SPEED_PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.2, 0.2, 0.2, 0.0, 0.0);
    private static final RarityValue<Double> STRENGTH_BASE = new RarityValue<>(0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0);
    private static final RarityValue<Double> STRENGTH_PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.05, 0.05, 0.05, 0.0, 0.0);

    private int stacks;
    private long lastProc;
    private long effectGeneration;

    @Override
    public String getName() {
        return "Stampede";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String speed = decimalify(SPEED_BASE.getForRarity(rarity) + SPEED_PER_LEVEL.getForRarity(rarity) * level, 1);
        String strength = decimalify(STRENGTH_BASE.getForRarity(rarity) + STRENGTH_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Mob kills grant a stack of <f>+" + speed + " <stat:speed>",
                "<7>and <c>+" + strength + " <stat:strength> <7>for <a>5s<7>.",
                "<8>(Max " + MAX_STACKS + " stacks)"
        );
    }

    @PetEventHandler
    public void onKilledMob(PetEvent.KilledMob event) {
        long now = System.currentTimeMillis();
        var sourcePet = event.pet();
        Rarity rarity = sourcePet.getAttributeHandler().getRarity();
        int level = sourcePet.getAttributeHandler().getPetData().getAsLevel(rarity);
        if (now - lastProc > DURATION_MILLIS) stacks = 0;

        stacks = Math.min(stacks + 1, MAX_STACKS);
        lastProc = now;
        double speed = (SPEED_BASE.getForRarity(rarity) + SPEED_PER_LEVEL.getForRarity(rarity) * level) * stacks;
        double strength = (STRENGTH_BASE.getForRarity(rarity) + STRENGTH_PER_LEVEL.getForRarity(rarity) * level) * stacks;
        ItemStatistics snapshot = ItemStatistics.builder()
                .withBase(ItemStatistic.SPEED, speed)
                .withBase(ItemStatistic.STRENGTH, strength)
                .build();
        long generation = ++effectGeneration;
        long expiresAt = now + DURATION_MILLIS;

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
