package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mithril_golem;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.MITHRIL_GOLEM, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits BlockMined dispatch in CustomBlockBreakEvent + a Mithril block check")
public final class MithrilAffinityAbility implements PetAbility {
    private static final RarityValue<Double> MINING_SPEED_PER_LEVEL =
            new RarityValue<>(1.0, 1.5, 1.5, 2.0, 2.0, 2.0, 0.0);

    @Override
    public String getName() {
        return "Mithril Affinity";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = commaify(MINING_SPEED_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Grants <6>+" + value + " <stat:mining_speed> <7>when",
                "<7>mining <2>Mithril<7>."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMined event) {
    }
}
