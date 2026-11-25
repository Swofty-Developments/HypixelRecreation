package net.swofty.service.replay.endpoints;

import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.replay.ReplayListProtocolObject;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.service.replay.ReplayService;
import org.bson.Document;
import org.tinylog.Logger;

import java.util.*;

public class ReplayListEndpoint implements RedisMessageHandler<
        ReplayListProtocolObject.ListRequest,
        ReplayListProtocolObject.ListResponse> {

    @Override
    public ReplayListProtocolObject protocol() {
        return new ReplayListProtocolObject();
    }

    @Override
    public ReplayListProtocolObject.ListResponse handle(ReplayListProtocolObject.ListRequest msg, RedisMessageContext context) {

        try {
            List<Document> docs = ReplayService.getDatabase().getReplaysByPlayer(msg.playerId(), msg.limit());

            List<ReplayListProtocolObject.ReplaySummary> replays = new ArrayList<>();
            for (Document doc : docs) {
                Map<UUID, String> players = new HashMap<>();
                List<Document> participants = doc.getList("participants", Document.class, List.of());
                for (Document participant : participants) {
                    players.put(UUID.fromString(participant.getString("uuid")), participant.getString("username"));
                }

                replays.add(new ReplayListProtocolObject.ReplaySummary(
                        UUID.fromString(doc.getString("replayId")),
                        doc.getString("gameId"),
                        ServerType.valueOf(doc.getString("serverType")),
                        doc.getString("serverId"),
                        doc.getString("gameType"),
                        doc.getString("mapName"),
                        doc.getLong("startTime"),
                        doc.getLong("endTime"),
                        doc.getInteger("durationTicks"),
                        players.size(),
                        players,
                        doc.getString("winnerId"),
                        doc.getString("winnerType"),
                        doc.getLong("dataSize")
                ));
            }

            Logger.debug("Returning {} replays for player {}", replays.size(), msg.playerId());
            return new ReplayListProtocolObject.ListResponse(true, replays);

        } catch (Exception e) {
            Logger.error(e, "Failed to fetch replays for player {}", msg.playerId());
            return new ReplayListProtocolObject.ListResponse(false, List.of());
        }
    }
}
