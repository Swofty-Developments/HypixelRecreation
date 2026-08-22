package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.pigman;

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

@PetAbilityRegistration(pet = PetHandler.PIGMAN, minimumRarity = Rarity.LEGENDARY)
public final class GiantSlayerAbility implements PetAbility {
    @Override
    public String getName() {
        return "Giant Slayer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Deal <c>+50% <7>damage to monsters",
                "<7>Level <a>50+ <7>and <c>+75% <7>damage",
                "<7>to monsters Level <a>100+<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet, @Nullable LivingEntity entity) {
        if (!(entity instanceof SkyBlockMob mob)) return ItemStatistics.empty();

        Integer level = mob.getLevel();
        double bonus = level == null ? 0
                : level >= 100 ? 0.75
                : level >= 50 ? 0.5
                : 0;
        if (bonus == 0) return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.DAMAGE, 1 + bonus)
                .build();
    }
}
