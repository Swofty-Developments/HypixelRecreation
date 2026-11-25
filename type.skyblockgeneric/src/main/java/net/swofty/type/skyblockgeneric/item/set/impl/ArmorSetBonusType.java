package net.swofty.type.skyblockgeneric.item.set.impl;

public enum ArmorSetBonusType {
    FULL_SET("Full Set Bonus"),
    TIERED("Tiered Bonus"),
    PIECE("Piece Bonus"),
    ABILITY("Ability");

    private final String displayName;

    ArmorSetBonusType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
