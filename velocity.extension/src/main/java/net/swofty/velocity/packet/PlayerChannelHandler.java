package net.swofty.velocity.packet;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.packet.BossBarPacket;
import com.velocitypowered.proxy.protocol.packet.KeepAlivePacket;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import com.velocitypowered.proxy.protocol.packet.RespawnPacket;
import com.velocitypowered.proxy.protocol.packet.chat.SystemChatPacket;
import com.velocitypowered.proxy.protocol.packet.chat.legacy.LegacyChatPacket;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChatPacket;
import com.velocitypowered.proxy.protocol.packet.config.*;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.swofty.velocity.gamemanager.TransferHandler;
import org.tinylog.Logger;

public final class PlayerChannelHandler extends ChannelDuplexHandler {
    private final Player player;
    private RespawnPacket respawn = null;

    public PlayerChannelHandler(final Player player) {
        this.player = player;
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object packet, final ChannelPromise promise) throws Exception {
        try {
            if (new TransferHandler(player).isInLimbo()
                    //&& packet.getClass() != RespawnPacket.class
                    //&& packet.getClass() != JoinGamePacket.class
                    && packet.getClass() != BossBarPacket.class
                    && packet.getClass() != StartUpdatePacket.class
                    && packet.getClass() != KeepAlivePacket.class
                    && packet.getClass() != RegistrySyncPacket.class
                    && packet.getClass() != FinishedUpdatePacket.class
                    && packet.getClass() != PluginMessagePacket.class
                    && packet.getClass() != LegacyChatPacket.class
                    && packet.getClass() != SystemChatPacket.class
                    && packet.getClass() != SessionPlayerChatPacket.class
                    && packet.getClass() != KnownPacksPacket.class
                    && packet.getClass() != TagsUpdatePacket.class
            ) {
                return;
            }
            if (respawn == null && packet.getClass() == RespawnPacket.class) {
                respawn = (RespawnPacket) packet;
            }
            if (packet.getClass() != RespawnPacket.class && new TransferHandler(player).isInLimbo()) {
                if (respawn != null) {
                    write(ctx, respawn, ctx.newPromise());
                }
            }
        } catch (Exception e) {
            // Limbo packet replay is best-effort: a malformed cached respawn
            // shouldn't drop the player. Log at debug so it surfaces during
            // diagnostic runs without spamming production logs.
            Logger.debug(e, "Failed to replay cached respawn during limbo transfer");
        }


        super.write(ctx, packet, promise);
    }
}