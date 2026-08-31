package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.hound;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.HOUND, minimumRarity = Rarity.EPIC, order = 0)
public final class ScavengerAbility implements PetAbility {
    private static final RarityValue<Double> COINS_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Scavenger";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String coins = commaify(COINS_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Gain +<a>" + coins + " <7>coins per monster kill."
        );
    }

    @PetEventHandler
    public void onKilledMob(PetEvent.KilledMob event) {
        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        event.player().addCoins(COINS_PER_LEVEL.getForRarity(rarity) * level);
    }
}
