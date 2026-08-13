package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.generic.utility.BlockProps;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

public final class GuardianManaPoolAbility implements PetAbility {
    private static final RarityValue<Double> MANA_REGEN_PER_LEVEL = new RarityValue<>(
            0.0, 0.0, 0.0, 0.0, 0.30, 0.30, 0.0);

    @Override
    public String getName() {
        return "Mana Pool";
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        Rarity rarity = instance.getAttributeHandler().getRarity();
        int level = instance.getAttributeHandler().getPetData().getAsLevel(rarity);
        double regeneration = MANA_REGEN_PER_LEVEL.getForRarity(rarity) * level;
        return List.of(
                "<7>Regenerate <b>" + decimalify(regeneration, 2)
                        + "%<7> extra <b><glyph:stat_mana> Mana<7>,",
                "<7>doubled when near or in water."
        );
    }

    @Override
    public double getManaRegenerationPercent(net.swofty.type.skyblockgeneric.user.SkyBlockPlayer player,
                                             SkyBlockItem pet) {
        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        double regeneration = MANA_REGEN_PER_LEVEL.getForRarity(rarity) * level;
        if (nearWater(player)) regeneration *= 2;
        return regeneration;
    }

    private static boolean nearWater(net.swofty.type.skyblockgeneric.user.SkyBlockPlayer player) {
        if (player.getInstance() == null) return false;
        var position = player.getPosition();
        for (int x = position.blockX() - 1; x <= position.blockX() + 1; x++) {
            for (int y = position.blockY() - 1; y <= position.blockY() + 2; y++) {
                for (int z = position.blockZ() - 1; z <= position.blockZ() + 1; z++) {
                    if (BlockProps.isWater(player.getInstance().getBlock(x, y, z))) return true;
                }
            }
        }
        return false;
    }
}
