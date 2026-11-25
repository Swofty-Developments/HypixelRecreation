package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.magma_cube;

import net.minestom.server.entity.EntityType;
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

@PetAbilityRegistration(pet = PetHandler.MAGMA_CUBE, minimumRarity = Rarity.RARE)
public final class SaltBladeAbility implements PetAbility {
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.2, 0.25, 0.25, 0.0, 0.0);

    @Override
    public String getName() {
        return "Salt Blade";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(DAMAGE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Deal <a>" + percent + "% <7>more damage to slimes."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet, @Nullable LivingEntity entity) {
        if (entity == null || (entity.getEntityType() != EntityType.SLIME && entity.getEntityType() != EntityType.MAGMA_CUBE))
            return ItemStatistics.empty();

        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.DAMAGE, 1 + DAMAGE_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
