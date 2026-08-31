package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ocelot;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.OCELOT, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits BlockMined dispatch in CustomBlockBreakEvent + a foraging XP hook")
public final class TreeEssenceAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.3, 0.0, 0.0);

    @Override
    public String getName() {
        return "Tree Essence";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Gain a <a>" + chance + "% <7>chance to get",
                "<7>exp from breaking a log."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMined event) {
    }
}
