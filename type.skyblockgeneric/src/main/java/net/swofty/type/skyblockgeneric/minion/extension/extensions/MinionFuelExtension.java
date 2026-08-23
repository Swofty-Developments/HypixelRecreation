package net.swofty.type.skyblockgeneric.minion.extension.extensions;

import lombok.NonNull;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.GUIMinion;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.MinionFuelComponent;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.minion.IslandMinionData;
import net.swofty.type.skyblockgeneric.minion.extension.MinionExtension;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MinionFuelExtension extends MinionExtension {
    private long insertionTime = 0;
    private int count = 0;

    public MinionFuelExtension(@Nullable ItemType itemTypeLinker, @Nullable Object data) {
        super(itemTypeLinker, data);

        if (data != null) {
            insertionTime = (long) data;
        }
    }

    /**
     * Advances a finite fuel stack based on wall-clock time. This is deliberately
     * called from the minion tick as well as the GUI: fuel must expire while nobody
     * has the menu open.
     */
    public boolean refresh() {
        if (getItemTypePassedIn() == null) return false;

        long duration = new SkyBlockItem(getItemTypePassedIn())
                .getComponent(MinionFuelComponent.class).getFuelLastTimeInMS();
        if (duration == 0) return true;

        long elapsed = Math.max(0, System.currentTimeMillis() - insertionTime);
        long consumed = elapsed / duration;
        if (consumed == 0) return count > 0;

        count -= (int) Math.min(consumed, Integer.MAX_VALUE);
        if (count <= 0) {
            count = 0;
            setItemTypePassedIn(null);
            return false;
        }
        insertionTime += consumed * duration;
        return true;
    }

    @Override
    public @NonNull GUIClickableItem getDisplayItem(IslandMinionData.IslandMinion minion, int slot) {
        boolean shouldDisplayItem = true;

        if (getItemTypePassedIn() == null) {
            shouldDisplayItem = false;
        } else {
            if (!refresh()) {
                shouldDisplayItem = false;
                minion.getExtensionData().setData(slot, MinionFuelExtension.this);
            }
        }

        if (!shouldDisplayItem)
            return new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    SkyBlockItem fuelItem = new SkyBlockItem(p.getInventory().getCursorItem());
                    e.setCancelled(true);

                    if (fuelItem.hasComponent(MinionFuelComponent.class)) {
                        p.getInventory().setCursorItem(ItemStack.AIR);
                        MinionFuelExtension.this.addFuel(minion, slot, fuelItem);
                    } else {
                        player.sendMessage("<c>This item is not a valid Minion Fuel item.");
                    }

                    new GUIMinion(minion).open(player);
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.ORANGE_STAINED_GLASS_PANE, 1, """
                            <a>Fuel
                            <7>Increase the speed of your
                            <7>minion by adding minion fuel
                            <7>items here.

                            <c>Note: <7>You can't take fuel
                            <7>back out after you place it
                            <7>here.""");
                }
            };

        return new GUIClickableItem(slot) {

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                SkyBlockItem item = new SkyBlockItem(e.getClickedItem());
                if (item.getComponent(MinionFuelComponent.class).getFuelLastTimeInMS() == 0) {
                    player.addAndUpdateItem(getItemTypePassedIn());
                    setItemTypePassedIn(null);
                    minion.getExtensionData().setData(slot, MinionFuelExtension.this);
                    new GUIMinion(minion).open(player);
                    return;
                }

                if (e.getClick() instanceof Click.Right) {
                    setItemTypePassedIn(null);
                    minion.getExtensionData().setData(slot, MinionFuelExtension.this);
                    new GUIMinion(minion).open(player);
                    return;
                }

                SkyBlockItem fuelItem = new SkyBlockItem(p.getInventory().getCursorItem());
                if (!(fuelItem.hasComponent(MinionFuelComponent.class))) {
                    player.sendMessage("<c>You can only put fuel in this slot.");
                    return;
                }

                if (getItemTypePassedIn() != fuelItem.getAttributeHandler().getPotentialType())
                    player.sendMessage("<a>Replaced your old fuel!");

                int added = MinionFuelExtension.this.addFuel(minion, slot, fuelItem);
                if (added > 0)
                    fuelItem.setAmount(fuelItem.getAmount() - added);

                if (fuelItem.getAmount() > 0) {
                    p.getInventory().setCursorItem(new NonPlayerItemUpdater(fuelItem.getItemStack()).getUpdatedItem().build());
                } else p.getInventory().setCursorItem(ItemStack.AIR);

                new GUIMinion(minion).open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                long timeFuelLasts = new SkyBlockItem(getItemTypePassedIn()).getComponent(MinionFuelComponent.class).getFuelLastTimeInMS();

                ItemStack.Builder itemBuilder = new NonPlayerItemUpdater(new SkyBlockItem(getItemTypePassedIn(), count)).getUpdatedItem();

                List<Text> additionalLore = new ArrayList<>();

                if (timeFuelLasts > 0) {
                    additionalLore.add(Text.empty());
                    additionalLore.add(Text.of("<7>Time Remaining: <b>{:time}",
                            timeFuelLasts * count - (System.currentTimeMillis() - insertionTime)));
                    additionalLore.add(Text.empty());
                    additionalLore.add(Text.of("<c>Right Click to destroy this fuel."));
                } else {
                    additionalLore.add(Text.empty());
                    additionalLore.add(Text.of("<e>Click to take fuel out."));
                }

                return ItemStacks.appendLore(itemBuilder, additionalLore);
            }
        };
    }

    // Returns the amount of fuel added
    public int addFuel(IslandMinionData.IslandMinion minion, int slot, SkyBlockItem fuelItem) {
        if (fuelItem.hasComponent(MinionFuelComponent.class)) {
            insertionTime = System.currentTimeMillis();
            int added = fuelItem.getAmount();

            if (getItemTypePassedIn() != fuelItem.getAttributeHandler().getPotentialType()) {
                count = added;
            } else {
                int together = count + added;
                if (together > 64) {
                    added = 64 - count;
                    count = 64;
                } else {
                    count = together;
                }
            }

            setItemTypePassedIn(fuelItem.getAttributeHandler().getPotentialType());
            minion.getExtensionData().setData(slot, MinionFuelExtension.this);
            return added;
        }
        return 0;
    }

    @Override
    public String toString() {
        if (getItemTypePassedIn() == null) {
            return "null";
        }
        return getItemTypePassedIn() + ":" + insertionTime + ":" + count;
    }

    @Override
    public void fromString(String string) {
        if (string.equals("null")) {
            setItemTypePassedIn(null);
            return;
        }
        String[] split = string.split(":");
        setItemTypePassedIn(ItemType.valueOf(split[0]));
        insertionTime = Long.parseLong(split[1]);
        if (split.length > 2)
            count = Integer.parseInt(split[2]);
    }
}
