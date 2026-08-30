package net.swofty.type.skywarsgame.redis.service;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.game.InstantiateGamePushProtocol;
import net.swofty.commons.protocol.objects.game.InstantiateGamePushProtocol.Request;
import net.swofty.commons.protocol.objects.game.InstantiateGamePushProtocol.Response;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.skywars.map.SkywarsMapsConfig;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class InstantiateGameHandler implements RedisMessageHandler<Request, Response> {

    private static final InstantiateGamePushProtocol PROTOCOL = new InstantiateGamePushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request request, RedisMessageContext context) {
        try {
            SkywarsGameType gameType = SkywarsGameType.from(request.gameType());
            if (gameType == null) {
                return Response.failure("Invalid game type: " + request.gameType());
            }

            SkywarsMapsConfig.MapEntry mapEntry = null;
            boolean hasRequestedMap = request.map() != null && !request.map().isBlank();
            if (TypeSkywarsGameLoader.getMapsConfig() != null) {
                List<SkywarsMapsConfig.MapEntry> availableMaps = TypeSkywarsGameLoader.getMapsConfig().getMaps();
                if (availableMaps == null) availableMaps = List.of();

                if (hasRequestedMap) {
                    for (SkywarsMapsConfig.MapEntry entry : availableMaps) {
                        if (entry != null && (Objects.equals(entry.getId(), request.map())
                                || Objects.equals(entry.getName(), request.map()))) {
                            mapEntry = entry;
                            break;
                        }
                    }
                } else {
                    List<SkywarsMapsConfig.MapEntry> compatibleMaps = availableMaps.stream()
                            .filter(Objects::nonNull)
                            .filter(entry -> TypeSkywarsGameLoader.supportsGameType(entry, gameType))
                            .toList();

                    if (!compatibleMaps.isEmpty()) {
                        mapEntry = compatibleMaps.get(ThreadLocalRandom.current().nextInt(compatibleMaps.size()));
                    }
                }
            }

            if (mapEntry == null) {
                return Response.failure(hasRequestedMap
                        ? "Map not found: " + request.map()
                        : "No compatible maps available for " + gameType);
            }
            if (!TypeSkywarsGameLoader.supportsGameType(mapEntry, gameType)) {
                return Response.failure("Map does not support " + gameType);
            }

            SkywarsGame game = TypeSkywarsGameLoader.createGame(mapEntry, gameType);
            if (game == null) {
                return Response.failure("Server at capacity, cannot create new game");
            }

            return Response.success(game.getGameId(), mapEntry.getName(), gameType.toString());
        } catch (Exception e) {
            return Response.failure("Failed to instantiate game: " + e.getMessage());
        }
    }
}
