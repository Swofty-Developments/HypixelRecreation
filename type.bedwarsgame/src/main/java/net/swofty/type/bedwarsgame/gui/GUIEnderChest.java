package net.swofty.type.bedwarsgame.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GUIEnderChest implements StatefulView<GUIEnderChest.State> {

    @Override
    public State initialState() {
        return new State();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Ender Chest", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        BedWarsPlayer player = (BedWarsPlayer) ctx.player();
        BedWarsGame game = player.getGame();

        for (int slot = 0; slot < InventoryType.CHEST_3_ROW.getSize(); slot++) {
            int targetSlot = slot;
            layout.editable(slot, (s, c) -> {
                if (game == null) {
                    return ItemStack.AIR.builder();
                }
                Map<Integer, ItemStack> enderChest = game.getEnderChests().get(player.getUuid());
                if (enderChest == null) {
                    return ItemStack.AIR.builder();
                }
                return enderChest.getOrDefault(targetSlot, ItemStack.AIR).builder();
            }, (changedSlot, oldItem, newItem, s) -> {
            });
        }

        layout.allowHotkey(true);
    }

    @Override
    public boolean onBottomClick(ClickContext<State> click, ViewContext ctx) {
        return true;
    }

    @Override
    public void onClose(State state, ViewContext ctx, ViewSession.CloseReason reason) {
        BedWarsPlayer player = (BedWarsPlayer) ctx.player();
        BedWarsGame game = player.getGame();
        if (game == null) {
            return;
        }

        Map<Integer, ItemStack> enderChest = new ConcurrentHashMap<>();
        for (int slot = 0; slot < ctx.inventory().getSize(); slot++) {
            ItemStack item = ctx.inventory().getItemStack(slot);
            if (!item.isAir()) {
                enderChest.put(slot, item);
            }
        }

        game.getEnderChests().put(player.getUuid(), enderChest);
    }

    public record State() {
    }
}
