package net.swofty.type.skyblockgeneric.experimentation;

import net.swofty.commons.text.Text;

public enum ExperimentType {
    SUPERPAIRS("Superpairs"),
    CHRONOMATRON("Chronomatron"),
    ULTRASEQUENCER("Ultrasequencer");

    private final Text displayName;

    ExperimentType(String displayName) {
        this.displayName = Text.literal(displayName);
    }

    public Text displayName() {
        return displayName;
    }

    public static ExperimentType fromName(String name) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(name) || type.displayName.plain().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown experiment type: " + name));
    }
}
