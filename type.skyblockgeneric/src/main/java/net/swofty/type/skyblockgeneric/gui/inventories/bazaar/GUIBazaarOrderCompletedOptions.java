package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarConnector;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCompletedBazaarTransactions;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GUIBazaarOrderCompletedOptions extends HypixelInventoryGUI {
    private final List<DatapointCompletedBazaarTransactions.CompletedBazaarTransaction> completions;
    private final BazaarConnector.BazaarOrder activeOrder;
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            .withZone(ZoneId.systemDefault());

    // Cached calculation results
    private final TransactionSummary summary;

    public GUIBazaarOrderCompletedOptions(List<DatapointCompletedBazaarTransactions.CompletedBazaarTransaction> completions,
                                          BazaarConnector.BazaarOrder activeOrder) {
        super(Text.key("gui_bazaar.order_completed.title"), InventoryType.CHEST_4_ROW);
        this.completions = completions;
        this.activeOrder = activeOrder;
        this.summary = calculateSummary(completions);

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(27, new GUIBazaarOrders()));
        set(GUIClickableItem.getCloseItem(35));

        setupItems();
    }

    /**
     * Calculate all transaction statistics in a single pass for better performance
     */
    private static TransactionSummary calculateSummary(List<DatapointCompletedBazaarTransactions.CompletedBazaarTransaction> completions) {
        if (completions == null || completions.isEmpty()) {
            return new TransactionSummary(0, 0, 0, 0, 0);
        }

        double totalQuantity = 0;
        double totalValue = 0;
        double totalSpent = 0;
        double totalSecondaryAmount = 0;

        for (var tx : completions) {
            totalQuantity += tx.getQuantity();
            totalValue += tx.getTotalValue();
            totalSpent += tx.getPricePerUnit() * tx.getQuantity();
            totalSecondaryAmount += tx.getSecondaryAmount();
        }

        return new TransactionSummary(
                totalQuantity,
                totalValue,
                totalSpent,
                totalSecondaryAmount,
                completions.size()
        );
    }

    private record TransactionSummary(
            double totalQuantity,
            double totalValue,
            double totalSpent,
            double totalSecondaryAmount, // Tax for sells, savings/refund for buys
            int transactionCount
    ) {}

    private void setupItems() {
        if (completions == null || completions.isEmpty()) return;

        var firstCompletion = completions.getFirst();
        String itemName = firstCompletion.getItemName();
        ItemType itemType;

        try {
            itemType = ItemType.valueOf(itemName);
        } catch (IllegalArgumentException e) {
            itemType = ItemType.STONE;
        }

        boolean isSell = isSellOrder();

        ItemType finalItemType = itemType;
        set(new GUIItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();

                lore.add(Text.key("gui_bazaar.order_completed.order_completed_label"));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.order_completed.completed_count", (int) summary.totalQuantity, finalItemType.getDisplayName()));
                lore.add(Text.key("gui_bazaar.order_completed.transactions_count", summary.transactionCount));
                lore.add(Text.literal(" "));

                if (isSell) {
                    lore.add(Text.key("gui_bazaar.order_completed.gross_earnings", FORMATTER.format(summary.totalSpent)));
                    lore.add(Text.key("gui_bazaar.order_completed.tax_paid", FORMATTER.format(summary.totalSecondaryAmount)));
                    lore.add(Text.key("gui_bazaar.order_completed.net_earnings", FORMATTER.format(Math.abs(summary.totalValue))));
                } else {
                    lore.add(Text.key("gui_bazaar.order_completed.total_spent", FORMATTER.format(summary.totalSpent)));
                    if (summary.totalSecondaryAmount > 0) {
                        lore.add(Text.key("gui_bazaar.order_completed.total_saved", FORMATTER.format(summary.totalSecondaryAmount)));
                        lore.add(Text.key("gui_bazaar.order_completed.refund_ready", FORMATTER.format(summary.totalSecondaryAmount)));
                    }
                }

                return ItemStacks.item(
                        finalItemType.material,
                        Math.max(1, (int) summary.totalQuantity),
                        Text.key("gui_bazaar.order_completed.order_name", finalItemType.getDisplayName()),
                        lore
                );
            }
        });

        setupTransactionHistory();
        setupClaimButton();
        setupToggleButton();
    }

    private void setupTransactionHistory() {
        set(new GUIItem(10) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.order_completed.transaction_subtitle"));
                lore.add(Text.literal(" "));

                int count = 0;
                for (var tx : completions) {
                    if (count >= 10) {
                        lore.add(Text.key("gui_bazaar.order_completed.transaction_more", completions.size() - 10));
                        break;
                    }

                    String timeStr = TIME_FORMATTER.format(tx.getTimestamp());

                    if (tx.getType() == DatapointCompletedBazaarTransactions.TransactionType.BUY_COMPLETED) {
                        lore.add(Text.of("<a>▲ {}x at <6>{} <8>({})",
                                (int) tx.getQuantity(), FORMATTER.format(tx.getPricePerUnit()), timeStr));
                        if (tx.getSecondaryAmount() > 0) {
                            lore.add(Text.of("  ").append(Text.key("gui_bazaar.order_completed.transaction_saved",
                                    FORMATTER.format(tx.getSecondaryAmount()))));
                        }
                    } else if (tx.getType() == DatapointCompletedBazaarTransactions.TransactionType.SELL_COMPLETED) {
                        lore.add(Text.of("<6>▼ {}x at <6>{} <8>({})",
                                (int) tx.getQuantity(), FORMATTER.format(tx.getPricePerUnit()), timeStr));
                        lore.add(Text.of("  ").append(Text.key("gui_bazaar.order_completed.transaction_tax",
                                FORMATTER.format(tx.getSecondaryAmount()))));
                    } else if (tx.getType() == DatapointCompletedBazaarTransactions.TransactionType.REFUND) {
                        lore.add(Text.key("gui_bazaar.order_completed.refund_label", FORMATTER.format(tx.getSecondaryAmount()))
                                .append(" <8>({})", timeStr));
                    }
                    count++;
                }

                return ItemStacks.item(Material.BOOK, 1, Text.key("gui_bazaar.order_completed.transaction_history"), lore);
            }
        });
    }

    private void setupClaimButton() {
        set(new GUIClickableItem(16) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                claimRewards(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>(Text.keyLines("gui_bazaar.order_completed.claim_rewards_header"));
                lore.add(Text.literal(" "));

                boolean isSell = isSellOrder();
                if (isSell) {
                    lore.add(Text.of("<6>▶ +{} coins", FORMATTER.format(Math.abs(summary.totalValue))));
                } else {
                    lore.add(Text.of("<a>▶ +{}x {}", (int) summary.totalQuantity, getItemType().getDisplayName()));
                    if (summary.totalSecondaryAmount > 0) {
                        lore.add(Text.of("<6>▶ +{} coins refund", FORMATTER.format(summary.totalSecondaryAmount)));
                    }
                }

                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.order_completed.claim_click"));

                return ItemStacks.item(Material.CHEST, 1, Text.key("gui_bazaar.order_completed.claim_rewards"), lore);
            }
        });
    }

    private void setupToggleButton() {
        if (activeOrder == null) return;

        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIBazaarOrderOptions(activeOrder).open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.COMPASS, 1, Text.key("gui_bazaar.order_completed.view_unfilled"),
                        Text.keyLines("gui_bazaar.order_completed.view_unfilled.lore",
                                (int) activeOrder.amount(), FORMATTER.format(activeOrder.price())));
            }
        });
    }

    private void claimRewards(SkyBlockPlayer player) {
        if (completions == null || completions.isEmpty()) {
            player.sendMessage(Text.key("gui_bazaar.order_completed.no_rewards"));
            return;
        }

        boolean isSell = isSellOrder();
        ItemType itemType = getItemType();

        try {
            if (isSell) {
                player.addCoins(Math.abs(summary.totalValue));
                player.sendMessage(Text.key("gui_bazaar.order_completed.received_coins", FORMATTER.format(Math.abs(summary.totalValue))));
            } else {
                SkyBlockItem item = new SkyBlockItem(itemType);
                item.setAmount((int) summary.totalQuantity);
                player.addAndUpdateItem(item);
                player.sendMessage(Text.key("gui_bazaar.order_completed.received_items", (int) summary.totalQuantity, itemType.getDisplayName()));

                if (summary.totalSecondaryAmount > 0) {
                    player.addCoins(summary.totalSecondaryAmount);
                    player.sendMessage(Text.key("gui_bazaar.order_completed.received_refund", FORMATTER.format(summary.totalSecondaryAmount)));
                }
            }

            var completedTransactions = player.getSkyblockDataHandler().get(
                    SkyBlockDataHandler.Data.COMPLETED_BAZAAR_TRANSACTIONS,
                    DatapointCompletedBazaarTransactions.class
            ).getValue();

            List<String> transactionIds = completions.stream()
                    .map(DatapointCompletedBazaarTransactions.CompletedBazaarTransaction::getId)
                    .toList();

            completedTransactions.claimTransactions(transactionIds);

            player.sendMessage(Text.key("gui_bazaar.order_completed.all_claimed"));
            player.playSuccessSound();

            new GUIBazaarOrders().open(player);

        } catch (Exception e) {
            player.sendMessage(Text.key("gui_bazaar.order_completed.claim_failed", e.getMessage()));
            org.tinylog.Logger.error(e, "Failed to claim bazaar rewards");
        }
    }

    private boolean isSellOrder() {
        if (completions == null || completions.isEmpty()) return false;
        var firstCompletion = completions.getFirst();
        return firstCompletion.getType() == DatapointCompletedBazaarTransactions.TransactionType.SELL_COMPLETED ||
                firstCompletion.getType() == DatapointCompletedBazaarTransactions.TransactionType.SELL_ORDER_EXPIRED;
    }

    private ItemType getItemType() {
        if (completions == null || completions.isEmpty()) return ItemType.STONE;
        try {
            return ItemType.valueOf(completions.getFirst().getItemName());
        } catch (IllegalArgumentException e) {
            return ItemType.STONE;
        }
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
