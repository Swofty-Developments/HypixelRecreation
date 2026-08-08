package io.github.term4.polyp.platform.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientAnimationPacket;
import net.minestom.server.network.packet.client.play.ClientAttackPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionStatusPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A 1.8 client sends attack-then-look within one tick, so an ungated attack lands one look stale (the flick-hit
 * desync). The hold releases the attack after that tick's look is applied but before the move.
 */
class AttackAimSyncTest {

    private static final ClientAttackPacket ATTACK = new ClientAttackPacket(42);

    private final AttackAimSync sync = new AttackAimSync();
    private final List<ClientPacket> out = new ArrayList<>();

    private void feed(boolean gate, ClientPacket... packets) {
        for (ClientPacket p : packets) sync.intercept(p, () -> gate, out::add);
    }

    @Test
    void rotationIsAppliedBeforeTheAttack() {
        ClientPlayerRotationPacket rot = new ClientPlayerRotationPacket(90.0f, -15.0f, (byte) 1);
        feed(true, ATTACK, rot);
        assertEquals(List.of(rot, ATTACK), out);
    }

    @Test
    void positionAndRotationSplitsLookBeforeAttackMoveAfter() {
        ClientPlayerPositionAndRotationPacket posRot =
                new ClientPlayerPositionAndRotationPacket(new Pos(1, 2, 3, 45.0f, 30.0f), (byte) 1);
        feed(true, ATTACK, posRot);
        // synthetic look-only update keeps the pre-move position
        assertEquals(List.of(new ClientPlayerRotationPacket(45.0f, 30.0f, (byte) 1), ATTACK, posRot), out);
    }

    @Test
    void positionOnlyReleasesTheAttackBeforeTheMove() {
        ClientPlayerPositionPacket pos = new ClientPlayerPositionPacket(new Pos(1, 2, 3), (byte) 1);
        feed(true, ATTACK, pos);
        assertEquals(List.of(ATTACK, pos), out);
    }

    @Test
    void idleFlyingReleasesTheAttackBeforeStatus() {
        ClientPlayerPositionStatusPacket idle = new ClientPlayerPositionStatusPacket((byte) 1);
        feed(true, ATTACK, idle);
        assertEquals(List.of(ATTACK, idle), out);
    }

    @Test
    void nonFlyingPacketsPassThroughWhileHolding() {
        ClientAnimationPacket swing = new ClientAnimationPacket(PlayerHand.MAIN);
        ClientPlayerRotationPacket rot = new ClientPlayerRotationPacket(90.0f, 0.0f, (byte) 1);
        feed(true, ATTACK, swing, rot);
        assertEquals(List.of(swing, rot, ATTACK), out);
    }

    @Test
    void gateOffPassesStraightThrough() {
        ClientPlayerRotationPacket rot = new ClientPlayerRotationPacket(90.0f, 0.0f, (byte) 1);
        feed(false, ATTACK, rot);
        assertEquals(List.of(ATTACK, rot), out);
    }

    @Test
    void staleHoldReleasesOnTimeout() throws InterruptedException {
        feed(true, ATTACK);
        Thread.sleep(150); // past the 100ms hold cap
        ClientAnimationPacket swing = new ClientAnimationPacket(PlayerHand.MAIN);
        feed(true, swing);
        assertEquals(List.of(ATTACK, swing), out);
    }
}
