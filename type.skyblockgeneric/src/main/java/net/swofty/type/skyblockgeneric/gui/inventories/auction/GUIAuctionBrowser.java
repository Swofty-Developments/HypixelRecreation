package net.swofty.type.skyblockgeneric.gui.inventories.auction;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.protocol.objects.auctions.AuctionFetchItemsProtocol;
import net.swofty.commons.skyblock.auctions.AuctionCategories;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.auctions.AuctionsFilter;
import net.swofty.commons.skyblock.auctions.AuctionsSorting;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import org.tinylog.Logger;
import net.swofty.type.skyblockgeneric.auction.AuctionItemLoreHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Setter
public class GUIAuctionBrowser extends HypixelInventoryGUI implements RefreshingGUI {
    private static final int[] PAGINATED_SLOTS = new int[]{
            11, 12, 13, 14, 15, 16,
            20, 21, 22, 23, 24, 25,
            29, 30, 31, 32, 33, 34,
            38, 39, 40, 41, 42, 43
    };

    private AuctionsSorting sorting = AuctionsSorting.HIGHEST_BID;
    private AuctionsFilter filter = AuctionsFilter.SHOW_ALL;
    @Getter
    private AuctionCategories category = AuctionCategories.WEAPONS;

    private int page = 1;
    @Getter
    private List<AuctionItem> itemCache = new ArrayList<>();

    public GUIAuctionBrowser() {
        super(Text.key("gui_auction.browser.title"), InventoryType.CHEST_6_ROW);

        Thread.startVirtualThread(this::updateItemsCache);
    }

    private void updateItemsCache() {
        AuctionFetchItemsProtocol.AuctionFetchItemsMessage message =
                new AuctionFetchItemsProtocol.AuctionFetchItemsMessage(
                        sorting,
                        filter,
                        category
                );

        new ProxyService(ServiceType.AUCTION_HOUSE).handleRequest(message)
                .thenAccept(responseRaw -> {
                    AuctionFetchItemsProtocol.AuctionFetchItemsResponse response = (AuctionFetchItemsProtocol.AuctionFetchItemsResponse) responseRaw;
                    List<AuctionItem> auctionItems = response.items();

                    // Items are already sorted, so just paginate them
                    PaginationList<AuctionItem> paginationList = new PaginationList<>(auctionItems, 24);
                    paginationList.addAll(auctionItems);

                    // Set the items in the GUI
                    List<AuctionItem> paginatedItems = paginationList.getPage(page);
                    setItemCache(paginatedItems);
                })
                .exceptionally(ex -> {
                    Logger.error(ex, "Auction browse failed for category {}", category);
                    return null;
                });
    }

