package net.swofty.type.skyblockgeneric.gui.inventories.bazaar.selections;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GUIBazaarPriceSelection extends HypixelInventoryGUI implements RefreshingGUI {
    private CompletableFuture<Double> future = new CompletableFuture<>();
    private final boolean isSellOrder;
    private final ItemType itemTypeLinker;
    private final Double lowestPrice;
    private final Double highestPrice;
    private final Integer amount;


    public GUIBazaarPriceSelection(HypixelInventoryGUI previousGUI, Integer amount,
                                   Double lowestPrice, Double highestPrice,
                                   ItemType itemTypeLinker, boolean isSellOrder) {
        super(Text.key(isSellOrder ? "gui_bazaar.price_selection.title_sell" : "gui_bazaar.price_selection.title_buy"), InventoryType.CHEST_4_ROW);

        this.lowestPrice = lowestPrice;
        this.highestPrice = highestPrice;
        this.itemTypeLinker = itemTypeLinker;
        this.isSellOrder = isSellOrder;
        this.amount = amount;

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(31, previousGUI));
    }

    public CompletableFuture<Double> openPriceSelection(SkyBlockPlayer player) {
        future = new CompletableFuture<>();
        open(player);

        Thread.startVirtualThread(() -> {
            double spread = highestPrice - lowestPrice;
            double spreadPrice = isSellOrder ? highestPrice - (spread / 10) : lowestPrice + (spread / 10);
            set(new GUIClickableItem(14) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    future.complete(spreadPrice);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStacks.item(Material.GOLDEN_HORSE_ARMOR, 1,
                            Text.key("gui_bazaar.price_selection.spread_10pct"),
                            List.of(
                                    Text.key(isSellOrder
                                            ? "gui_bazaar.price_selection.offer_setup_sell"
                                            : "gui_bazaar.price_selection.offer_setup_buy"),
                                    Text.literal(" "),
                                    Text.key("gui_bazaar.price_selection.lowest_price", lowestPrice),
                                    Text.key("gui_bazaar.price_selection.highest_price", highestPrice),
                                    Text.key("gui_bazaar.price_selection.spread", highestPrice, lowestPrice, spread),
                                    Text.literal(" "),
                                    Text.key(isSellOrder
                                            ? "gui_bazaar.price_selection.selling_amount"
                                            : "gui_bazaar.price_selection.buying_amount", amount),
                                    Text.key("gui_bazaar.price_selection.unit_price", spreadPrice),
                                    Text.literal(" "),
                                    Text.key("gui_bazaar.price_selection.total", spreadPrice * amount),
                                    Text.literal(" "),
                                    Text.key("gui_bazaar.price_selection.click_to_use")));
                }
            });


            double incrementedOffer = isSellOrder ? lowestPrice - 0.1 : highestPrice + 0.1;
            set(new GUIClickableItem(12) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    future.complete(incrementedOffer);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.key(isSellOrder
                            ? "gui_bazaar.price_selection.offer_setup_sell"
                            : "gui_bazaar.price_selection.offer_setup_buy"));
                    lore.add(Text.literal(" "));
                    lore.addAll(Text.keyLines("gui_bazaar.price_selection.best_offer_beat"));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key(isSellOrder
                            ? "gui_bazaar.price_selection.selling_amount"
                            : "gui_bazaar.price_selection.buying_amount", amount));
                    lore.add(Text.key("gui_bazaar.price_selection.unit_price", incrementedOffer));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.price_selection.total", incrementedOffer * amount));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.price_selection.click_to_use"));
                    return ItemStacks.item(Material.GOLD_NUGGET, 1,
                            Text.key(isSellOrder
                                    ? "gui_bazaar.price_selection.best_offer_minus"
                                    : "gui_bazaar.price_selection.best_offer_plus"),
                            lore);
                }
            });

            double bestOffer = isSellOrder ? highestPrice : lowestPrice;
            set(new GUIClickableItem(10) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    future.complete(bestOffer);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.key(isSellOrder
                            ? "gui_bazaar.price_selection.offer_setup_sell"
                            : "gui_bazaar.price_selection.offer_setup_buy"));
                    lore.add(Text.literal(" "));
                    lore.addAll(Text.keyLines("gui_bazaar.price_selection.same_as_best.lore"));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key(isSellOrder
                            ? "gui_bazaar.price_selection.selling_amount"
                            : "gui_bazaar.price_selection.buying_amount", amount));
                    lore.add(Text.key("gui_bazaar.price_selection.unit_price", bestOffer));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.price_selection.total", bestOffer * amount));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.price_selection.click_to_use"));
                    return ItemStacks.item(itemTypeLinker.material, 1,
                            Text.key("gui_bazaar.price_selection.same_as_best"), lore);
                }
            });
            set(new GUIQueryItem(16) {
                @Override
                public HypixelInventoryGUI onQueryFinish(String query, HypixelPlayer player) {
                    try {
                        double price = Double.parseDouble(query);
                        if (price <= 0) {
                            throw new NumberFormatException();
                        }
                        future.complete(price);
                        return null;
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.key("gui_bazaar.price_selection.invalid_price"));
                        return null;
                    }
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.key(isSellOrder
                            ? "gui_bazaar.price_selection.offer_setup_sell"
                            : "gui_bazaar.price_selection.offer_setup_buy"));
                    lore.add(Text.literal(" "));
                    lore.addAll(Text.keyLines("gui_bazaar.price_selection.custom_price.lore"));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.price_selection.ordering_amount", amount));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.price_selection.custom_click"));
                    return ItemStacks.item(Material.OAK_SIGN, 1,
                            Text.key("gui_bazaar.price_selection.custom_price"), lore);
                }
            });

            refreshItems(player);
        });

        return future;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        if (reason == CloseReason.SIGN_OPENED) return;
        future.complete(0D);
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        future.complete(0D);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        if (!new ProxyService(ServiceType.BAZAAR).isOnline().join()) {
            player.sendMessage(Text.key("gui_bazaar.price_selection.offline_message"));
            player.closeInventory();
        }
    }

    @Override
    public int refreshRate() {
        return 10;
    }
}
