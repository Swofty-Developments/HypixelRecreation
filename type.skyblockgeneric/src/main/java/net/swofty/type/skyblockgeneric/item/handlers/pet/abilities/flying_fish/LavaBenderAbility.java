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

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.FLYING_FISH, minimumRarity = Rarity.MYTHIC, order = 3)
public final class LavaBenderAbility implements PetAbility {
    private static final int MAX_RADIUS = 3;
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);

    @Override
    public String getName() {
        return "Lava Bender";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Gives <a>" + commaify(value) + " <stat:strength> <7>and",
                "<7><a>" + commaify(value) + " <stat:defense> <7>when near lava."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (!isNearLava(player)) return ItemStatistics.empty();

        double value = PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.STRENGTH, value)
                .withBase(ItemStatistic.DEFENSE, value)
                .build();
    }

    private static boolean isNearLava(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return false;
        Pos pos = player.getPosition();
        for (int x = pos.blockX() - MAX_RADIUS; x <= pos.blockX() + MAX_RADIUS; x++) {
            for (int y = pos.blockY() - MAX_RADIUS; y <= pos.blockY() + MAX_RADIUS; y++) {
                for (int z = pos.blockZ() - MAX_RADIUS; z <= pos.blockZ() + MAX_RADIUS; z++) {
                    if (BlockProps.isLava(instance.getBlock(x, y, z))) return true;
                }
            }
        }
        return false;
    }
}
