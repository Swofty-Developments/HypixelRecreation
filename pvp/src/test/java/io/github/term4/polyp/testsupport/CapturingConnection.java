package io.github.term4.polyp.testsupport;

import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.PlayerConnection;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** A connection that only records what was sent - for players built outside the join flow. */
public final class CapturingConnection extends PlayerConnection {

    public final List<SendablePacket> sent = new CopyOnWriteArrayList<>();

    @Override
    public void sendPacket(SendablePacket packet) { sent.add(packet); }

    @Override
    public SocketAddress getRemoteAddress() { return new InetSocketAddress("localhost", 25565); }

    @Override
    public void disconnect() {}
}
