package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.dolphin;

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

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.DOLPHIN, minimumRarity = Rarity.COMMON)
public final class PodTacticsAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL = new RarityValue<>(0.06, 0.08, 0.08, 0.1, 0.1, 0.0, 0.0);

    @Override
    public String getName() {
        return "Pod Tactics";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>Grants <stat:fishing_speed:+" + decimalify(value, 2) + ">,",
                "<7>for each player within <a>30",
                "<7>blocks, up to <a>5 <7>players."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double perPlayer = PER_LEVEL.getForRarity(rarity) * level;

        Instance instance = player.getInstance();
        if (instance == null) return ItemStatistics.empty();

        int count = 0;
        for (Entity entity : instance.getNearbyEntities(player.getPosition(), 30)) {
            if (count >= 5) break;
            if (!(entity instanceof SkyBlockPlayer) || entity == player) continue;
            count++;
        }

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_SPEED, perPlayer * count)
                .build();
    }
}
