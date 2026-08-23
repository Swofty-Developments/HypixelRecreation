package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

public final class PoweredByPollenAbility implements PetAbility {
    private static final double PER_LEVEL = 1.6;

    @Override
    public String getName() {
        return "Powered by Pollen";
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        Rarity rarity = instance.getAttributeHandler().getRarity();
        int level = instance.getAttributeHandler().getPetData().getAsLevel(rarity);
        double fortune = PER_LEVEL * level;

        return List.of(
                "<7>Grants <6>+" + decimalify(fortune, 1) + "☘ Sunflower<7>,",
                "<6>Moonflower<7>, and <6>Wild Rose Fortune",
                "<7>while in <a>The Garden<7>.",
                "",
                "<c>⚠ <l>NOT IMPLEMENTED<r><c> — The Garden region check not implemented"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet) {
        // TODO: Implement region check — only grant fortune while player is in The Garden
        return ItemStatistics.empty();
    }
}
