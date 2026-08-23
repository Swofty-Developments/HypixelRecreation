package net.swofty.type.generic.data.domain;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.user.HypixelPlayer;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerDataServiceDiscardTest {
    @Test
    void discardsEveryDomainWithoutSavingAndSuppressesLaterSaves() {
        ServerType type = ServerType.REPLAY_VIEWER;
        RecordingDomain first = new RecordingDomain("test-discard-a", type);
        RecordingDomain second = new RecordingDomain("test-discard-b", type);
        PlayerDataService.register(first);
        PlayerDataService.register(second);

        UUID uuid = UUID.randomUUID();
        PlayerDataService.loadAll(type, uuid);
        assertFalse(PlayerDataService.isSaveSuppressed(uuid));
        assertTrue(PlayerDataService.isLoaded(first.key(), uuid));
        assertTrue(PlayerDataService.isLoaded(second.key(), uuid));

        PlayerDataService.discardAll(type, uuid);

        assertEquals(List.of(uuid), first.unloaded);
        assertEquals(List.of(uuid), second.unloaded);
        assertEquals(0, first.saves.get());
        assertEquals(0, second.saves.get());
        assertFalse(PlayerDataService.isLoaded(first.key(), uuid));
        assertFalse(PlayerDataService.isLoaded(second.key(), uuid));
        assertTrue(PlayerDataService.isSaveSuppressed(uuid));
    }

    @Test
    void clearsSuppressionWhenTheUserIsLoadedAgain() {
        ServerType type = ServerType.RAVENGARD_LOBBY;
        RecordingDomain domain = new RecordingDomain("test-discard-reload", type);
        PlayerDataService.register(domain);

        UUID uuid = UUID.randomUUID();
        PlayerDataService.loadAll(type, uuid);
        PlayerDataService.discardAll(type, uuid);
        assertTrue(PlayerDataService.isSaveSuppressed(uuid));

        PlayerDataService.loadAll(type, uuid);
        assertFalse(PlayerDataService.isSaveSuppressed(uuid));
    }

    @Test
    void holdsTheLifecycleLockForTheWholeDiscard() throws Exception {
        ServerType type = ServerType.SKYWARS_CONFIGURATOR;
        UUID uuid = UUID.randomUUID();
        CountDownLatch competitorStarted = new CountDownLatch(1);
        CountDownLatch competitorFinished = new CountDownLatch(1);

        RecordingDomain domain = new RecordingDomain("test-discard-lock", type) {
            @Override
            public void unload(UUID unloading) {
                super.unload(unloading);
                Thread competitor = new Thread(() -> {
                    competitorStarted.countDown();
                    PlayerDataService.loadAll(type, uuid);
                    competitorFinished.countDown();
                });
                competitor.setDaemon(true);
                competitor.start();
                try {
                    assertTrue(competitorStarted.await(5, TimeUnit.SECONDS));
                    assertFalse(competitorFinished.await(250, TimeUnit.MILLISECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        PlayerDataService.register(domain);

        PlayerDataService.discardAll(type, uuid);

        assertTrue(competitorFinished.await(5, TimeUnit.SECONDS));
    }

    private static class RecordingDomain implements PlayerDataDomain<RecordingHandler> {
        private final DomainKey<RecordingHandler> key;
        private final ServerType only;
        private final List<UUID> unloaded = new CopyOnWriteArrayList<>();
        private final AtomicInteger saves = new AtomicInteger();

        private RecordingDomain(String id, ServerType only) {
            this.key = new DomainKey<>(id, RecordingHandler.class);
            this.only = only;
        }

        @Override
        public DomainKey<RecordingHandler> key() {
            return key;
        }

        @Override
        public boolean appliesTo(ServerType type) {
            return type == only;
        }

        @Override
        public void load(UUID uuid) {
            PlayerDataService.store(key, uuid, new RecordingHandler(uuid));
        }

        @Override
        public void applyToPlayer(HypixelPlayer player) {
        }

        @Override
        public void save(HypixelPlayer player) {
            saves.incrementAndGet();
        }

        @Override
        public void unload(UUID uuid) {
            unloaded.add(uuid);
            PlayerDataService.evict(key, uuid);
        }
    }

    private static class RecordingHandler extends DataHandler {
        private RecordingHandler(UUID uuid) {
            super(uuid);
        }

        @Override
        public DataHandler fromDocument(Document document) {
            return this;
        }

        @Override
        public Document toDocument() {
            return new Document();
        }

        @Override
        public void runOnLoad(HypixelPlayer player) {
        }

        @Override
        public void runOnSave(HypixelPlayer player) {
        }
    }
}
