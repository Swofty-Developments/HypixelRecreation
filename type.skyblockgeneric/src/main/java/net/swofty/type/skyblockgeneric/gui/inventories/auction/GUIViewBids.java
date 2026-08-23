package net.swofty.type.skyblockgeneric.gui.inventories.auction;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.auctions.AuctionFetchItemProtocol;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import net.swofty.type.skyblockgeneric.auction.AuctionItemLoreHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GUIViewBids extends HypixelInventoryGUI implements RefreshingGUI {
    public GUIViewBids() {
        super(Text.key("gui_auction.bids.title"), InventoryType.CHEST_3_ROW);

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(22, new GUIAuctionHouse()));
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        setItems();
    }

    public void setItems() {
        List<UUID> auctions = ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ACTIVE_BIDS, DatapointUUIDList.class).getValue();
        List<CompletableFuture<AuctionFetchItemProtocol.AuctionFetchItemResponse>> futures = new ArrayList<>(auctions.size());
        PaginationList<AuctionItem> auctionItems = new PaginationList<>(7);

        auctions.forEach(uuid -> {
            AuctionFetchItemProtocol.AuctionFetchItemMessage message =
                    new AuctionFetchItemProtocol.AuctionFetchItemMessage(uuid);
            CompletableFuture<AuctionFetchItemProtocol.AuctionFetchItemResponse> future =
                    new ProxyService(ServiceType.AUCTION_HOUSE).handleRequest(message);

            future.thenAccept(response -> {
                AuctionItem item = response.item();
                synchronized (auctionItems) {
                    auctionItems.add(item);
                }
            }).exceptionally(ex -> {
                Logger.error(ex, "Failed to process auction bid");
                return null;
            });
            futures.add(future);
        });

        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allDone.thenRun(() -> {
            auctionItems.sort((o1, o2) -> Long.compare(o2.getEndTime(), o1.getEndTime()));

            List<AuctionItem> auctionItemsPage = auctionItems.getPage(1);

            for (int i = 0; i < 7; i++) {
                int slot = i + 10;

                if (i >= auctionItems.size()) {
                    set(new GUIItem(slot) {
                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            SkyBlockPlayer player = (SkyBlockPlayer) p;
                            return ItemStack.builder(Material.AIR);
                        }
                    });
                    continue;
                }

                AuctionItem item = auctionItemsPage.get(i);
                set(new GUIClickableItem(slot) {
                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        new GUIAuctionViewItem(item.getUuid(), GUIViewBids.this).open(player);
                    }

                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        return ItemStacks.item(item.getItem().material(), item.getItem().amount(),
                                ItemStacks.nameText(new NonPlayerItemUpdater(item.getItem()).getUpdatedItem()),
                                new AuctionItemLoreHandler(item).getLoreTexts(player));
                    }
                });
            }
        });
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {

    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {

    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        new ProxyService(ServiceType.AUCTION_HOUSE).isOnline().thenAccept(online -> {
            if (!online) {
                player.sendMessage(Text.key("gui_auction.bids.offline_message"));
                player.closeInventory();
                return;
            }
            setItems();
        });
    }

    @Override
    public int refreshRate() {
        return 10;
    }
}
