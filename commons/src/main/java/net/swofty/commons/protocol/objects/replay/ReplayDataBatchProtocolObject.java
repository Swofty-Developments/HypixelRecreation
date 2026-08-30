package net.swofty.commons.protocol.objects.replay;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.replay.protocol.ReplaySection;

import java.util.UUID;

public class ReplayDataBatchProtocolObject extends RedisProtocol<
        ReplayDataBatchProtocolObject.BatchMessage,
        ReplayDataBatchProtocolObject.BatchResponse> {

    @Override
    public Serializer<BatchMessage> getSerializer() {
        return new JacksonSerializer<>(BatchMessage.class);
    }

    @Override
    public Serializer<BatchResponse> getReturnSerializer() {
        return new JacksonSerializer<>(BatchResponse.class);
    }

    public record BatchMessage(
            UUID replayId,
            ReplaySection section,
            int sequence,
            int startTick,
            int endTick,
            int uncompressedLength,
            int recordCount,
            int checksum,
            byte[] compressedPayload
    ) {
        public BatchMessage {
            compressedPayload = compressedPayload.clone();
        }

        @Override
        public byte[] compressedPayload() {
            return compressedPayload.clone();
        }
    }

    public record BatchResponse(boolean success, long bytesReceived) {}
}
