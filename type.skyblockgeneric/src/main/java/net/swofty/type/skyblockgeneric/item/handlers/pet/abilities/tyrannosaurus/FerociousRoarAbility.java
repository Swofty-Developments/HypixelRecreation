package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.tyrannosaurus;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TYRANNOSAURUS, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits DamageDealt dispatch in PlayerActionDamageMob + a stun effect")
public final class FerociousRoarAbility implements PetAbility {
    private static final double PER_LEVEL = 0.1;

    @Override
    public String getName() {
        return "Ferocious Roar";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Attacks have a <a>" + decimalify(value, 1) + "% <7>chance to stun",
                "<7>the target <8>(10s cooldown)."
        );
    }

    @PetEventHandler
    public void onDamageDealt(PetEvent.DamageDealt event) {
    }
}
