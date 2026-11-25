package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.griffin;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GRIFFIN, minimumRarity = Rarity.EPIC,
        implemented = false, notImplementedReason = "awaits per-mob Magic Find application against Mythological mobs")
public final class KingOfKingsAbility implements PetAbility {
    private static final RarityValue<Double> MAGIC_FIND_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.2, 0.2, 0.2, 0.0);

    @Override
    public String getName() {
        return "King of Kings";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String magicFind = decimalify(MAGIC_FIND_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Grants <b>+" + magicFind + " <stat:magic_find> <7>on",
                "<2>Mythological <7>mobs."
        );
    }
}
