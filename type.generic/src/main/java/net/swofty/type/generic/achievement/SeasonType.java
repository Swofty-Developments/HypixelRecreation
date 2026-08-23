package net.swofty.type.generic.achievement;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.text.Text;

@Getter
public enum SeasonType {
    HOLIDAY("Holiday", NamedTextColor.RED, "Christmas/Winter event"),
    EASTER("Easter", NamedTextColor.AQUA, "Easter/Spring event"),
    SUMMER("Summer", NamedTextColor.YELLOW, "Summer event"),
    HALLOWEEN("Halloween", NamedTextColor.GOLD, "Halloween event");

    private final String displayName;
    private final TextColor color;
    private final String description;

    SeasonType(String displayName, TextColor color, String description) {
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    public static SeasonType fromString(String name) {
        for (SeasonType season : values()) {
            if (season.name().equalsIgnoreCase(name)) {
                return season;
            }
        }
        return null;
    }

    public Text getFormattedName() {
        return Text.of("<color:{}>{} Achievement", color, displayName);
    }
}
