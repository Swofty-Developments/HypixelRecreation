package net.swofty.type.skyblockgeneric.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.text.Text;

@Getter
@AllArgsConstructor
public enum IslandType {
    HUB("Hub", NamedTextColor.AQUA, "<b>"),
    CRIMSON_ISLE("Crimson Isle", NamedTextColor.RED, "<c>"),
    DEEP_CAVERNS("Deep Caverns", NamedTextColor.AQUA, "<b>"),
    BACKWATER_BAYOU("Backwater Bayou", NamedTextColor.DARK_GREEN, "<2>"),
    GOLD_MINE("Gold Mine", NamedTextColor.GOLD, "<6>"),
    THE_PARK("The Park", NamedTextColor.GREEN, "<a>"),
    CRYSTAL_HOLLOWS("Crystal Hollows", NamedTextColor.DARK_PURPLE, "<5>"),
    THE_END("The End", NamedTextColor.LIGHT_PURPLE, "<d>"),
    DUNGEON_HUB("Dungeon Hub", NamedTextColor.RED, "<c>"),
    DWARVEN_MINES("Dwarven Mines", NamedTextColor.DARK_GREEN, "<2>"),
    THE_FARMING_ISLANDS("The Farming Islands", NamedTextColor.YELLOW, "<e>"),
    SPIDERS_DEN("Spider's Den", NamedTextColor.RED, "<c>"),
    GALATEA("Galatea", NamedTextColor.DARK_GREEN, "<2>");

    private final String name;
    private final TextColor color;
    private final String colourTag;

    public Text getFormattedName() {
        return Text.of(colourTag + "<glyph:gui_location> {}", name);
    }

    public Text getResidentLabel() {
        return Text.of(colourTag + "{} Resident", name);
    }
}
