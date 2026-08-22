package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.skeleton;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SKELETON, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a Dungeons system")
public final class BoneArrowsAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.1, 0.2, 0.35, 0.5, 0.75, 0.0, 0.0);

    @Override
    public String getName() {
        return "Bone Arrows";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increase arrow damage by <a>" + percent + "%",
                "<7>which is doubled while in dungeons."
        );
    }

    @PetEventHandler
    public void onRangedDamageDealt(PetEvent.RangedDamageDealt event) {

    }
}
