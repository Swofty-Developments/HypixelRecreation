package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.zombie;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ZOMBIE, minimumRarity = Rarity.RARE)
public final class RottenBladeAbility implements PetAbility {
    private static final double BASE = 25.0;
    private static final double PER_LEVEL = 1.25;

    @Override
    public String getName() {
        return "Rotten Blade";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = BASE + PER_LEVEL * level;

        return List.of(
                "<7>Deal <a>" + decimalify(value, 1) + "% <7>more damage to <2>Undead",
                "<7>mobs"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet, @Nullable LivingEntity entity) {
        if (!(entity instanceof SkyBlockMob mob)) return ItemStatistics.empty();
        if (!mob.getMobTypes().contains(MobType.UNDEAD)) return ItemStatistics.empty();

        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        double value = BASE + PER_LEVEL * level;

        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.DAMAGE, 1 + value / 100)
                .build();
    }
}
