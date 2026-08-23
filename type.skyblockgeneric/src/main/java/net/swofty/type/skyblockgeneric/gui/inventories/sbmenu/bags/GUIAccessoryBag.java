package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class GUIAccessoryBag implements StatefulView<GUIAccessoryBag.AccessoryBagState> {
    private static final SortedMap<CustomCollectionAward, Integer> SLOTS_PER_UPGRADE = new TreeMap<>(Map.of(
            CustomCollectionAward.ACCESSORY_BAG, 3,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_1, 9,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_2, 15,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_3, 21,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_4, 27,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_5, 33,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_6, 39,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_7, 45,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_8, 51,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_9, 57
    ));

    @Override
    public ViewConfiguration<AccessoryBagState> configuration() {
        return ViewConfiguration.withText(
                (state, ctx) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
                    int totalSlots = getTotalSlots(player);
                    int totalPages = Math.max(1, (int) Math.ceil((double) totalSlots / 45));
                    return Text.key("gui_sbmenu.bags.accessory.title", state.page() + 1, totalPages);
                },
                InventoryType.CHEST_6_ROW
        );
    }

    @Override
    public AccessoryBagState initialState() {
        return new AccessoryBagState(0);
    }

    @Override
    public void layout(ViewLayout<AccessoryBagState> layout, AccessoryBagState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int page = state.page();
        int totalSlots = getTotalSlots(player);
        int slotsPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) totalSlots / slotsPerPage));
        int startIndex = page * slotsPerPage;
        int endSlot = Math.min(totalSlots - startIndex, slotsPerPage);

        // Editable slots for accessories
        for (int i = 0; i < endSlot; i++) {
            int absoluteSlot = i + startIndex;
            SkyBlockItem item = player.getAccessoryBag().getInSlot(absoluteSlot);

            layout.editable(i, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                if (item == null) {
                    return ItemStack.builder(Material.AIR);
                } else {
                    return PlayerItemUpdater.playerUpdate(p, item.getItemStack());
                }
            }, (slot, oldItem, newItem, s) -> {
                // Save happens on close
            });
        }

        // Locked slots
        for (int i = endSlot; i < slotsPerPage; i++) {
            int slotIndex = i + startIndex;
            CustomCollectionAward nextUpgrade = getUpgradeNeededForSlotIndex(slotIndex);
            if (nextUpgrade != null) {
                layout.slot(i, (s, c) -> ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1,
                    Text.key("gui_sbmenu.bags.accessory.locked"),
                    Text.keyLines("gui_sbmenu.bags.accessory.locked.lore", nextUpgrade.getDisplay())));
            }
        }

        // Previous page
        if (page > 0) {
            layout.slot(45, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                            Text.key("gui_sbmenu.bags.accessory.previous_page"), List.of()),
                    (click, c) -> c.session(AccessoryBagState.class).update(s -> s.withPage(s.page() - 1)));
        }

        // Next page
        if (page < totalPages - 1) {
            layout.slot(53, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                            Text.key("gui_sbmenu.bags.accessory.next_page"), List.of()),
                    (click, c) -> c.session(AccessoryBagState.class).update(s -> s.withPage(s.page() + 1)));
        }
    }

    @Override
    public void onClose(AccessoryBagState state, ViewContext ctx, ViewSession.CloseReason reason) {
        save((SkyBlockPlayer) ctx.player(), ctx, state.page());
    }

    @Override
    public boolean onBottomClick(net.swofty.type.generic.gui.v2.context.ClickContext<AccessoryBagState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockItem cursorItem = new SkyBlockItem(player.getInventory().getCursorItem());
        SkyBlockItem clickedItem = new SkyBlockItem(player.getInventory().getItemStack(click.slot()));

        if (isItemAllowed(cursorItem, player) && isItemAllowed(clickedItem, player)) {
            return true;
        }

        player.sendMessage(Text.key("gui_sbmenu.bags.accessory.msg.cannot_put"));
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

    private void save(SkyBlockPlayer player, ViewContext ctx, int page) {
        DatapointAccessoryBag.PlayerAccessoryBag accessoryBag = player.getAccessoryBag();
        int totalSlots = getTotalSlots(player);
        int slotsPerPage = 45;
        int startIndex = page * slotsPerPage;
        int endSlot = Math.min(totalSlots - startIndex, slotsPerPage);

        for (int i = 0; i < endSlot; i++) {
            int absoluteSlot = i + startIndex;
            SkyBlockItem item = new SkyBlockItem(ctx.inventory().getItemStack(i));
            if (item.isNA() || item.getMaterial() == Material.AIR) {
                accessoryBag.removeFromSlot(absoluteSlot);
            } else {
                accessoryBag.setInSlot(absoluteSlot, item);
            }
        }

        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ACCESSORY_BAG, DatapointAccessoryBag.class).setValue(accessoryBag);
    }

    private boolean isItemAllowed(SkyBlockItem item, SkyBlockPlayer player) {
        if (item.isNA()) return true;
        if (item.getMaterial().equals(Material.AIR)) return true;

        if (item.hasComponent(AccessoryComponent.class)) {
            DatapointAccessoryBag.PlayerAccessoryBag accessoryBag = player.getAccessoryBag();
            accessoryBag.addDiscoveredAccessory(item.getAttributeHandler().getPotentialType());
            player.getSkyBlockExperience().addExperience(
                    SkyBlockLevelCause.getAccessoryCause(item.getAttributeHandler().getPotentialType())
            );
            return true;
        } else {
            return false;
        }
    }

    public record AccessoryBagState(int page) {
        public AccessoryBagState withPage(int newPage) {
            return new AccessoryBagState(newPage);
        }
    }
}
