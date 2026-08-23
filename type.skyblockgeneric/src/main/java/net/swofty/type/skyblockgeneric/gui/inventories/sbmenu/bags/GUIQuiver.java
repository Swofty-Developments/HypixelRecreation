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
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointQuiver;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public class GUIQuiver implements StatefulView<GUIQuiver.QuiverState> {
    private static final Map<CustomCollectionAward, Integer> SLOTS_PER_UPGRADE = Map.of(
            CustomCollectionAward.QUIVER, 18,
            CustomCollectionAward.QUIVER_UPGRADE_1, 9,
            CustomCollectionAward.QUIVER_UPGRADE_2, 9
    );

    @Override
    public ViewConfiguration<QuiverState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.bags.quiver.title", InventoryType.CHEST_5_ROW);
    }

    @Override
    public QuiverState initialState() {
        return new QuiverState(0);
    }

    @Override
    public void layout(ViewLayout<QuiverState> layout, QuiverState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        Components.back(layout, 39, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int amountOfSlots = getUnlockedSlots(player);
        int rawAmountOfSlots = 0;

        // Set up unlocked/locked slots
        for (Map.Entry<CustomCollectionAward, Integer> entry : SLOTS_PER_UPGRADE.entrySet()) {
            if (player.hasCustomCollectionAward(entry.getKey())) {
                rawAmountOfSlots += entry.getValue();
            } else {
                for (int i = 0; i < entry.getValue(); i++) {
                    int slotIndex = i + rawAmountOfSlots;
                    if (slotIndex < 36) {
                        layout.slot(slotIndex, (s, c) -> ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1,
                            Text.key("gui_sbmenu.bags.quiver.locked_slot"),
                            Text.keyLines("gui_sbmenu.bags.quiver.locked_slot.lore", entry.getKey().getDisplay())));
                    }
                }
                rawAmountOfSlots += entry.getValue();
            }
        }

        // Set up editable slots
        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
        for (int i = 0; i < amountOfSlots; i++) {
            int slotIndex = i;
            SkyBlockItem item = quiver.getInSlot(slotIndex);

            layout.editable(slotIndex, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                if (item == null) {
                    return ItemStack.builder(Material.AIR);
                } else {
                    return PlayerItemUpdater.playerUpdate(p, item.getItemStack());
                }
            }, (slot, oldItem, newItem, s) -> {
                SkyBlockItem newSkyBlockItem = new SkyBlockItem(newItem);
                if (!isItemAllowed(newSkyBlockItem)) {
                    ctx.player().sendMessage(Text.key("gui_sbmenu.bags.quiver.msg.cannot_put"));
                }
            });
        }
    }

    @Override
    public void onClose(QuiverState state, ViewContext ctx, ViewSession.CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        save(player, ctx.inventory(), getUnlockedSlots(player));
    }

    private int getUnlockedSlots(SkyBlockPlayer player) {
        int amountOfSlots = 0;
        for (Map.Entry<CustomCollectionAward, Integer> entry : SLOTS_PER_UPGRADE.entrySet()) {
            if (player.hasCustomCollectionAward(entry.getKey())) {
                amountOfSlots += entry.getValue();
            }
        }
        return amountOfSlots;
    }

    private boolean isItemAllowed(SkyBlockItem item) {
        if (item.isNA()) return true;
        if (item.getMaterial().equals(Material.AIR)) return true;
        return item.getMaterial() == Material.ARROW;
    }

    private void save(SkyBlockPlayer player, net.minestom.server.inventory.Inventory inventory, int slotToSaveUpTo) {
        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
        for (int i = 0; i < slotToSaveUpTo; i++) {
            SkyBlockItem item = new SkyBlockItem(inventory.getItemStack(i));
            if (item.isNA()) {
                quiver.getQuiverMap().remove(i);
            } else {
                quiver.getQuiverMap().put(i, item);
            }
        }
    }

    public record QuiverState(int placeholder) {}
}
