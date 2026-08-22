package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.chicken;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.CHICKEN, minimumRarity = Rarity.LEGENDARY)
public final class LightFeetAbility implements PetAbility {
    @Override
    public String getName() {
        return "Light Feet";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double reduction = level;
        return List.of("<7>Reduces fall damage by <a>" + commaify(reduction) + "%<7>.");
    }

    @PetEventHandler
    public void onFallDamage(PetEvent.FallDamage fall) {
        double reduction = fall.pet().getAttributeHandler().getPetData()
                .getAsLevel(fall.pet().getAttributeHandler().getRarity());
        fall.damage(fall.damage() * (1 - reduction / 100));
    }
}
