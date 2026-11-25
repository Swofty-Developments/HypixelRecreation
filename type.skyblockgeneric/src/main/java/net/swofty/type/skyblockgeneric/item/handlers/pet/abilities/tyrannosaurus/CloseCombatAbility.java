package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.tyrannosaurus;

import net.minestom.server.entity.LivingEntity;
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

@PetAbilityRegistration(pet = PetHandler.TYRANNOSAURUS, minimumRarity = Rarity.LEGENDARY)
public final class CloseCombatAbility implements PetAbility {
    private static final double PER_LEVEL = 1.0;
    private static final double RANGE = 1.5;

    @Override
    public String getName() {
        return "Close Combat";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Deal <a>" + decimalify(value, 1) + "% <7>more <c>damage <7>to",
                "<7>enemies within <a>1.5 <7>blocks."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet, @Nullable LivingEntity entity) {
        if (player == null || !(entity instanceof SkyBlockMob mob)) return ItemStatistics.empty();
        if (player.getPosition().distance(mob.getPosition()) > RANGE) return ItemStatistics.empty();

        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);

        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.DAMAGE, 1 + PER_LEVEL * level / 100)
                .build();
    }
}
