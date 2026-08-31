package net.swofty.type.replayviewer.redis.service;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.game.ViewReplayPushProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TypedViewReplayHandler implements RedisMessageHandler<ViewReplayPushProtocol.Request, ViewReplayPushProtocol.Response> {
    public static Map<UUID, String> replay = new ConcurrentHashMap<>();
    public static Map<UUID, String> shareCode = new ConcurrentHashMap<>();

    private static final ViewReplayPushProtocol PROTOCOL = new ViewReplayPushProtocol();

    @Override
    public RedisProtocol<ViewReplayPushProtocol.Request, ViewReplayPushProtocol.Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public ViewReplayPushProtocol.Response handle(ViewReplayPushProtocol.Request request, RedisMessageContext context) {
        UUID uuid = request.uuid();
        String gameId = request.replayId();
        String code = request.shareCode();

        replay.put(uuid, gameId);
        if (code != null && !code.isEmpty()) {
            shareCode.put(uuid, code);
        }
        return new ViewReplayPushProtocol.Response(true);
    }

    @Nullable
    public static String getAndRemoveShareCode(UUID uuid) {
        return shareCode.remove(uuid);
    }
}
