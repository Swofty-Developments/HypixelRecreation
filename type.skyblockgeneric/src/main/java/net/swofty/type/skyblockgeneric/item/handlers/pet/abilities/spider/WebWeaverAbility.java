package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.spider;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SPIDER, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits DamageDealt dispatch in PlayerActionDamageMob + a mob slow effect")
public final class WebWeaverAbility implements PetAbility {
    private static final RarityValue<Double> SLOWNESS_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.4, 0.4, 0.4, 0.4, 0.0);

    @Override
    public String getName() {
        return "Web-Weaver";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = SLOWNESS_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Upon hitting a monster it becomes",
                "<7>slowed by <a>" + decimalify(value, 1) + "%"
        );
    }

    @PetEventHandler
    public void onDamageDealt(PetEvent.DamageDealt event) {
    }
}
