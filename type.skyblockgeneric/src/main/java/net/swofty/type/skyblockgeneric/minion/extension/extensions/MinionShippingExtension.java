package net.swofty.type.skyblockgeneric.minion.extension.extensions;

import lombok.NonNull;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.GUIMinion;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.MinionShippingComponent;
import net.swofty.type.skyblockgeneric.minion.IslandMinionData;
import net.swofty.type.skyblockgeneric.minion.extension.MinionExtension;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MinionShippingExtension extends MinionExtension {
    private double heldCoins = 0;
    private double itemsSold = 0;

    public MinionShippingExtension(@Nullable ItemType itemTypeLinker, @Nullable Object data) {
        super(itemTypeLinker, data);

        if (data != null) {
            String[] split = ((String) data).split(":");
            setItemTypePassedIn(ItemType.valueOf(split[0]));
            itemsSold = Double.parseDouble(split[1]);
        }
    }

    public void addCoins(double coins, int itemCount) {
        heldCoins += coins;
        itemsSold += itemCount;
    }

    @Override
    public @NonNull GUIClickableItem getDisplayItem(IslandMinionData.IslandMinion minion, int slot) {
        if (getItemTypePassedIn() == null) {
            return new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    SkyBlockItem shippingItem = new SkyBlockItem(p.getInventory().getCursorItem());

                    if (!shippingItem.hasComponent(MinionShippingComponent.class)) {
                        player.sendMessage("<c>This item is not a valid Minion Shipping item.");
                        e.setCancelled(true);
                        return;
                    }

                    p.getInventory().setCursorItem(ItemStack.AIR);
                    e.setCancelled(true);
                    setItemTypePassedIn(shippingItem.getAttributeHandler().getPotentialType());
                    minion.getExtensionData().setData(slot, MinionShippingExtension.this);
                    new GUIMinion(minion).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.BLUE_STAINED_GLASS_PANE, 1, """
                            <a>Automated Shipping
                            <7>Add a <a>Budget Hopper <7>or
                            <9>Enchanted Hopper <7>here to make
                            <7>your minion automatically sell
                            <7>generated items after its
                            <7>inventory is full.""");
                }
            };
        } else {
            return new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    if (!p.getInventory().getCursorItem().isAir()) {
                        player.sendMessage("<c>Your cursor must be empty to pick this item up!");
                        e.setCancelled(true);
                        return;
                    }

                    if (e.getClick() instanceof Click.Right) {
                        e.setCancelled(true);

                        if (heldCoins == 0)
                            return;

                        player.addCoins(heldCoins);
                        player.sendMessage("<a>You have received <6>{:,} coins<a> from your Minion!", heldCoins);
                        heldCoins = 0;
                        return;
                    }

                    player.addAndUpdateItem(getItemTypePassedIn());
                    if (heldCoins > 0) {
                        player.addCoins(heldCoins);
                        player.sendMessage("<a>You have received <6>{:,} coins<a> from your Minion!", heldCoins);
                    }
                    setItemTypePassedIn(null);
                    itemsSold = 0;
                    heldCoins = 0;
                    p.getInventory().setCursorItem(ItemStack.AIR);
                    e.setCancelled(true);
                    minion.getExtensionData().setData(slot, MinionShippingExtension.this);
                    new GUIMinion(minion).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockItem shippingItem = new SkyBlockItem(getItemTypePassedIn());

                    List<Text> lore = new ArrayList<>();
                    for (String line : shippingItem.getLore()) {
                        lore.add(Text.literal(line));
                    }
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>Items Sold: <b>{:,}", itemsSold));
                    lore.add(Text.of("<7>Held Coins: <b>{:,}", heldCoins));
                    lore.add(Text.empty());
                    lore.add(Text.of("<b>Right-click to get held coins."));
                    lore.add(Text.of("<e>Click to remove."));

                    return ItemStacks.item(shippingItem.getMaterial(), 1, Text.literal(shippingItem.getDisplayName()), lore);
                }
            };
        }
    }

    @Override
    public String toString() {
        if (getItemTypePassedIn() == null)
            return "null";
        return getItemTypePassedIn() + ":" + itemsSold + ":" + heldCoins;
    }

    @Override
    public void fromString(String string) {
        if (string.equals("null"))
            return;
        String[] split = string.split(":");
        setItemTypePassedIn(ItemType.valueOf(split[0]));
        itemsSold = Double.parseDouble(split[1]);
        heldCoins = Double.parseDouble(split[2]);
    }
}
