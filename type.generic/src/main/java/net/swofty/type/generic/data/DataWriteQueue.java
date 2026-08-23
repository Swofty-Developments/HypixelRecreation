package net.swofty.type.generic.data;

import org.tinylog.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DataWriteQueue {
    private static final long IDLE_POLL_MILLIS = 250;

    private static final Map<UUID, PlayerQueue> queues = new ConcurrentHashMap<>();

    private DataWriteQueue() {}

    public static void submit(UUID uuid, String key, Runnable write) {
        if (uuid == null) {
            runWrite(null, key, write);
            return;
        }
        queues.computeIfAbsent(uuid, PlayerQueue::new).submit(key, write);
    }

    public static void drain(UUID uuid) {
        if (uuid == null) return;
        PlayerQueue queue = queues.get(uuid);
        if (queue != null) queue.awaitIdle();
    }

    public static void drainAll() {
        for (PlayerQueue queue : queues.values()) queue.awaitIdle();
    }

    public static void remove(UUID uuid) {
        if (uuid == null) return;
        PlayerQueue queue = queues.remove(uuid);
        if (queue != null) queue.awaitIdle();
    }

    private static void runWrite(UUID uuid, String key, Runnable write) {
        try {
            write.run();
        } catch (Exception e) {
            Logger.error(e, "Failed to write datapoint {} for user {}", key, uuid);
        }
    }

    private record PendingWrite(String key, Runnable write) {}

    private static final class PlayerQueue {
        private final UUID uuid;
        private final Deque<PendingWrite> pending = new ArrayDeque<>();
        private Thread worker;

        private PlayerQueue(UUID uuid) {
            this.uuid = uuid;
        }

        private synchronized void submit(String key, Runnable write) {
            pending.add(new PendingWrite(key, write));
            if (worker == null) worker = Thread.startVirtualThread(this::run);
        }

        private void run() {
            while (true) {
                PendingWrite next;
                synchronized (this) {
                    next = pending.poll();
                    if (next == null) {
                        worker = null;
                        notifyAll();
                        return;
                    }
                }
                runWrite(uuid, next.key(), next.write());
            }
        }

        private synchronized void awaitIdle() {
            if (Thread.currentThread() == worker) return;
            while (worker != null || !pending.isEmpty()) {
                try {
                    wait(IDLE_POLL_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
