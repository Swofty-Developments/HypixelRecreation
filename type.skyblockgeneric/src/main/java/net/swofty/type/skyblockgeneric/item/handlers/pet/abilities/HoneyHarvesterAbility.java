package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

public final class HoneyHarvesterAbility implements PetAbility {
    private static final double CHANCE_PER_LEVEL = 0.0002;

    @Override
    public String getName() {
        return "Honey Harvester";
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        Rarity rarity = instance.getAttributeHandler().getRarity();
        int level = instance.getAttributeHandler().getPetData().getAsLevel(rarity);
        double chance = CHANCE_PER_LEVEL * level;

        return List.of(
                "<7>You have a <a>" + decimalify(chance, 3) + "% <7>chance to find a",
                "<a>Honey Jar <7>when farming crops.",
                "",
                "<c>⚠ <l>NOT IMPLEMENTED<r><c> — no game hook for CropHarvested yet"
        );
    }

    @Override
    public void onEvent(PetEvent event) {
        if (event instanceof PetEvent.CropHarvested crop) {
            onCropHarvested(crop);
        }
    }

    private void onCropHarvested(PetEvent.CropHarvested event) {
        SkyBlockPlayer player = event.player();
        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double chance = CHANCE_PER_LEVEL * level;
        if (Math.random() * 100 >= chance) return;

        player.addAndUpdateItem(new SkyBlockItem(ItemType.HONEY_JAR));
    }
}
