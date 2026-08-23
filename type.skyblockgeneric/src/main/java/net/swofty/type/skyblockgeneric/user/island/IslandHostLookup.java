package net.swofty.type.skyblockgeneric.user.island;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.swofty.commons.ServerType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.protocol.objects.proxy.from.DoesServerHaveIslandProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.proxyapi.ProxyInformation;
import net.swofty.type.generic.HypixelConst;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IslandHostLookup {
    private static final long LOOKUP_TIMEOUT_MILLIS = 3000;

    public static @Nullable UUID findRemoteHost(UUID islandId) {
        List<UnderstandableProxyServer> servers = islandServers();
        if (servers.isEmpty()) return null;

        UUID localServer = HypixelConst.getServerUUID();
        Map<UUID, CompletableFuture<DoesServerHaveIslandProtocol.Response>> responses = new LinkedHashMap<>();

        for (UnderstandableProxyServer server : servers) {
            if (server.uuid().equals(localServer)) continue;
            responses.put(server.uuid(), RedisClient.requestServer(
                    server.uuid(),
                    new DoesServerHaveIslandProtocol(),
                    new DoesServerHaveIslandProtocol.Request(islandId.toString())));
        }

        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(LOOKUP_TIMEOUT_MILLIS);
        for (Map.Entry<UUID, CompletableFuture<DoesServerHaveIslandProtocol.Response>> entry : responses.entrySet()) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) break;

            try {
                if (entry.getValue().get(remaining, TimeUnit.NANOSECONDS).serverHasIt()) return entry.getKey();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception ignoredException) {
            }
        }

        return null;
    }

    private static List<UnderstandableProxyServer> islandServers() {
        try {
            return new ProxyInformation()
                    .getServerInformation(ServerType.SKYBLOCK_ISLAND)
                    .get(LOOKUP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (Exception exception) {
            Logger.error(exception, "Failed to list island servers from the proxy");
            return List.of();
        }
    }
}
