package net.swofty.type.generic.gui.inventory;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.TooltipDisplay;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.text.HypixelTextRenderer;
import net.swofty.type.generic.text.RenderContext;
import net.swofty.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Do not use this class anymore, prefer the View System.
 *
 * @see net.swofty.type.generic.gui.v2.View View System
 * @deprecated do not use this anymore.
 */
@Getter
@Deprecated
public abstract class HypixelInventoryGUI {
    public static final Map<UUID, HypixelInventoryGUI> GUI_MAP = new ConcurrentHashMap<>();
    public static final ItemStack.Builder FILLER_ITEM = ItemStack.builder(Material.BLACK_STAINED_GLASS_PANE)
            .set(DataComponents.CUSTOM_NAME, Component.space())
            .set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.EMPTY);

    protected Text title;
    protected InventoryType size;
    protected final List<GUIItem> items;
    private Inventory inventory;
    protected HypixelPlayer player;
    private boolean hasFinishedLoading = false;
    private int itemInHand = 0;

    public HypixelInventoryGUI(String titleMarkup, InventoryType size, Object... arguments) {
        this(Text.of(titleMarkup, arguments), size);
    }

    public HypixelInventoryGUI(Text title, InventoryType size) {
        this.title = title;
        this.size = size;
        this.items = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Set an item inside the GUI
     *
     * @param a the item you want to set
     */
    public void set(GUIItem a) {
        synchronized (items) {
            items.removeIf(i -> i.itemSlot == a.itemSlot);
            items.add(a);
        }
    }

    /**
     * Set an item inside the gui
     *
     * @param slot   the slot the item should be in (any number between 0-53 depending on your GUI size)
     * @param stack  the {@link ItemStack} that should be in the slot
     * @param pickup can the player pick up the item by clicking on it or no
     */
    public void set(int slot, ItemStack.Builder stack, boolean pickup) {
        if (stack == null) {
            items.removeIf(i -> i.itemSlot == slot);
            return;
        }
        set(new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return stack;
            }

            @Override
            public boolean canPickup() {
                return pickup;
            }
        });
    }

    /**
     * Set an item inside the gui
     * In this method pickup is set to false by default
     *
     * @param slot  the slot the item should be in (any number between 0-53 depending on your GUI size)
     * @param stack the {@link ItemStack} that should be in the slot
     */
    public void set(int slot, ItemStack.Builder stack) {
        set(slot, stack, false);
    }

    /**
     * Get a GUI item that is in a slot
     *
     * @param slot which slot the GUI item is in
     * @return the GUIItem in that slot, if there is none, null
     */
    public GUIItem get(int slot) {
        synchronized (items) {
            return items.stream().filter(item -> item.itemSlot == slot).findFirst().orElse(null);
        }
    }

    /**
     * Fill a rectangle between two corners of the GUI
     *
     * @param stack       what {@link ItemStack} are we filling our rectangle with
     * @param cornerSlot  the top left corner of our rectangle
     * @param cornerSlot2 the bottom right corner of our rectangle
     * @param overwrite   if this is set to true, the method will ignore any items in the rectangle and replace them with 'stack'
     * @param pickup      is the player able to pick up the items in the rectangle?
     */
    public void fill(ItemStack.Builder stack, int cornerSlot, int cornerSlot2, boolean overwrite, boolean pickup) {
        if (cornerSlot < 0 || cornerSlot >= size.getSize())
            throw new IllegalArgumentException("Corner 1 of the border described is out of bounds");
        if (cornerSlot2 < 0 || cornerSlot2 >= size.getSize())
            throw new IllegalArgumentException("Corner 2 of the border described is out of bounds");

        int row1 = cornerSlot / 9, col1 = cornerSlot % 9;
        int row2 = cornerSlot2 / 9, col2 = cornerSlot2 % 9;

        int minRow = Math.min(row1, row2), maxRow = Math.max(row1, row2);
        int minCol = Math.min(col1, col2), maxCol = Math.max(col1, col2);

        List<Integer> slotsToSet = new ArrayList<>();
        synchronized (items) {
            int total = size.getSize();
            boolean[] occupied = new boolean[total];
            for (GUIItem item : items) {
                int s = item.itemSlot;
                if (s >= 0 && s < total) occupied[s] = true;
            }

            for (int r = minRow; r <= maxRow; r++) {
                for (int c = minCol; c <= maxCol; c++) {
                    int slot = r * 9 + c;
                    if (slot < 0 || slot >= total) continue;
                    if (!overwrite && occupied[slot]) continue;
                    slotsToSet.add(slot);
                }
            }
        }

        for (int slot : slotsToSet) {
            set(slot, stack, pickup);
        }
    }

    /**
     * Fill a rectangle between two corners of the GUI
     * Overwrite is set to true by default in this method
     *
     * @param stack       what {@link ItemStack} are we filling our rectangle with
     * @param cornerSlot  the top left corner of our rectangle
     * @param cornerSlot2 the bottom right corner of our rectangle
     * @param pickup      is the player able to pick up the items in the rectangle?
     */
    public void fill(ItemStack.Builder stack, int cornerSlot, int cornerSlot2, boolean pickup) {
        fill(stack, cornerSlot, cornerSlot2, true, pickup);
    }

    /**
     * Fill a rectangle between two corners of the GUI
     * Overwrite is set to true by default in this method
     * Pickup is set to false by default in this method
     *
     * @param stack       what {@link ItemStack} are we filling our rectangle with
     * @param cornerSlot  the top left corner of our rectangle
     * @param cornerSlot2 the bottom right corner of our rectangle
     */
    public void fill(ItemStack.Builder stack, int cornerSlot, int cornerSlot2) {
        fill(stack, cornerSlot, cornerSlot2, false);
    }

    /**
     * Fills the whole inventory with the item stack of your choice
     *
     * @param stack the {@link ItemStack} of your choice
     */
    public void fill(ItemStack.Builder stack) {
        fill(stack, 0, size.getSize() - 1);
    }

    /**
     * Fills the whole inventory with the Material of your choice
     *
     * @param material the {@link Material} of your choice
     */
    public void fill(Material material, String name) {
        fill(ItemStacks.named(material, name));
    }

    /**
     * Fills the border of a rectangle defined with two slots
     *
     * @param stack       the {@link ItemStack} of your choice
     * @param cornerSlot  the top left corner of your rectangle
     * @param cornerSlot2 the bottom right corner of your rectangle
     * @param overwrite   if this is set to true, the method will ignore any items in between the lines and replace them
     * @param pickup      if this is set to true, players will be able to pick up the items in your border
     */
    public void border(ItemStack.Builder stack, int cornerSlot, int cornerSlot2, boolean overwrite, boolean pickup) {
        if (cornerSlot < 0 || cornerSlot > size.getSize())
            throw new IllegalArgumentException("Corner 1 of the border described is out of bounds");
        if (cornerSlot2 < 0 || cornerSlot2 > size.getSize())
            throw new IllegalArgumentException("Corner 2 of the border described is out of bounds");
        int topLeft = Math.min(cornerSlot, cornerSlot2);
        int bottomRight = Math.max(cornerSlot, cornerSlot2);
        int topRight;
        for (topRight = bottomRight; topRight > topLeft; topRight -= 9) ;
        int bottomLeft;
        for (bottomLeft = topLeft; bottomLeft < bottomRight; bottomLeft += 9) ;
        topRight += 9;
        bottomLeft -= 9;

        // build a set of occupied slots for O(1) lookup
        synchronized (items) {
            Set<Integer> occupiedSlots = new HashSet<>();
            for (GUIItem item : items) {
                occupiedSlots.add(item.itemSlot);
            }

            int rightBound = topRight - topLeft;
            for (int y = topLeft; y <= bottomLeft; y += 9) {
                for (int x = y; x <= rightBound + y; x++) {
                    if (!occupiedSlots.contains(x) || overwrite) {
                        if (y == topLeft || y == bottomLeft)
                            set(x, stack, pickup);
                        if (x == y || x == rightBound + y)
                            set(x, stack, pickup);
                    }
                }
            }
        }
    }

    /**
     * Fills the border in a rectangle between two slots\
     * Overwrite is set to true by default in this method
     *
     * @param stack       the {@link ItemStack} of your choice
     * @param cornerSlot  the top left corner of your rectangle
     * @param cornerSlot2 the bottom right corner of your rectangle
     * @param pickup      if this is set to true, players will be able to pick up the items in your border
     */
    public void border(ItemStack.Builder stack, int cornerSlot, int cornerSlot2, boolean pickup) {
        border(stack, cornerSlot, cornerSlot2, true, pickup);
    }

    /**
     * Fills the border in a rectangle between two slots\
     * Overwrite is set to true by default in this method
     * Pickup is set to false by default in this method
     *
     * @param stack       the {@link ItemStack} of your choice
     * @param cornerSlot  the top left corner of your rectangle
     * @param cornerSlot2 the bottom right corner of your rectangle
     */
    public void border(ItemStack.Builder stack, int cornerSlot, int cornerSlot2) {
        border(stack, cornerSlot, cornerSlot2, false);
    }

    /**
     * Fills the border between two corners of your GUI with the item stack of your choice
     *
     * @param stack the {@link ItemStack} of your choice
     */
    public void border(ItemStack.Builder stack) {
        border(stack, 0, size.getSize() - 1);
    }

    /**
     * Iterates through all the slots and finds the first empty one
     *
     * @return an empty slot index
     */
    public int firstEmpty() {
        synchronized (items) {
            Set<Integer> occupiedSlots = new HashSet<>();
            for (GUIItem item : items) {
                occupiedSlots.add(item.itemSlot);
            }
            for (int i = 0; i < size.getSize(); i++) {
                if (!occupiedSlots.contains(i))
                    return i;
            }
        }
        return -1;
    }

    /**
     * Opens the GUI for a player
     *
     * @param player the player the gui is being opened for
     */
    public void open(HypixelPlayer player) {
        this.player = player;
        this.itemInHand = player.getHeldSlot();
        this.inventory = new Inventory(size, renderedTitle());

        HypixelInventoryGUI previouslyOpen = GUI_MAP.get(player.getUuid());
        if (previouslyOpen != null) {
            if (!previouslyOpen.hasFinishedLoading) {
                return;
            }

            previouslyOpen.onClose(
                    new InventoryCloseEvent(previouslyOpen.getInventory(), player, true),
                    net.swofty.type.generic.gui.inventory.HypixelInventoryGUI.CloseReason.SERVER_EXITED
            );
            GUI_MAP.remove(player.getUuid());

            if (previouslyOpen.getInventory().getInventoryType() == size) {
                inventory = previouslyOpen.getInventory();
                inventory.setTitle(renderedTitle());
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    inventory.setItemStack(slot, ItemStack.AIR);
                }
            }
        }

        InventoryGUIOpenEvent openEvent = new InventoryGUIOpenEvent(player, this, inventory);

        // Initializing GUI
        Thread.startVirtualThread(() -> {
            try {
                setItems(openEvent);
                updateItemStacks(inventory, player);
                onOpen(openEvent);
                if (player.getHeldSlot() != itemInHand) {
                    player.sendMessage("<c>Your item in hand cannot change in between opening GUIs");
                    return;
                }
            } catch (Exception e) {
                player.sendMessage("<c>An error occurred while opening the GUI");
                player.closeInventory();
                Logger.error(e, "Failed to open GUI '{}' for player {}", getTitle(), player.getUsername());
                return;
            }
            hasFinishedLoading = true;

            if (this instanceof RefreshingGUI gui) {
                MinecraftServer.getSchedulerManager().submitTask(() -> {
                    // Player is removed from Map on disconnect, so we just need to check that
                    if (!GUI_MAP.containsKey(player.getUuid()) || GUI_MAP.get(player.getUuid()) != this) {
                        return TaskSchedule.stop();
                    }
                    Thread.startVirtualThread(() -> {
                        synchronized (items) {
                            gui.refreshItems(player);
                            updateItemStacks(inventory, player);
                        }
                    });
                    return TaskSchedule.tick(gui.refreshRate());
                });
            }
        });
        player.openInventory(inventory);

        GUI_MAP.put(player.getUuid(), this);
        afterOpen(openEvent);
    }

    protected void setTitle(String titleMarkup, Object... arguments) {
        setTitle(Text.of(titleMarkup, arguments));
    }

    protected void setTitle(Text title) {
        this.title = title;
        inventory.setTitle(renderedTitle());
    }

    private Component renderedTitle() {
        return HypixelTextRenderer.render(title.asComponent(), RenderContext.of(player));
    }

    /**
     * Method to allow people to hotkey items into the GUI or not
     *
     * @return a boolean
     */
    public abstract boolean allowHotkeying();

    /**
     * Runs when the player opens the gui
     *
     * @param e the event of the gui opening
     */
    public void onOpen(InventoryGUIOpenEvent e) {
    }

    /**
     * Runs when the player closes the gui
     *
     * @param e the event of the gui closing
     */
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
    }

    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
    }

    /**
     * Runs when the player clicks on their own inventory whole this GUI is open
     *
     * @param e the event of the click
     */
    public abstract void onBottomClick(InventoryPreClickEvent e);

    /**
     * Runs after the GUI opens to the player
     *
     * @param e the event of the gui opening
     */
    public void afterOpen(InventoryGUIOpenEvent e) {
    }

    /**
     * Runs before the GUI opens, in order to set the items in
     *
     * @param e the event of the GUI opening
     */
    public void setItems(InventoryGUIOpenEvent e) {
    }

    /**
     * re-set all the GUIItems inside the inventory
     *
     * @param inventory an inventory object to set the items in
     */
    public void updateItemStacks(Inventory inventory, HypixelPlayer player) {
        synchronized (items) {
            RenderContext context = RenderContext.of(player);
            for (GUIItem item : items) {
                ItemStack toReplace = HypixelTextRenderer.renderStack(item.getItem(player).build(), context);
                if (!inventory.getItemStack(item.itemSlot).equals(toReplace)) {
                    inventory.setItemStack(item.itemSlot, toReplace);
                }
            }
        }
    }

    public record InventoryGUIOpenEvent(HypixelPlayer player, HypixelInventoryGUI opened, Inventory inventory) {
    }

    public enum CloseReason {
        SIGN_OPENED,
        PLAYER_EXITED,
        SERVER_EXITED
    }
}
