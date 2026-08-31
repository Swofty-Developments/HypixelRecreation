package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.skeleton;

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

@PetAbilityRegistration(pet = PetHandler.SKELETON, minimumRarity = Rarity.RARE)
public final class ComboAbility implements PetAbility {
    private static final RarityValue<Double> STACKS_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.15, 0.17, 0.2, 0.0, 0.0);
    private static final long STACK_DURATION_MS = 8_000L;

    private int stacks;
    private long lastProc;
    private long effectGeneration;

    @Override
    public String getName() {
        return "Combo";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String stacks = decimalify(STACKS_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gain a combo stack for every bow hit",
                "<7>granting +<a>3 <stat:strength><7>. Max <a>" + stacks + " stacks<7>,",
                "<7>stacks disappear after <a>8 <7>seconds."
        );
    }

    @PetEventHandler
    public void onRangedDamageDealt(PetEvent.RangedDamageDealt event) {
        var sourcePet = event.pet();
        Rarity rarity = sourcePet.getAttributeHandler().getRarity();
        int level = sourcePet.getAttributeHandler().getPetData().getAsLevel(rarity);
        int maxStacks = maxStacks(rarity, level);
        if (maxStacks <= 0) {
            stacks = 0;
            return;
        }

        long now = System.currentTimeMillis();
        if (stacks > 0 && now - lastProc > STACK_DURATION_MS) stacks = 0;
        stacks = Math.min(stacks + 1, maxStacks);
        lastProc = now;
        ItemStatistics snapshot = ItemStatistics.builder()
                .withBase(ItemStatistic.STRENGTH, stacks * 3D)
                .build();
        long generation = ++effectGeneration;
        long expiresAt = now + STACK_DURATION_MS;

        event.player().getStatistics().boostStatistic(
                TemporaryConditionalStatistic.builder()
                        .withStatistics(player -> snapshot)
                        .withExpiry(player -> player.getPetData().isActive(sourcePet)
                                && effectGeneration == generation
                                && System.currentTimeMillis() < expiresAt)
                        .build()
        );
    }

    private static int maxStacks(Rarity rarity, int level) {
        return Math.max(0, (int) (STACKS_PER_LEVEL.getForRarity(rarity) * level));
    }
}
