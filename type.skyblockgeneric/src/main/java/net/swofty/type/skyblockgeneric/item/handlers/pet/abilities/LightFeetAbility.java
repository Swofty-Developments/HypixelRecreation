package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

public final class LightFeetAbility implements PetAbility {
    @Override
    public String getName() {
        return "Light Feet";
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        double reduction = instance.getAttributeHandler().getPetData()
                .getAsLevel(instance.getAttributeHandler().getRarity());
        return List.of("<7>Reduces fall damage by <a>" + commaify(reduction) + "%<7>.");
    }

    @Override
    public void onEvent(PetEvent event) {
        if (event instanceof PetEvent.FallDamage fall) {
            double reduction = fall.pet().getAttributeHandler().getPetData()
                    .getAsLevel(fall.pet().getAttributeHandler().getRarity());
            fall.damage(fall.damage() * (1 - reduction / 100));
        }
    }
}
