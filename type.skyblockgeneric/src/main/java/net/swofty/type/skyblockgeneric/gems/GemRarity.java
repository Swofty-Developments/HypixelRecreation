package net.swofty.type.skyblockgeneric.gems;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.skyblock.item.Rarity;

import java.util.List;

@Getter
public enum GemRarity {
    ROUGH("Rough", Rarity.COMMON, 1, NamedTextColor.WHITE, "<7>Taken right from the heart of a", "<7>crystal vein in the <5>Crystal", "<5>Hollows<7>."),
    FLAWED("Flawed", Rarity.UNCOMMON, 100, NamedTextColor.GREEN, "<7>A slightly better version of", "{GEM}<7>, but it could still use some work."),
    FINE("Fine", Rarity.RARE, 10000, NamedTextColor.AQUA, "<7>A type of {GEM} <7>that has", "<7>clearly been treated with care."),
    FLAWLESS("Flawless", Rarity.EPIC, 100000, NamedTextColor.DARK_PURPLE, "<7>A new perfect version of", "{GEM}<7>."),
    PERFECT("Perfect", Rarity.LEGENDARY, 500000, NamedTextColor.GOLD, "<7>A perfectly refined {GEM}<7>."),
    ;

    public final String name;
    public final Rarity rarity;
    public final Integer costToRemove;
    public final List<String> description;
    public final TextColor bracketColor;

    GemRarity(String name, Rarity rarity, Integer costToRemove, TextColor bracketColor, String... description) {
        this.name = name;
        this.rarity = rarity;
        this.costToRemove = costToRemove;
        this.bracketColor = bracketColor;
        this.description = List.of(description);
    }
}
