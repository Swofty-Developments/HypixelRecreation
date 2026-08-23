package net.swofty.type.skyblockgeneric.minion.extension.extensions;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.GUIMinion;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.MinionSkinComponent;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.minion.IslandMinionData;
import net.swofty.type.skyblockgeneric.minion.extension.MinionExtension;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MinionSkinExtension extends MinionExtension {

    public MinionSkinExtension(@Nullable ItemType itemTypeLinker, @Nullable Object data) {
        super(itemTypeLinker, data);
    }

    @Override
    public @NotNull GUIClickableItem getDisplayItem(IslandMinionData.IslandMinion minion, int slot) {
        if (getItemTypePassedIn() == null) {
            return new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    SkyBlockItem skinItem = new SkyBlockItem(p.getInventory().getCursorItem());
                    e.setCancelled(true);

                    if (skinItem.hasComponent(MinionSkinComponent.class)) {
                        p.getInventory().setCursorItem(ItemStack.AIR);
                        setItemTypePassedIn(skinItem.getAttributeHandler().getPotentialType());
                        minion.getExtensionData().setData(slot, MinionSkinExtension.this);
                        minion.getMinionEntity().updateMinionDisplay(minion);
                    } else {
                        player.sendMessage("<c>This item is not a valid Minion Skin.");
                    }
                    new GUIMinion(minion).open(player);
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, 1, """
                            <a>Minion Skin Slot
                            <7>You can insert a Minion Skin
                            <7>here to change the appearance of
                            <7>your minion.""");
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

                    player.addAndUpdateItem(getItemTypePassedIn());
                    setItemTypePassedIn(null);
                    p.getInventory().setCursorItem(ItemStack.AIR);
                    e.setCancelled(true);
                    minion.getExtensionData().setData(slot, MinionSkinExtension.this);
                    minion.getMinionEntity().updateMinionDisplay(minion);
                    new GUIMinion(minion).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    ItemStack.Builder item = new NonPlayerItemUpdater(new SkyBlockItem(getItemTypePassedIn())).getUpdatedItem();
                    ItemStacks.name(item, "<a>Minion Skin Slot");
                    item = ItemStacks.lore(item, List.of(
                            Text.of("<7>You can insert a Minion Skin"),
                            Text.of("<7>here to change the appearance of"),
                            Text.of("<7>your minion."),
                            Text.empty(),
                            Text.of("<7>Current Skin: <color:{}>{}", getItemTypePassedIn().rarity.getColor(), getItemTypePassedIn().getDisplayName()),
                            Text.empty(),
                            Text.of("<e>Click to remove.")
                    ));

                    return item;
                }
            };
        }
    }

    @Override
    public String toString() {
        if (getItemTypePassedIn() == null)
            return "null";
        return getItemTypePassedIn().name();
    }

    @Override
    public void fromString(String string) {
        if (string.equals("null")) {
            setItemTypePassedIn(null);
            return;
        }
        setItemTypePassedIn(ItemType.valueOf(string));
    }
}
