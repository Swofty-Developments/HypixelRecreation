package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.golem;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GOLEM, minimumRarity = Rarity.EPIC, order = 1)
public final class RicochetAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.25, 0.25, 0.0, 0.0);

    @Override
    public String getName() {
        return "Ricochet";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Your iron plating causes <a>" + chance + "% <7>of",
                "<7>attacks to ricochet and hit the attacker."
        );
    }

    @PetEventHandler
    public void onDamagedByMob(PetEvent.DamagedByMob event) {
        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        if (Math.random() * 100 >= CHANCE_PER_LEVEL.getForRarity(rarity) * level) return;

        double reflected = event.damage();
        event.damage(0);
        event.mob().damage(new Damage(DamageType.PLAYER_ATTACK, event.player(), event.player(),
                event.player().getPosition(), (float) reflected));
    }
}
