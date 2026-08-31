package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.frog;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.FROG, minimumRarity = Rarity.LEGENDARY)
public final class HappyTreeFriendsAbility implements PetAbility {
    private static final int MAX_FROGS = 10;
    private static final RarityValue<Double> FORAGING_FORTUNE_BASE = new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0);
    private static final RarityValue<Double> FORAGING_FORTUNE_PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.09, 0.09, 0.0);
    private static final RarityValue<Double> FISHING_SPEED_BASE = new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0);
    private static final RarityValue<Double> FISHING_SPEED_PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.019, 0.0);

    @Override
    public String getName() {
        return "Happy Tree Friends";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double foragePerFrog = FORAGING_FORTUNE_BASE.getForRarity(rarity) + FORAGING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level;
        String forage = decimalify(foragePerFrog, 2);

        if (rarity == Rarity.MYTHIC) {
            double fishPerFrog = FISHING_SPEED_BASE.getForRarity(rarity) + FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level;
            return List.of(
                    "<7>Grants <6>" + forage + " <stat:foraging_fortune> <7>and",
                    "<b>" + decimalify(fishPerFrog, 2) + " <stat:fishing_speed> <7>for every",
                    "<7>other <2>Frog Pet <7>on the island, up to <b>" + MAX_FROGS,
                    "<7>frogs."
            );
        }
        return List.of(
                "<7>Grants <6>" + forage + " <stat:foraging_fortune> <7>for",
                "<7>every other <2>Frog Pet <7>on the island, up to",
                "<b>" + MAX_FROGS + " <7>frogs."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        int frogCount = countOtherFrogPets(player);
        if (frogCount == 0) return ItemStatistics.empty();

        double forage = (FORAGING_FORTUNE_BASE.getForRarity(rarity) + FORAGING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level) * frogCount;
        double fishing = (FISHING_SPEED_BASE.getForRarity(rarity) + FISHING_SPEED_PER_LEVEL.getForRarity(rarity) * level) * frogCount;

        ItemStatistics.Builder builder = ItemStatistics.builder()
                .withBase(ItemStatistic.FORAGING_FORTUNE, forage);
        if (fishing > 0) {
            builder.withBase(ItemStatistic.FISHING_SPEED, fishing);
        }
        return builder.build();
    }

    private static int countOtherFrogPets(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return 0;

        int count = 0;
        for (Player other : instance.getPlayers()) {
            if (count >= MAX_FROGS) break;
            if (other == player) continue;
            if (!(other instanceof SkyBlockPlayer skyPlayer)) continue;

            SkyBlockItem enabledPet = skyPlayer.getPetData().getEnabledPet();
            if (enabledPet == null) continue;
            if (enabledPet.getAttributeHandler().getPotentialType() == ItemType.FROG_PET) count++;
        }
        return count;
    }
}
