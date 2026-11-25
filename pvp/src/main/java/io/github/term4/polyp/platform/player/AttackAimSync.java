package io.github.term4.polyp.platform.player;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientAttackPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionStatusPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * <b>Experimental</b>: syncs an attacker's knockback aim to the click - the attack analog of {@link UseItemAimSync},
 * gated by {@code KnockbackConfig.experimentalAimSync}. Knockback's aim-weighted part reads the attacker's server look
 * when the attack packet is processed; the attack packet carries no rotation and lands a beat before the same-tick
 * flying packet on EVERY client version (attack goes out during input dispatch, look during the entity tick), so that
 * component fires along the STALE look. Only the LOOK of that flying packet is applied before the held attack - the
 * position stays pre-move, so the position-delta base is untouched (vanilla-exact); a position+rotation packet is split
 * into a synthetic look-only update for that. Real servers keep the one-packet lag, so no preset enables it.
 *
 * <p>Runs on the connection's read thread (single-threaded per player). A flying packet without rotation releases the
 * attack unchanged ahead of the move - the look didn't change that tick, so the stored aim is already the click aim.
 */
final class AttackAimSync {

    /** Stale-hold safety: a vanilla client sends a flying packet every tick; past this, release unheld. */
    private static final long HOLD_TIMEOUT_NANOS = 100_000_000L;

    private @Nullable ClientAttackPacket held;
    private long heldAt;

    /** Routes one arriving packet to {@code out}, possibly holding an attack until its tick's look is applied. */
    void intercept(ClientPacket packet, BooleanSupplier gate, Consumer<ClientPacket> out) {
        if (held != null && System.nanoTime() - heldAt > HOLD_TIMEOUT_NANOS) releaseHeld(out);
        if (packet instanceof ClientAttackPacket attack) {
            if (held != null) releaseHeld(out); // a vanilla 1.8 client can't double-attack in a tick; don't stack
            if (gate.getAsBoolean()) {
                held = attack;
                heldAt = System.nanoTime();
            } else {
                out.accept(attack);
            }
            return;
        }
        if (held == null) {
            out.accept(packet);
            return;
        }
        switch (packet) {
            // look changed, position didn't: apply it, then the attack reads the click aim
            case ClientPlayerRotationPacket rot -> { out.accept(rot); releaseHeld(out); }
            // position + look: apply the look alone first (position stays pre-move for the away-vector), attack, then the move
            case ClientPlayerPositionAndRotationPacket posRot -> {
                out.accept(new ClientPlayerRotationPacket(posRot.position().yaw(), posRot.position().pitch(), posRot.flags()));
                releaseHeld(out);
                out.accept(posRot);
            }
            // no look change this tick: the stored aim is already the click aim - release ahead of the move (keeps the pre-move position)
            case ClientPlayerPositionPacket ignored -> { releaseHeld(out); out.accept(packet); }
            case ClientPlayerPositionStatusPacket ignored -> { releaseHeld(out); out.accept(packet); }
            default -> out.accept(packet); // non-flying (swing, chat, ...): pass through, keep holding
        }
    }

    private void releaseHeld(Consumer<ClientPacket> out) {
        out.accept(held);
        held = null;
    }
}
