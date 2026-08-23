package net.swofty.type.hub.gui;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.protocol.objects.darkauction.PlaceBidProtocol;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.darkauction.DarkAuctionHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.List;

public class GUIDarkAuction extends HypixelInventoryGUI implements RefreshingGUI {
    private static final ProxyService darkAuctionService = new ProxyService(ServiceType.DARK_AUCTION);

    // Zigzag pattern slots for bid buttons
    private static final int[] ZIGZAG_SLOTS = {
            9, 18, 27, 36, 45, 46, 47, 38, 29, 20, 21, 22, 31, 40, 49, 50, 51, 42, 33, 24, 25, 26, 35, 44, 53
    };

    // Bid increments per page (in coins)
    private static final long[] PAGE_INCREMENTS = {
            20_000,      // Page 1: 20k
            50_000,      // Page 2: 50k
            100_000,     // Page 3: 100k
            200_000,     // Page 4: 200k
            500_000,     // Page 5: 500k
            1_000_000,   // Page 6: 1m
            5_000_000,   // Page 7: 5m
            10_000_000   // Page 8: 10m
    };

    private int currentPage = 0;

    public GUIDarkAuction() {
        super("Dark Auction", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        updateItems((SkyBlockPlayer) e.player());
    }

    private void updateItems(SkyBlockPlayer player) {
        DarkAuctionHandler.DarkAuctionLocalState state = DarkAuctionHandler.getLocalState();

        if (state == null) {
            set(22, ItemStacks.item(Material.BARRIER, """
                    <c>No Active Auction
                    <7>The Dark Auction is not currently active."""));
            return;
        }

        boolean biddingOpen = DarkAuctionHandler.isBiddingOpen();

        if (!biddingOpen) {
            // Countdown phase - waiting for bidding to start
            renderCountdownPhase(state);
        } else {
            // Bidding phase - active bidding
            renderBiddingPhase(state);
        }

        updateItemStacks(getInventory(), player);
    }

    private void renderCountdownPhase(DarkAuctionHandler.DarkAuctionLocalState state) {
        // Update title to show item and round (strip color codes for clean title)
        String itemName = StringUtility.stripColor(getFormattedItemName(state.getCurrentItemType()));
        int round = state.getCurrentRound() + 1;
        setTitle("{} - Round {}", itemName, round);

        // Fill with glass
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));

