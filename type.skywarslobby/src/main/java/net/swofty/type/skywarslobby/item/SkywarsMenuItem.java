package net.swofty.type.skywarslobby.item;

import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.item.LobbyItem;
import net.swofty.type.skywarslobby.gui.GUISkyWarsMenu;

import java.util.function.Consumer;

/**
 * SkyWars Menu hotbar item (emerald in slot 2).
 * Opens the main SkyWars Menu when right-clicked.
 */
public class SkywarsMenuItem extends LobbyItem {

    private final Consumer<HypixelPlayer> onInteract;

    public SkywarsMenuItem(Consumer<HypixelPlayer> onInteract) {
        super("skywars_menu");
        this.onInteract = onInteract;
    }

    public SkywarsMenuItem() {
        this(player -> new GUISkyWarsMenu().open(player));
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.EMERALD, 1, """
                <a>SkyWars Menu <7>(Right Click)
                <7>Right Click to open the SkyWars Menu!""").build();
    }

    @Override
    public void onItemDrop(ItemDropEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        if (event instanceof CancellableEvent cancellable) {
            cancellable.setCancelled(true);
        }
        onInteract.accept((HypixelPlayer) event.getPlayer());
    }
}
