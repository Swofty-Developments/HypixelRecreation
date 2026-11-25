package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.zombie;

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

@PetAbilityRegistration(pet = PetHandler.ZOMBIE, minimumRarity = Rarity.COMMON)
public final class BiteShieldAbility implements PetAbility {
    private static final RarityValue<Double> BASE =
            new RarityValue<>(5.0, 10.0, 10.0, 15.0, 15.0, 0.0, 0.0);
    private static final RarityValue<Double> PER_LEVEL = RarityValue.singleDouble(0.1);

    @Override
    public String getName() {
        return "Bite Shield";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = BASE.getForRarity(rarity) + PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Reduce the damage taken from",
                "<7>zombies by <a>" + decimalify(value, 1) + "%<7>."
        );
    }

    @PetEventHandler
    public void onDamagedByMob(PetEvent.DamagedByMob event) {
        SkyBlockMob mob = event.mob();
        if (mob == null || mob.getEntityType() != EntityType.ZOMBIE) return;

        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double reduction = BASE.getForRarity(rarity) + PER_LEVEL.getForRarity(rarity) * level;

        event.damage(event.damage() * (1 - reduction / 100));
    }
}
