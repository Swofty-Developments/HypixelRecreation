package net.swofty.type.ravengardgeneric.region;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum RavengardRegionType {
    NEVERMORE("The Nevermore", NamedTextColor.AQUA),
    RAVENPORT("Ravenport", NamedTextColor.YELLOW);

    private final String displayName;
    private final TextColor color;

    RavengardRegionType(String displayName) {
        this(displayName, NamedTextColor.WHITE);
    }

    RavengardRegionType(String displayName, TextColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public static RavengardRegionType fromKey(String key) {
        if (key == null) {
            return null;
        }
        for (RavengardRegionType value : values()) {
            if (value.name().equalsIgnoreCase(key)) {
                return value;
            }
        }
        return null;
    }
}
