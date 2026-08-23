package net.swofty.type.skywarslobby.gui;

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
import net.swofty.type.skywarslobby.perk.SkywarsPerk;
import net.swofty.type.skywarslobby.perk.SkywarsPerkRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for viewing and managing Normal mode perk slots.
 * Shows 6 perk slots + 3 global perks.
 */
public class GUISelectNormalPerks extends HypixelInventoryGUI {
    private static final String MODE = "NORMAL";
    private static final int[] PERK_SLOTS = {11, 12, 13, 14, 15, 16};

    public GUISelectNormalPerks() {
        super("Select Normal Perks", InventoryType.CHEST_6_ROW);
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

        // Perk Slots label (slot 9)
        set(new GUIItem(9) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.GOLD_BLOCK, 1, """
                        <a>Perk Slots
                        <7>Your selected perks will be active
                        <7>during your <a>Normal SkyWars</a> games.""");
            }
        });

        // 6 Perk slots (slots 11-16)
        for (int i = 0; i < 6; i++) {
            final int slotIndex = i;
            final int guiSlot = PERK_SLOTS[i];

            set(new GUIClickableItem(guiSlot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    String perkId = unlocks.getPerkAtSlot(MODE, slotIndex);

                    if (perkId == null || perkId.isEmpty()) {
                        // Empty slot
                        return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1, """
                                <c>Empty
                                <8>Perk Slot #{}

                                <e>Click to select a perk!""", slotIndex + 1);
                    }

                    SkywarsPerk perk = SkywarsPerkRegistry.getPerk(perkId);
                    if (perk == null) {
                        return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1, """
                                <c>Empty
                                <8>Perk Slot #{}

                                <e>Click to select a perk!""", slotIndex + 1);
                    }

                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.of("<8>Perk Slot #{}", slotIndex + 1));
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>{}", perk.getEffectDescription()));
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>Rarity: {}", perk.getRarity().getFormattedName()));
                    lore.add(Text.empty());
                    lore.add(Text.of("<e>Left-click to replace!"));
                    lore.add(Text.of("<e>Right-click to clear!"));

                    return ItemStacks.item(perk.getIconMaterial(), 1,
                            Text.of("<6>{}", perk.getName()), lore);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    if (e.getClick() instanceof Click.Right) {
                        // Right-click: clear slot
                        unlocks.clearPerkSlot(MODE, slotIndex);
                        player.sendMessage("<7>Cleared perk slot #{}", slotIndex + 1);
                        new GUISelectNormalPerks().open(player);
                    } else {
                        // Left-click: open perk selector for this slot
                        new GUISelectPerk(slotIndex).open(player);
                    }
                }
            });
        }

        // Close button (slot 17)
        set(GUIClickableItem.getCloseItem(17));

        // Global Perks label (slot 27)
        set(new GUIItem(27) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DIAMOND_BLOCK, 1, """
                        <a>Global Perks
                        <7>All players will have these perks
                        <7>active during <a>Normal SkyWars</a> games.""");
            }
        });

        // Global perks (slots 29-31)
        List<SkywarsPerk> globalPerks = SkywarsPerkRegistry.getGlobalPerks();
        int[] globalSlots = {29, 30, 31};
        for (int i = 0; i < Math.min(globalPerks.size(), 3); i++) {
            final SkywarsPerk perk = globalPerks.get(i);
            final int guiSlot = globalSlots[i];

            set(new GUIItem(guiSlot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.of("<8>Global Perk"));
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>{}", perk.getEffectDescription()));
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>Rarity: {}", perk.getRarity().getFormattedName()));

                    return ItemStacks.item(perk.getIconMaterial(), 1,
                            Text.of("<6>{}", perk.getName()), lore);
                }
            });
        }

        // Go Back button (slot 48)
        set(GUIClickableItem.getGoBackItem(48, new GUIKitsPerks()));

        // Total Coins display (slot 49)
        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.EMERALD, 1, """
                        <7>Total Coins: <6>{:,}
                        <6>https://store.hypixel.net""", coins);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
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
