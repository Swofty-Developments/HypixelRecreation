package net.swofty.type.skyblockgeneric.gui.inventories.rusty;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.gui.inventory.HypixelPaginatedGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import net.swofty.type.skyblockgeneric.gui.inventories.shop.ConfirmBuyView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GUIRustySubMenu<T extends GUIRustySubMenu.ShopEntry>
        extends HypixelPaginatedGUI<GUIRustySubMenu.DisplayEntry<T>> {

    private final String titleKey;
    private final Supplier<List<T>> entriesSupplier;

    public GUIRustySubMenu(
            String titleKey,
            Supplier<List<T>> entriesSupplier
    ) {
        super(InventoryType.CHEST_6_ROW);
        this.titleKey = titleKey;
        this.entriesSupplier = entriesSupplier;
    }

    @Override
    protected Text getTitleText(HypixelPlayer player, String query, int page, PaginationList<DisplayEntry<T>> paged) {
        return Text.key(titleKey);
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
    public int[] getPaginatedSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    @Override
    public PaginationList<DisplayEntry<T>> fillPaged(
            HypixelPlayer player,
            PaginationList<DisplayEntry<T>> paged
    ) {
        SkyBlockPlayer skyblockPlayer = (SkyBlockPlayer) player;
        List<DisplayEntry<T>> items = new ArrayList<>();

        for (T entry : entriesSupplier.get()) {
            items.add(new DisplayEntry<>(entry, entry.hasUnlocked().apply(skyblockPlayer)));
        }

        if (player.getToggles().get(DatapointToggles.Toggles.ToggleType.RUSTY_SORT_BY_RARITY)) {
            items.sort((a, b) -> {
                if (a.unlocked && !b.unlocked) return -1;
                if (!a.unlocked && b.unlocked) return 1;
                return Integer.compare(
                        a.entry.item().getAttributeHandler().getRarity().ordinal(),
                        b.entry.item().getAttributeHandler().getRarity().ordinal()
                );
            });
        }

        paged.addAll(items);
        return paged;
    }

    @Override
    public boolean shouldFilterFromSearch(String query, DisplayEntry<T> item) {
        return false;
    }

    @Override public void performSearch(HypixelPlayer player, String query, int page, int maxPage) {
        border(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(49, new GUIRusty()));

        set(new GUIClickableItem(48) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                boolean purchaseConfirmation = player.getToggles().get(DatapointToggles.Toggles.ToggleType.RUSTY_PURCHASE_CONFIRMATION);
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.RUSTY_PURCHASE_CONFIRMATION, !purchaseConfirmation);

                GUIRustyMiscellaneous newGui = new GUIRustyMiscellaneous();
                newGui.open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                boolean enabled = player.getToggles().get(DatapointToggles.Toggles.ToggleType.RUSTY_PURCHASE_CONFIRMATION);
                return ItemStacks.item(enabled ? Material.LIME_DYE : Material.LIGHT_GRAY_DYE, 1,
                        Text.key("gui_rusty.submenu.shop_confirmations"),
                        Text.keyLines("gui_rusty.submenu.shop_confirmations.lore", enabled ? "disable" : "enable"));
            }
        });

        set(new GUIItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.REDSTONE_TORCH, 1, Text.key("gui_rusty.submenu.janitor_info"),
                        Text.keyLines("gui_rusty.submenu.janitor_info.lore"));
            }
        });

        set(new GUIClickableItem(51) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                boolean sortByRarity = player.getToggles().get(DatapointToggles.Toggles.ToggleType.RUSTY_SORT_BY_RARITY);
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.RUSTY_SORT_BY_RARITY, !sortByRarity);

                GUIRustyMiscellaneous newGui = new GUIRustyMiscellaneous();
                newGui.open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                boolean sortByRarity = player.getToggles().get(DatapointToggles.Toggles.ToggleType.RUSTY_SORT_BY_RARITY);
                return ItemStacks.item(Material.ENDER_EYE, 1, Text.key("gui_rusty.submenu.sort_by_rarity"),
                        Text.keyLines("gui_rusty.submenu.sort_by_rarity.lore",
                                sortByRarity ? Text.of("<a>YES") : Text.of("<c>NO")));
            }
        });

        if (page > 1) {
            set(createNavigationButton(this, 45, query, page, false));
        }
        if (page < maxPage) {
            set(createNavigationButton(this, 53, query, page, true));
        }
    }

    @Override
    public GUIClickableItem createItemFor(DisplayEntry<T> display, int slot, HypixelPlayer player) {
        T entry = display.entry;
        boolean unlocked = display.unlocked;

        if (!unlocked) {
            return new GUIClickableItem(slot) {
                @Override public void run(InventoryPreClickEvent e, HypixelPlayer player) {}
                @Override public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.head("5359d91277242fc01c309accb87b533f1929be176ecba2cde63bf635e05e699b",
                            Text.key("gui_rusty.submenu.unknown_item"), List.of());
                }
            };
        }

        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                SkyBlockPlayer skyblockPlayer = (SkyBlockPlayer) player;
                SkyBlockItem item = entry.item();
                int price = entry.price();

                if (player.getToggles()
                        .get(DatapointToggles.Toggles.ToggleType.RUSTY_PURCHASE_CONFIRMATION)
                        && price >= 1_000_000) {
                    player.openView(new ConfirmBuyView(), new ConfirmBuyView.State(item, price));
                    return;
                }

                if (skyblockPlayer.getCoins() >= price) {
                    skyblockPlayer.addAndUpdateItem(item);
                    skyblockPlayer.removeCoins(price);
                    skyblockPlayer.sendMessage(Text.key("gui_rusty.submenu.bought_message",
                            item.getDisplayName(), price));
                } else {
                    skyblockPlayer.sendMessage(Text.key("gui_rusty.submenu.not_enough_coins"));
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                ItemStack.Builder stack =
                        new NonPlayerItemUpdater(entry.item()).getUpdatedItem();

                return ItemStacks.appendLore(stack, List.of(
                        Text.empty(),
                        Text.key("gui_rusty.submenu.cost_label"),
                        Text.of("<6>{:,} Coins", entry.price()),
                        Text.empty(),
                        Text.key("gui_rusty.submenu.click_to_trade")));
            }
        };
    }

    public record DisplayEntry<T>(T entry, boolean unlocked) {}

    public interface ShopEntry {
        SkyBlockItem item();
        int price();
        Function<SkyBlockPlayer, Boolean> hasUnlocked();
    }
}
