package net.swofty.type.generic.gui;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import net.swofty.type.generic.gui.inventory.Inventories;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HypixelAnvilGUI {
    public static Map<HypixelPlayer, Map.Entry<String, CompletableFuture<String>>> anvilGUIs = new HashMap<>();
    private final HypixelPlayer player;

    public HypixelAnvilGUI(HypixelPlayer player) {
        this.player = player;
    }

    public CompletableFuture<String> open(String text) {
        Inventory inventory = Inventories.of(InventoryType.ANVIL, "Insert Data: {}", text);
        inventory.setItemStack(0, ItemStacks.named(Material.PAPER, "").build());
        inventory.setItemStack(1, ItemStack.of(Material.AIR));
        inventory.setItemStack(2, ItemStacks.named(Material.PAPER, "").build());

        player.openInventory(inventory);

        player.sendPacket(new WindowPropertyPacket(player.getOpenInventory().getWindowId(), (short) 0, (short) 0));

        CompletableFuture<String> future = new CompletableFuture<>();
        anvilGUIs.put(player, Map.entry(text, future));
        return future;
    }
}
