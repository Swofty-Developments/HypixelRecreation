package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.frog;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.FROG, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits dispatch(PetEvent.FishCaught) in FishingLootResolver + a Shard catch system")
public final class HuntingEnjoyerAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.09, 0.09, 0.09, 0.09, 0.09, 0.09, 0.0);

    @Override
    public String getName() {
        return "Hunting Enjoyer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = commaify((int) Math.floor(1 + CHANCE_PER_LEVEL.getForRarity(rarity) * level));

        if (rarity == Rarity.COMMON) {
            return List.of(
                    "<7>Increases your chance to catch <2>Forest <7>Shards",
                    "<7>by <a>" + chance + "%<7>."
            );
        } else if (rarity == Rarity.UNCOMMON) {
            return List.of(
                    "<7>Increases your chance to catch <2>Forest <7>and",
                    "<b>Water <7>Shards by <a>" + chance + "%<7>."
            );
        }
        return List.of(
                "<7>Increases your chance to catch <2>Forest<7>, <b>Water<7>,",
                "<7>and <c>Combat <7>Shards by <a>" + chance + "%<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {

    }
}
