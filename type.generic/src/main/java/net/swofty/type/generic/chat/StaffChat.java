package net.swofty.type.generic.chat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.swofty.commons.protocol.objects.proxy.to.StaffChatProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StaffChat {

    private static final StaffChatProtocol PROTOCOL = new StaffChatProtocol();

    public static void sendMessage(HypixelPlayer sender, String message) {
        Text formatted = Text.of("<b>[STAFF] <rank:{}>{}<f>: {}",
                sender.getRank(), sender.getUsername(), message);
        broadcastViaProxy(formatted.serialize());
    }

    public static void sendNotification(String message) {
        broadcastViaProxy(Text.of("<b>[STAFF] <7>{}", message).serialize());
    }

    private static void broadcastViaProxy(String formattedMessage) {
        RedisClient.requestProxy(PROTOCOL, new StaffChatProtocol.Request("message", formattedMessage, null));
    }
}
