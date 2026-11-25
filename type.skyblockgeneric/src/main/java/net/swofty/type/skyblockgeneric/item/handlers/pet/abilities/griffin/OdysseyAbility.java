package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.griffin;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.GRIFFIN, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits Griffin Burrows + a Mythological mob spawn system")
public final class OdysseyAbility implements PetAbility {
    private static final RarityValue<Integer> TYPES = new RarityValue<>(2, 4, 6, 8, 10, 12, 0);

    @Override
    public String getName() {
        return "Odyssey";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        int types = TYPES.getForRarity(rarity);

        return List.of(
                "<c>" + types + " <7>types of <2>Mythological <7>can spawn",
                "<7>from <e>Griffin Burrows<7>. Their stats scale with",
                "<7>your Griffin's rarity."
        );
    }
}
