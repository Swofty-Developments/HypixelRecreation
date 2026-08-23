package net.swofty.type.skywarslobby.gui;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.HypixelPaginatedGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import net.swofty.type.skywarslobby.perk.SkywarsPerk;
import net.swofty.type.skywarslobby.perk.SkywarsPerkRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for toggling Insane mode perks on/off.
 * Shows all perks (owned + unowned) with toggle/purchase options.
 */
public class GUIToggleInsanePerks extends HypixelPaginatedGUI<SkywarsPerk> {
    private static final int[] PAGINATED_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private static final String MODE = "INSANE";

    public GUIToggleInsanePerks() {
        super(InventoryType.CHEST_6_ROW);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return PAGINATED_SLOTS;
    }

    @Override
    protected PaginationList<SkywarsPerk> fillPaged(HypixelPlayer player, PaginationList<SkywarsPerk> paged) {
        // Show all selectable perks (owned + unowned)
        List<SkywarsPerk> perks = SkywarsPerkRegistry.getSelectablePerksSortedByRarity(MODE, true);
        paged.addAll(perks);
        return paged;
    }

    @Override
    protected boolean shouldFilterFromSearch(String query, SkywarsPerk item) {
        return !item.getName().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    protected void performSearch(HypixelPlayer player, String query, int page, int maxPage) {
        // Navigation
        if (page > 1) {
            set(createNavigationButton(this, 45, query, page, false));
        }
        if (page < maxPage) {
            set(new GUIClickableItem(53) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.ARROW, 1, """
                            <e>Left-click for next page!
                            <b>Right-click for last page!""");
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    new GUIToggleInsanePerks().open(player, query, page + 1);
                }
            });
        }

        // Go Back (slot 49)
        set(GUIClickableItem.getGoBackItem(49, new GUIKitsPerks()));
    }

    @Override
    protected String getTitle(HypixelPlayer player, String query, int page, PaginationList<SkywarsPerk> paged) {
        return "Toggle Insane Perks";
    }

    @Override
    protected GUIClickableItem createItemFor(SkywarsPerk perk, int slot, HypixelPlayer player) {
        return new GUIClickableItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
                if (handler == null) return ItemStack.builder(Material.BARRIER);

                DatapointSkywarsUnlocks.SkywarsUnlocks unlocks = handler.get(
                        SkywarsDataHandler.Data.UNLOCKS,
                        DatapointSkywarsUnlocks.class
                ).getValue();

                boolean owned = unlocks.hasPerk(perk.getId());
                boolean enabled = unlocks.isInsanePerkEnabled(MODE, perk.getId());
                long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<8>Perk"));
                lore.add(Text.empty());
                lore.add(Text.of("<7>{}", perk.getEffectDescription()));
                lore.add(Text.empty());
                lore.add(Text.of("<7>Rarity: {}", perk.getRarity().getFormattedName()));
                lore.add(Text.empty());

                String specialStatus = perk.getSpecialStatus();
                if (specialStatus != null) {
                    lore.add(Text.of("<c><l>!! {}", specialStatus));
                } else if (owned) {
                    if (enabled) {
                        lore.add(Text.of("<a><l>ENABLED"));
                    } else {
                        lore.add(Text.of("<c><l>DISABLED"));
                    }
                    lore.add(Text.empty());
                    lore.add(Text.of("<e>Click to toggle!"));
                } else if (perk.isPurchasableWithCoins()) {
                    lore.add(Text.of("<7>Cost: <6>{:,}", perk.getCost()));
                    if (perk.isSoulWellDrop()) {
                        lore.add(Text.of("<b>Also found in the Soul Well!"));
                    }
                    lore.add(Text.empty());
                    if (coins >= perk.getCost()) {
                        lore.add(Text.of("<e>Click to purchase!"));
                    } else {
                        lore.add(Text.of("<c>Not enough coins!"));
                    }
                } else if (perk.costsOpal()) {
                    lore.add(Text.of("<7>Cost: <9>{} Opal{}", perk.getOpalCost(), perk.getOpalCost() > 1 ? "s" : ""));
                    if (perk.isSoulWellDrop()) {
                        lore.add(Text.of("<b>Also found in the Soul Well!"));
                    }
                    lore.add(Text.empty());
                    lore.add(Text.of("<9>Purchase with Opals in Angel's Descent"));
                } else if (perk.isFree()) {
                    lore.add(Text.of("<a>FREE"));
                    if (perk.isSoulWellDrop()) {
                        lore.add(Text.of("<b>Also found in the Soul Well!"));
                    }
                    lore.add(Text.empty());
                    lore.add(Text.of("<e>Click to unlock!"));
                }

                TextColor nameColor = owned || specialStatus == null ? NamedTextColor.GREEN : NamedTextColor.RED;
                ItemStack.Builder builder = ItemStacks.item(perk.getIconMaterial(), 1,
                        Text.of("<color:{}>{}", nameColor, perk.getName()), lore);

                // Add enchant glow if enabled
                if (owned && enabled) {
                    return ItemStacks.enchanted(builder);
                }
                return builder;
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
                if (handler == null) return;

                DatapointSkywarsUnlocks.SkywarsUnlocks unlocks = handler.get(
                        SkywarsDataHandler.Data.UNLOCKS,
                        DatapointSkywarsUnlocks.class
                ).getValue();

                String specialStatus = perk.getSpecialStatus();
                if (specialStatus != null) {
                    player.sendMessage("<c>{} - cannot purchase here.", specialStatus);
                    return;
                }

                boolean owned = unlocks.hasPerk(perk.getId());

                if (owned) {
                    // Toggle the perk
                    boolean wasEnabled = unlocks.isInsanePerkEnabled(MODE, perk.getId());
                    unlocks.toggleInsanePerk(MODE, perk.getId());

                    if (wasEnabled) {
                        player.sendMessage("<c>✖ <7>Disabled <e>{} <7>for Insane mode.", perk.getName());
                    } else {
                        player.sendMessage("<a>✔ <7>Enabled <e>{} <7>for Insane mode.", perk.getName());
                    }
                    new GUIToggleInsanePerks().open(player);
                } else if (perk.isPurchasableWithCoins()) {
                    long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();
                    if (coins >= perk.getCost()) {
                        handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class)
                                .setValue(coins - perk.getCost());
                        unlocks.unlockPerk(perk.getId());
                        player.sendMessage("<a>You purchased <e>{}<a>! It is now enabled.", perk.getName());
                        new GUIToggleInsanePerks().open(player);
                    } else {
                        player.sendMessage("<c>You don't have enough coins to purchase this perk!");
                    }
                } else if (perk.isFree()) {
                    unlocks.unlockPerk(perk.getId());
                    player.sendMessage("<a>Unlocked <e>{}<a>! It is now enabled.", perk.getName());
                    new GUIToggleInsanePerks().open(player);
                } else {
                    player.sendMessage("<c>This perk cannot be purchased here.");
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
