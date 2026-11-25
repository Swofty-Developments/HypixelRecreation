package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.goblin;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.GOBLIN, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits dispatch(PetEvent.BlockMined) + an ore-conditional Mining Speed buff")
public final class GruntWorkAbility implements PetAbility {
    private static final RarityValue<Double> MINING_SPEED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 2.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Grunt Work";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = commaify(MINING_SPEED_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Gain <6>+" + value + " <stat:mining_speed> <7>when mining",
                "<6>Ores<7>."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMining event) {

    }
}
