package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarItemSet;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.EnchantedComponent;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GUIBazaarItemSet extends HypixelInventoryGUI implements RefreshingGUI {
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

    private final BazaarItemSet itemSet;

    public GUIBazaarItemSet(BazaarCategories category, BazaarItemSet itemSet) {
        super(Text.key("gui_bazaar.item_set.title", StringUtility.toNormalCase(category.name()), itemSet.displayName), InventoryType.CHEST_4_ROW);

        this.itemSet = itemSet;

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getCloseItem(31));
        set(GUIClickableItem.getGoBackItem(30, new GUIBazaar(category)));
        set(new GUIClickableItem(32) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIBazaarOrders().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.BOOK, """
                        <key:gui_bazaar.item_set.manage_orders_button>
                        <key:gui_bazaar.item_set.manage_orders_button.lore.1>
                        <key:gui_bazaar.item_set.manage_orders_button.lore.2>
                        <key:gui_bazaar.item_set.manage_orders_button.lore.3>""");
            }
        });
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        int i = 0;
        for (ItemType itemType : itemSet.items) {
            int slot = SLOTS.get(itemSet.items.size())[i];

            CompletableFuture<Void> future = ((SkyBlockPlayer) e.player()).getBazaarConnector().getItemStatistics(itemType)
                    .thenAccept(stats -> {
                        set(new GUIClickableItem(slot) {
                            @Override
                            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                                SkyBlockPlayer player = (SkyBlockPlayer) p;
                                new GUIBazaarItem(itemType).open(player);
                            }

                            @Override
                            public ItemStack.Builder getItem(HypixelPlayer p) {
                                SkyBlockPlayer player = (SkyBlockPlayer) p;
                                List<Text> lore = new ArrayList<>();
                                lore.add(Text.key("gui_bazaar.item_set.commodity_label", StringUtility.toNormalCase(itemType.rarity.name())));
                                lore.add(Text.literal(" "));

                                if (stats.bestAsk() > 0) {
                                    lore.add(Text.key("gui_bazaar.item_set.buy_price", new DecimalFormat("#,###").format(stats.bestAsk())));
                                    lore.add(Text.key("gui_bazaar.item_set.buy_price_best", StringUtility.shortenNumber(stats.bestAsk())));
                                } else {
                                    lore.add(Text.key("gui_bazaar.item_set.buy_price_none"));
                                    lore.add(Text.key("gui_bazaar.item_set.buy_price_none_desc"));
                                }

                                lore.add(Text.literal(" "));

                                if (stats.bestBid() > 0) {
                                    lore.add(Text.key("gui_bazaar.item_set.sell_price", new DecimalFormat("#,###").format(stats.bestBid())));
                                    lore.add(Text.key("gui_bazaar.item_set.sell_price_best", StringUtility.shortenNumber(stats.bestBid())));
                                } else {
                                    lore.add(Text.key("gui_bazaar.item_set.sell_price_none"));
                                    lore.add(Text.key("gui_bazaar.item_set.sell_price_none_desc"));
                                }

                                lore.add(Text.literal(" "));
                                lore.add(Text.key("gui_bazaar.item_set.click_to_view"));

                                SkyBlockItem item = new SkyBlockItem(itemType);

                                return ItemStacks.lore(getFromSkyBlockItem(item), lore);
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        // Handle errors gracefully
                        set(new GUIClickableItem(slot) {
                            @Override
                            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                                SkyBlockPlayer player = (SkyBlockPlayer) p;
                                new GUIBazaarItem(itemType).open(player);
                            }

                            @Override
                            public ItemStack.Builder getItem(HypixelPlayer p) {
                                SkyBlockPlayer player = (SkyBlockPlayer) p;
                                List<Text> lore = new ArrayList<>();
                                lore.add(Text.key("gui_bazaar.item_set.commodity_label", StringUtility.toNormalCase(itemType.rarity.name())));
                                lore.add(Text.literal(" "));
                                lore.add(Text.key("gui_bazaar.item_set.error_loading"));
                                lore.add(Text.key("gui_bazaar.item_set.error_try_again"));
                                lore.add(Text.literal(" "));
                                lore.add(Text.key("gui_bazaar.item_set.click_to_view"));

                                return ItemStacks.item(itemType.material, 1,
                                        Text.of("<color:{}>{}", itemType.rarity.getColor(), itemType.getDisplayName()),
                                        lore);
                            }
                        });
                        return null;
                    });

            futures.add(future);
            i++;
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> updateItemStacks(getInventory(), getPlayer()));
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
        SkyBlockItem clickedItem = new SkyBlockItem(e.getClickedItem());
        ItemType type = clickedItem.getAttributeHandler().getPotentialType();
        e.setCancelled(true);

        if (clickedItem.isNA()) {
            return;
        }

        if (type == null) {
            return;
        }

        Map.Entry<BazaarCategories, BazaarItemSet> entry = BazaarCategories.getFromItem(type);

        if (entry == null) {
            return;
        }

        Thread.startVirtualThread(() -> {
            new GUIBazaarItemSet(entry.getKey(), entry.getValue()).open((SkyBlockPlayer) e.getPlayer());
        });
    }

    @Override
    public void refreshItems(HypixelPlayer p) {
        if (!(p instanceof SkyBlockPlayer player)) {
            return;
        }
        player.getBazaarConnector().isOnline().thenAccept(online -> {
            if (!online) {
                player.sendMessage(Text.key("gui_bazaar.item_set.offline_message"));
                player.closeInventory();
            }
        });
    }

    @Override
    public int refreshRate() {
        return 10;
    }

    private ItemStack.Builder getFromSkyBlockItem(SkyBlockItem item) {
        ItemStack.Builder builder;
        Text name = Text.literal(item.getDisplayName());

        if (item.hasComponent(SkullHeadComponent.class)) {
            builder = ItemStacks.head(item.getComponent(SkullHeadComponent.class).getSkullTexture(item),
                    item.getAmount(), name, List.of());
        } else {
            builder = ItemStacks.item(item.getMaterial(), item.getAmount(), name, List.of());
        }

        if (item.hasComponent(EnchantedComponent.class)) return ItemStacks.enchanted(builder);
        else return builder;
    }
}
