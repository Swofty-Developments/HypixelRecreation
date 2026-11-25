package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.crow;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.CROW, minimumRarity = Rarity.LEGENDARY)
public final class InsightfulAbility implements PetAbility {
    private static final int BASE = 3;
    private static final double PER_LEVEL = 0.12;

    @Override
    public String getName() {
        return "Insightful";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = BASE + PER_LEVEL * level;

        return Arrays.asList(
                "<7>Gives a <a>" + decimalify(value, 2) + "%<7> chance to not consume",
                "<7>Mana when using an ability."
        );
    }

    @PetEventHandler
    public void onManaCost(PetEvent.ManaCost event) {
        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double chance = BASE + PER_LEVEL * level;

        if (Math.random() * 100 < chance)
            event.free(true);
    }
}
