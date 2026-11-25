package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.crow;

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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.CROW, minimumRarity = Rarity.RARE)
public final class CamouflageAbility implements PetAbility {
    private static final int BASE = 5;
    private static final RarityValue<Double> PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.1, 0.15, 0.15, 0.0, 0.0);
    private static final long BUFF_DURATION_MS = 20_000L;
    private static final int DEFENSE_CAP = 500;

    private final ProcWindow window = new ProcWindow();

    @Override
    public String getName() {
        return "Camouflage";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = BASE + PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>After casting an ability, increase",
                "<7>your <a>Defense <7>by <a>+" + decimalify(value, 2) + " <7>for <b>20",
                "<b>seconds<7>.",
                "<8>Capped at " + DEFENSE_CAP + " Defense"
        );
    }

    @PetEventHandler
    public void onAbilityCast(PetEvent.AbilityCast event) {
        long now = System.currentTimeMillis();
        int active = window.active(now, BUFF_DURATION_MS);
        var sourcePet = event.pet();
        Rarity rarity = sourcePet.getAttributeHandler().getRarity();
        int level = sourcePet.getAttributeHandler().getPetData().getAsLevel(rarity);
        double perCast = BASE + PER_LEVEL.getForRarity(rarity) * level;
        if ((active + 1) * perCast > DEFENSE_CAP) return;

        window.record(now);
        ItemStatistics snapshot = ItemStatistics.builder()
                .withBase(ItemStatistic.DEFENSE, perCast)
                .build();
        long expiresAt = now + BUFF_DURATION_MS;
        event.player().getStatistics().boostStatistic(
                TemporaryConditionalStatistic.builder()
                        .withStatistics(player -> snapshot)
                        .withExpiry(player -> player.getPetData().isActive(sourcePet)
                                && System.currentTimeMillis() < expiresAt)
                        .build()
        );
    }

    private static final class ProcWindow {
        private final Deque<Long> window = new ArrayDeque<>();

        public void record(long now) {
            window.addLast(now);
        }

        public int active(long now, long durationMs) {
            while (!window.isEmpty() && window.peekFirst() + durationMs <= now)
                window.removeFirst();
            return window.size();
        }
    }
}

