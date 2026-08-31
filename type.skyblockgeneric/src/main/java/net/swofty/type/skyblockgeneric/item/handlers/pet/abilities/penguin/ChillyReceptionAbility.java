package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.penguin;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
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

@PetAbilityRegistration(pet = PetHandler.PENGUIN, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class ChillyReceptionAbility implements PetAbility {
    private static final int MAX_PLAYERS = 10;
    private static final double PLAYER_RANGE = 30;
    private static final RarityValue<Double> COLD_RESISTANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.01, 0.0, 0.0);

    @Override
    public String getName() {
        return "Chilly Reception";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String resistance = decimalify(COLD_RESISTANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Grants <b>+" + resistance + " <stat:cold_resistance> <7>for",
                "<7>each player within <a>30 <7>blocks, up to",
                "<a>10 <7>players."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        int playerCount = countNearbyPlayers(player);
        if (playerCount == 0) return ItemStatistics.empty();

        double resistance = COLD_RESISTANCE_PER_LEVEL.getForRarity(rarity) * level * playerCount;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.COLD_RESISTANCE, resistance)
                .build();
    }

    private static int countNearbyPlayers(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return 0;

        int count = 0;
        for (Entity entity : instance.getNearbyEntities(player.getPosition(), PLAYER_RANGE)) {
            if (count >= MAX_PLAYERS) break;
            if (!(entity instanceof SkyBlockPlayer)) continue;
            count++;
        }
        return count;
    }
}
