package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.sheep;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SHEEP, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a Dungeons system")
public final class DungeonWizardAbility implements PetAbility {
    private static final RarityValue<Double> MANA_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.25, 0.0, 0.0);

    @Override
    public String getName() {
        return "Dungeon Wizard";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String mana = decimalify(MANA_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Increases your total mana by",
                "<a>" + mana + "% <7>while in dungeons."
        );
    }
}
