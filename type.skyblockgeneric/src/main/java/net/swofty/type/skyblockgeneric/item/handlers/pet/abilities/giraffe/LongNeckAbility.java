package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.giraffe;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GIRAFFE, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "can't determine damage type")
public final class LongNeckAbility implements PetAbility {
    private static final double BASE_DAMAGE = 50;
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Long Neck";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(BASE_DAMAGE + DAMAGE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increases your melee damage by <c>" + value + "%<7>",
                "<7>if you are more than 3 blocks away from",
                "<7>the target."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet, @Nullable LivingEntity entity) {
        if (entity == null) return ItemStatistics.empty();

        double distance = player.getPosition().distance(entity.getPosition());
        if (distance <= 3) return ItemStatistics.empty();

        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.DAMAGE,
                        1 + (BASE_DAMAGE + DAMAGE_PER_LEVEL.getForRarity(rarity) * level) / 100)
                .build();
    }
}
