package net.swofty.commons.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.redisapi.api.ChannelRegistry;
import net.swofty.redisapi.api.RedisAPI;
import net.swofty.redisapi.events.EventRegistry;
import org.tinylog.Logger;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisMessageBus {
    private static final Map<String, List<RegisteredHandler<?, ?>>> handlers = new ConcurrentHashMap<>();
    private static final Map<UUID, CompletableFuture<String>> pendingResponses = new ConcurrentHashMap<>();
    private static final Map<UUID, BroadcastCollector> pendingBroadcasts = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> registeredChannels = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "redis-message-bus-timeouts");
        thread.setDaemon(true);
        return thread;
    });

    public static <T, R> void registerHandler(RedisEndpoint localEndpoint,
                                              String channel,
                                              RedisMessageHandler<T, R> handler,
                                              ContextFactory contextFactory,
                                              Function<RedisEnvelope, String> responseTarget,
                                              Function<RedisEnvelope, String> responseChannel) {
        handlers.computeIfAbsent(channel, ignored -> new CopyOnWriteArrayList<>())
                .add(new RegisteredHandler<>(localEndpoint, handler, contextFactory, responseTarget, responseChannel));
        ensureChannelRegistered(channel);
    }

    public static void registerResponseChannel(String channel) {
        ensureChannelRegistered(channel);
    }

    public static <T, R> CompletableFuture<R> request(RedisEndpoint origin,
                                                       String target,
                                                       String requestChannel,
                                                       String responseChannel,
                                                       RedisProtocol<T, R> protocol,
                                                       T message) {
        return requestRaw(origin, target, requestChannel, responseChannel, protocol, message)
                .thenApply(protocol::translateReturnFromString);
    }

    public static CompletableFuture<String> requestRaw(RedisEndpoint origin,
                                                       String target,
                                                       String requestChannel,
                                                       String responseChannel,
                                                       RedisProtocol protocol,
                                                       Object message) {
        UUID requestId = UUID.randomUUID();
        CompletableFuture<String> future = new CompletableFuture<>();

        ensureChannelRegistered(responseChannel);
        pendingResponses.put(requestId, future);
        future.orTimeout(10, TimeUnit.SECONDS)
                .whenComplete((ignored, throwable) -> pendingResponses.remove(requestId));

        publishRaw(requestId, origin, target, requestChannel, protocol.translateToString(message));
        return future;
    }

    public static void publish(RedisEndpoint origin,
                               String target,
                               String channel,
                               RedisProtocol protocol,
                               Object message) {
        publishRaw(UUID.randomUUID(), origin, target, channel, protocol.translateToString(message));
    }

    public static <T, R> CompletableFuture<Map<UUID, R>> requestBroadcast(RedisEndpoint origin,
                                                                          String target,
                                                                          String requestChannel,
                                                                          String responseChannel,
                                                                          RedisProtocol<T, R> protocol,
                                                                          T message,
                                                                          int timeoutMs) {
        UUID requestId = UUID.randomUUID();
        BroadcastCollector collector = new BroadcastCollector();
        pendingBroadcasts.put(requestId, collector);
        ensureChannelRegistered(responseChannel);

        publishRaw(requestId, origin, target, requestChannel, protocol.translateToString(message));

        scheduler.schedule(() -> {
            BroadcastCollector removed = pendingBroadcasts.remove(requestId);
            if (removed != null) {
                removed.complete();
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);

        return collector.future().thenApply(rawResponses -> {
            Map<UUID, R> typedResponses = new ConcurrentHashMap<>();
            rawResponses.forEach((uuid, payload) -> {
                try {
                    typedResponses.put(uuid, protocol.translateReturnFromString(payload));
                } catch (Exception exception) {
                    Logger.error(exception, "Failed to deserialize Redis broadcast response from {}", uuid);
                }
            });
            return typedResponses;
        });
    }

    public static void publishRaw(UUID requestId,
                                  RedisEndpoint origin,
                                  String target,
                                  String channel,
                                  String payload) {
        RedisAPI.getInstance().publishMessage(
                target,
                ChannelRegistry.getFromName(channel),
                new RedisEnvelope(requestId.toString(), origin.id(), payload).serialize()
        ).exceptionally(throwable -> {
            Logger.error(throwable, "Failed to publish Redis message on channel {}", channel);
            return null;
        });
    }

    private static void ensureChannelRegistered(String channel) {
        if (registeredChannels.putIfAbsent(channel, true) != null) return;

        RedisAPI.getInstance().registerChannel(channel, event -> {
            RedisEnvelope envelope = unwrap(event.message);
            UUID requestId = UUID.fromString(envelope.id());

            CompletableFuture<String> pendingResponse = pendingResponses.remove(requestId);
            if (pendingResponse != null) {
                String payload = envelope.payload();
                Thread.startVirtualThread(() -> pendingResponse.complete(payload));
                return;
            }

            BroadcastCollector collector = pendingBroadcasts.get(requestId);
            if (collector != null) {
                collector.add(UUID.fromString(envelope.from()), envelope.payload());
                return;
            }

            List<RegisteredHandler<?, ?>> channelHandlers = handlers.get(event.channel);
            if (channelHandlers == null) return;

            channelHandlers.forEach(handler -> Thread.startVirtualThread(() -> handler.handle(envelope, event.channel)));
        });

        subscribeIfListening(channel);
    }

    private static void subscribeIfListening(String channel) {
        JedisPubSub pubSub = EventRegistry.pubSub;
        if (pubSub == null || !pubSub.isSubscribed()) return;

        try {
            pubSub.subscribe(channel);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to subscribe to Redis channel {}", channel);
        }
    }

    private static RedisEnvelope unwrap(String message) {
        int split = message.indexOf(";");
        return RedisEnvelope.deserialize(split == -1 ? message : message.substring(split + 1));
    }

    @FunctionalInterface
    public interface ContextFactory {
        RedisMessageContext create(RedisEnvelope envelope, String channel);
    }

    private record RegisteredHandler<T, R>(
            RedisEndpoint localEndpoint,
            RedisMessageHandler<T, R> handler,
            ContextFactory contextFactory,
            Function<RedisEnvelope, String> responseTarget,
            Function<RedisEnvelope, String> responseChannel
    ) {
        private void handle(RedisEnvelope envelope, String channel) {
            RedisProtocol<T, R> protocol = handler.protocol();

            try {
                T message = protocol.translateFromString(envelope.payload());
                RedisMessageContext context = contextFactory.create(envelope, channel);
                R response = handler.handle(message, context);
                if (response == null) return;

                publishRaw(
                        UUID.fromString(envelope.id()),
                        localEndpoint,
                        responseTarget.apply(envelope),
                        responseChannel.apply(envelope),
                        protocol.translateReturnToString(response)
                );
            } catch (Exception exception) {
                Logger.error(exception, "Failed to handle Redis message on channel {}", channel);
            }
        }
    }

    private static final class BroadcastCollector {
        private final CompletableFuture<Map<UUID, String>> future = new CompletableFuture<>();
        private final Map<UUID, String> responses = new ConcurrentHashMap<>();

        private void add(UUID endpoint, String payload) {
            if (!future.isDone()) {
                responses.put(endpoint, payload);
            }
        }

        private void complete() {
            future.complete(Map.copyOf(responses));
        }

        private CompletableFuture<Map<UUID, String>> future() {
            return future;
        }
    }
}
