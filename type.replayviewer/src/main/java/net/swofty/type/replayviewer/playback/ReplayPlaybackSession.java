package net.swofty.type.replayviewer.playback;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class ReplayPlaybackSession {
    private final IntSupplier totalTicks;
    private final BooleanSupplier activeViewers;
    private final IntConsumer tickConsumer;
    private final Runnable endHandler;
    private volatile int currentTick;
    private volatile boolean playing;
    private volatile float speed = 1.0f;
    private final FractionalTickAccumulator accumulator = new FractionalTickAccumulator();
    private Task task;

    public ReplayPlaybackSession(IntSupplier totalTicks, BooleanSupplier activeViewers, IntConsumer tickConsumer, Runnable endHandler) {
        this.totalTicks = totalTicks;
        this.activeViewers = activeViewers;
        this.tickConsumer = tickConsumer;
        this.endHandler = endHandler;
    }

    public void play() {
        if (playing) return;
        playing = true;
        task = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!playing || !activeViewers.getAsBoolean()) {
                pause();
                return;
            }
            int ticksToAdvance = accumulator.advance(speed);
            while (playing && ticksToAdvance-- > 0 && currentTick < totalTicks.getAsInt()) {
                tickConsumer.accept(++currentTick);
            }
            if (currentTick >= totalTicks.getAsInt()) endHandler.run();
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    public void pause() {
        playing = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void seek(int tick) {
        currentTick = tick;
        accumulator.reset();
    }

    public void speed(float value) {
        speed = Math.clamp(value, 0.25f, 4.0f);
    }

    public int currentTick() {
        return currentTick;
    }

    public boolean playing() {
        return playing;
    }

    public float speed() {
        return speed;
    }
}
