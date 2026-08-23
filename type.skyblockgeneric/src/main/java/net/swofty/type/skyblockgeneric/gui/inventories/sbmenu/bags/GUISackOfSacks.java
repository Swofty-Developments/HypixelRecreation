package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSackOfSacks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.SackComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class GUISackOfSacks implements StatefulView<GUISackOfSacks.SackOfSacksState> {
    private static final SortedMap<CustomCollectionAward, Integer> SLOTS_PER_UPGRADE = new TreeMap<>(Map.of(
            CustomCollectionAward.SACK_OF_SACKS, 3,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_1, 6,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_2, 9,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_3, 12,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_4, 15,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_5, 18
    ));

    @Override
    public ViewConfiguration<SackOfSacksState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.bags.sack_of_sacks.title", InventoryType.CHEST_5_ROW);
    }

    @Override
    public SackOfSacksState initialState() {
        return new SackOfSacksState(0);
    }

    @Override
    public void layout(ViewLayout<SackOfSacksState> layout, SackOfSacksState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        Components.back(layout, 39, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int totalSlots = getTotalSlots(player);
        int slotsPerPage = 45;

        // Editable slots for sacks
        for (int i = 0; i < totalSlots; i++) {
            int slotIndex = i;
            SkyBlockItem item = player.getSackOfSacks().getInSlot(slotIndex);

            layout.slot(slotIndex, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                if (item == null) {
                    return ItemStack.builder(Material.AIR);
                } else {
                    return PlayerItemUpdater.playerUpdate(p, item.getItemStack());
                }
            }, (click, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                if (click.click() instanceof Click.Right) {
                    SkyBlockItem skyBlockItem = new SkyBlockItem(c.inventory().getItemStack(slotIndex));
                    if (skyBlockItem.isNA() || skyBlockItem.isAir()) return;
                    c.push(new GUISack(skyBlockItem.getAttributeHandler().getPotentialType(), true));
                }
            });

            // Also make it editable
            layout.editable(slotIndex, (slot, oldItem, newItem, s) -> {
                // Will be saved on close
            });
        }

        // Locked slots
        for (int i = totalSlots; i < slotsPerPage; i++) {
            CustomCollectionAward nextUpgrade = getUpgradeNeededForSlotIndex(i);
            if (nextUpgrade != null) {
                layout.slot(i, (s, c) -> ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1,
                    Text.key("gui_sbmenu.bags.sack_of_sacks.locked_slot"),
                    Text.keyLines("gui_sbmenu.bags.sack_of_sacks.locked_slot.lore", nextUpgrade.getDisplay())));
            }
        }

        // Insert inventory button
        layout.slot(38, (s, c) -> ItemStacks.item(Material.CHEST, 1,
                        Text.key("gui_sbmenu.bags.sack_of_sacks.insert_inventory"),
                        Text.keyLines("gui_sbmenu.bags.sack_of_sacks.insert_inventory.lore")),
                (click, c) -> {
                    SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                    int slot = 0;
                    for (ItemStack itemStack : p.getInventory().getItemStacks()) {
                        SkyBlockItem sItem = new SkyBlockItem(itemStack);
                        ItemType type = sItem.getAttributeHandler().getPotentialType();
                        if (p.canInsertItemIntoSacks(type)) {
                            p.getSackItems().increase(type, sItem.getAmount());
                            p.getInventory().setItemStack(slot, ItemStack.AIR);
                        }
                        slot++;
                    }
                });
    }

    @Override
    public void onClose(SackOfSacksState state, ViewContext ctx, ViewSession.CloseReason reason) {
        save((SkyBlockPlayer) ctx.player(), ctx);
    }

    @Override
    public boolean onBottomClick(ClickContext<SackOfSacksState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockItem cursorItem = new SkyBlockItem(player.getInventory().getCursorItem());
        SkyBlockItem clickedItem = new SkyBlockItem(player.getInventory().getItemStack(click.slot()));

        if (isItemAllowed(cursorItem) && isItemAllowed(clickedItem)) {
            return true;
        }

        player.sendMessage(Text.key("gui_sbmenu.bags.sack_of_sacks.msg.cannot_put"));
        return false;
    }

    private int getTotalSlots(SkyBlockPlayer player) {
        int totalSlots = 0;
        for (CustomCollectionAward entry : SLOTS_PER_UPGRADE.keySet()) {
            if (player.hasCustomCollectionAward(entry)) {
                totalSlots = Math.max(totalSlots, SLOTS_PER_UPGRADE.get(entry));
            }
        }
        return totalSlots;
    }

    private CustomCollectionAward getUpgradeNeededForSlotIndex(int slotIndex) {
        for (CustomCollectionAward entry : SLOTS_PER_UPGRADE.keySet()) {
            if (slotIndex < SLOTS_PER_UPGRADE.get(entry)) {
                return entry;
            }
        }
        return null;
    }

    private void save(SkyBlockPlayer player, ViewContext ctx) {
        DatapointSackOfSacks.PlayerSackOfSacks sackOfSacks = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.SACK_OF_SACKS, DatapointSackOfSacks.class).getValue();
        int totalSlots = getTotalSlots(player);

        for (int i = 0; i < totalSlots; i++) {
            SkyBlockItem item = new SkyBlockItem(ctx.inventory().getItemStack(i));
            if (item.isNA() || item.getMaterial() == Material.AIR) {
                sackOfSacks.removeFromSlot(i);
            } else {
                sackOfSacks.setInSlot(i, item);
            }
        }
    }

    private boolean isItemAllowed(SkyBlockItem item) {
        if (item.isNA()) return true;
        if (item.getMaterial().equals(Material.AIR)) return true;
        return item.hasComponent(SackComponent.class);
    }

    public record SackOfSacksState(int placeholder) {}
}
