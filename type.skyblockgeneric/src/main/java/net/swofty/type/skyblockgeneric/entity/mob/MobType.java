package net.swofty.type.skyblockgeneric.entity.mob;

import net.swofty.commons.skyblock.PackSprite;
import net.swofty.commons.text.Text;

public enum MobType {
    AIRBORNE(PackSprite.MOBS_AIRBORNE, "<7>", "Airborne", "Mobs that can fly or are airborne."),
    ANIMAL(PackSprite.MOBS_ANIMAL, "<a>", "Animal", "Mobs that are animals or have animalistic characteristics. They can be typically found at The Barn."),
    AQUATIC(PackSprite.MOBS_AQUATIC, "<1>", "Aquatic", "Consists mostly of Water Sea Creatures or mobs that reside in water."),
    ARCANE(PackSprite.MOBS_ARCANE, "<5>", "Arcane", "Mobs that specialize in magic or have high magical density."),
    ARTHROPOD(PackSprite.MOBS_ARTHROPOD, "<4>", "Arthropod", "Mainly consists of spiders and other invertebrate-adjacent mobs."),
    CONSTRUCT(PackSprite.MOBS_CONSTRUCT, "<7>", "Construct", "Mobs that are summoned by other enemies or are artificially created."),
    CUBIC(PackSprite.MOBS_CUBIC, "<a>", "Cubic", "Mobs that have cube-like or blocky appearances."),
    ELUSIVE(PackSprite.MOBS_ELUSIVE, "<d>", "Elusive", "Mobs that are rare and hard to find."),
    ENDER(PackSprite.MOBS_ENDER, "<5>", "Ender", "Mobs related to the End dimension."),
    FROZEN(PackSprite.MOBS_FROZEN, "<f>", "Frozen", "Mobs that reside in Jerry's Workshop."),
    GLACIAL(PackSprite.MOBS_GLACIAL, "<b>", "Glacial", "Mobs that reside in the Glacite Mineshaft."),
    HUMANOID(PackSprite.MOBS_HUMANOID, "<e>", "Humanoid", "Enemies that are found in the Crystal Hollows."),
    INFERNAL(PackSprite.MOBS_INFERNAL, "<4>", "Infernal", "Extremely dangerous mobs native to the Crimson Isle."),
    MAGMATIC(PackSprite.MOBS_MAGMATIC, "<c>", "Magmatic", "Mobs that spawn in lava or fiery environments."),
    MYTHOLOGICAL(PackSprite.MOBS_MYTHOLOGICAL, "<2>", "Mythological", "Mobs that appear during the Mythological Ritual Event."),
    PEST(PackSprite.MOBS_PEST, "<2>", "Pest", "Mobs considered nuisances, often found in the Garden."),
    SHIELDED(PackSprite.MOBS_SHIELDED, "<e>", "Shielded", "Mobs that take only one point of damage per hit."),
    SKELETAL(PackSprite.MOBS_SKELETAL, "<f>", "Skeletal", "Skeleton-based mobs or those with skeletal traits."),
    SPOOKY(PackSprite.MOBS_SPOOKY, "<6>", "Spooky", "Mobs that appear during the Spooky Festival and Great Spook."),
    SUBTERRANEAN(PackSprite.MOBS_SUBTERRANEAN, "<6>", "Subterranean", "Mobs found in Dwarven Mines."),
    UNDEAD(PackSprite.MOBS_UNDEAD, "<2>", "Undead", "Mobs that have risen from their graves."),
    WITHER(PackSprite.MOBS_WITHER, "<8>", "Wither", "Mobs related to the Wither or found in the Catacombs."),
    WOODLAND(PackSprite.MOBS_WOODLAND, "<2>", "Woodland", "Mobs that reside in Galatea.");

    private final PackSprite symbol;
    private final String color;
    private final String displayName;
    private final String description;

    MobType(PackSprite symbol, String color, String displayName, String description) {
        this.symbol = symbol;
        this.color = color;
        this.displayName = displayName;
        this.description = description;
    }

    public Text getSymbol() {
        return Text.of("{}", symbol.getSprite());
    }

    public Text getColoredSymbol() {
        return Text.of(color + "{}<r>", symbol.getSprite());
    }

    public Text getFullDisplayName() {
        return Text.of(color + "{} {}<r>", symbol.getSprite(), displayName);
    }

    public String getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getFullDisplayName().serialize();
    }
}
