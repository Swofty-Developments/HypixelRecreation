package net.swofty.type.generic.rates;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.rates.GameRates;
import net.swofty.commons.rates.RateCategory;
import net.swofty.type.generic.data.mongodb.AttributeDatabase;
import org.bson.Document;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RatesSync {
    private static final String DOCUMENT_ID = "rates";
    private static final int REFRESH_SECONDS = 5;
    private static final AtomicBoolean started = new AtomicBoolean(false);
    private static volatile Map<RateCategory, Double> lastLoaded = Map.of();

    private RatesSync() {
    }

    public static void start() {
        if (!started.compareAndSet(false, true)) return;
        load();
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            CompletableFuture.runAsync(RatesSync::load);
            return TaskSchedule.seconds(REFRESH_SECONDS);
        });
    }

    public static synchronized void load() {
        try {
            Document document = AttributeDatabase.getDocument(DOCUMENT_ID);
            Map<RateCategory, Double> values = document == null ? Map.of() : read(document);
            if (!values.equals(lastLoaded)) {
                GameRates.apply(values);
                lastLoaded = values;
                Logger.info("Applied rate multipliers: {}", values.isEmpty() ? "defaults" : values);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to load rate multipliers");
        }
    }

    public static CompletableFuture<Void> save() {
        Map<RateCategory, Double> values = Map.copyOf(GameRates.snapshot());
        return CompletableFuture.runAsync(() -> {
            synchronized (RatesSync.class) {
                Document document = new Document("_id", DOCUMENT_ID);
                values.forEach((category, value) -> document.append(category.getKey(), value));
                AttributeDatabase.saveDocument(document);
                lastLoaded = values.isEmpty() ? Map.of() : new EnumMap<>(values);
            }
        });
    }

    private static Map<RateCategory, Double> read(Document document) {
        EnumMap<RateCategory, Double> values = new EnumMap<>(RateCategory.class);
        document.forEach((key, value) -> {
            if (value instanceof Number number) {
                RateCategory.fromKey(key).ifPresent(category -> values.put(category, number.doubleValue()));
            }
        });
        return values;
    }
}
