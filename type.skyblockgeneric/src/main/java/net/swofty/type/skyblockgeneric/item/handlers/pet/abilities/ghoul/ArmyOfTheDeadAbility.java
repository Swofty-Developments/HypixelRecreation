package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ghoul;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GHOUL, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a Necromancy soul system (soul storage + soul capture chance)")
public final class ArmyOfTheDeadAbility implements PetAbility {
    private static final RarityValue<Double> SOUL_CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.2, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Army of the Dead";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(SOUL_CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increases the amount of souls you can",
                "<7>store by <a>2 <7>and the chance of getting",
                "<7>a mob's soul by <a>" + chance + "%<7>."
        );
    }
}
