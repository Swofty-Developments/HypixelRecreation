package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

public final class GuardianLuckySevenAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL = new RarityValue<>(
            0.0, 0.0, 0.0, 0.0, 0.0, 0.07, 0.0);

    @Override
    public String getName() {
        return "Lucky Seven";
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        Rarity rarity = instance.getAttributeHandler().getRarity();
        int level = instance.getAttributeHandler().getPetData().getAsLevel(rarity);
        return List.of(
                "<7>Gain <b>+" + decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2)
                        + "%<7> chance to find",
                "<5>ultra rare <7>books in",
                "<d>Superpairs<7>."
        );
    }

    @Override
    public double getSuperpairsUltraRareBookMultiplier(SkyBlockPlayer player, SkyBlockItem pet) {
        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        return 1 + CHANCE_PER_LEVEL.getForRarity(rarity) * level / 100;
    }
}
