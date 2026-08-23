package net.swofty.type.skyblockgeneric.gui.inventories.auction;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import net.swofty.commons.skyblock.auctions.AuctionCategories;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionEscrow;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AuctionCategoryComponent;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GUIAuctionCreateItem extends HypixelInventoryGUI implements RefreshingGUI {
    private final HypixelInventoryGUI previousGUI;

    public GUIAuctionCreateItem(HypixelInventoryGUI previousGUI) {
        super(Text.key("gui_auction.create.title"), InventoryType.CHEST_6_ROW);

        this.previousGUI = previousGUI;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        set(GUIClickableItem.getGoBackItem(49, previousGUI));

        DatapointAuctionEscrow.AuctionEscrow escrow = ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).getValue();
        if (escrow.isBin())
            setTitle(Text.key("gui_auction.create.title_bin"));

        set(new GUIClickableItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                if (escrow.getItem() == null)
                    return ItemStacks.item(Material.STONE_BUTTON, 1,
                            Text.key("gui_auction.create.select_item"),
                            List.of(Text.key("gui_auction.create.select_item.lore")));

                SkyBlockItem item = escrow.getItem();
                ItemStack itemStack = new NonPlayerItemUpdater(item).getUpdatedItem().build();
                List<Text> lore = new ArrayList<>();

                lore.add(Text.literal(" "));
                lore.add(ItemStacks.nameText(itemStack));
                lore.addAll(ItemStacks.loreText(itemStack));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.auction_for_item_pickup"));

                return ItemStacks.item(itemStack.material(), itemStack.amount(),
                        Text.key("gui_auction.create.auction_for_item"), lore);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (escrow.getItem() == null) return;
                player.addAndUpdateItem(escrow.getItem());
                escrow.setItem(null);

                new GUIAuctionCreateItem(previousGUI).open(player);
            }
        });

        set(new GUIClickableItem(33) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIAuctionDuration().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();
                if (escrow.isBin()) {
                    lore.addAll(Text.keyLines("gui_auction.create.duration_bin.lore"));
                } else {
                    lore.addAll(Text.keyLines("gui_auction.create.duration_normal.lore"));
                }
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.duration_extra_fee", escrow.getDuration() / 180000));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.duration_click"));

                return ItemStacks.item(Material.CLOCK, 1,
                        Text.key("gui_auction.create.duration_label", StringUtility.getAuctionSetupFormattedTime(escrow.getDuration())),
                        lore);
            }
        });
        set(new GUIClickableItem(48) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                escrow.setBin(!escrow.isBin());
                new GUIAuctionCreateItem(previousGUI).open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                if (escrow.isBin()) {
                    return ItemStacks.item(Material.POWERED_RAIL, 1,
                            Text.key("gui_auction.create.switch_to_auction"),
                            Text.keyLines("gui_auction.create.switch_to_auction.lore"));
                } else {
                    return ItemStacks.item(Material.GOLD_INGOT, 1,
                            Text.key("gui_auction.create.switch_to_bin"),
                            Text.keyLines("gui_auction.create.switch_to_bin.lore"));
                }
            }
        });
        set(new GUIClickableItem(29) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ProxyService auctionService = new ProxyService(ServiceType.AUCTION_HOUSE);

                auctionService.isOnline().thenAccept((response) -> {
                    if (escrow.getItem() == null || !response)
                        return;

                    long fee = (long) ((escrow.getPrice() * 0.05) + ((double) escrow.getDuration() / 180000));
                    DatapointDouble coins = player.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.COINS, DatapointDouble.class);
                    if (coins.getValue() < fee) {
                        player.sendMessage(Text.key("gui_auction.create.not_enough_coins"));
                        return;
                    }
                    coins.setValue(coins.getValue() - fee);

                    player.closeInventory();

                    player.sendMessage(Text.key("gui_auction.create.escrow_message"));

                    AuctionItem item = new AuctionItem(escrow.getItem().toUnderstandable(), player.getUuid(), escrow.getDuration() + System.currentTimeMillis(),
                            escrow.isBin(), escrow.getPrice());
                    Text itemName = escrow.getItem().getDisplayNameText();

                    AuctionCategories category = AuctionCategories.TOOLS;
                    if (escrow.getItem().hasComponent(AuctionCategoryComponent.class))
                        category = escrow.getItem().getComponent(AuctionCategoryComponent.class).getCategory();

                    player.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).clearEscrow();
                    player.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ACTIVE_OWNED, DatapointUUIDList.class).getValue().add(item.getUuid());

                    player.sendMessage(Text.key("gui_auction.create.setup_message"));

                    AuctionAddItemProtocol.AuctionAddItemMessage message =
                            new AuctionAddItemProtocol.AuctionAddItemMessage(item, category);
                    CompletableFuture<AuctionAddItemProtocol.AuctionAddItemResponse> future =
                            auctionService.handleRequest(message);
                    future.thenAccept(addResponse -> {
                        UUID auctionUUID = addResponse.uuid();
                        player.sendMessage(Text.key("gui_auction.create.started_message", itemName));
                        player.sendMessage(Text.key("gui_auction.create.started_id", auctionUUID.toString()));
                    });
                });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                if (escrow.getItem() == null) {
                    return ItemStacks.item(Material.RED_TERRACOTTA, 1,
                            Text.key("gui_auction.create.submit_no_item"),
                            Text.keyLines("gui_auction.create.submit_no_item.lore"));
                } else {
                    return ItemStacks.item(Material.GREEN_TERRACOTTA, 1,
                        Text.key("gui_auction.create.submit_ready", escrow.isBin() ? "Bin " : ""),
                        Text.keyLines("gui_auction.create.submit_ready.lore",
                            escrow.getItem().getDisplayNameText(),
                            StringUtility.getAuctionSetupFormattedTime(escrow.getDuration()),
                            escrow.isBin() ? Text.key("gui_auction.create.price_bin_label") : Text.key("gui_auction.create.price_normal_label"),
                            StringUtility.commaify(escrow.getPrice()),
                            (escrow.getPrice() * 0.05) + (escrow.getDuration() / 180000)));
                }
            }
        });
        set(new GUIQueryItem(31) {

            @Override
            public HypixelInventoryGUI onQueryFinish(String query, HypixelPlayer player) {
                long val;
                try {
                    val = Long.parseLong(query);
                } catch (NumberFormatException ex) {
                    player.sendMessage(Text.key("gui_auction.create.number_parse_error"));
                    return GUIAuctionCreateItem.this;
                }
                if (val <= 50) {
                    player.sendMessage(Text.key("gui_auction.create.price_too_low"));
                    return GUIAuctionCreateItem.this;
                }
                escrow.setPrice(val);

                return GUIAuctionCreateItem.this;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                Material material;
                List<Text> lore = new ArrayList<>();
                if (escrow.isBin()) {
                    material = Material.GOLD_INGOT;
                    lore.addAll(Text.keyLines("gui_auction.create.price_bin.lore"));
                } else {
                    material = Material.POWERED_RAIL;
                    lore.addAll(Text.keyLines("gui_auction.create.price_normal.lore"));
                }
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.price_extra_fee", escrow.getPrice() * 0.05));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.price_click"));

                String priceKey = escrow.isBin() ? "gui_auction.create.price_label_bin" : "gui_auction.create.price_label_normal";
                return ItemStacks.item(material, 1,
                    Text.key(priceKey, StringUtility.commaify(escrow.getPrice())), lore);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
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
        ItemStack current = e.getClickedItem();
        SkyBlockItem item = new SkyBlockItem(current);

        if (item.isNA()) return;
        if (item.isAir()) return;

        DatapointAuctionEscrow.AuctionEscrow escrow = ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).getValue();

        if (escrow.getItem() != null) {
            ((HypixelPlayer) e.getPlayer()).sendMessage(Text.key("gui_auction.create.already_have_item"));
            return;
        }

        e.setCancelled(true);
        escrow.setItem(item);
        e.getPlayer().getInventory().setItemStack(e.getSlot(), ItemStack.AIR);
        new GUIAuctionCreateItem(previousGUI).open(getPlayer());
    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        new ProxyService(ServiceType.AUCTION_HOUSE).isOnline().thenAccept(online -> {
            if (!online) {
                player.sendMessage(Text.key("gui_auction.create.offline_message"));
                player.closeInventory();
            }
        });
    }

    @Override
    public int refreshRate() {
        return 10;
    }
}
