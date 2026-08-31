package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.lion;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.LION, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits mob-attacked-you tracking + dispatch(PetEvent.DamageDealt)")
public final class KingOfTheJungleAbility implements PetAbility {
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "King of the Jungle";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(DAMAGE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Deal <c>+" + percent + "% <stat:damage>",
                "<7>against mobs that have attacked you."
        );
    }

    @PetEventHandler
    public void onDamageDealt(PetEvent.DamageDealt event) {
    }
}
