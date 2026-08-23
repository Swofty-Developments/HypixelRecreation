package net.swofty.velocity.gamemanager;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerPingTrackerTest {

    @Test
    void keepsAServerThatMissesFewerPingsThanTheThreshold() {
        ServerPingTracker tracker = new ServerPingTracker(5);
        UUID server = UUID.randomUUID();

        for (int attempt = 1; attempt < 5; attempt++) {
            assertFalse(tracker.recordFailure(server));
            assertEquals(attempt, tracker.missed(server));
        }
    }

    @Test
    void dropsAServerOnceItMissesTheThreshold() {
        ServerPingTracker tracker = new ServerPingTracker(3);
        UUID server = UUID.randomUUID();

        assertFalse(tracker.recordFailure(server));
        assertFalse(tracker.recordFailure(server));
        assertTrue(tracker.recordFailure(server));
        assertEquals(0, tracker.missed(server));
    }

    @Test
    void aSingleAnsweredPingForgivesEveryEarlierMiss() {
        ServerPingTracker tracker = new ServerPingTracker(3);
        UUID server = UUID.randomUUID();

        tracker.recordFailure(server);
        tracker.recordFailure(server);
        tracker.recordSuccess(server);

        assertEquals(0, tracker.missed(server));
        assertFalse(tracker.recordFailure(server));
        assertFalse(tracker.recordFailure(server));
    }

    @Test
    void tracksEachServerSeparately() {
        ServerPingTracker tracker = new ServerPingTracker(2);
        UUID booting = UUID.randomUUID();
        UUID healthy = UUID.randomUUID();

        assertFalse(tracker.recordFailure(booting));
        tracker.recordSuccess(healthy);

        assertEquals(1, tracker.missed(booting));
        assertEquals(0, tracker.missed(healthy));
        assertTrue(tracker.recordFailure(booting));
        assertFalse(tracker.recordFailure(healthy));
    }

    @Test
    void reportsRemovalExactlyOnceWhenVerdictsRunConcurrently() throws Exception {
        ServerPingTracker tracker = new ServerPingTracker(4);
        UUID server = UUID.randomUUID();

        int verdicts = 8;
        AtomicInteger removals = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(verdicts);

        for (int index = 0; index < verdicts; index++) {
            Thread.startVirtualThread(() -> {
                try {
                    start.await();
                    if (tracker.recordFailure(server)) removals.incrementAndGet();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(finished.await(10, TimeUnit.SECONDS));
        assertEquals(2, removals.get());
    }

    @Test
    void refusesAThresholdBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new ServerPingTracker(0));
    }
}
