package net.swofty.type.skywarslobby.gui;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarslobby.kit.SkywarsKit;
import net.swofty.type.skywarslobby.kit.SkywarsKitRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Kit selector GUI for a specific game mode.
 * Matches the Hypixel SkyWars Normal/Insane Kits layout.
 */
public class GUIKitSelector extends HypixelInventoryGUI {
    private static final int[] KIT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int KITS_PER_PAGE = KIT_SLOTS.length;

    private final String mode;
    private final int page;
    private final boolean lowestFirst;
    private final boolean ownedFirst;

    public GUIKitSelector(String mode) {
        this(mode, 1, true, false);
    }

    public GUIKitSelector(String mode, int page) {
        this(mode, page, true, false);
    }

    public GUIKitSelector(String mode, int page, boolean lowestFirst, boolean ownedFirst) {
        super(getTitle(mode, page), InventoryType.CHEST_6_ROW);
        this.mode = mode;
        this.page = page;
        this.lowestFirst = lowestFirst;
        this.ownedFirst = ownedFirst;
    }

    private static String getTitle(String mode, int page) {
        String modeName = mode.equals("NORMAL") ? "Normal" : "Insane";
        return modeName + " Kits";
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        if (handler == null) return;

        DatapointSkywarsUnlocks.SkywarsUnlocks unlocks = handler.get(
                SkywarsDataHandler.Data.UNLOCKS,
                DatapointSkywarsUnlocks.class
        ).getValue();

        long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();

        // Get kits sorted by rarity with owned first option
        List<SkywarsKit> allKits = SkywarsKitRegistry.getKitsSortedByRarity(
                mode, lowestFirst, ownedFirst, unlocks.getUnlockedKits()
        );
        int totalPages = (int) Math.ceil((double) allKits.size() / KITS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        // Calculate which kits to show on this page
        int startIndex = (page - 1) * KITS_PER_PAGE;
        int endIndex = Math.min(startIndex + KITS_PER_PAGE, allKits.size());

        // Fill kit slots
        for (int i = 0; i < KIT_SLOTS.length; i++) {
            int kitIndex = startIndex + i;
            int slot = KIT_SLOTS[i];

            if (kitIndex < endIndex) {
                SkywarsKit kit = allKits.get(kitIndex);
                set(createKitItem(kit, slot, player, unlocks, coins));
            }
        }

        // Go Back (slot 48)
        set(new GUIClickableItem(48) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, 1, """
                        <a>Go Back
                        <7>To Kits & Perks""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIKitsPerks().open(player);
            }
        });

        // Total Coins (slot 49)
        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.EMERALD, 1, """
                        <7>Total Coins: <6>{:,}
                        <6>https://store.hypixel.net""", coins);
            }
        });

