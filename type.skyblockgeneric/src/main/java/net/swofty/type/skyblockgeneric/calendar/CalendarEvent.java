package net.swofty.type.skyblockgeneric.calendar;

import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.protocol.objects.darkauction.TriggerDarkAuctionProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record CalendarEvent(
    String id,
    ItemStack representation,
    Function<Integer, Text> displayName,
    List<Text> description,
    List<Long> times,
    Duration duration,
    boolean tracksYear,
    BiConsumer<Long, Integer> action
) {
    private static final Duration DARK_AUCTION_DURATION = Duration.ofMinutes(5);
    private static final long THREE_DAYS = 3L * SkyBlockCalendar.DAY;
    private static final long YEAR = SkyBlockCalendar.YEAR;
    private static final long MONTH = SkyBlockCalendar.MONTH;
    private static final Map<Long, List<CalendarEvent>> eventCache = new HashMap<>();
    private static final List<CalendarEvent> allEvents = new ArrayList<>();

    public static CalendarEvent NEW_YEAR = new CalendarEvent(
        "new_year",
        ItemStack.of(Material.CAKE).with(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true),
        year -> Text.of("<d>{} New Year Celebration", StringUtility.ntify(year)),
        List.of(
            Text.of("<7>To celebrate the SkyBlock New Year,"),
            Text.of("<7>the Baker is giving out free Cake!")
        ),
        List.of(10L),
        Duration.ofHours(1),
        true,
        (time, year) -> {
            // New Year's actions
        }
    );

    // Dark Auction occurs every 3 SkyBlock days at midnight
    public static CalendarEvent DARK_AUCTION = new CalendarEvent(
        "dark_auction",
        ItemStack.of(Material.NETHER_STAR).with(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true),
        year -> Text.of("<5>Dark Auction"),
        List.of(
            Text.of("<7>The Dark Auction is a secret"),
            Text.of("<7>underground auction where"),
            Text.of("<7>special items are sold.")
        ),
        calculateDarkAuctionTimes(),
        DARK_AUCTION_DURATION,
        false,
            (time, year) -> triggerDarkAuctionUntil(time, System.nanoTime() + DARK_AUCTION_DURATION.toNanos())
    );

    private static void triggerDarkAuctionUntil(long eventTime, long deadlineNanos) {
        RedisClient.isServiceOnline(ServiceType.DARK_AUCTION).thenAccept(online -> {
            if (!online) {
                retryDarkAuction(eventTime, deadlineNanos);
                return;
            }
            new ProxyService(ServiceType.DARK_AUCTION).handleRequest(
                    new TriggerDarkAuctionProtocol(),
                            new TriggerDarkAuctionProtocol.TriggerMessage(eventTime))
                .thenAccept(response -> {
                    if (response.success()) {
                        Logger.info("Dark Auction started successfully");
                    } else {
                        Logger.debug("Dark Auction trigger: {}", response.message());
                    }
                })
                .exceptionally(throwable -> {
                    Logger.debug("Dark Auction service became unavailable while triggering: {}", throwable.getMessage());
                    retryDarkAuction(eventTime, deadlineNanos);
                    return null;
                });
        });
    }

    private static void retryDarkAuction(long eventTime, long deadlineNanos) {
        if (System.nanoTime() >= deadlineNanos) return;
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                .execute(() -> triggerDarkAuctionUntil(eventTime, deadlineNanos));
    }

    private static List<Long> calculateDarkAuctionTimes() {
        List<Long> times = new ArrayList<>();
        for (long time = 0; time < YEAR; time += THREE_DAYS) {
            times.add(time);
        }
        return times;
    }

    public static CalendarEvent ELECTION_OPEN = new CalendarEvent(
        "election_open",
        ItemStack.of(Material.JUKEBOX),
        year -> Text.of("<b>{} Election Booth Opens", StringUtility.ntify(year)),
        List.of(
            Text.of("<7>The Mayor Election voting booth"),
            Text.of("<7>is now open! Cast your vote for"),
            Text.of("<7>your favorite candidate.")
        ),
        List.of(5 * MONTH),
        Duration.ofMinutes(5),
        true,
        (_, _) -> {
            ElectionManager.onElectionStart();
        }
    );

    public static CalendarEvent ELECTION_CLOSE = new CalendarEvent(
        "election_close",
        ItemStack.of(Material.JUKEBOX),
        year -> Text.of("<b>{} Election Over!", StringUtility.ntify(year)),
        List.of(
            Text.of("<7>The Mayor Election has concluded!"),
            Text.of("<7>A new Mayor has been elected.")
        ),
        List.of(8 * MONTH),
        Duration.ofMinutes(5),
        true,
        (_, _) -> {
            ElectionManager.onElectionEnd();
        }
    );

    static {
        registerEvent(NEW_YEAR);
        registerEvent(DARK_AUCTION);
        registerEvent(ELECTION_OPEN);
        registerEvent(ELECTION_CLOSE);
    }

    public Text getDisplayName(int year) {
        return displayName.apply(year);
    }

    private static void registerEvent(CalendarEvent event) {
        allEvents.add(event);
        for (Long time : event.times()) {
            eventCache.computeIfAbsent(time, k -> new ArrayList<>()).add(event);
        }
    }

    public static List<CalendarEvent> getCurrentEvents(long time) {
        List<CalendarEvent> activeEvents = new ArrayList<>();
        for (CalendarEvent event : allEvents) {
            for (Long eventStartTime : event.times()) {
                // convert ticks to milliseconds
                Duration timeSinceEventStart = Duration.ofMillis((time - eventStartTime) * 50);

                // checks if the event is currently active
                if (!timeSinceEventStart.isNegative() && timeSinceEventStart.compareTo(event.duration()) < 0) {
                    activeEvents.add(event);
                    break;
                }
            }
        }
        return activeEvents;
    }

    public static List<CalendarEvent> getAllEvents() {
        return new ArrayList<>(allEvents);
    }

    public static Map<Long, List<CalendarEvent>> getEventCache() {
        return new HashMap<>(eventCache);
    }

    public static CalendarEvent fromId(String id) {
        for (CalendarEvent event : allEvents) {
            if (event.id().equalsIgnoreCase(id)) {
                return event;
            }
        }
        return null;
    }
}
