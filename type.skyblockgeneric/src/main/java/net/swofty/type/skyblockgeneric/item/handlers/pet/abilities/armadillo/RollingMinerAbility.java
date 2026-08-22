package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.armadillo;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.gems.Gemstone;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.ArrayList;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ARMADILLO, minimumRarity = Rarity.RARE, order = 0,
        implemented = false, notImplementedReason = "awaits a dispatch(PetEvent.BlockMined) hook")
public final class RollingMinerAbility implements PetAbility {
    private static final double BASE_SECONDS = 60;
    private static final RarityValue<Double> SECONDS_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, -0.2, -0.3, -0.4, -0.4, 0.0);

    private long lastProc;

    @Override
    public String getName() {
        return "Rolling Miner";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double seconds = BASE_SECONDS + SECONDS_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Every <a>" + decimalify(seconds, 1) + " <7>seconds, the next",
                "<d>Gemstone <7>you mine gives <a>2x <7>drops."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMined event) {
        boolean minedGemstone = event.drops().stream()
                .anyMatch(drop -> Gemstone.getFromItemType(drop.getItemType()) != null);
        if (!minedGemstone) return;

        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double intervalMillis = (BASE_SECONDS + SECONDS_PER_LEVEL.getForRarity(rarity) * level) * 1000;

        if (System.currentTimeMillis() - lastProc < intervalMillis) return;
        lastProc = System.currentTimeMillis();

        List<SkyBlockItem> doubled = new ArrayList<>(event.drops());
        doubled.addAll(event.drops());
        event.drops(doubled);
    }
}
