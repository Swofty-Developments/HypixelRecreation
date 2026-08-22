package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.hermit_crab;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.HERMIT_CRAB, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "Treasure is not defined; awaits dispatch(PetEvent.FishCaught) in FishingLootResolver")
public final class ComfortZoneAbility implements PetAbility {
    private static final long DURATION_MILLIS = 30_000L;
    private static final RarityValue<Double> FISHING_SPEED_PER_LEVEL =
            new RarityValue<>(0.2, 0.3, 0.3, 0.4, 0.4, 0.4, 0.0);

    private long buffUntil;

    @Override
    public String getName() {
        return "Comfort Zone";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String fishingSpeed = decimalify(FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <b>+" + fishingSpeed + " <stat:fishing_speed> <7>for",
                "<a>30s <7>upon catching <6>Treasure<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (buffUntil <= System.currentTimeMillis()) return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_SPEED, FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {
        if (!(event.payload() instanceof CatchPayload.Item item) || !item.fromTreasure()) return;
        buffUntil = System.currentTimeMillis() + DURATION_MILLIS;
    }
}
