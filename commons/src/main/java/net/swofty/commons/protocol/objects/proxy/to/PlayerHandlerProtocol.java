package net.swofty.commons.protocol.objects.proxy.to;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PlayerHandlerProtocol extends RedisProtocol<
        PlayerHandlerProtocol.Request,
        PlayerHandlerProtocol.Response> {
    private static final Serializer<Request> SERIALIZER =
            new JacksonSerializer<>(Request.class);
    private static final Serializer<Response> RETURN_SERIALIZER =
            new JacksonSerializer<>(Response.class);

    @Override
    public Serializer<Request> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<Response> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public enum Action {
        TRANSFER,
        RESOLVE_TRANSFER,
        TELEPORT,
        VERSION,
        IS_ONLINE,
        EVENT,
        MESSAGE,
        TRANSFER_WITH_UUID,
        GET_SERVER,
        LIMBO
    }

    public record Request(String uuid, Action action, Map<String, Object> data) {}
    public record Response(Map<String, Object> data, boolean success, @Nullable String error) {}
}
