package net.swofty.type.lobby.item.impl;

import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.gui.GUIGameMenu;
import net.swofty.type.lobby.item.LobbyItem;

import java.util.function.Consumer;

public class PlayCompass extends LobbyItem {

    private final Consumer<HypixelPlayer> onInteract;

    public PlayCompass(Consumer<HypixelPlayer> onInteract) {
        super("play_compass");
        this.onInteract = onInteract;
    }

    public PlayCompass() {
        this(player -> player.openView(new GUIGameMenu()));
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.COMPASS, """
                <a>Game Menu <7>(Right Click)
                <7>Right Click to bring up the Game Menu!""").build();
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
