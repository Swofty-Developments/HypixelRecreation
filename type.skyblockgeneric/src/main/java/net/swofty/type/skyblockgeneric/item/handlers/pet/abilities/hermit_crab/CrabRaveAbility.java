package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.hermit_crab;

import net.minestom.server.entity.Entity;
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

@PetAbilityRegistration(pet = PetHandler.HERMIT_CRAB, minimumRarity = Rarity.LEGENDARY)
public final class CrabRaveAbility implements PetAbility {
    private static final int MAX_PLAYERS = 5;
    private static final double RADIUS_BLOCKS = 30;
    private static final RarityValue<Double> TREASURE_CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.002, 0.002, 0.0);

    @Override
    public String getName() {
        return "Crab Rave";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String treasureChance = decimalify(TREASURE_CHANCE_PER_LEVEL.getForRarity(rarity) * level, 3);

        return List.of(
                "<7>Grants <6>+" + treasureChance + " <stat:treasure_chance> <7>for",
                "<7>each player with a <a>Hermit Crab Pet<7> within",
                "<a>30 <7>blocks, up to <a>" + MAX_PLAYERS + " <7>players."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        int crabCount = countNearbyCrabPets(player);
        if (crabCount == 0) return ItemStatistics.empty();

        double treasureChance = TREASURE_CHANCE_PER_LEVEL.getForRarity(rarity) * level * crabCount;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.TREASURE_CHANCE, treasureChance)
                .build();
    }

    private static int countNearbyCrabPets(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return 0;

        int count = 0;
        for (Entity entity : instance.getNearbyEntities(player.getPosition(), RADIUS_BLOCKS)) {
            if (count >= MAX_PLAYERS) break;
            if (!(entity instanceof SkyBlockPlayer skyPlayer) || skyPlayer == player) continue;

            SkyBlockItem enabledPet = skyPlayer.getPetData().getEnabledPet();
            if (enabledPet == null) continue;
            if (enabledPet.getAttributeHandler().getPotentialType() == ItemType.HERMIT_CRAB_PET) count++;
        }
        return count;
    }
}
