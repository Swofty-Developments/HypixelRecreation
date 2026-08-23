package net.swofty.type.skyblockgeneric.gui.inventories.auction;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionStatistics;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIAuctionHouseStats extends HypixelInventoryGUI {
    public GUIAuctionHouseStats() {
        super(Text.key("gui_auction.stats.title"), InventoryType.CHEST_4_ROW);

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(31, new GUIAuctionHouse()));

        set(new GUIItem(11) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                DatapointAuctionStatistics.AuctionStatistics stats = player.getSkyblockDataHandler().get(
                        SkyBlockDataHandler.Data.AUCTION_STATISTICS,
                        DatapointAuctionStatistics.class
                ).getValue();

                return ItemStacks.item(Material.PAPER, 1,
                    Text.key("gui_auction.stats.seller_stats"),
                    Text.keyLines("gui_auction.stats.seller_stats.lore",
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.AUCTIONS_CREATED),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.AUCTIONS_COMPLETED_WITHOUT_BIDS),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.AUCTIONS_COMPLETED_WITH_BIDS),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.HIGHEST_AUCTION_HELD),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.TOTAL_COINS_EARNED),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.COINS_SPENT_ON_FEES),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.COMMON_SOLD),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.UNCOMMON_SOLD),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.RARE_SOLD),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.EPIC_SOLD),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.LEGENDARY_SOLD),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.MYTHIC_SOLD),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.SPECIAL_SOLD),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.ULTIMATE_SOLD)));
            }
        });
        set(new GUIItem(15) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                DatapointAuctionStatistics.AuctionStatistics stats = player.getSkyblockDataHandler().get(
                        SkyBlockDataHandler.Data.AUCTION_STATISTICS,
                        DatapointAuctionStatistics.class
                ).getValue();

                return ItemStacks.item(Material.FILLED_MAP, 1,
                    Text.key("gui_auction.stats.buyer_stats"),
                    Text.keyLines("gui_auction.stats.buyer_stats.lore",
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.AUCTIONS_WON),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.TOTAL_BIDS),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.HIGHEST_BID_MADE),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.TOTAL_COINS_SPENT),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.COMMON_BOUGHT),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.UNCOMMON_BOUGHT),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.RARE_BOUGHT),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.EPIC_BOUGHT),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.LEGENDARY_BOUGHT),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.MYTHIC_BOUGHT),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.SPECIAL_BOUGHT),
                        stats.get(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.ULTIMATE_BOUGHT)));
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
        e.setCancelled(true);
    }
}
