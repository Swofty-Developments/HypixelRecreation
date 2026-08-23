package net.swofty.type.generic.redis.service;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.messaging.SendMessagePushProtocol;
import net.swofty.commons.protocol.objects.messaging.SendMessagePushProtocol.Request;
import net.swofty.commons.protocol.objects.messaging.SendMessagePushProtocol.Response;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.commons.redis.RedisMessageContext;

public class SendMessageHandler implements RedisMessageHandler<Request, Response> {

    private static final SendMessagePushProtocol PROTOCOL = new SendMessagePushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request message, RedisMessageContext context) {
        try {
            HypixelPlayer player = HypixelGenericLoader.getLoadedPlayers().stream()
                    .filter(p -> p.getUuid().equals(message.playerUUID()))
                    .findFirst()
                    .orElse(null);

            if (player != null) {
                String raw = message.message();
                player.sendMessage(raw);
                return new Response(true, null);
            }

            return new Response(false, "Player not found or error");
        } catch (Exception e) {
            return new Response(false, "Player not found or error");
        }
    }
}