        // Sort options (slot 50)
        set(new GUIClickableItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                String currentSort = lowestFirst ? "Lowest rarity first" : "Highest rarity first";
                String nextSort = lowestFirst ? "Highest rarity first" : "Lowest rarity first";
                Text ownedFirstStatus = Text.of(ownedFirst ? "<a>Yes" : "<c>No");

                return ItemStacks.item(Material.HOPPER, 1, """
                        <6>Sorted by: <a>{}
                        <7>Sorts by rarity: {}

                        <7>Next sort: <a>{}
                        <e>Left click to use!

                        <7>Owned items first: {}
                        <e>Right click to toggle!""",
                        currentSort, currentSort, nextSort, ownedFirstStatus);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                boolean isRightClick = e.getClick() instanceof Click.Right;
                if (isRightClick) {
                    // Toggle owned first
                    new GUIKitSelector(mode, 1, lowestFirst, !ownedFirst).open(player);
                } else {
                    // Toggle rarity sort order
                    new GUIKitSelector(mode, 1, !lowestFirst, ownedFirst).open(player);
                }
            }
        });

        // Previous page (slot 45)
        if (page > 1) {
            int prevPage = page - 1;
            set(new GUIClickableItem(45) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.ARROW, 1, """
                            <a>Previous Page
                            <e>Page {}""", prevPage);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    new GUIKitSelector(mode, prevPage, lowestFirst, ownedFirst).open(player);
                }
            });
        }

        // Next page (slot 53)
        int finalTotalPages = totalPages;
        if (page < totalPages) {
            int nextPage = page + 1;
            set(new GUIClickableItem(53) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.ARROW, 1, """
                            <a>Next Page
                            <e>Page {}""", nextPage);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    new GUIKitSelector(mode, nextPage, lowestFirst, ownedFirst).open(player);
                }
            });
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    private GUIClickableItem createKitItem(SkywarsKit kit, int slot, HypixelPlayer player,
                                           DatapointSkywarsUnlocks.SkywarsUnlocks unlocks, long coins) {
        return new GUIClickableItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                boolean owned = unlocks.hasKit(kit.getId());
                boolean selected = unlocks.getSelectedKitForMode(mode).equals(kit.getId());

                // Items preview
                List<Text> lore = new ArrayList<>();
                lore.addAll(kit.getItemsLore(mode));
                lore.add(Text.empty());

                // Rarity
                lore.add(Text.of("<7>Rarity: {}", kit.getRarity().getFormattedName()));

                // Status and action
                if (selected) {
                    lore.add(Text.of("<a><l>SELECTED"));
                    lore.add(Text.empty());
                    lore.add(Text.of("<e>Right-click to open breakdown!"));
                } else if (owned) {
                    lore.add(Text.of("<a>UNLOCKED"));
                    lore.add(Text.empty());
                    lore.add(Text.of("<e>Left-click to select!"));
                    lore.add(Text.of("<e>Right-click to open breakdown!"));
                } else {
                    // Show cost
                    if (kit.costsCoin()) {
                        lore.add(Text.of("<7>Cost: <6>{:,}", kit.getCost()));
                    } else if (kit.costsOpal()) {
                        lore.add(Text.of("<7>Cost: <9>{} Opal{}",
                                kit.getOpalCost(), kit.getOpalCost() > 1 ? "s" : ""));
                    } else if (kit.getUnlockMethod() != null) {
                        lore.add(Text.of("<7>{}", kit.getFormattedCost()));
                    }

                    if (kit.isSoulWellDrop()) {
                        lore.add(Text.of("<b>Also found in the Soul Well!"));
                    }
                    lore.add(Text.empty());

                    if (kit.costsCoin()) {
                        if (coins >= kit.getCost()) {
                            lore.add(Text.of("<e>Left-click to purchase!"));
                        } else {
                            lore.add(Text.of("<c>Not enough coins!"));
                        }
                    } else if (kit.costsOpal()) {
                        lore.add(Text.of("<9>Purchase with Opals in Angel's Descent"));
                    }
                    lore.add(Text.of("<e>Right-click to open breakdown!"));
                }

                TextColor nameColor = owned ? NamedTextColor.GREEN : NamedTextColor.RED;
                Text name = Text.of("<color:{}>{}", nameColor, kit.getName());

                if (kit.hasCustomTexture()) {
                    return ItemStacks.head(kit.getIconTexture(), name, lore);
                } else {
                    return ItemStacks.item(kit.getIconMaterial(), 1, name, lore);
                }
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
                if (handler == null) return;

                DatapointSkywarsUnlocks.SkywarsUnlocks currentUnlocks = handler.get(
                        SkywarsDataHandler.Data.UNLOCKS,
                        DatapointSkywarsUnlocks.class
                ).getValue();

                boolean owned = currentUnlocks.hasKit(kit.getId());
                boolean isRightClick = e.getClick() instanceof Click.Right;

                if (isRightClick) {
                    // Open kit breakdown
                    new GUIKitBreakdown(kit, mode).open(player);
                    return;
                }

                if (owned) {
                    // Select the kit
                    currentUnlocks.selectKitForMode(mode, kit.getId());
                    player.sendMessage("<a>You selected the <e>{} <a>kit for {} mode!", kit.getName(), mode);
                    // Refresh GUI with preserved sorting
                    new GUIKitSelector(mode, page, lowestFirst, ownedFirst).open(player);
                } else if (kit.costsCoin()) {
                    long currentCoins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();
                    if (currentCoins >= kit.getCost()) {
                        // Open confirmation dialog
                        new GUIKitPurchaseConfirm(kit, mode, page).open(player);
                    } else {
                        player.sendMessage("<c>You don't have enough coins to purchase this kit!");
                    }
                } else {
                    player.sendMessage("<c>This kit cannot be purchased with coins.");
                }
            }
        };
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
