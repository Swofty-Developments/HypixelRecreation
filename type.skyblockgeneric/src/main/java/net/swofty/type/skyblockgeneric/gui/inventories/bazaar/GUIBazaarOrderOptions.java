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

public class GUIBazaarOrderOptions extends HypixelInventoryGUI {
    private final BazaarConnector.BazaarOrder order;
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            .withZone(ZoneId.systemDefault());

    public GUIBazaarOrderOptions(BazaarConnector.BazaarOrder order) {
        super(Text.key("gui_bazaar.order_options.title", order.getItemType().getDisplayName()), InventoryType.CHEST_4_ROW);
        this.order = order;

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(27, new GUIBazaarOrders()));
        set(GUIClickableItem.getCloseItem(35));

        setupItems();
    }

    private void setupItems() {
        ItemType itemType = order.getItemType();
        if (itemType == null) return;

        boolean isSell = order.side() == BazaarConnector.OrderSide.SELL;

        set(new GUIItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();

                var relatedTransactions = getRelatedTransactions(player);
                double originalQuantity = getOriginalQuantity(player);
                double filledQuantity = getFilledQuantity(relatedTransactions);

                lore.add(Text.key(isSell ? "gui_bazaar.order_options.order_type_sell" : "gui_bazaar.order_options.order_type_buy"));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.order_options.original_amount", (int) originalQuantity));
                lore.add(Text.key("gui_bazaar.order_options.remaining_amount", (int) order.amount()));
                lore.add(Text.key("gui_bazaar.order_options.price_per_unit", FORMATTER.format(order.price())));
                lore.add(Text.literal(" "));

                if (filledQuantity > 0) {
                    lore.add(Text.key("gui_bazaar.order_options.filled_items", (int) filledQuantity));
                    lore.add(Text.key("gui_bazaar.order_options.filled_progress",
                            String.format("%.1f%%", (filledQuantity / originalQuantity) * 100)));
                } else {
                    lore.add(Text.key("gui_bazaar.order_options.no_fills"));
                    lore.add(Text.key("gui_bazaar.order_options.no_fills_progress"));
                }

                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.order_options.order_id", order.orderId().toString().substring(0, 8)));

                return ItemStacks.item(
                        itemType.material,
                        Math.max(1, (int) order.amount()),
                        Text.of(isSell ? "<6>{} Order" : "<a>{} Order", itemType.getDisplayName()),
                        lore
                );
            }
        });

        setupTransactionHistory();
        setupFinancialSummary();
        setupActionButtons();
        setupToggleButton();
    }

    private void setupTransactionHistory() {
        set(new GUIItem(10) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.order_options.transaction_history_subtitle"));
                lore.add(Text.literal(" "));

                var transactions = getRelatedTransactions(player);
                if (transactions.isEmpty()) {
                    lore.add(Text.key("gui_bazaar.order_options.no_transactions"));
                    lore.add(Text.key("gui_bazaar.order_options.no_transactions_pending"));
                } else {
                    lore.add(Text.key("gui_bazaar.order_options.transaction_count",
                            transactions.size(), transactions.size() == 1 ? "" : "s"));
                    lore.add(Text.literal(" "));

                    int count = 0;
                    for (var tx : transactions) {
                        if (count >= 5) {
                            lore.add(Text.key("gui_bazaar.order_options.transaction_more", transactions.size() - 5));
                            break;
                        }

                        String timeStr = TIME_FORMATTER.format(tx.getTimestamp());
                        if (tx.getType() == DatapointCompletedBazaarTransactions.TransactionType.BUY_COMPLETED) {
                            lore.add(Text.of("<a>▲ {}x at <6>{} <8>({})",
                                    (int) tx.getQuantity(), FORMATTER.format(tx.getPricePerUnit()), timeStr));
                            if (tx.getSecondaryAmount() > 0) {
                                lore.add(Text.of("  ").append(Text.key("gui_bazaar.order_options.transaction_saved",
                                        FORMATTER.format(tx.getSecondaryAmount()))));
                            }
                        } else if (tx.getType() == DatapointCompletedBazaarTransactions.TransactionType.SELL_COMPLETED) {
                            lore.add(Text.of("<6>▼ {}x at <6>{} <8>({})",
                                    (int) tx.getQuantity(), FORMATTER.format(tx.getPricePerUnit()), timeStr));
                            lore.add(Text.of("  ").append(Text.key("gui_bazaar.order_options.transaction_tax",
                                    FORMATTER.format(tx.getSecondaryAmount()))));
                        }
                        count++;
                    }
                }

                return ItemStacks.item(Material.BOOK, 1, Text.key("gui_bazaar.order_options.transaction_history"), lore);
            }
        });
    }

    private void setupFinancialSummary() {
        boolean isSell = order.side() == BazaarConnector.OrderSide.SELL;

        set(new GUIItem(16) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.order_options.financial_subtitle"));
                lore.add(Text.literal(" "));

                var transactions = getRelatedTransactions(player);
                double originalValue = getOriginalQuantity(player) * order.price();
                double currentValue = order.amount() * order.price();

                lore.add(Text.key("gui_bazaar.order_options.original_order_value"));
                lore.add(Text.of("  <6>{} coins", FORMATTER.format(originalValue)));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.order_options.remaining_order_value"));
                lore.add(Text.of("  <6>{} coins", FORMATTER.format(currentValue)));
                lore.add(Text.literal(" "));

                if (!transactions.isEmpty()) {
                    if (isSell) {
                        double totalEarned = transactions.stream()
                                .mapToDouble(tx -> tx.getPricePerUnit() * tx.getQuantity())
                                .sum();
                        double totalTax = transactions.stream()
                                .mapToDouble(DatapointCompletedBazaarTransactions.CompletedBazaarTransaction::getSecondaryAmount)
                                .sum();
                        double netEarned = totalEarned - totalTax;

                        lore.add(Text.key("gui_bazaar.order_options.transactions_completed"));
                        lore.add(Text.of("  ").append(Text.key("gui_bazaar.order_options.gross_prefix"))
                                .append("<6>{} coins", FORMATTER.format(totalEarned)));
                        lore.add(Text.of("  ").append(Text.key("gui_bazaar.order_options.tax_prefix"))
                                .append("<6>{} coins", FORMATTER.format(totalTax)));
                        lore.add(Text.of("  ").append(Text.key("gui_bazaar.order_options.net_prefix"))
                                .append("<6>{} coins", FORMATTER.format(netEarned)));
                        lore.add(Text.literal(" "));
                        lore.add(Text.key("gui_bazaar.order_options.avg_sell_price"));
                        lore.add(Text.of("  <6>{} coins/item", FORMATTER.format(totalEarned / getFilledQuantity(transactions))));
                    } else {
                        double totalSpent = transactions.stream()
                                .mapToDouble(tx -> tx.getPricePerUnit() * tx.getQuantity())
                                .sum();
                        double totalSaved = transactions.stream()
                                .mapToDouble(DatapointCompletedBazaarTransactions.CompletedBazaarTransaction::getSecondaryAmount)
                                .sum();

                        lore.add(Text.key("gui_bazaar.order_options.transactions_completed"));
                        lore.add(Text.of("  ").append(Text.key("gui_bazaar.order_options.spent_prefix"))
                                .append("<6>{} coins", FORMATTER.format(totalSpent)));
                        lore.add(Text.of("  ").append(Text.key("gui_bazaar.order_options.saved_prefix"))
                                .append("<6>{} coins", FORMATTER.format(totalSaved)));
                        lore.add(Text.literal(" "));
                        lore.add(Text.key("gui_bazaar.order_options.avg_buy_price"));
                        lore.add(Text.of("  <6>{} coins/item", FORMATTER.format(totalSpent / getFilledQuantity(transactions))));
                        lore.add(Text.key("gui_bazaar.order_options.vs_your_bid", FORMATTER.format(order.price())));
                    }
                }

                return ItemStacks.item(Material.GOLD_INGOT, 1, Text.key("gui_bazaar.order_options.financial_summary"), lore);
            }
        });
    }

    private void setupActionButtons() {
        ItemType itemType = order.getItemType();
        boolean isSell = order.side() == BazaarConnector.OrderSide.SELL;

        set(new GUIClickableItem(20) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                p.sendMessage(Text.key("gui_bazaar.order_options.cancel_message"));

                player.getBazaarConnector().cancelOrder(order.orderId())
                        .thenAccept(success -> {
                            if (success) {
                                p.sendMessage(Text.key("gui_bazaar.order_options.cancel_success"));

                                if (isSell) {
                                    SkyBlockItem item = new SkyBlockItem(order.getItemType());
                                    item.setAmount((int) order.amount());
                                    player.addAndUpdateItem(item);
                                    p.sendMessage(Text.key("gui_bazaar.order_options.cancel_return_items", (int) order.amount(), itemType.getDisplayName()));
                                } else {
                                    double refund = order.price() * order.amount();
                                    player.addCoins(refund);
                                    p.sendMessage(Text.key("gui_bazaar.order_options.cancel_refund_coins", FORMATTER.format(refund)));
                                }

                                new GUIBazaarOrders().open(p);
                            } else {
                                p.sendMessage(Text.key("gui_bazaar.order_options.cancel_failed"));
                            }
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>(Text.keyLines("gui_bazaar.order_options.cancel_order.lore_header"));
                lore.add(Text.literal(" "));

                if (isSell) {
                    lore.add(Text.of("<a>▶ {}x {}", (int) order.amount(), itemType.getDisplayName()));
                } else {
                    lore.add(Text.of("<6>▶ {} coins", FORMATTER.format(order.amount() * order.price())));
                }

                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.order_options.cancel_undone"));
                lore.addAll(Text.keyLines("gui_bazaar.order_options.cancel_completed_remain"));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.order_options.cancel_click"));

                return ItemStacks.item(Material.RED_DYE, 1, Text.key("gui_bazaar.order_options.cancel_order"), lore);
            }
        });

        set(new GUIClickableItem(24) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIBazaarItem(itemType).open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.EMERALD, 1, Text.key("gui_bazaar.order_options.view_market"),
                        Text.keyLines("gui_bazaar.order_options.view_market.lore", itemType.getDisplayName()));
            }
        });
    }

    private void setupToggleButton() {
        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                var completedTransactions = player.getSkyblockDataHandler().get(
                        SkyBlockDataHandler.Data.COMPLETED_BAZAAR_TRANSACTIONS,
                        DatapointCompletedBazaarTransactions.class
                ).getValue();

                var unclaimedForThisOrder = completedTransactions.getUnclaimedTransactions().stream()
                        .filter(tx -> order.orderId().equals(tx.getRelatedOrderId()))
                        .toList();

                if (!unclaimedForThisOrder.isEmpty()) {
                    new GUIBazaarOrderCompletedOptions(unclaimedForThisOrder, order).open(p);
                } else {
                    p.sendMessage(Text.key("gui_bazaar.order_options.no_completed_message"));
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                var completedTransactions = player.getSkyblockDataHandler().get(
                        SkyBlockDataHandler.Data.COMPLETED_BAZAAR_TRANSACTIONS,
                        DatapointCompletedBazaarTransactions.class
                ).getValue();

                var unclaimedForThisOrder = completedTransactions.getUnclaimedTransactions().stream()
                        .filter(tx -> order.orderId().equals(tx.getRelatedOrderId()))
                        .toList();

                if (unclaimedForThisOrder.isEmpty()) {
                    return ItemStacks.item(Material.GRAY_DYE, 1, Text.key("gui_bazaar.order_options.no_completed"),
                            Text.keyLines("gui_bazaar.order_options.no_completed.lore"));
                } else {
                    double totalValue = unclaimedForThisOrder.stream()
                            .mapToDouble(DatapointCompletedBazaarTransactions.CompletedBazaarTransaction::getTotalValue)
                            .sum();

                    return ItemStacks.item(Material.CHEST, 1, Text.key("gui_bazaar.order_options.view_completed"),
                            Text.keyLines("gui_bazaar.order_options.view_completed.lore",
                                    unclaimedForThisOrder.size(),
                                    unclaimedForThisOrder.size() == 1 ? "" : "s",
                                    FORMATTER.format(Math.abs(totalValue))));
                }
            }
        });
    }

    private List<DatapointCompletedBazaarTransactions.CompletedBazaarTransaction> getRelatedTransactions(SkyBlockPlayer p) {
        var completedTransactions = p.getSkyblockDataHandler().get(
                SkyBlockDataHandler.Data.COMPLETED_BAZAAR_TRANSACTIONS,
                DatapointCompletedBazaarTransactions.class
        ).getValue();

        return completedTransactions.getTransactions().stream()
                .filter(tx -> order.orderId().equals(tx.getRelatedOrderId()))
                .filter(tx -> order.itemName().equals(tx.getItemName()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList();
    }

    private double getOriginalQuantity(SkyBlockPlayer p) {
        var transactions = getRelatedTransactions(p);
        double filledQty = transactions.stream()
                .mapToDouble(DatapointCompletedBazaarTransactions.CompletedBazaarTransaction::getQuantity)
                .sum();
        return order.amount() + filledQty;
    }

    private double getFilledQuantity(List<DatapointCompletedBazaarTransactions.CompletedBazaarTransaction> transactions) {
        return transactions.stream()
                .mapToDouble(DatapointCompletedBazaarTransactions.CompletedBazaarTransaction::getQuantity)
                .sum();
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
