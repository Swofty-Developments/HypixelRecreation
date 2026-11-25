package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.precursor_drone;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.PRECURSOR_DRONE, minimumRarity = Rarity.COMMON, order = 2,
        implemented = false, notImplementedReason = "awaits BlockMined dispatch in CustomBlockBreakEvent + a collection progress hook")
public final class MiningOffCameraAbility implements PetAbility {
    @Override
    public String getName() {
        return "Mining Off Camera";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>While mining, each collection",
                "<7>progress grants a <a>0.005% <7>chance to",
                "<7>drop a random enchanted mining item."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMined event) {
    }
}
