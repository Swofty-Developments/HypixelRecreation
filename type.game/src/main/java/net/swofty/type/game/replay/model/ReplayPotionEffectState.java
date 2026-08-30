package net.swofty.type.game.replay.model;

public record ReplayPotionEffectState(int effectId, byte amplifier, int remainingTicks, byte flags) {
    public ReplayPotionEffectState {
        if (effectId < 0 || remainingTicks < 0) throw new IllegalArgumentException("Invalid potion effect state");
    }
}
