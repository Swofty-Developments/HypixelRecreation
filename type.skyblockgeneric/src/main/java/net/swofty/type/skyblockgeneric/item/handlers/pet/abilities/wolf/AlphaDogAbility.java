package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.wolf;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.WOLF, minimumRarity = Rarity.COMMON)
public final class AlphaDogAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.1, 0.2, 0.2, 0.3, 0.3, 0.0, 0.0);

    @Override
    public String getName() {
        return "Alpha Dog";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Take <a>" + decimalify(value, 1) + "% <7>less damage from",
                "<7>wolves."
        );
    }

    @PetEventHandler
    public void onDamagedByMob(PetEvent.DamagedByMob event) {
        SkyBlockMob mob = event.mob();
        if (mob == null || mob.getEntityType() != EntityType.WOLF) return;

        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double reduction = PER_LEVEL.getForRarity(rarity) * level;

        event.damage(event.damage() * (1 - reduction / 100));
    }
}
