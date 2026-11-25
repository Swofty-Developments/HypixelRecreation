package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.wolf;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.WOLF, minimumRarity = Rarity.RARE)
public final class PackLeaderAbility implements PetAbility {
    private static final int MAX_WOLVES = 10;
    private static final double WOLF_RANGE = 20;
    private static final RarityValue<Double> CRIT_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.1, 0.15, 0.15, 0.0, 0.0);

    @Override
    public String getName() {
        return "Pack Leader";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = CRIT_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Gain <a>" + decimalify(value, 1) + " <stat:crit_damage> <7>for",
                "<7>every nearby wolf monster. <8>Max 10 wolves"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        int wolfCount = countNearbyWolves(player);
        if (wolfCount == 0) return ItemStatistics.empty();

        double critDamage = CRIT_PER_LEVEL.getForRarity(rarity) * level * wolfCount;

        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.CRITICAL_DAMAGE, critDamage)
                .build();
    }

    private static int countNearbyWolves(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return 0;

        int count = 0;
        for (Entity entity : instance.getNearbyEntities(player.getPosition(), WOLF_RANGE)) {
            if (count >= MAX_WOLVES) break;
            if (!(entity instanceof SkyBlockMob mob)) continue;
            if (mob.getEntityType() != EntityType.WOLF) continue;
            count++;
        }
        return count;
    }
}
