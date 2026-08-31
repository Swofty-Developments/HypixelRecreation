package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.spirit;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SPIRIT, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a dungeons ghost ability system")
public final class SpiritCooldownsAbility implements PetAbility {
    private static final double BASE = 5.0;
    private static final double PER_LEVEL = 0.45;

    @Override
    public String getName() {
        return "Spirit Cooldowns";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = BASE + PER_LEVEL * level;

        return List.of(
                "<7>Reduces the cooldown of your",
                "<7>ghost abilities in dungeons by",
                "<a>" + decimalify(value, 1) + "%<7>."
        );
    }

    @PetEventHandler
    public void onAbilityCooldown(PetEvent.AbilityCooldown event) {
    }
}
