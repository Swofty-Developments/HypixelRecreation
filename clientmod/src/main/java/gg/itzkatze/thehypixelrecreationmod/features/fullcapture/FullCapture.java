package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import java.io.IOException;
import java.nio.file.Path;

public final class FullCapture {
    private static CaptureSession session;

    private FullCapture() {
    }

    public static boolean isActive() {
        return session != null;
    }

    public static Path start(String name, boolean raw) throws IOException {
        if (session != null) {
            throw new IllegalStateException("A full capture is already running: " + session.name());
        }
        session = CaptureSession.open(name, raw);
        return session.directory();
    }

    public static StopResult stop(boolean convertPolar) {
        if (session == null) {
            throw new IllegalStateException("No full capture is running.");
        }
        StopResult result = session.stop(convertPolar);
        session = null;
        return result;
    }

    public static Status status() {
        if (session == null) {
            throw new IllegalStateException("No full capture is running.");
        }
        return session.status();
    }

    public static void note(String text) {
        if (session != null) {
            session.note(text);
        }
    }

    public static void split(String label) {
        if (session != null) {
            session.split(label);
        }
    }

    public static void tick() {
        if (session != null) {
            session.tick();
        }
    }

    public static void recordInbound(Packet<?> packet) {
        if (session != null) {
            session.recordInbound(packet);
        }
    }

    public static void recordOutbound(Packet<?> packet) {
        if (session != null) {
            session.recordOutbound(packet);
        }
    }

    public static void onSlotClick(AbstractContainerScreen<?> screen, Slot slot, int slotId, int button,
                                   ContainerInput input) {
        if (session != null) {
            session.onSlotClick(screen, slot, slotId, button, input);
        }
    }

    public static void onScreenClosed() {
        if (session != null) {
            session.onScreenClosed();
        }
    }

    public static String describeDuration(long milliseconds) {
        return CaptureSession.describeDuration(milliseconds);
    }

    public record Status(String name, Path directory, long elapsedMs, int worlds, String currentWorld, int chunks,
                         int packets, int events, int screens, int scoreboardUpdates, int entitySpawns,
                         int failures) {
    }

    public record StopResult(Path directory, int worlds, int chunks, int packets, int events, int polarWorlds) {
    }
}
