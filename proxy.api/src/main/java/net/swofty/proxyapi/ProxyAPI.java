package net.swofty.proxyapi;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.redis.*;
import net.swofty.redisapi.api.RedisAPI;
import net.swofty.redisapi.api.requests.DataRequestResponder;
import org.json.JSONObject;

import java.util.UUID;

public class ProxyAPI {
    private final UUID serverUUID;

    public ProxyAPI(String URI, UUID serverUUID) {
        this.serverUUID = serverUUID;

        RedisAPI.generateInstance(URI);
        RedisAPI.getInstance().setFilterId(serverUUID.toString());
        RedisClient.identify(RedisEndpoint.server(serverUUID));

        DataRequestResponder.create("player-transfer-data", request -> {
            UUID playerUuid = UUID.fromString(request.getString("uuid"));
            PlayerTransferDataCache.put(
                    playerUuid,
                    request.getString("account_document"),
                    request.optString("profile_document", null)
            );
            return new JSONObject().put("cached", true);
        });
    }

    public <T, R> void registerProxyHandler(RedisMessageHandler<T, R> handler) {
        RedisProtocol<T, R> protocol = handler.protocol();

        RedisMessageBus.registerHandler(
                RedisEndpoint.server(serverUUID),
                RedisChannels.protocol(protocol),
                handler,
                (envelope, channel) -> RedisMessageContext.between(
                        UUID.fromString(envelope.id()),
                        originOf(envelope),
                        RedisEndpoint.server(serverUUID),
                        protocol.channel()
                ),
                RedisEnvelope::from,
                envelope -> RedisChannels.protocol(protocol)
        );
    }

    private static RedisEndpoint originOf(RedisEnvelope envelope) {
        return RedisEndpoint.proxy().id().equals(envelope.from())
                ? RedisEndpoint.proxy()
                : RedisEndpoint.server(envelope.from());
    }

    public <T, R> void registerServiceHandler(RedisMessageHandler<T, R> handler) {
        RedisProtocol<T, R> protocol = handler.protocol();

        RedisMessageBus.registerHandler(
                RedisEndpoint.server(serverUUID),
                RedisChannels.serviceRequest(protocol),
                handler,
                (envelope, channel) -> RedisMessageContext.between(
                        UUID.fromString(envelope.id()),
                        RedisEndpoint.service(envelope.from()),
                        RedisEndpoint.server(serverUUID),
                        protocol.channel()
                ),
                envelope -> envelope.from(),
                envelope -> RedisChannels.SERVICE_RESPONSE
        );

        RedisMessageBus.registerHandler(
                RedisEndpoint.server(serverUUID),
                RedisChannels.serviceBroadcast(protocol),
                handler,
                (envelope, channel) -> RedisMessageContext.between(
                        UUID.fromString(envelope.id()),
                        RedisEndpoint.service(envelope.from()),
                        RedisEndpoint.server(serverUUID),
                        protocol.channel()
                ).asBroadcast(),
                envelope -> envelope.from(),
                envelope -> RedisChannels.SERVICE_BROADCAST_RESPONSE
        );
    }

    public void start() {
        RedisAPI.getInstance().startListeners();
    }
}
