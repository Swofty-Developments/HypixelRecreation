package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.hound;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.HOUND, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a LootRoll/armor-drop system + dispatch(PetEvent.KilledMob already connected)")
public final class FinderAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.3, 0.3, 0.0, 0.0);

    @Override
    public String getName() {
        return "Finder";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Increases the chance for monsters",
                "<7>to drop their armor by <a>" + chance + "%<7>."
        );
    }

    @PetEventHandler
    public void onKilledMob(PetEvent.KilledMob event) {

    }
}
