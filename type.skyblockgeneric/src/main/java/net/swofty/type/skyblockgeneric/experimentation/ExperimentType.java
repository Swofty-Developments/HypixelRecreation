package net.swofty.type.skyblockgeneric.experimentation;

public enum ExperimentType {
    SUPERPAIRS("Superpairs"),
    CHRONOMATRON("Chronomatron"),
    ULTRASEQUENCER("Ultrasequencer");

    private final String displayName;

    ExperimentType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static ExperimentType fromName(String name) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown experiment type: " + name));
    }
}
