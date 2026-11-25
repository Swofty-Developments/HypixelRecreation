package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.snowman;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SNOWMAN, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a snowball projectile system")
public final class SnowCannonAbility implements PetAbility {
    private static final double DAMAGE_BASE = 10;
    private static final double DAMAGE_PER_LEVEL = 0.1;

    @Override
    public String getName() {
        return "Snow Cannon";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(DAMAGE_BASE + DAMAGE_PER_LEVEL * level, 1);

        return List.of(
                "<7>Shoots a snowball towards an enemy",
                "<7>when you attack dealing <a>" + percent + "% <7>of",
                "<7>your last dealt melee damage,",
                "<7>capped at <f>200,000<7>. <8>(1s cooldown)."
        );
    }

    @PetEventHandler
    public void onMeleeDamageDealt(PetEvent.MeleeDamageDealt event) {

    }
}
