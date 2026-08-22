package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jerry;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.JERRY, minimumRarity = Rarity.COMMON, order = 0)
public final class JerryDamageAbility implements PetAbility {

    @Override
    public String getName() {
        return "Jerry";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Gain <a>50% <7>chance to deal",
                "<7>your regular damage."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.DAMAGE,0.0)
                .build();
    }
}
