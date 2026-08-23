package net.swofty.type.skyblockgeneric.gui.inventories.auction;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionEscrow;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GUIAuctionDuration extends HypixelInventoryGUI {
    public GUIAuctionDuration() {
        super(Text.key("gui_auction.duration.title"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        set(GUIClickableItem.getGoBackItem(31, new GUIAuctionCreateItem(new GUIAuctionHouse())));
        set(createTime(Material.RED_TERRACOTTA, 1, 10, (SkyBlockPlayer) getPlayer()));
        set(createTime(Material.PINK_TERRACOTTA, 6, 11, (SkyBlockPlayer) getPlayer()));
        set(createTime(Material.ORANGE_TERRACOTTA, 12, 12, (SkyBlockPlayer) getPlayer()));
        set(createTime(Material.YELLOW_TERRACOTTA, 24, 13, (SkyBlockPlayer) getPlayer()));
        set(createTime(Material.WHITE_TERRACOTTA, 48, 14, (SkyBlockPlayer) getPlayer()));

        AtomicBoolean right = new AtomicBoolean();
        set(new GUIQueryItem(16) {
            @Override
            public HypixelInventoryGUI onQueryFinish(String query, HypixelPlayer player) {
                long val;
                try {
                    val = Long.parseLong(query);
                } catch (NumberFormatException ex) {
                    player.sendMessage(Text.key("gui_auction.duration.number_parse_error"));
                    return null;
                }
                if (val <= 1) {
                    player.sendMessage(Text.key("gui_auction.duration.invalid_time"));
                    return null;
                }
                if (val >= 336 && !right.get()) {
                    player.sendMessage(Text.key("gui_auction.duration.max_exceeded"));
                    return null;
                }

                ((SkyBlockPlayer) player).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).getValue().setDuration(
                        val * (right.get() ? 60000 : 3600000)
                );

                return new GUIAuctionCreateItem(GUIAuctionDuration.this);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                right.set(e.getClick() instanceof Click.Right);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.COMPASS, 1,
                        Text.key("gui_auction.duration.custom"),
                        Text.keyLines("gui_auction.duration.custom.lore"));
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
        e.setCancelled(true);
    }

    private static GUIClickableItem createTime(Material color, int hours, int slot, SkyBlockPlayer user) {
        long millis = hours * 3600000L;
        return new GUIClickableItem(slot) {

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                ItemStack.Builder stack = ItemStacks.item(color, 1,
                        Text.key("gui_auction.duration.hours", hours),
                        List.of(
                                Text.literal(" "),
                                Text.key("gui_auction.duration.hours_click")));

                if (user.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).getValue().getDuration() == millis) {
                    stack = ItemStacks.enchanted(stack);
                }

                return stack;
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                user.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).getValue().setDuration(millis);
                new GUIAuctionDuration().open(player);
            }
        };
    }
}
