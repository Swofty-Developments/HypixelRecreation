package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.wither_skeleton;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.WITHER_SKELETON, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits DamageDealt dispatch in PlayerActionDamageMob + a wither damage-over-time effect")
public final class DeathsTouchAbility implements PetAbility {
    private static final double PER_LEVEL = 2.0;

    @Override
    public String getName() {
        return "Death's Touch";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Upon hitting an enemy inflict",
                "<7>the wither effect for <a>" + decimalify(value, 1) + "%<7>",
                "<7>damage over 3 seconds. <8>Does not stack"
        );
    }

    @PetEventHandler
    public void onDamageDealt(PetEvent.DamageDealt event) {
    }
}