    @SneakyThrows
    private void setItems() {
        fill(ItemStacks.named(category.getMaterial(), ""));
        set(GUIClickableItem.getGoBackItem(49, new GUIAuctionHouse()));
        setTitle(Text.key("gui_auction.browser.title_with_category",
            StringUtility.toNormalCase(category.name())));

        set(new GUIClickableItem(50) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                AuctionsSorting nextSort = sorting.next();
                setSorting(nextSort);

                if (filter.equals(AuctionsFilter.BIN_ONLY)) {
                    // Ensure that the auctions sorting isn't MOST_BIDS
                    if (nextSort.equals(AuctionsSorting.MOST_BIDS)) {
                        setSorting(AuctionsSorting.HIGHEST_BID);
                    }
                }

                Thread.startVirtualThread(() -> updateItemsCache());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>(List.of(Text.literal(" ")));

                Arrays.stream(AuctionsSorting.values()).forEach(sort -> {
                    if (filter.equals(AuctionsFilter.BIN_ONLY)) {
                        if (sort.equals(AuctionsSorting.MOST_BIDS)) {
                            return;
                        }
                    }

                    if (sort == sorting) {
                        lore.add(Text.of("<key:'gui_auction.browser.sort_selected_prefix'><b>{}",
                                StringUtility.toNormalCase(sort.name())));
                    } else {
                        lore.add(Text.of("<key:'gui_auction.browser.sort_unselected_prefix'><7>{}",
                                StringUtility.toNormalCase(sort.name())));
                    }
                });

                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.browser.sort_click"));

                return ItemStacks.item(Material.HOPPER, 1,
                        Text.key("gui_auction.browser.sort_button"), lore);
            }
        });
        set(new GUIClickableItem(52) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                if (e.getClick() instanceof Click.Right) {
                    AuctionsFilter nextFilter = filter.previous();
                    setFilter(nextFilter);

                    Thread.startVirtualThread(() -> updateItemsCache());
                    return;
                }
                AuctionsFilter nextFilter = filter.next();
                setFilter(nextFilter);

                Thread.startVirtualThread(() -> updateItemsCache());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>(List.of(Text.literal(" ")));

                Arrays.stream(AuctionsFilter.values()).forEach(filter -> {
                    if (filter == GUIAuctionBrowser.this.filter) {
                        lore.add(Text.of("<key:'gui_auction.browser.filter_selected_prefix'><b>{}",
                                StringUtility.toNormalCase(filter.name())));
                    } else {
                        lore.add(Text.of("<key:'gui_auction.browser.filter_unselected_prefix'><7>{}",
                                StringUtility.toNormalCase(filter.name())));
                    }
                });

                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.browser.filter_right_click"));
                lore.add(Text.key("gui_auction.browser.filter_click"));

                return ItemStacks.item(Material.GOLD_BLOCK, 1,
                        Text.key("gui_auction.browser.filter_button"), lore);
            }
        });
        for (int i = 0; i < AuctionCategories.values().length; i++) {
            AuctionCategories category = AuctionCategories.values()[i];
            set(new GUIClickableItem(i * 9) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    if (category.equals(getCategory())) {
                        return;
                    }

                    setCategory(category);
                    Thread.startVirtualThread(() -> updateItemsCache());
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    List<Text> lore = new ArrayList<>(List.of(
                            Text.key("gui_auction.browser.category_subtitle"),
                            Text.literal(" "),
                            Text.key("gui_auction.browser.category_examples")));
                    category.getExamples().forEach(example ->
                            lore.add(Text.of("<key:'gui_auction.browser.category_example_prefix'><7>{}", example)));
                    lore.add(Text.literal(" "));

                    if (category.equals(getCategory())) {
                        lore.add(Text.key("gui_auction.browser.category_browsing"));
                    } else {
                        lore.add(Text.key("gui_auction.browser.category_click"));
                    }

                    return ItemStacks.item(category.getDisplayMaterial(), 1,
                            Text.of("<color:{}>{}", category.getColor(), StringUtility.toNormalCase(category.name())), lore);
                }
            });
        }

        int highestCoveredSlot = 0;

        if (getItemCache() == null) {
            fillInAir(highestCoveredSlot);
            return;
        }

        for (AuctionItem auctionItem : getItemCache()) {
            int slot = PAGINATED_SLOTS[getItemCache().indexOf(auctionItem)];
            highestCoveredSlot++;

            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    new GUIAuctionViewItem(auctionItem.getUuid(), GUIAuctionBrowser.this).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    SkyBlockItem skyBlockItem = new SkyBlockItem(auctionItem.getItem());

                    return ItemStacks.item(skyBlockItem.getMaterial(), skyBlockItem.getAmount(),
                            skyBlockItem.getDisplayNameText(player),
                            new AuctionItemLoreHandler(auctionItem).getLoreTexts());
                }
            });
        }

        // Fill the rest of the slots with air
        fillInAir(highestCoveredSlot / 2);
    }

    private void fillInAir(int highestCoveredSlot) {
        for (int i = highestCoveredSlot; i < PAGINATED_SLOTS.length; i++) {
            int slot = PAGINATED_SLOTS[i];
            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.AIR, 1, Text.literal(" "), List.of());
                }
            });
        }
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
        e.setCancelled(true);
    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        new ProxyService(ServiceType.AUCTION_HOUSE).isOnline().thenAccept(online -> {
            if (!online) {
                player.sendMessage(Text.key("gui_auction.browser.offline_message"));
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
