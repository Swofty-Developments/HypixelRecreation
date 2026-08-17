package net.swofty.type.skyblockgeneric.skilltree;

public enum TreePowder {
    MITHRIL("Mithril Powder", "<2>"),
    GEMSTONE("Gemstone Powder", "<d>"),
    GLACITE("Glacite Powder", "<b>");

    private final String displayName;
    private final String color;

    TreePowder(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String displayName() {
        return displayName;
    }

    public String color() {
        return color;
    }
}
