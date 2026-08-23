package net.swofty.type.generic.redis;

import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.from.GivePlayersOriginTypeProtocol;
import net.swofty.commons.redis.RedisMessageHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.swofty.commons.redis.RedisMessageContext;

public class RedisOriginServer implements RedisMessageHandler<GivePlayersOriginTypeProtocol.Request, GivePlayersOriginTypeProtocol.Response> {
    public static final Map<UUID, ServerType> origin = new ConcurrentHashMap<>();

    public static ServerType consume(UUID uuid) {
        return origin.remove(uuid);
    }

    @Override
    public RedisProtocol<GivePlayersOriginTypeProtocol.Request, GivePlayersOriginTypeProtocol.Response> protocol() {
        return new GivePlayersOriginTypeProtocol();
    }

    @Override
    public GivePlayersOriginTypeProtocol.Response handle(GivePlayersOriginTypeProtocol.Request message, RedisMessageContext context) {
        UUID uuid = UUID.fromString(message.uuid());
        ServerType originType = ServerType.valueOf(message.originType());
        origin.put(uuid, originType);
        return new GivePlayersOriginTypeProtocol.Response();
    }
}