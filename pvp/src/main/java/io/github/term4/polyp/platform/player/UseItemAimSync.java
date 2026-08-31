package io.github.term4.polyp.platform.player;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionStatusPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Syncs a legacy client's use-item aim to the click (the MineMen behavior). The 1.8 use packet carries no rotation;
 * the click-time aim rides the SAME-TICK flying packet right behind it (the client sends use during input dispatch,
 * look during its entity tick), so Via fills the modern packet's yaw/pitch from the PREVIOUS look - a flick-and-throw
 * launches along the stale aim. Held at queue entry until that tick's flying packet arrives, the use packet is patched
 * from it and the stream reads like a modern client's (whose use packet carries the click aim natively).
 *
 * <p>Runs on the connection's read thread (single-threaded per player). A flying packet without rotation releases
 * unpatched - the aim didn't change that tick, so the stored rotation is already the click aim.
 */
final class UseItemAimSync {

    /** Stale-hold safety: a vanilla client sends a flying packet every tick; past this, release unpatched. */
    private static final long HOLD_TIMEOUT_NANOS = 100_000_000L;

    private @Nullable ClientUseItemPacket held;
    private long heldAt;
    // re-held re-feeds get a fresh instance per flying packet; an identity-tracking re-feeder (lag sim) then never passes the use
    private final ClientPacket[] emitted = new ClientPacket[4];
    private int emittedIndex;

    /** Routes one arriving packet to {@code out}, possibly holding/patching a use packet. {@code gate} = the knob + legacy check, read at use arrival. */
    synchronized void intercept(ClientPacket packet, BooleanSupplier gate, Consumer<ClientPacket> out) {
        if (packet instanceof ClientUseItemPacket && wasEmitted(packet)) {
            out.accept(packet);
            return;
        }
        if (held != null && System.nanoTime() - heldAt > HOLD_TIMEOUT_NANOS) releaseUnpatched(out);
        if (packet instanceof ClientUseItemPacket use) {
            if (held != null) releaseUnpatched(out); // a vanilla 1.8 client can't double-use in a tick; don't stack
            if (gate.getAsBoolean()) {
                held = use;
                heldAt = System.nanoTime();
            } else {
                out.accept(use);
            }
            return;
        }
        if (held == null) {
            out.accept(packet);
            return;
        }
        switch (packet) {
            case ClientPlayerRotationPacket rot -> release(rot.yaw(), rot.pitch(), packet, out);
            case ClientPlayerPositionAndRotationPacket posRot ->
                    release(posRot.position().yaw(), posRot.position().pitch(), packet, out);
            case ClientPlayerPositionPacket ignored -> { releaseUnpatched(out); out.accept(packet); }
            case ClientPlayerPositionStatusPacket ignored -> { releaseUnpatched(out); out.accept(packet); }
            default -> out.accept(packet); // non-flying (swing, chat, ...): pass through, keep holding
        }
    }

    private void release(float yaw, float pitch, ClientPacket flying, Consumer<ClientPacket> out) {
        ClientUseItemPacket patched = new ClientUseItemPacket(held.hand(), held.sequence(), yaw, pitch);
        remember(patched);
        out.accept(patched);
        held = null;
        out.accept(flying);
    }

    private void releaseUnpatched(Consumer<ClientPacket> out) {
        remember(held);
        out.accept(held);
        held = null;
    }

    private void remember(ClientPacket packet) {
        emitted[emittedIndex] = packet;
        emittedIndex = (emittedIndex + 1) % emitted.length;
    }

    private boolean wasEmitted(ClientPacket packet) {
        for (ClientPacket p : emitted) {
            if (p == packet) return true;
        }
        return false;
    }
}
