package net.swofty.service.darkauction;

import net.swofty.commons.skyblock.auctions.DarkAuctionPhase;
import net.swofty.commons.protocol.objects.darkauction.DarkAuctionEventProtocol;
import net.swofty.commons.protocol.objects.darkauction.DarkAuctionEventPushProtocol;
import net.swofty.commons.redis.RedisClient;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages Dark Auction phases and timing.
 * This scheduler only manages the auction phases once triggered.
 */
public class DarkAuctionScheduler {
    // Auction timing constants (in seconds)
    private static final int QUEUE_DURATION_SECONDS = 35;
    private static final int AUCTION_START_DELAY_SECONDS = 10;
    private static final int ROUND_TIMER_SECONDS = 10;
    private static final int BETWEEN_ROUNDS_SECONDS = 5;

    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> roundTimerFuture;

    public static void start() {
        executor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "dark-auction-scheduler");
            t.setDaemon(true);
            return t;
        });

        Logger.info("Dark Auction Scheduler started (waiting for calendar triggers)");
    }

    public static void startNewAuction() {
        Logger.info("Starting new Dark Auction!");

        DarkAuctionState auction = new DarkAuctionState();
        auction.setPhase(DarkAuctionPhase.QUEUE);
        DarkAuctionService.setCurrentAuction(auction);

        try {
            // Broadcast AUCTION_START to all hubs
            broadcastEvent(DarkAuctionEventProtocol.EventType.AUCTION_START, auction, 0);

            // Schedule auction begin after queue duration
            executor.schedule(() -> beginAuction(auction), QUEUE_DURATION_SECONDS, TimeUnit.SECONDS);
        } catch (RuntimeException exception) {
            abortAuction(auction);
            throw exception;
        }
    }

    private static void abortAuction(DarkAuctionState auction) {
        if (auction != DarkAuctionService.getCurrentAuction()) {
            return;
        }

        Logger.warn("Dark Auction failed to start, rolling it back");
        auction.setPhase(DarkAuctionPhase.COMPLETE);
        DarkAuctionService.setCurrentAuction(null);
        broadcastEvent(DarkAuctionEventProtocol.EventType.AUCTION_END, auction, 0);
    }

    private static void beginAuction(DarkAuctionState auction) {
        if (auction != DarkAuctionService.getCurrentAuction()) {
            return; // Auction was cancelled
        }

        Logger.info("Auction queue phase complete, beginning bidding!");
        auction.setPhase(DarkAuctionPhase.BIDDING);
        auction.setCurrentRound(0);

        // Broadcast AUCTION_BEGIN
        broadcastEvent(DarkAuctionEventProtocol.EventType.AUCTION_BEGIN, auction, 0);

        // Start first round after delay (gives players time to get to basement)
        executor.schedule(() -> startRound(auction), AUCTION_START_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private static void startRound(DarkAuctionState auction) {
        if (auction != DarkAuctionService.getCurrentAuction()) {
            return; // Auction was cancelled
        }

        auction.resetForNextRound();
        auction.setPhase(DarkAuctionPhase.BIDDING);

        Logger.info("Starting round {} with item {}", auction.getCurrentRound() + 1, auction.getCurrentItem());

        // Broadcast ROUND_START
        broadcastEvent(DarkAuctionEventProtocol.EventType.ROUND_START, auction, ROUND_TIMER_SECONDS);

        // Schedule round end
        scheduleRoundEnd(auction, ROUND_TIMER_SECONDS);
    }

    public static void resetRoundTimer(DarkAuctionState auction) {
        // Cancel existing timer
        if (roundTimerFuture != null && !roundTimerFuture.isDone()) {
            roundTimerFuture.cancel(false);
        }

        // Reschedule for 10 more seconds
        scheduleRoundEnd(auction, ROUND_TIMER_SECONDS);
    }

    private static void scheduleRoundEnd(DarkAuctionState auction, int seconds) {
        roundTimerFuture = executor.schedule(() -> endRound(auction), seconds, TimeUnit.SECONDS);
    }

    private static void endRound(DarkAuctionState auction) {
        if (auction != DarkAuctionService.getCurrentAuction()) {
            return; // Auction was cancelled
        }

        Logger.info("Round {} ended. Winner: {}, Bid: {}",
                auction.getCurrentRound() + 1,
                auction.getHighestBidderName(),
                auction.getCurrentBid());

        // Broadcast ROUND_END
        broadcastEvent(DarkAuctionEventProtocol.EventType.ROUND_END, auction, 0);

        // Check if more rounds
        if (auction.hasMoreRounds()) {
            auction.setPhase(DarkAuctionPhase.BETWEEN);

            // Schedule next round
            executor.schedule(() -> {
                auction.advanceToNextRound();
                startRound(auction);
            }, BETWEEN_ROUNDS_SECONDS, TimeUnit.SECONDS);
        } else {
            // Auction complete
            endAuction(auction);
        }
    }

    private static void endAuction(DarkAuctionState auction) {
        Logger.info("Dark Auction complete!");
        auction.setPhase(DarkAuctionPhase.COMPLETE);

        // Broadcast AUCTION_END
        broadcastEvent(DarkAuctionEventProtocol.EventType.AUCTION_END, auction, 0);

        // Clear auction
        DarkAuctionService.setCurrentAuction(null);
    }

    public static void broadcastEvent(DarkAuctionEventProtocol.EventType type, DarkAuctionState auction, int countdown) {
        broadcastEvent(type, auction, countdown, null, 0);
    }

    public static void broadcastBidPlaced(DarkAuctionState auction, UUID refundPlayerId, long refundAmount) {
        broadcastEvent(DarkAuctionEventProtocol.EventType.BID_PLACED, auction, ROUND_TIMER_SECONDS,
                refundPlayerId, refundAmount);
    }

    private static void broadcastEvent(DarkAuctionEventProtocol.EventType type, DarkAuctionState auction,
                                       int countdown, UUID refundPlayerId, long refundAmount) {
        List<String> roundItems = new ArrayList<>(auction.getRoundItemNames());

        String newBidderId = null;
        long newBidAmount = 0;
        if (type == DarkAuctionEventProtocol.EventType.BID_PLACED) {
            newBidderId = auction.getHighestBidderId().toString();
            newBidAmount = auction.getCurrentBid();
        }

        DarkAuctionEventPushProtocol.Request request = new DarkAuctionEventPushProtocol.Request(
                type.name(),
                auction.getAuctionId().toString(),
                auction.getPhase().name(),
                auction.getCurrentRound(),
                auction.getCurrentItem() != null ? auction.getCurrentItem().name() : null,
                auction.getCurrentBid(),
                auction.getHighestBidderId() != null ? auction.getHighestBidderId().toString() : null,
                auction.getHighestBidderName(),
                countdown,
                roundItems,
                refundPlayerId != null ? refundPlayerId.toString() : null,
                refundAmount,
                newBidderId,
                newBidAmount
        );

        Logger.debug("Broadcasting {} event for auction {}", type, auction.getAuctionId());

        RedisClient.requestAllServersFromService(
                new DarkAuctionEventPushProtocol(),
                request,
                5000
        ).thenAccept(responses -> {
            Logger.debug("Received {} responses for {} event", responses.size(), type);
        }).exceptionally(throwable -> {
            Logger.error("Error broadcasting {} event: {}", type, throwable.getMessage());
            return null;
        });
    }

}
