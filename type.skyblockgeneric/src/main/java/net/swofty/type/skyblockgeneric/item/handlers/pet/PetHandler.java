package net.swofty.type.skyblockgeneric.item.handlers.pet;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.*;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;

import java.util.List;

public enum PetHandler {
    BEE(List.of(
            at(new HiveAbility(), Rarity.COMMON),
            at(new BusyBuzzBuzzAbility(), Rarity.RARE),
            at(new HoneyHarvesterAbility(), Rarity.LEGENDARY),
            at(new PoweredByPollenAbility(), Rarity.MYTHIC)
    )),
    GRANDMA_WOLF(List.of(
            at(new KillComboAbility(), Rarity.COMMON)
    )),
    CHICKEN(List.of(
            at(new FreeRangeAbility(), Rarity.COMMON),
            at(new EggstraLootAbility(), Rarity.RARE),
            at(new LightFeetAbility(), Rarity.LEGENDARY)
    )),
    GUARDIAN(List.of(
            at(new GuardianLazerbeamAbility(), Rarity.COMMON),
            at(new GuardianEnchantingWisdomAbility(), Rarity.RARE),
            at(new GuardianManaPoolAbility(), Rarity.LEGENDARY),
            at(new GuardianLuckySevenAbility(), Rarity.MYTHIC)
    ));

    private final List<AbilityEntry> abilities;

    PetHandler(List<AbilityEntry> abilities) {
        this.abilities = abilities;
    }

    public List<PetAbility> getAbilities(SkyBlockItem item) {
        Rarity rarity = item.getAttributeHandler().getRarity();
        return abilities.stream()
                .filter(e -> rarity.isAtLeast(e.minimumRarity()))
                .map(AbilityEntry::ability)
                .toList();
    }

    public record AbilityEntry(PetAbility ability, Rarity minimumRarity) {
    }

    private static AbilityEntry at(PetAbility ability, Rarity rarity) {
        return new AbilityEntry(ability, rarity);
    }
}
