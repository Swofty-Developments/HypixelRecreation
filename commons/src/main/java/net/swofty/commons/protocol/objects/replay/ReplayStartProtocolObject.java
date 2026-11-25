package net.swofty.commons.protocol.objects.replay;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;

public class ReplayStartProtocolObject extends RedisProtocol<
        ReplayStartProtocolObject.StartMessage,
        ReplayStartProtocolObject.StartResponse> {

    @Override
    public Serializer<StartMessage> getSerializer() {
        return new JacksonSerializer<>(StartMessage.class);
    }

    @Override
    public Serializer<StartResponse> getReturnSerializer() {
        return new JacksonSerializer<>(StartResponse.class);
    }

    public record StartMessage(
            ReplayProtocolDto.Metadata metadata
    ) {}

    public record StartResponse(boolean success, String message) {}

}
