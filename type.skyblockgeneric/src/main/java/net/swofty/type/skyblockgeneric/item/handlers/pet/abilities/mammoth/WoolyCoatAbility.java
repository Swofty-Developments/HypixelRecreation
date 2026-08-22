package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mammoth;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.MAMMOTH, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a Cold system (Glacite Mineshafts mob cold-infliction hook); DamagedByMob hook already dispatched")
public final class WoolyCoatAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Wooly Coat";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = commaify(CHANCE_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Gain a <a>" + chance + "% <7>chance for mobs to not",
                "<7>inflict <b>Cold <7>when damaging you in",
                "<7>the <b>Glacite Mineshafts<7>."
        );
    }

    @PetEventHandler
    public void onDamagedByMob(PetEvent.DamagedByMob event) {
    }
}
