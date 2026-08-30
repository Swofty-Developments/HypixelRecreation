package io.github.term4.polyp.platform.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientAnimationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionStatusPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A 1.8 client sends use-then-look within one tick, so Via's aim fill is one look stale (the flick-throw desync).
 * The hold waits for that tick's flying packet and takes the click aim from it.
 */
class UseItemAimSyncTest {

    private static final ClientUseItemPacket USE = new ClientUseItemPacket(PlayerHand.MAIN, 7, 10.0f, 20.0f); // Via-filled stale aim

    private final UseItemAimSync sync = new UseItemAimSync();
    private final List<ClientPacket> out = new ArrayList<>();

    private void feed(boolean gate, ClientPacket... packets) {
        for (ClientPacket p : packets) sync.intercept(p, () -> gate, out::add);
    }

    @Test
    void rotationPacketSuppliesTheClickAim() {
        ClientPlayerRotationPacket rot = new ClientPlayerRotationPacket(90.0f, -15.0f, (byte) 1);
        feed(true, USE, rot);
        assertEquals(List.of(new ClientUseItemPacket(PlayerHand.MAIN, 7, 90.0f, -15.0f), rot), out);
    }

    @Test
    void positionAndRotationPacketSuppliesTheClickAim() {
        ClientPlayerPositionAndRotationPacket posRot =
                new ClientPlayerPositionAndRotationPacket(new Pos(1, 2, 3, 45.0f, 30.0f), (byte) 1);
        feed(true, USE, posRot);
        assertEquals(List.of(new ClientUseItemPacket(PlayerHand.MAIN, 7, 45.0f, 30.0f), posRot), out);
    }

    @Test
    void idleFlyingReleasesUnpatched() {
        // no rotation that tick = the aim didn't change at the click, so the Via-filled aim is already right
        ClientPlayerPositionStatusPacket idle = new ClientPlayerPositionStatusPacket((byte) 1);
        feed(true, USE, idle);
        assertEquals(List.of(USE, idle), out);
    }

    @Test
    void positionOnlyFlyingReleasesUnpatched() {
        ClientPlayerPositionPacket pos = new ClientPlayerPositionPacket(new Pos(1, 2, 3), (byte) 1);
        feed(true, USE, pos);
        assertEquals(List.of(USE, pos), out);
    }

    @Test
    void nonFlyingPacketsPassThroughWhileHolding() {
        ClientAnimationPacket swing = new ClientAnimationPacket(PlayerHand.MAIN);
        ClientPlayerRotationPacket rot = new ClientPlayerRotationPacket(90.0f, 0.0f, (byte) 1);
        feed(true, USE, swing, rot);
        assertEquals(List.of(swing, new ClientUseItemPacket(PlayerHand.MAIN, 7, 90.0f, 0.0f), rot), out);
    }

    @Test
    void gateOffPassesStraightThrough() {
        ClientPlayerRotationPacket rot = new ClientPlayerRotationPacket(90.0f, 0.0f, (byte) 1);
        feed(false, USE, rot);
        assertEquals(List.of(USE, rot), out);
    }

    @Test
    void staleHoldReleasesOnTimeout() throws InterruptedException {
        feed(true, USE);
        Thread.sleep(150); // past the 100ms hold cap
        ClientAnimationPacket swing = new ClientAnimationPacket(PlayerHand.MAIN);
        feed(true, swing);
        assertEquals(List.of(USE, swing), out);
    }
}
