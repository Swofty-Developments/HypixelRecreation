package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.bee;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BEE, minimumRarity = Rarity.LEGENDARY,
        implemented = false,
        notImplementedReason = "awaits a dispatch(PetEvent.CropHarvested) hook")
public final class HoneyHarvesterAbility implements PetAbility {
    private static final double CHANCE_PER_LEVEL = 0.0002;

    @Override
    public String getName() {
        return "Honey Harvester";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double chance = CHANCE_PER_LEVEL * level;

        return List.of(
                "<7>You have a <a>" + decimalify(chance, 3) + "% <7>chance to find a",
                "<a>Honey Jar <7>when farming crops."
        );
    }

    @PetEventHandler
    public void onCropHarvested(PetEvent.CropHarvested event) {
        SkyBlockPlayer player = event.player();
        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double chance = CHANCE_PER_LEVEL * level;
        if (Math.random() * 100 >= chance) return;

        player.addAndUpdateItem(new SkyBlockItem(ItemType.HONEY_JAR));
    }
}
