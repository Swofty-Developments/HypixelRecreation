package net.swofty.type.replayviewer.playback;

public final class FractionalTickAccumulator {
    private double value;

    public int advance(float speed) {
        value += speed;
        int ticks = (int) value;
        value -= ticks;
        return ticks;
    }

    public void reset() {
        value = 0;
    }
}
