package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.frog;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.FROG, minimumRarity = Rarity.MYTHIC,
        implemented = false, notImplementedReason = "awaits dispatch(PetEvent.FishCaught) in FishingLootResolver + a Trophy Frog catch system")
public final class HomeSweetHomeAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0);

    @Override
    public String getName() {
        return "Home Sweet Home";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increases your chance of catching",
                "<6><l>GOLD<r> <7>and <b><l>DIAMOND<r> <2>Trophy Frogs",
                "<7>by <a>" + chance + "%<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {

    }
}
