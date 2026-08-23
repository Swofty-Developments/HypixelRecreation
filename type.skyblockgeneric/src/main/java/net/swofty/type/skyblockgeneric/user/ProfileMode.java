package net.swofty.type.skyblockgeneric.user;

import net.swofty.commons.text.Text;

public enum ProfileMode {
    CLASSIC("<a>Classic"),
    IRONMAN("<7>♲ <7>Ironman");

    private final String displayName;

    ProfileMode(String displayName) {
        this.displayName = displayName;
    }

    public Text getDisplayName() {
        return Text.of(displayName);
    }

    public static ProfileMode fromStored(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return CLASSIC;
        }
    }
}
