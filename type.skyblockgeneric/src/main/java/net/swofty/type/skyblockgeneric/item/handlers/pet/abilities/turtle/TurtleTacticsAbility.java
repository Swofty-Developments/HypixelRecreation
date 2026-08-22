package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.turtle;

import net.minestom.server.coordinate.Point;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TURTLE, minimumRarity = Rarity.EPIC, order = 0)
public final class TurtleTacticsAbility implements PetAbility {
    private static final double DEFENSE_BASE = 3.0;
    private static final RarityValue<Double> DEFENSE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.27, 0.27, 0.0, 0.0);
    private static final double STANDING_STILL_BONUS = 10.0;
    private static final long STANDING_STILL_MILLIS = 1_000;
    private static final double MOVE_TOLERANCE = 0.1;

    private Point lastPosition;
    private long lastMovedAt = System.currentTimeMillis();

    @Override
    public String getName() {
        return "Turtle Tactics";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = DEFENSE_BASE + DEFENSE_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Gain <a>+" + decimalify(value, 1) + "% <stat:defense> <7>and an",
                "additional <a>+10% <stat:defense> <7>when",
                "standing still."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double percent = DEFENSE_BASE + DEFENSE_PER_LEVEL.getForRarity(rarity) * level;

        if (isStandingStill(player)) percent += STANDING_STILL_BONUS;

        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.DEFENSE, percent)
                .build();
    }

    private boolean isStandingStill(SkyBlockPlayer player) {
        Point position = player.getPosition();
        long now = System.currentTimeMillis();

        if (lastPosition != null && position.distance(lastPosition) > MOVE_TOLERANCE) {
            lastMovedAt = now;
        }
        lastPosition = position;
        return now - lastMovedAt >= STANDING_STILL_MILLIS;
    }
}
