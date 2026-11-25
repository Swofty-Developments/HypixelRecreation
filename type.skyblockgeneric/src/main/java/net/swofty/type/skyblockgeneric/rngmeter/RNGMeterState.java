package net.swofty.type.skyblockgeneric.rngmeter;

public record RNGMeterState(String selectedReward, double storedXp) {
    public RNGMeterState {
        selectedReward = selectedReward == null ? "" : selectedReward;
        if (storedXp < 0) throw new IllegalArgumentException("Stored XP cannot be negative");
    }
}
