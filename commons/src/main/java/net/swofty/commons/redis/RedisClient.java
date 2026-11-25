package net.swofty.commons.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.PingProtocol;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisClient {
    private static final Map<String, RedisProtocol<?, ?>> protocolsByRequest = new ConcurrentHashMap<>();
    private static volatile RedisEndpoint localEndpoint;

    public static void identify(RedisEndpoint endpoint) {
        localEndpoint = endpoint;
    }

    public static RedisEndpoint localEndpoint() {
        if (localEndpoint == null) {
            throw new IllegalStateException("RedisClient local endpoint has not been identified");
        }
        return localEndpoint;
    }

    public static void registerResponseProtocol(RedisProtocol<?, ?> protocol) {
        protocolsByRequest.put(requestTypeName(protocol), protocol);
        RedisMessageBus.registerResponseChannel(RedisChannels.protocol(protocol));
    }

    public static void registerResponseChannel(String channel) {
        RedisMessageBus.registerResponseChannel(channel);
    }

    public static void registerResponseProtocols(Iterable<? extends RedisProtocol<?, ?>> protocols) {
        protocols.forEach(RedisClient::registerResponseProtocol);
    }

    public static <T, R> CompletableFuture<R> requestProxy(RedisProtocol<T, R> protocol, T message) {
        return request(RedisEndpoint.proxy().id(), RedisChannels.protocol(protocol), RedisChannels.protocol(protocol), protocol, message);
    }

    public static <T, R> CompletableFuture<R> requestServer(UUID server, RedisProtocol<T, R> protocol, T message) {
        return request(server.toString(), RedisChannels.protocol(protocol), RedisChannels.protocol(protocol), protocol, message);
    }

    public static <T, R> CompletableFuture<R> requestService(ServiceType service, RedisProtocol<T, R> protocol, T message) {
        return request(service.name(), RedisChannels.protocol(protocol), RedisChannels.protocol(protocol), protocol, message);
    }

    public static <T, R> CompletableFuture<R> requestService(ServiceType service, T message) {
        RedisProtocol<T, R> protocol = protocolFor(message);
        return requestService(service, protocol, message);
    }

    public static <T, R> CompletableFuture<R> requestServiceFromServer(ServiceType service, RedisProtocol<T, R> protocol, T message) {
        return requestService(service, protocol, message);
    }

    public static <T> void publishService(ServiceType service, RedisProtocol<T, ?> protocol, T message) {
        RedisMessageBus.publish(localEndpoint(), service.name(), RedisChannels.protocol(protocol), protocol, message);
    }

    public static <T> void publishAllServices(RedisProtocol<T, ?> protocol, T message) {
        for (ServiceType serviceType : ServiceType.values()) {
            publishService(serviceType, protocol, message);
        }
    }

    public static <T, R> CompletableFuture<R> requestServerFromService(UUID server,
                                                                        RedisProtocol<T, R> protocol,
                                                                        T message) {
        return RedisMessageBus.request(
                localEndpoint(),
                server.toString(),
                RedisChannels.serviceRequest(protocol),
                RedisChannels.SERVICE_RESPONSE,
                protocol,
                message
        );
    }

    public static <T, R> CompletableFuture<Map<UUID, R>> requestAllServersFromService(RedisProtocol<T, R> protocol,
                                                                                       T message,
                                                                                       int timeoutMs) {
        return RedisMessageBus.requestBroadcast(
                localEndpoint(),
                RedisChannels.ALL_SERVERS,
                RedisChannels.serviceBroadcast(protocol),
                RedisChannels.SERVICE_BROADCAST_RESPONSE,
                protocol,
                message,
                timeoutMs
        );
    }

    public static <T, R> CompletableFuture<Map<UUID, R>> requestServersFromService(Iterable<UUID> servers,
                                                                                    RedisProtocol<T, R> protocol,
                                                                                    T message) {
        Map<UUID, CompletableFuture<R>> futures = new ConcurrentHashMap<>();
        for (UUID server : servers) {
            futures.put(server, requestServerFromService(server, protocol, message));
        }

        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .thenApply(ignored -> {
                    Map<UUID, R> responses = new ConcurrentHashMap<>();
                    futures.forEach((server, future) -> {
                        try {
                            responses.put(server, future.get());
                        } catch (Exception ignoredException) {
                        }
                    });
                    return responses;
                });
    }

    public static CompletableFuture<Boolean> isServiceOnline(ServiceType service) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        AtomicBoolean responded = new AtomicBoolean(false);

        requestService(service, new PingProtocol(), new PingProtocol.EmptyMessage()).thenAccept(response -> {
            responded.set(true);
            future.complete(true);
        });

        CompletableFuture.delayedExecutor(150, TimeUnit.MILLISECONDS).execute(() -> {
            if (!responded.get()) {
                future.complete(false);
            }
        });

        return future;
    }

    @SuppressWarnings("unchecked")
    public static <T, R> RedisProtocol<T, R> protocolFor(T request) {
        RedisProtocol<?, ?> protocol = protocolsByRequest.get(request.getClass().getSimpleName());
        if (protocol == null) {
            throw new IllegalArgumentException("No Redis protocol registered for " + request.getClass().getSimpleName());
        }
        return (RedisProtocol<T, R>) protocol;
    }

    private static <T, R> CompletableFuture<R> request(String target,
                                                       String requestChannel,
                                                       String responseChannel,
                                                       RedisProtocol<T, R> protocol,
                                                       T message) {
        return RedisMessageBus.request(localEndpoint(), target, requestChannel, responseChannel, protocol, message);
    }

    private static String requestTypeName(RedisProtocol<?, ?> protocol) {
        Type genericSuperclass = protocol.getClass().getGenericSuperclass();

        if (genericSuperclass instanceof ParameterizedType paramType) {
            Type[] typeArguments = paramType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                Type firstTypeArg = typeArguments[0];
                if (firstTypeArg instanceof Class) {
                    return ((Class<?>) firstTypeArg).getSimpleName();
                }
                return firstTypeArg.getTypeName();
            }
        }

        throw new IllegalArgumentException("Could not determine request type for " + protocol.getClass().getName());
    }
}
