package net.swofty.service.replay.endpoints;

import net.swofty.commons.protocol.objects.replay.ReplayDataBatchProtocolObject;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.replay.protocol.ReplayChunk;
import net.swofty.service.replay.ReplayService;
import org.tinylog.Logger;

public class ReplayDataBatchEndpoint implements RedisMessageHandler<ReplayDataBatchProtocolObject.BatchMessage, ReplayDataBatchProtocolObject.BatchResponse> {
    @Override
    public ReplayDataBatchProtocolObject protocol() {
        return new ReplayDataBatchProtocolObject();
    }

    @Override
    public ReplayDataBatchProtocolObject.BatchResponse handle(ReplayDataBatchProtocolObject.BatchMessage msg, RedisMessageContext context) {
        try {
            ReplayChunk chunk = new ReplayChunk(msg.section(), msg.sequence(), msg.startTick(), msg.endTick(),
                    msg.uncompressedLength(), msg.recordCount(), msg.checksum(), msg.compressedPayload());
            ReplayService.getSessionManager().receiveChunk(msg.replayId(), chunk);
            return new ReplayDataBatchProtocolObject.BatchResponse(true, msg.compressedPayload().length);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to process replay chunk {} for {}", msg.sequence(), msg.replayId());
            return new ReplayDataBatchProtocolObject.BatchResponse(false, 0);
        }
    }
}