        // Item display in slot 22 using NonPlayerItemUpdater
        set(new GUIItem(22) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                String itemTypeName = state.getCurrentItemType();
                if (itemTypeName == null) {
                    return ItemStacks.item(Material.HOPPER, "<c>Waiting...");
                }

                try {
                    ItemType itemType = ItemType.valueOf(itemTypeName);
                    SkyBlockItem item = new SkyBlockItem(itemType);
                    return new NonPlayerItemUpdater(item).getUpdatedItem();
                } catch (Exception ex) {
                    return ItemStacks.item(Material.CHEST, """
                            <e>{}
                            <7>Current auction item""", formatItemName(itemTypeName));
                }
            }
        });

        // Close button in slot 49
        set(new GUIClickableItem(49) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sp = (SkyBlockPlayer) p;

                // Check if player is highest bidder
                if (state.getHighestBidderId() != null &&
                        state.getHighestBidderId().equals(sp.getUuid())) {
                    sp.sendMessage("<c>You cannot leave while you are the highest bidder!");
                    return;
                }

                DarkAuctionHandler.removePlayerFromAuction(sp.getUuid());
                sp.closeInventory();
                sp.sendMessage("<7>You left the Dark Auction.");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BARRIER, """
                        <c>Close
                        <7>Click to close this menu

                        <c>Warning: <7>You cannot leave if
                        <7>you are the highest bidder!""");
            }
        });
    }

    private void renderBiddingPhase(DarkAuctionHandler.DarkAuctionLocalState state) {
        // Update title to match countdown phase format
        String itemName = StringUtility.stripColor(getFormattedItemName(state.getCurrentItemType()));
        int round = state.getCurrentRound() + 1;
        setTitle("{} - Round {}", itemName, round);

        // Fill with glass
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));

        // Item display in slot 4 using NonPlayerItemUpdater
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                String itemTypeName = state.getCurrentItemType();
                if (itemTypeName == null) {
                    return ItemStacks.item(Material.HOPPER, "<c>Waiting...");
                }

                try {
                    ItemType itemType = ItemType.valueOf(itemTypeName);
                    SkyBlockItem item = new SkyBlockItem(itemType);
                    return new NonPlayerItemUpdater(item).getUpdatedItem();
                } catch (Exception ex) {
                    return ItemStacks.item(Material.CHEST, """
                            <e>{}
                            <7>Current auction item""", formatItemName(itemTypeName));
                }
            }
        });

        // Timer clock in slot 5
        int timeLeft = DarkAuctionHandler.getTimeLeft().get();
        set(new GUIItem(5) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.CLOCK, Math.max(1, Math.min(64, timeLeft)), """
                        <e>Time Left
                        <7>Time remaining: <b>{} seconds

                        <7>Current bid: <6>{:,} coins
                        <7>Highest bidder: <e>{}""",
                        timeLeft, state.getCurrentBid(),
                        state.getHighestBidderName() != null ? state.getHighestBidderName() : "None");
            }
        });

        // Calculate the page based on current bid
        currentPage = calculatePageForBid(state.getCurrentBid());

        // Render zigzag bid pattern
        renderZigzagBids(state);
    }

    private int calculatePageForBid(long currentBid) {
        // Find the appropriate page for the current bid level
        long accumulatedValue = 0;

        for (int page = 0; page < PAGE_INCREMENTS.length; page++) {
            long pageMaxValue = accumulatedValue + (PAGE_INCREMENTS[page] * ZIGZAG_SLOTS.length);
            if (currentBid < pageMaxValue) {
                return page;
            }
            accumulatedValue = pageMaxValue;
        }

        return PAGE_INCREMENTS.length - 1; // Max page
    }

    private void renderZigzagBids(DarkAuctionHandler.DarkAuctionLocalState state) {
        long currentBid = state.getCurrentBid();
        long increment = PAGE_INCREMENTS[Math.min(currentPage, PAGE_INCREMENTS.length - 1)];

        // Calculate the base value for the current page
        long pageBaseValue = 0;
        for (int i = 0; i < currentPage; i++) {
            pageBaseValue += PAGE_INCREMENTS[i] * ZIGZAG_SLOTS.length;
        }

        for (int i = 0; i < ZIGZAG_SLOTS.length; i++) {
            int slot = ZIGZAG_SLOTS[i];
            long bidValue = pageBaseValue + ((i + 1) * increment);

            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer sp = (SkyBlockPlayer) p;

                    // Only allow bidding on future values (iron ingots)
                    if (bidValue <= currentBid) {
                        sp.sendMessage("<c>This bid amount has already been passed!");
                        return;
                    }

                    // Check if player has enough coins
                    if (sp.getCoins() < bidValue) {
                        sp.sendMessage("<c>You don't have enough coins! You need <6>{:,} coins</6>.", bidValue);
                        return;
                    }

                    // Check if already highest bidder
                    if (state.getHighestBidderId() != null &&
                            state.getHighestBidderId().equals(sp.getUuid())) {
                        sp.sendMessage("<c>You are already the highest bidder!");
                        return;
                    }

                    // Send bid to service
                    sp.sendMessage("<7>Placing bid of <6>{:,} coins</6>...", bidValue);

                    PlaceBidProtocol.PlaceBidMessage bidMessage = new PlaceBidProtocol.PlaceBidMessage(
                            state.getAuctionId(),
                            sp.getUuid(),
                            sp.getUsername(),
                            bidValue
                    );

                    darkAuctionService.handleRequest(bidMessage)
                            .thenAccept(response -> {
                                if (response instanceof PlaceBidProtocol.PlaceBidResponse(boolean success, String message, String error)) {
                                    if (!success) {
                                        sp.sendMessage("<c>{}", message);
                                    }
                                }
                            })
                            .exceptionally(throwable -> {
                                Logger.error(throwable, "Error placing bid");
                                sp.sendMessage("<c>Failed to place bid. Please try again.");
                                return null;
                            });
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer sp = (SkyBlockPlayer) p;
                    boolean canAfford = sp.getCoins() >= bidValue;

                    if (bidValue < currentBid) {
                        // Past bid - barrier
                        return ItemStacks.item(Material.BARRIER, """
                                <c>{}
                                <7>This bid has already been passed

                                <c>Bid amount: <6>{:,} coins""", formatCoins(bidValue), bidValue);
                    } else if (bidValue == currentBid) {
                        // Current bid - gold block
                        return ItemStacks.item(Material.GOLD_BLOCK, """
                                <6>{} <7>(Current Bid)
                                <7>This is the current highest bid

                                <e>Bid amount: <6>{:,} coins
                                <7>Bidder: <e>{}""", formatCoins(bidValue), bidValue,
                                state.getHighestBidderName() != null ? state.getHighestBidderName() : "None");
                    } else {
                        // Future bid - enchanted iron ingot
                        return ItemStacks.enchanted(ItemStacks.item(Material.IRON_INGOT, 1,
                                Text.of((canAfford ? "<a>" : "<c>") + "{}", formatCoins(bidValue)),
                                List.of(
                                        Text.of("<7>Click to place this bid"),
                                        Text.empty(),
                                        Text.of("<e>Bid amount: <6>{:,} coins", bidValue),
                                        Text.empty(),
                                        Text.of(canAfford ? "<e>Click to bid!" : "<c>Not enough coins!")
                                )));
                    }
                }
            });
        }
    }

    private String formatCoins(long coins) {
        if (coins >= 1_000_000) {
            double millions = coins / 1_000_000.0;
            if (millions == (long) millions) {
                return (long) millions + "M";
            }
            return String.format("%.1fM", millions);
        } else if (coins >= 1_000) {
            double thousands = coins / 1_000.0;
            if (thousands == (long) thousands) {
                return (long) thousands + "k";
            }
            return String.format("%.1fk", thousands);
        }
        return String.valueOf(coins);
    }

    private String formatItemName(String itemTypeName) {
        if (itemTypeName == null) return "Unknown";
        return itemTypeName.replace("_", " ").toLowerCase();
    }

    private String getFormattedItemName(String itemTypeName) {
        if (itemTypeName == null) return "Unknown Item";
        try {
            ItemType itemType = ItemType.valueOf(itemTypeName);
            SkyBlockItem item = new SkyBlockItem(itemType);
            // Get display name from the updated item
            ItemStack builtItem = new NonPlayerItemUpdater(item).getUpdatedItem().build();
            return StringUtility.getTextFromComponent(builtItem.get(DataComponents.CUSTOM_NAME));
        } catch (Exception e) {
            // Fallback formatting
            String formatted = itemTypeName.replace("_", " ");
            String[] words = formatted.split(" ");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(word.substring(0, 1).toUpperCase())
                            .append(word.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            return result.toString().trim();
        }
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        // Don't remove from auction on close - they might reopen
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        // Player disconnected - they'll be refunded if they were highest bidder
        // when the round ends
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public int refreshRate() {
        return 10; // Refresh every half second
    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        updateItems((SkyBlockPlayer) player);
    }
}
