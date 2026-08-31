package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.blue_whale;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BLUE_WHALE, minimumRarity = Rarity.COMMON,
        implemented = false,
        notImplementedReason = "awaits a dispatch(PetEvent.Damaged) hook")
public final class IngestAbility implements PetAbility {
    @Override
    public String getName() {
        return "Ingest";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double reduction = level * 0.05;

        return Arrays.asList(
                "<7>If you have any absorption active,",
                "<7>gain <a>+" + decimalify(reduction, 2) + "% Damage Reduction<7>."
        );
    }

    @PetEventHandler
    public void onDamaged(PetEvent.Damaged damaged) {
        SkyBlockPlayer player = damaged.player();
        if (player.getAdditionalHearts() <= 0) return;

        Rarity rarity = damaged.pet().getAttributeHandler().getRarity();
        int level = damaged.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double reduction = level * 0.05;
        damaged.damage(damaged.damage() * (1 - reduction / 100));
    }
}