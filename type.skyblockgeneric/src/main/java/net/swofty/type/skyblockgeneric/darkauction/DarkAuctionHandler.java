package net.swofty.type.skyblockgeneric.darkauction;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.darkauction.PlayerLeftAuctionProtocol;
import net.swofty.commons.skyblock.auctions.DarkAuctionPhase;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles all Dark Auction logic on the client side.
 * This includes player tracking, event handling, and state management.
 */
public class DarkAuctionHandler {
    private static final String NPC_PREFIX = "<e>[NPC] <6>Sirius<f>: ";

    private static final Set<UUID> playersInAuction = ConcurrentHashMap.newKeySet();
    private static final ProxyService darkAuctionService = new ProxyService(ServiceType.DARK_AUCTION);
    @Getter
    private static DarkAuctionLocalState localState = null;
    @Getter
    private static final AtomicInteger timeLeft = new AtomicInteger(0);
    @Getter
    private static boolean biddingOpen = false;
    private static boolean serviceHealthCheckRunning = false;
    /**
     * -- SETTER --
     *  Sets a callback to be invoked when the Dark Auction state changes.
     *  Used to refresh NPCs when the auction phase changes.
     */
    @Setter
    private static Runnable onStateChangeCallback = null;

    private static void notifyStateChange() {
        if (onStateChangeCallback != null) {
            onStateChangeCallback.run();
        }
    }

