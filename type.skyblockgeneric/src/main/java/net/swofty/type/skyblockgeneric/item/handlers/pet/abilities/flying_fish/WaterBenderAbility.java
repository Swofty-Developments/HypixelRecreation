package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.flying_fish;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.utility.BlockProps;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.FLYING_FISH, minimumRarity = Rarity.RARE, maximumRarity = Rarity.LEGENDARY, order = 1)
public final class WaterBenderAbility implements PetAbility {
    private static final int MAX_RADIUS = 3;
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.8, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Water Bender";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Gives <a>" + decimalify(value, 2) + " <stat:strength> <7>and",
                "<a>" + decimalify(value, 2) + " <stat:defense> <7>when near water."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (!isNearWater(player)) return ItemStatistics.empty();

        double value = PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.STRENGTH, value)
                .withBase(ItemStatistic.DEFENSE, value)
                .build();
    }

    private static boolean isNearWater(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return false;
        Pos pos = player.getPosition();
        for (int x = pos.blockX() - MAX_RADIUS; x <= pos.blockX() + MAX_RADIUS; x++) {
            for (int y = pos.blockY() - MAX_RADIUS; y <= pos.blockY() + MAX_RADIUS; y++) {
                for (int z = pos.blockZ() - MAX_RADIUS; z <= pos.blockZ() + MAX_RADIUS; z++) {
                    if (BlockProps.isWater(instance.getBlock(x, y, z))) return true;
                }
            }
        }
        return false;
    }
}
