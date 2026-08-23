package net.swofty.type.skyblockgeneric.gui.inventories.museum;

import lombok.SneakyThrows;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.TrackedItem;
import net.swofty.commons.protocol.objects.itemtracker.TrackedItemRetrieveProtocol;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.museum.MuseumDisplays;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.ItemPriceCalculator;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIMuseumNonEmptyDisplay extends HypixelInventoryGUI {
    private static final Map<Integer, int[]> SLOTS = Map.of(
            1, new int[]{13},
            2, new int[]{12, 14},
            3, new int[]{11, 13, 15},
            4, new int[]{10, 12, 14, 16},
            5, new int[]{11, 12, 13, 14, 15},
            6, new int[]{11, 12, 13, 14, 15, 22},
            7, new int[]{10, 11, 12, 13, 14, 15, 16},
            8, new int[]{11, 12, 13, 14, 15, 21, 22, 23},
            9, new int[]{10, 11, 12, 13, 14, 15, 16, 21, 23},
            10, new int[]{11, 12, 13, 14, 15, 20, 21, 22, 23, 24}
    );

    private final MuseumDisplays display;
    private final int position;
    private List<SkyBlockItem> items;

    public GUIMuseumNonEmptyDisplay(MuseumDisplays display, int position) {
        super(Text.key("gui_museum.display.title"), InventoryType.CHEST_4_ROW);

        this.display = display;
        this.position = position;

        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        set(GUIClickableItem.getCloseItem(31));
    }

    @SneakyThrows
    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        DatapointMuseum.MuseumData data = player.getMuseumData();

        // Get all items at this display position
        items = data.getDisplayHandler().getItemsAtSlot(display, position);

        if (items.isEmpty()) {
            player.sendMessage(Text.key("gui_museum.display.no_items"));
            player.closeInventory();
            return;
        }

        // Update title based on items
        if (items.size() == 1) {
            setTitle(Text.literal(items.getFirst().getAttributeHandler().getPotentialType().getDisplayName()));
        } else {
            setTitle(Text.key("gui_museum.display.title_with_count", items.size()));
        }

        if (!new ProxyService(ServiceType.ITEM_TRACKER).isOnline().join()) {
            player.sendMessage(Text.key("gui_museum.display.item_tracker_offline"));
            player.closeInventory();
            return;
        }

        set(new GUIClickableItem(35) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                DatapointMuseum.MuseumData data = player.getMuseumData();
                player.closeInventory();
                data.getDisplayHandler().removeAllFromSlot(display, position);
                MuseumDisplays.updateDisplay(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BEDROCK, 1, Text.key("gui_museum.display.remove_button"),
                        items.size() == 1
                                ? Text.keyLines("gui_museum.display.remove_single.lore")
                                : Text.keyLines("gui_museum.display.remove_multiple.lore", items.size()));
            }
        });

        // Display items using the slot mapping
        int itemCount = Math.min(items.size(), 10); // Cap at 10 items max
        int[] slotsToUse = SLOTS.get(itemCount);

        for (int i = 0; i < itemCount; i++) {
            final SkyBlockItem item = items.get(i);
            final int slotIndex = slotsToUse[i];

            set(new GUIItem(slotIndex) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return createItemDisplay(item, player);
                }
            });
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    private ItemStack.Builder createItemDisplay(SkyBlockItem item, SkyBlockPlayer player) {
        DatapointMuseum.MuseumData data = player.getMuseumData();

        // Get tracked item info
        TrackedItem trackedItem = null;
        try {
            TrackedItemRetrieveProtocol.TrackedItemRetrieveMessage message =
                    new TrackedItemRetrieveProtocol.TrackedItemRetrieveMessage(
                            UUID.fromString(item.getAttributeHandler().getUniqueTrackedID())
                    );
            ProxyService proxyService = new ProxyService(ServiceType.ITEM_TRACKER);
            TrackedItemRetrieveProtocol.TrackedItemResponse response =
                    (TrackedItemRetrieveProtocol.TrackedItemResponse) proxyService.handleRequest(message).join();
            trackedItem = response.trackedItem();
        } catch (Exception e) {
            Logger.error(e, "Error displaying museum items");
        }

        ItemStack.Builder stack = new NonPlayerItemUpdater(item).getUpdatedItem();
        List<Text> lore = new ArrayList<>();
        UUID trackedItemUUID = UUID.fromString(item.getAttributeHandler().getUniqueTrackedID());

        lore.add(Text.of("<8><m>---------------------"));
        lore.add(Text.key("gui_museum.display.item_donated_label"));
        lore.add(Text.of("<b>{}", StringUtility.formatAsDate(data.getInsertionTimes().get(trackedItemUUID))));
        lore.add(Text.literal(" "));

        if (trackedItem != null) {
            lore.add(Text.key("gui_museum.display.item_created_label"));
            lore.add(Text.of("<a>{}", StringUtility.formatAsDate(trackedItem.getCreated())));
            lore.add(Text.of("<6>  {} <7>created", StringUtility.commaifyAndTh(trackedItem.getNumberMade())));
            lore.add(Text.literal(" "));
        }

        lore.add(Text.key("gui_museum.display.item_clean_value_label"));
        lore.add(Text.of("<6>{:,} Coins", new ItemPriceCalculator(item).calculateCleanPrice()));
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_museum.display.item_value_label"));
        if (data.getCalculatedPrices().containsKey(trackedItemUUID)) {
            lore.add(Text.of("<6>{:,} Coins", data.getCalculatedPrices().get(trackedItemUUID)));
        } else {
            lore.add(Text.key("gui_museum.category.uncalculated"));
        }

        return ItemStacks.appendLore(stack, lore);
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
