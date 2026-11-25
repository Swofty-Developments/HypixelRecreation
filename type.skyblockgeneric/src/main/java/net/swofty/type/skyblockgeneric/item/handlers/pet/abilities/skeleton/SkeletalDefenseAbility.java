package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.skeleton;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.SKELETON, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a proximity-trigger system for nearby mobs + a minion arrow projectile")
public final class SkeletalDefenseAbility implements PetAbility {

    @Override
    public String getName() {
        return "Skeletal Defense";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Your skeleton shoots an arrow dealing",
                "<a>30x <7>your <stat:crit_damage> <7>when a mob gets",
                "<7>close to you (<a>5s <7>cooldown)."
        );
    }
}
