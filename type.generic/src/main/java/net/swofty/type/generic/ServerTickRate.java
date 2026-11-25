package net.swofty.type.generic;

final class ServerTickRate {
    private static final int SAMPLE_SIZE = 100;
    private static final long WARNING_INTERVAL_NANOS = 30_000_000_000L;

    private final long[] intervals = new long[SAMPLE_SIZE];
    private long previousTick;
    private long totalNanos;
    private long lastWarning;
    private int sampleCount;
    private int nextSample;
    private volatile double ticksPerSecond = 20;

    void recordTick() {
        long now = System.nanoTime();
        if (previousTick != 0) {
            long interval = now - previousTick;
            totalNanos += interval - intervals[nextSample];
            intervals[nextSample] = interval;
            nextSample = (nextSample + 1) % SAMPLE_SIZE;
            sampleCount = Math.min(sampleCount + 1, SAMPLE_SIZE);
            ticksPerSecond = Math.min(20, sampleCount * 1_000_000_000.0 / totalNanos);
        }
        previousTick = now;
    }

    double getTicksPerSecond() {
        return Math.round(ticksPerSecond * 100.0) / 100.0;
    }

    boolean shouldWarn() {
        if (sampleCount < SAMPLE_SIZE || ticksPerSecond >= 19) {
            return false;
        }
        long now = System.nanoTime();
        if (lastWarning != 0 && now - lastWarning < WARNING_INTERVAL_NANOS) {
            return false;
        }
        lastWarning = now;
        return true;
    }
}
