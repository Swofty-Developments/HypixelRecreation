package net.swofty.service.replay.endpoints;

import net.swofty.commons.protocol.objects.replay.ReplayStartProtocolObject;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.replay.protocol.ReplayFormat;
import net.swofty.service.replay.ReplayService;
import org.tinylog.Logger;

public class ReplayStartEndpoint implements RedisMessageHandler<ReplayStartProtocolObject.StartMessage, ReplayStartProtocolObject.StartResponse> {
    @Override
    public ReplayStartProtocolObject protocol() {
        return new ReplayStartProtocolObject();
    }

    @Override
    public ReplayStartProtocolObject.StartResponse handle(ReplayStartProtocolObject.StartMessage msg, RedisMessageContext context) {
        try {
            var metadata = msg.metadata();
            if (metadata == null || metadata.descriptor() == null || metadata.gameMetadata() == null) {
                throw new IllegalArgumentException("Replay metadata is incomplete");
            }
            if (metadata.descriptor().formatVersion() != ReplayFormat.MAJOR_VERSION) {
                throw new IllegalArgumentException("Unsupported replay format version: " + metadata.descriptor().formatVersion());
            }
            if (!metadata.descriptor().gameType().equals(metadata.gameMetadata().gameType())) {
                throw new IllegalArgumentException("Replay game metadata type mismatch");
            }
            if (!ReplayService.getSessionManager().isFinalized(metadata.descriptor().replayId())) {
                ReplayService.getSessionManager().startSession(metadata);
            }
            return new ReplayStartProtocolObject.StartResponse(true, null);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to start replay session");
            return new ReplayStartProtocolObject.StartResponse(false, exception.getMessage());
        }
    }
}
