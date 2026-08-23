package net.swofty.type.generic.data.domain;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.data.DataWriteQueue;
import net.swofty.type.generic.user.HypixelPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerDataServiceTest {

    @Test
    void lifecycleTransitionsForOneUuidNeverOverlap() throws InterruptedException {
        UUID uuid = UUID.randomUUID();
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger overlaps = new AtomicInteger();
        CountDownLatch finished = new CountDownLatch(8);

        for (int i = 0; i < 8; i++) {
            Thread.startVirtualThread(() -> {
                PlayerDataService.runLifecycleTransition(uuid, () -> {
                    if (inside.incrementAndGet() > 1) overlaps.incrementAndGet();
                    sleep(10);
                    inside.decrementAndGet();
                });
                finished.countDown();
            });
        }

        assertTrue(finished.await(10, TimeUnit.SECONDS));
        assertEquals(0, overlaps.get());
    }

    @Test
    void lifecycleTransitionsCanNest() {
        UUID uuid = UUID.randomUUID();
        AtomicInteger runs = new AtomicInteger();

        PlayerDataService.runLifecycleTransition(uuid, () ->
                PlayerDataService.runLifecycleTransition(uuid, runs::incrementAndGet));

        assertEquals(1, runs.get());
    }

    @Test
    void lifecycleTransitionsWaitForQueuedWrites() {
        UUID uuid = UUID.randomUUID();
        List<String> order = new CopyOnWriteArrayList<>();

        DataWriteQueue.submit(uuid, "queued", () -> {
            sleep(50);
            order.add("write");
        });
        PlayerDataService.runLifecycleTransition(uuid, () -> order.add("transition"));

        assertEquals(List.of("write", "transition"), order);
    }

    @Test
    void domainResolvesTheRegisteredInstanceById() {
        DomainKey<DataHandler> key = new DomainKey<>("player-data-service-test", DataHandler.class);
        StubDomain domain = new StubDomain(key);
        PlayerDataService.register(domain);

        assertSame(domain, PlayerDataService.domain(key));
        assertSame(domain, PlayerDataService.domain(new DomainKey<>(key.id(), DataHandler.class)));
    }

    @Test
    void domainRejectsUnregisteredKeys() {
        assertThrows(IllegalStateException.class,
                () -> PlayerDataService.domain(new DomainKey<>("player-data-service-absent", DataHandler.class)));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private record StubDomain(DomainKey<DataHandler> key) implements PlayerDataDomain<DataHandler> {
        @Override
        public boolean appliesTo(ServerType type) {
            return false;
        }

        @Override
        public void load(UUID uuid) {
        }

        @Override
        public void applyToPlayer(HypixelPlayer player) {
        }

        @Override
        public void save(HypixelPlayer player) {
        }

        @Override
        public void unload(UUID uuid) {
        }
    }
}
