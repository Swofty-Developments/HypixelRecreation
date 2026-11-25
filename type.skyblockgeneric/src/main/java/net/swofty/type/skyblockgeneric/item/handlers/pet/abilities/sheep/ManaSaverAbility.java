package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.sheep;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SHEEP, minimumRarity = Rarity.COMMON)
public final class ManaSaverAbility implements PetAbility {
    private static final RarityValue<Double> REDUCTION_PER_LEVEL =
            new RarityValue<>(0.1, 0.1, 0.1, 0.2, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Mana Saver";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String reduction = decimalify(REDUCTION_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Reduces the mana cost of",
                "<7>abilities by <a>" + reduction + "%<7>."
        );
    }

    @PetEventHandler
    public void onManaCost(PetEvent.ManaCost event) {
        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double reduction = REDUCTION_PER_LEVEL.getForRarity(rarity) * level;

        event.cost(event.cost() * (1 - reduction / 100));
    }
}