    /**
     * Starts a periodic health check for the Dark Auction service.
     * If the service goes offline, gracefully ends the auction.
     */
    public static void startServiceHealthCheck() {
        if (serviceHealthCheckRunning) return;
        serviceHealthCheckRunning = true;

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            // Only check if there's an active auction or players in queue
            if (localState == null && playersInAuction.isEmpty()) {
                serviceHealthCheckRunning = false;
                return TaskSchedule.stop();
            }

            darkAuctionService.isOnline().thenAccept(online -> {
                if (!online) {
                    Logger.warn("Dark Auction service went offline! Gracefully ending auction.");
                    handleServiceOffline();
                }
            });

            return TaskSchedule.seconds(5); // Check every 5 seconds
        });
    }

    /**
     * Handles the case when the Dark Auction service goes offline.
     * Refunds all players and kicks them from the auction.
     */
    private static void handleServiceOffline() {
        biddingOpen = false;
        timeLeft.set(0);

        for (UUID playerId : playersInAuction) {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
            if (player != null) {
                // Refund if they had a bid (based on local state)
                if (localState != null && playerId.equals(localState.getHighestBidderId())) {
                    long refundAmount = localState.getCurrentBid();
                    if (refundAmount > 0) {
                        player.addCoins(refundAmount);
                        player.sendMessage("<c>The Dark Auction has been interrupted! <6>{:,} Coins <c>have been refunded.",
                                refundAmount);
                    }
                }

                player.sendMessage(NPC_PREFIX + "I apologize, but the auction must be cancelled due to... unforeseen circumstances. Please come back another time!");
                player.closeInventory();
                player.sendTo(ServerType.SKYBLOCK_HUB);
            }
        }

        playersInAuction.clear();
        localState = null;
        serviceHealthCheckRunning = false;

        // Notify state change to refresh NPCs
        notifyStateChange();

        Logger.info("Dark Auction ended due to service going offline");
    }

    public static void handleAuctionStart(JSONObject msg) {
        Logger.info("Dark Auction is starting!");
        localState = DarkAuctionLocalState.fromMessage(msg);
        biddingOpen = false;
        timeLeft.set(0);

        // Start health check to handle service going offline
        startServiceHealthCheck();

        // Notify state change to refresh NPCs
        notifyStateChange();
    }

    public static void handleAuctionBegin(JSONObject msg) {
        Logger.info("Dark Auction bidding phase has begun!");
        if (localState != null) {
            localState.updateFromMessage(msg);
        }

        // Notify state change to refresh NPCs (Sirius moves to basement)
        notifyStateChange();

        for (UUID playerId : playersInAuction) {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
            player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_VISITED_DARK_AUCTION, true);
            if (player != null) {
                player.sendMessage(NPC_PREFIX + "Come on down to the basement, the auction is about to begin!");
                player.teleport(new Pos(91, 75, 182, 0, 0));
            }
        }

        // Schedule the welcome message after a brief delay
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (UUID playerId : playersInAuction) {
                SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
                if (player != null) {
                    player.sendMessage(NPC_PREFIX + "Welcome to the <5>Dark Auction</5>! I hope everyone's ready, the first item is about to come out!");
                }
            }
        }).delay(TaskSchedule.seconds(2)).schedule();
    }

    public static void handleRoundStart(JSONObject msg) {
        if (localState != null) {
            localState.updateFromMessage(msg);
        }

        String itemTypeName = msg.optString("currentItemType", "Unknown");
        int countdown = msg.optInt("countdown", 10);
        biddingOpen = false;
        timeLeft.set(countdown);

        Text formattedItem = getFormattedItemName(itemTypeName);
        int round = localState.getCurrentRound() + 1;
        String ordinal = getOrdinal(round);

        Logger.info("Round {} starting with item: {}", round, itemTypeName);

        // Send item announcement
        long startingBid = localState.getCurrentBid();
        for (UUID playerId : playersInAuction) {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
            if (player != null) {
                player.sendMessage(NPC_PREFIX + "{} up, we have a {}<f>, the starting bid is <6>{:,} Coins<f>!",
                        ordinal, formattedItem, startingBid);
            }
        }

        // Notify state change to update displays (after item is announced)
        notifyStateChange();

        // Start the 5 second countdown before bidding opens
        startBiddingCountdown();
    }

    private static void startBiddingCountdown() {
        AtomicInteger countdownSeconds = new AtomicInteger(5);

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            int current = countdownSeconds.getAndDecrement();

            if (current > 0) {
                for (UUID playerId : playersInAuction) {
                    SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
                    if (player != null) {
                        player.sendMessage(NPC_PREFIX + "The bidding will start in <b>{}</b>...", current);
                    }
                }
                return TaskSchedule.seconds(1);
            } else {
                // Bidding is now open
                biddingOpen = true;
                timeLeft.set(10); // 10 second timer for bidding

                for (UUID playerId : playersInAuction) {
                    SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
                    if (player != null) {
                        player.sendMessage(NPC_PREFIX + "You can place your bids now!");
                    }
                }

                // Start the bidding timer countdown
                startBiddingTimer();

                return TaskSchedule.stop();
            }
        });
    }

    private static void startBiddingTimer() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (!biddingOpen || localState == null) {
                return TaskSchedule.stop();
            }

            int current = timeLeft.decrementAndGet();
            if (current <= 0) {
                return TaskSchedule.stop();
            }

            return TaskSchedule.seconds(1);
        });
    }

    public static void handleBidPlaced(JSONObject msg) {
        if (localState != null) {
            localState.updateFromMessage(msg);
        }

        String bidderName = msg.optString("highestBidderName", "Unknown");
        long bid = msg.getLong("currentBid");

        // Reset timer on new bid
        timeLeft.set(10);

        // Handle refunds for previous bidder (if on this server)
        if (msg.has("refundPlayerId") && !msg.isNull("refundPlayerId")) {
            String refundPlayerIdStr = msg.getString("refundPlayerId");
            long refundAmount = msg.getLong("refundAmount");
            UUID refundPlayerId = UUID.fromString(refundPlayerIdStr);

            SkyBlockPlayer refundPlayer = SkyBlockGenericLoader.getFromUUID(refundPlayerId);
            if (refundPlayer != null && playersInAuction.contains(refundPlayerId)) {
                refundPlayer.addCoins(refundAmount);
                refundPlayer.sendMessage("<c>You have been outbid! <6>{:,} Coins <c>have been returned to you.",
                        refundAmount);
            }
        }

        // Handle new bidder
        UUID newBidderId = null;
        if (msg.has("newBidderId") && !msg.isNull("newBidderId")) {
            String newBidderIdStr = msg.getString("newBidderId");
            long newBidAmount = msg.getLong("newBidAmount");
            newBidderId = UUID.fromString(newBidderIdStr);

            SkyBlockPlayer newBidder = SkyBlockGenericLoader.getFromUUID(newBidderId);
            if (newBidder != null && playersInAuction.contains(newBidderId)) {
                newBidder.removeCoins(newBidAmount);
                // Send personal message to the bidder
                newBidder.sendMessage("<a>You have placed a bid of <6>{:,} Coins</6>!", bid);
            }
        }

        // Broadcast bid to all other participants
        UUID finalNewBidderId = newBidderId;
        for (UUID playerId : playersInAuction) {
            // Skip the bidder - they already got their message
            if (playerId.equals(finalNewBidderId)) {
                continue;
            }

            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
            if (player != null) {
                player.sendMessage("{} <f>placed a bid of <6>{:,} Coins</6>!", bidderName, bid);
            }
        }
    }

    public static void handleRoundEnd(JSONObject msg) {
        biddingOpen = false;
        timeLeft.set(0);

        String highestBidderIdStr = msg.optString("highestBidderId", null);
        String winnerName = msg.optString("highestBidderName", null);
        String itemTypeName = msg.getString("currentItemType");
        long bid = msg.getLong("currentBid");

        Text formattedItem = getFormattedItemName(itemTypeName);

        Logger.info("Round ended. Winner: {}, Item: {}, Bid: {}", winnerName, itemTypeName, bid);

        // Give item to winner if on this server
        if (highestBidderIdStr != null && !highestBidderIdStr.equals("null") && winnerName != null) {
            UUID winnerId = UUID.fromString(highestBidderIdStr);
            SkyBlockPlayer winner = SkyBlockGenericLoader.getFromUUID(winnerId);

            if (winner != null && playersInAuction.contains(winnerId)) {
                try {
                    ItemType itemType = ItemType.valueOf(itemTypeName);
                    SkyBlockItem skyBlockItem = new SkyBlockItem(itemType);

                    // Set dark auction price on ALL items won at auction
                    skyBlockItem.getAttributeHandler().setDarkAuctionPrice(bid);

                    // Apply Greed bonus for Midas' Sword using extra dynamic statistics
                    if (itemType == ItemType.MIDAS_SWORD) {
                        int greedBonus = calculateGreedBonus(bid);
                        ItemStatistics greedStats = ItemStatistics.builder()
                                .withBase(ItemStatistic.DAMAGE, (double) greedBonus)
                                .withBase(ItemStatistic.STRENGTH, (double) greedBonus)
                                .build();
                        skyBlockItem.getAttributeHandler().setExtraDynamicStatistics(greedStats);
                    }

                    winner.addAndUpdateItem(skyBlockItem);
                } catch (Exception e) {
                    Logger.error(e, "Error giving item to winner");
                }
            }

            // Announce winner to all
            for (UUID playerId : playersInAuction) {
                SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
                if (player != null) {
                    player.sendMessage(NPC_PREFIX + "Sold! {} <f>to {} <f>for <6>{:,} Coins<f>!",
                            formattedItem, winnerName, bid);
                }
            }
        } else {
            // No winner
            for (UUID playerId : playersInAuction) {
                SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
                if (player != null) {
                    player.sendMessage(NPC_PREFIX + "No bids were placed. Moving on to the next item!");
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static void handleAuctionEnd(JSONObject msg) {
        Logger.info("Dark Auction has ended!");
        biddingOpen = false;
        timeLeft.set(0);

        for (UUID playerId : playersInAuction) {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
            if (player != null) {
                player.sendMessage(NPC_PREFIX + "That's all for tonight's auction! Thank you all for coming!");
                player.closeInventory();
            }
        }

        // Teleport players out after 10 second delay
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (UUID playerId : playersInAuction) {
                SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(playerId);
                if (player != null) {
                    player.teleport(new Pos(91, 75, 177.5, 180, 0));
                }
            }

            playersInAuction.clear();
            localState = null;

            // Notify state change to refresh NPCs (Sirius returns to normal position)
            notifyStateChange();
        }).delay(TaskSchedule.seconds(10)).schedule();
    }

    private static Text getFormattedItemName(String itemTypeName) {
        if (itemTypeName == null) return Text.of("<f>Unknown Item");
        try {
            ItemType itemType = ItemType.valueOf(itemTypeName);
            SkyBlockItem item = new SkyBlockItem(itemType);
            return Text.literal(item.getDisplayName());
        } catch (Exception e) {
            // Fallback formatting
            String formatted = itemTypeName.replace("_", " ");
            return Text.of("<f>{}", formatted.substring(0, 1).toUpperCase() + formatted.substring(1).toLowerCase());
        }
    }

    private static String getOrdinal(int round) {
        return switch (round) {
            case 1 -> "First";
            case 2 -> "Second";
            case 3 -> "Third";
            case 4 -> "Fourth";
            case 5 -> "Fifth";
            default -> "Next";
        };
    }

    // Player management methods
    public static void addPlayerToAuction(SkyBlockPlayer player) {
        playersInAuction.add(player.getUuid());
        Logger.info("Player {} joined the Dark Auction queue", player.getUsername());

        // Start health check if not already running
        startServiceHealthCheck();
    }

    public static void removePlayerFromAuction(UUID playerId) {
        playersInAuction.remove(playerId);
    }

    public static boolean isPlayerInAuction(UUID playerId) {
        return playersInAuction.contains(playerId);
    }

    public static boolean isAuctionActive() {
        return localState != null;
    }

    public static int getPlayersInAuctionCount() {
        return playersInAuction.size();
    }

    /**
     * Handles a player leaving the server while in the Dark Auction.
     * Refunds their bid if they're the highest bidder and notifies the service.
     *
     * @param player the player who left
     */
    public static void handlePlayerLeft(SkyBlockPlayer player) {
        UUID playerId = player.getUuid();

        // Check if player was in the auction
        if (!playersInAuction.contains(playerId)) {
            return;
        }

        Logger.info("Player {} left server while in Dark Auction, cleaning up", player.getUsername());

        // Refund directly if they're the highest bidder (player is still available here)
        if (localState != null && playerId.equals(localState.getHighestBidderId())) {
            long refundAmount = localState.getCurrentBid();
            if (refundAmount > 0) {
                player.addCoins(refundAmount);
                Logger.info("Refunded {} coins to {} who left while highest bidder",
                        refundAmount, player.getUsername());
            }
        }

        // Remove from local cache
        playersInAuction.remove(playerId);

        // Get auction ID if available
        UUID auctionId = localState != null ? localState.getAuctionId() : null;

        // Notify the service to update auction state (remove bid, promote next bidder if needed)
        PlayerLeftAuctionProtocol.PlayerLeftMessage message = new PlayerLeftAuctionProtocol.PlayerLeftMessage(
                playerId,
                player.getUsername(),
                auctionId
        );

        darkAuctionService.handleRequest(message)
                .exceptionally(throwable -> {
                    Logger.error(throwable, "Error notifying service about player {} leaving",
                            player.getUsername());
                    return null;
                });
    }

    /**
     * Calculates the Greed bonus for Midas' Sword based on the price paid.
     * Tiered formula:
     * - <1M: price / 50,000 (max 20)
     * - 1M-2.5M: 20 + (price - 1M) / 100,000 (max 35 cumulative)
     * - 2.5M-7.5M: 35 + (price - 2.5M) / 200,000 (max 60 cumulative)
     * - 7.5M-25M: 60 + (price - 7.5M) / 500,000 (max 95 cumulative)
     * - 25M-50M: 95 + (price - 25M) / 1,000,000 (max 120 cumulative)
     * - >=50M: 120
     */
    private static int calculateGreedBonus(long price) {
        if (price >= 50_000_000L) return 120;
        if (price >= 25_000_000L) {
            int bonus = 95 + (int) ((price - 25_000_000L) / 1_000_000L);
            return Math.min(120, bonus);
        }
        if (price >= 7_500_000L) {
            int bonus = 60 + (int) ((price - 7_500_000L) / 500_000L);
            return Math.min(95, bonus);
        }
        if (price >= 2_500_000L) {
            int bonus = 35 + (int) ((price - 2_500_000L) / 200_000L);
            return Math.min(60, bonus);
        }
        if (price >= 1_000_000L) {
            int bonus = 20 + (int) ((price - 1_000_000L) / 100_000L);
            return Math.min(35, bonus);
        }
        return (int) Math.min(20, price / 50_000L);
    }

    /**
     * Tracks local state for the Dark Auction.
     */
    @Getter
    public static class DarkAuctionLocalState {
        private UUID auctionId;
        private DarkAuctionPhase phase;
        private int currentRound;
        private String currentItemType;
        private long currentBid;
        private UUID highestBidderId;
        private String highestBidderName;
        private List<String> roundItems;

        public static DarkAuctionLocalState fromMessage(JSONObject msg) {
            DarkAuctionLocalState state = new DarkAuctionLocalState();
            state.updateFromMessage(msg);
            return state;
        }

        public void updateFromMessage(JSONObject msg) {
            this.auctionId = UUID.fromString(msg.getString("auctionId"));

            if (msg.has("phase")) {
                this.phase = DarkAuctionPhase.valueOf(msg.getString("phase"));
            }

            this.currentRound = msg.getInt("currentRound");
            this.currentItemType = msg.optString("currentItemType", null);
            this.currentBid = msg.getLong("currentBid");

            String bidderIdStr = msg.optString("highestBidderId", null);
            this.highestBidderId = bidderIdStr != null && !bidderIdStr.equals("null") ?
                    UUID.fromString(bidderIdStr) : null;
            this.highestBidderName = msg.optString("highestBidderName", null);

            if (msg.has("roundItems")) {
                JSONArray itemsArray = msg.getJSONArray("roundItems");
                this.roundItems = new ArrayList<>();
                for (int i = 0; i < itemsArray.length(); i++) {
                    this.roundItems.add(itemsArray.getString(i));
                }
            }
        }
    }
}
