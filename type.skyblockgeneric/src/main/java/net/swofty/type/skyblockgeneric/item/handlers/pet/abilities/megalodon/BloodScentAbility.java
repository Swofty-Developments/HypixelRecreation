package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.megalodon;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
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

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.MEGALODON, minimumRarity = Rarity.EPIC, order = 0)
public final class BloodScentAbility implements PetAbility {
    private static final RarityValue<Double> MAX_DAMAGE_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 0.0);
    private static final RarityValue<Double> MAX_DAMAGE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Blood Scent";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = commaify(MAX_DAMAGE_BASE.getForRarity(rarity)
                + MAX_DAMAGE_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Deal up to <c>+" + percent + "% <c>Damage <7>based on",
                "<7>the enemy's missing health."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet, @Nullable LivingEntity entity) {
        double maxHealth = entity == null ? 0 : entity.getAttributeValue(Attribute.MAX_HEALTH);
        if (maxHealth <= 0) return ItemStatistics.empty();

        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        double missing = 1 - entity.getHealth() / maxHealth;
        double bonus = MAX_DAMAGE_BASE.getForRarity(rarity) + MAX_DAMAGE_PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.DAMAGE, 1 + missing * bonus / 100)
                .build();
    }
}
