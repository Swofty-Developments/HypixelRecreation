package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarConnector;
import net.swofty.type.skyblockgeneric.bazaar.BazaarItemSet;
import net.swofty.type.skyblockgeneric.gui.inventories.bazaar.selections.GUIBazaarOrderAmountSelection;
import net.swofty.type.skyblockgeneric.gui.inventories.bazaar.selections.GUIBazaarPriceSelection;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIBazaarItem extends HypixelInventoryGUI implements RefreshingGUI {
    private final ItemType itemType;
    private final boolean specialBazaar;
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
    private BazaarConnector.BazaarStatistics currentStats;

    public GUIBazaarItem(ItemType itemType) {
        this(itemType, false);
    }

    public GUIBazaarItem(ItemType itemType, boolean specialBazaar) {
        super(Text.key("gui_bazaar.item.title", BazaarCategories.getFromItem(itemType).getValue().displayName, itemType.getDisplayName()), InventoryType.CHEST_4_ROW);
        this.itemType = itemType;
        this.specialBazaar = specialBazaar;

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));

        Map.Entry<BazaarCategories, BazaarItemSet> bazaarCategory = BazaarCategories.getFromItem(itemType);
        set(GUIClickableItem.getGoBackItem(30,
                new GUIBazaarItemSet(bazaarCategory.getKey(), bazaarCategory.getValue())));

        if (!specialBazaar) set(new GUIClickableItem(32) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIBazaarOrders().open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.BOOK, """
                        <key:gui_bazaar.item.manage_orders_button>
                        <key:gui_bazaar.item.manage_orders_button.lore.1>
                        <key:gui_bazaar.item.manage_orders_button.lore.2>""");
            }
        });

        if (!specialBazaar) set(new GUIClickableItem(31) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIBazaar(BazaarCategories.getFromItem(itemType).getKey()).open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.head("c232e3820897429157619b0ee099fec0628f602fff12b695de54aef11d923ad7", """
                        <key:gui_bazaar.item.go_back_bazaar>
                        <key:gui_bazaar.item.go_back_bazaar.lore>""");
            }
        });

        set(new GUIItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return new NonPlayerItemUpdater(new SkyBlockItem(itemType)).getUpdatedItem();
            }
        });
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (player.isIronman() && itemType != ItemType.BOOSTER_COOKIE) {
            player.openView(new GUISpecialBazaar());
            return;
        }
        player.getBazaarConnector().getItemStatistics(itemType)
                .thenAccept(stats -> {
                    this.currentStats = stats;
                    updateItems(stats);
                });
    }

    private void updateItems(BazaarConnector.BazaarStatistics stats) {
        set(new GUIClickableItem(10) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (stats.bestAsk() <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.buy_no_offers_message"));
                    return;
                }

                int maxSpace = player.maxItemFit(itemType);
                if (maxSpace <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.buy_inventory_full"));
                    return;
                }

                double priceWithFee = stats.bestAsk() * 1.04;

                new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, itemType, true, true, maxSpace, priceWithFee)
                        .openAmountSelection(player)
                        .thenAccept(amount -> {
                            if (amount <= 0) return;

                            double totalCost = priceWithFee * amount;
                            if (totalCost > player.getCoins()) {
                                p.sendMessage(Text.key("gui_bazaar.item.buy_need_coins", FORMATTER.format(totalCost)));
                                return;
                            }

                            player.getBazaarConnector().instantBuy(itemType, amount)
                                    .thenAccept(result -> {
                                        player.sendMessage(Text.key("gui_bazaar.item.bazaar_result_prefix")
                                                .append(result.success() ? "<a> {}" : "<c> {}", result.message()));
                                        if (result.success()) p.closeInventory();
                                    });
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.item.buy_instantly_subtitle"));
                lore.add(Text.literal(" "));

                if (stats.bestAsk() <= 0) {
                    lore.add(Text.key("gui_bazaar.item.buy_no_offers"));
                } else {
                    double priceWithFee = stats.bestAsk() * 1.04;
                    lore.add(Text.key("gui_bazaar.item.buy_price", FORMATTER.format(priceWithFee)));
                    lore.add(Text.key("gui_bazaar.item.buy_max_space", player.maxItemFit(itemType)));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.item.buy_click"));
                }

                return ItemStacks.item(Material.GOLDEN_HORSE_ARMOR, 1, Text.key("gui_bazaar.item.buy_instantly"), lore);
            }
        });

        if (specialBazaar) {
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                int have = player.getAmountInInventory(itemType);
                if (have <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.sell_no_items", itemType.getDisplayName()));
                    return;
                }

                if (stats.bestBid() <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.sell_no_orders_message"));
                    return;
                }

                new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, itemType, false, true, have, stats.bestBid())
                        .openAmountSelection(player)
                        .thenAccept(amount -> {
                            if (amount <= 0) return;

                            player.getBazaarConnector().instantSell(itemType)
                                    .thenAccept(result -> {
                                        p.sendMessage(Text.key("gui_bazaar.item.bazaar_result_prefix")
                                                .append(result.success() ? "<a> {}" : "<c> {}", result.message()));
                                        if (result.success()) p.closeInventory();
                                    });
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.item.sell_instantly_subtitle"));
                lore.add(Text.literal(" "));

                int have = player.getAmountInInventory(itemType);
                if (have <= 0) {
                    lore.add(Text.key("gui_bazaar.item.sell_have_zero"));
                } else if (stats.bestBid() <= 0) {
                    lore.add(Text.key("gui_bazaar.item.sell_have", have));
                    lore.add(Text.key("gui_bazaar.item.sell_no_orders"));
                } else {
                    lore.add(Text.key("gui_bazaar.item.sell_have", have));
                    lore.add(Text.key("gui_bazaar.item.sell_price", FORMATTER.format(stats.bestBid())));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.item.sell_click"));
                }

                return ItemStacks.item(Material.HOPPER, 1, Text.key("gui_bazaar.item.sell_instantly"), lore);
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;

                new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, itemType, true, false, 71680, player.getCoins())
                        .openAmountSelection(player)
                        .thenAccept(amount -> {
                            if (amount <= 0) return;

                            new GUIBazaarPriceSelection(
                                    GUIBazaarItem.this, amount, stats.bestAsk(), stats.worstAsk(), itemType, false
                            ).openPriceSelection(player).thenAccept(price -> {
                                if (price <= 0) return;

                                double totalCost = price * amount;
                                if (totalCost > player.getCoins()) {
                                    p.sendMessage(Text.key("gui_bazaar.item.buy_order_need_coins", FORMATTER.format(totalCost)));
                                    new GUIBazaarItem(itemType).open(p);
                                    return;
                                }

                                player.removeCoins(totalCost);
                                p.sendMessage(Text.key("gui_bazaar.item.buy_order_escrow", FORMATTER.format(totalCost)));

                                player.getBazaarConnector().createBuyOrder(itemType, price, amount)
                                        .thenAccept(result -> {
                                            if (result.success()) {
                                                p.sendMessage(Text.key("gui_bazaar.item.buy_order_created", amount, FORMATTER.format(price)));
                                                p.closeInventory();
                                            } else {
                                                player.addCoins(totalCost);
                                                p.sendMessage(Text.key("gui_bazaar.item.buy_order_failed", FORMATTER.format(totalCost)));
                                                new GUIBazaarItem(itemType).open(p);
                                            }
                                        });
                            });
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.item.create_buy_order_subtitle"));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.item.create_buy_order_max"));
                if (stats.bestAsk() > 0) {
                    lore.add(Text.key("gui_bazaar.item.create_buy_order_best_ask", FORMATTER.format(stats.bestAsk())));
                }
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.item.create_buy_order_click"));

                return ItemStacks.item(Material.FILLED_MAP, 1, Text.key("gui_bazaar.item.create_buy_order"), lore);
            }
        });

        set(new GUIClickableItem(16) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                int have = player.getAmountInInventory(itemType);
                if (have <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.sell_order_no_items", itemType.getDisplayName()));
                    return;
                }

                new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, itemType, false, false, have, 0)
                        .openAmountSelection(player)
                        .thenAccept(amount -> {
                            if (amount <= 0) return;

                            new GUIBazaarPriceSelection(
                                    GUIBazaarItem.this, amount, stats.bestBid(), stats.worstBid(), itemType, true
                            ).openPriceSelection(player).thenAccept(price -> {
                                if (price <= 0) return;

                                var items = player.takeItem(itemType, amount);
                                if (items == null) {
                                    p.sendMessage(Text.key("gui_bazaar.item.sell_order_remove_fail"));
                                    new GUIBazaarItem(itemType).open(p);
                                    return;
                                }

                                p.sendMessage(Text.key("gui_bazaar.item.sell_order_escrow", amount, itemType.getDisplayName()));

                                player.getBazaarConnector().createSellOrder(itemType, price, amount)
                                        .thenAccept(result -> {
                                            if (result.success()) {
                                                p.sendMessage(Text.key("gui_bazaar.item.sell_order_created", amount, FORMATTER.format(price)));
                                                p.closeInventory();
                                            } else {
                                                player.addAndUpdateItem(items);
                                                p.sendMessage(Text.key("gui_bazaar.item.sell_order_failed"));
                                                new GUIBazaarItem(itemType).open(p);
                                            }
                                        });
                            });
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.item.create_sell_order_subtitle"));
                lore.add(Text.literal(" "));
                int have = player.getAmountInInventory(itemType);
                lore.add(Text.key("gui_bazaar.item.create_sell_order_have", have));
                if (stats.bestBid() > 0) {
                    lore.add(Text.key("gui_bazaar.item.create_sell_order_best_bid", FORMATTER.format(stats.bestBid())));
                }
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.item.create_sell_order_click"));

                return ItemStacks.item(Material.MAP, 1, Text.key("gui_bazaar.item.create_sell_order"), lore);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public void refreshItems(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        if (currentStats != null) {
            player.getBazaarConnector().getItemStatistics(itemType)
                    .thenAccept(stats -> {
                        this.currentStats = stats;
                        updateItems(stats);
                    });
        }
    }

    @Override
    public int refreshRate() {
        return 10;
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
