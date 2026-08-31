package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.silverfish;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SILVERFISH, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a permanent potion-effect application system (Haste I/II/III)")
public final class DexterityAbility implements PetAbility {
    private static final double MINING_SPEED_PER_LEVEL = 1.5;

    @Override
    public String getName() {
        return "Dexterity";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String miningSpeed = decimalify(MINING_SPEED_PER_LEVEL * level, 1);
        String haste = StringUtility.getAsRomanNumeral(1 + (level >= 50 ? 1 : 0) + (level >= 100 ? 1 : 0));

        return List.of(
                "<7>Grants <6>+" + miningSpeed + " <stat:mining_speed> <7>and",
                "<7>permanent <e>Haste " + haste + "<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.MINING_SPEED, MINING_SPEED_PER_LEVEL * level)
                .build();
    }
}
