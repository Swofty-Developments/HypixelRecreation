package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.sloth;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SLOTH, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits dispatch(PetEvent.BlockMined) + a timed next-cut buff system")
public final class SlowStartAbility implements PetAbility {
    private static final RarityValue<Double> SWEEP_PER_LEVEL =
            new RarityValue<>(0.15, 0.2, 0.2, 0.25, 0.25, 0.0, 0.0);
    private static final RarityValue<Double> FORAGING_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.3, 0.4, 0.4, 0.5, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Slow Start";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String sweep = decimalify(SWEEP_PER_LEVEL.getForRarity(rarity) * level, 2);
        String foraging = decimalify(FORAGING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Every <a>60s<7>, your next cut gains",
                "<2>+" + sweep + " <stat:sweep> <7>and <6>+" + foraging + " <stat:foraging_fortune><7>."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMined event) {

    }
}
