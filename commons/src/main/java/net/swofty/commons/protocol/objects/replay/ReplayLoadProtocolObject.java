package net.swofty.commons.protocol.objects.replay;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.replay.protocol.ReplayChunk;

import java.util.List;
import java.util.UUID;

public class ReplayLoadProtocolObject extends RedisProtocol<
        ReplayLoadProtocolObject.LoadRequest,
        ReplayLoadProtocolObject.LoadResponse> {

    @Override
    public Serializer<LoadRequest> getSerializer() {
        return new JacksonSerializer<>(LoadRequest.class);
    }

    @Override
    public Serializer<LoadResponse> getReturnSerializer() {
        return new JacksonSerializer<>(LoadResponse.class);
    }

    public record LoadRequest(UUID replayId) {}

    public record LoadResponse(
            boolean success,
            String errorMessage,
            ReplayProtocolDto.Metadata metadata,
            List<ReplayChunk> chunks
    ) {}
}
