package net.swofty.commons.redis;

import net.swofty.commons.protocol.objects.PingProtocol;
import net.swofty.redisapi.api.ChannelRegistry;
import net.swofty.redisapi.api.RedisAPI;
import net.swofty.redisapi.api.RedisChannel;
import net.swofty.redisapi.events.RedisMessagingReceiveEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisMessageBusDispatchTest {

    @Test
    void keepsDispatchingWhileAResponseCallbackIsStillRunning() throws Exception {
        RedisAPI.generateInstance("redis://127.0.0.1:6399");

        PingProtocol protocol = new PingProtocol();
        String channel = RedisChannels.protocol(protocol);
        RedisEndpoint origin = RedisEndpoint.server(UUID.randomUUID());

        CountDownLatch callbackRunning = new CountDownLatch(1);
        CountDownLatch releaseCallback = new CountDownLatch(1);
        AtomicBoolean ranOnDispatchThread = new AtomicBoolean();

        CompletableFuture<String> response = RedisMessageBus.requestRaw(
                origin, "proxy", channel, channel, protocol, new PingProtocol.EmptyMessage());

        Thread[] dispatchThread = new Thread[1];
        dispatchThread[0] = new Thread(() -> {
            RedisChannel registered = ChannelRegistry.registeredChannels.stream()
                    .filter(candidate -> candidate.channelName.equals(channel))
                    .findFirst()
                    .orElseThrow();

            registered.receiveEvent.accept(receiveEvent(channel,
                    origin.id() + ";" + new RedisEnvelope(pendingRequestId().toString(), "proxy", "{}").serialize()));
        }, "fake-redis-subscriber");

        response.thenAccept(ignored -> {
            ranOnDispatchThread.set(Thread.currentThread() == dispatchThread[0]);
            callbackRunning.countDown();
            try {
                releaseCallback.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });

        dispatchThread[0].start();

        assertTrue(callbackRunning.await(10, TimeUnit.SECONDS));
        dispatchThread[0].join(TimeUnit.SECONDS.toMillis(2));

        assertFalse(dispatchThread[0].isAlive());
        assertFalse(ranOnDispatchThread.get());

        releaseCallback.countDown();
    }

    private static RedisMessagingReceiveEvent receiveEvent(String channel, String message) {
        try {
            Constructor<RedisMessagingReceiveEvent> constructor =
                    RedisMessagingReceiveEvent.class.getDeclaredConstructor(String.class, String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(channel, message);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static UUID pendingRequestId() {
        try {
            Field field = RedisMessageBus.class.getDeclaredField("pendingResponses");
            field.setAccessible(true);
            Map<UUID, ?> pending = (Map<UUID, ?>) field.get(null);
            return pending.keySet().iterator().next();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
