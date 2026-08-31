package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.snowman;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SNOWMAN, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class FrostbiteAbility implements PetAbility {
    private static final double DAMAGE_REDUCTION_PER_LEVEL = 0.1;

    @Override
    public String getName() {
        return "Frostbite";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String reduction = decimalify(DAMAGE_REDUCTION_PER_LEVEL * level, 1);

        return List.of(
                "<7>Your freezing aura slows enemy",
                "<7>attacks causing you to take <a>" + reduction + "%",
                "<7>reduced damage."
        );
    }

    @PetEventHandler
    public void onDamagedByMob(PetEvent.DamagedByMob event) {
        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double reduction = DAMAGE_REDUCTION_PER_LEVEL * level;
        event.damage(event.damage() * (1 - reduction / 100));
    }
}
