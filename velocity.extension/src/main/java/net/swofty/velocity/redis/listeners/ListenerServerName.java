package net.swofty.velocity.redis.listeners;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.to.RequestServerNameProtocol;
import net.swofty.velocity.gamemanager.GameManager;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;

import java.util.UUID;

public class ListenerServerName implements RedisMessageHandler<
        RequestServerNameProtocol.Request,
        RequestServerNameProtocol.Response> {

    @Override
    public RedisProtocol<RequestServerNameProtocol.Request, RequestServerNameProtocol.Response> protocol() {
        return new RequestServerNameProtocol();
    }

    @Override
    public RequestServerNameProtocol.Response handle(RequestServerNameProtocol.Request message, RedisMessageContext context) {
        GameManager.GameServer server = GameManager.getFromUUID(UUID.fromString(context.origin().id()));
        if (server == null) {
            return new RequestServerNameProtocol.Response(null, null, false,
                    "Server " + context.origin().id() + " is not registered with the proxy");
        }

        return new RequestServerNameProtocol.Response(server.displayName(), server.shortDisplayName(), true, null);
    }
}
