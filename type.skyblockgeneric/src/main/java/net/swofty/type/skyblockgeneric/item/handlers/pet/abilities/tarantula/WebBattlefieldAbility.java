package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.tarantula;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TARANTULA, minimumRarity = Rarity.MYTHIC, order = 3,
        implemented = false, notImplementedReason = "awaits a buff-zone system granting Strength + Magic Find to nearby players for 40s on kill")
public final class WebBattlefieldAbility implements PetAbility {
    private static final RarityValue<Double> STRENGTH_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.06, 0.0);
    private static final RarityValue<Double> MAGIC_FIND_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.01, 0.0);

    @Override
    public String getName() {
        return "Web Battlefield";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double strength = STRENGTH_PER_LEVEL.getForRarity(rarity) * level;
        double magicFind = MAGIC_FIND_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Killing mobs grants <c>+" + decimalify(strength, 1) + " <stat:strength>",
                "<7>and <b>+" + decimalify(magicFind, 1) + " <stat:magic_find> <7>for <a>40s <7>to all",
                "<7>players staying within <a>20 <7>blocks of",
                "<7>where they died. <8>Stacks up to 10 times."
        );
    }

    @PetEventHandler
    public void onKill(PetEvent.KilledMob event) {
    }
}
