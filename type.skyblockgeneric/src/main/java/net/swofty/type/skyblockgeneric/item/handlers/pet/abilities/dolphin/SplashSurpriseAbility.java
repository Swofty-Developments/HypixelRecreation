package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.dolphin;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.Arrays;
import java.util.List;

@PetAbilityRegistration(pet = PetHandler.DOLPHIN, minimumRarity = Rarity.LEGENDARY,
        implemented = false,
        notImplementedReason = "awaits a dispatch(PetEvent.FishCaught) hook; CatchPayload.SeaCreature does not carry the spawned entity to stun")
public final class SplashSurpriseAbility implements PetAbility {
    @Override
    public String getName() {
        return "Splash Surprise";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return Arrays.asList(
                "<7>Stun sea creatures for <a>5s",
                "<7>after fishing them up."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {

    }
}
