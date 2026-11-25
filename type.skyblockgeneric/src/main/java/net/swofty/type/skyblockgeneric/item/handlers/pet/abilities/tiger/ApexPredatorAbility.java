package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.tiger;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TIGER, minimumRarity = Rarity.LEGENDARY)
public final class ApexPredatorAbility implements PetAbility {
    private static final double PER_LEVEL = 1.0;
    private static final double RANGE = 15;

    @Override
    public String getName() {
        return "Apex Predator";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Deal <c>+" + decimalify(value, 1) + "% <7>damage against",
                "<7>targets with no other mobs within",
                "<a>" + decimalify(RANGE, 0) + " <7>blocks."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet, @Nullable LivingEntity entity) {
        if (!(entity instanceof SkyBlockMob mob)) return ItemStatistics.empty();
        if (hasOtherMobNearby(mob)) return ItemStatistics.empty();

        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);

        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.DAMAGE, 1 + PER_LEVEL * level / 100)
                .build();
    }

    private static boolean hasOtherMobNearby(SkyBlockMob mob) {
        Instance instance = mob.getInstance();
        if (instance == null) return false;

        for (Entity entity : instance.getNearbyEntities(mob.getPosition(), RANGE)) {
            if (!(entity instanceof SkyBlockMob other) || entity == mob) continue;
            return true;
        }
        return false;
    }
}
