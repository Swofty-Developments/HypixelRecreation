package net.swofty.type.lobby.item.impl;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.item.LobbyItem;
import net.swofty.type.lobby.visibility.PlayerVisibilityManager;

import java.util.Arrays;

public class HidePlayers extends LobbyItem {

    public HidePlayers() {
        super("hide_players");
    }

    @Override
    public ItemStack getBlandItem() {
        return null;
    }

    @Override
    public ItemStack getItemStack(HypixelPlayer... p) {
        HypixelPlayer player = Arrays.stream(p).findFirst().orElseThrow();
        boolean showPlayers = player.getToggles().get(DatapointToggles.Toggles.ToggleType.LOBBY_SHOW_PLAYERS);

        Material toggleMaterial = showPlayers ? Material.LIME_DYE : Material.GRAY_DYE;
        Text toggleLabel = showPlayers ? Text.of("<a>Shown") : Text.of("<c>Hidden");

        return ItemStacks.item(toggleMaterial, "<f>Players: {} <7>(Right Click)\n<7>Right-click to toggle player visibility!", toggleLabel)
                .set(DataComponents.CUSTOM_DATA, new CustomData(CompoundBinaryTag.builder().putString("item", this.getId()).build())).build();
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
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        PlayerVisibilityManager.toggleVisibility(player);

        if (event instanceof PlayerUseItemOnBlockEvent e) {
            player.setItemInHand(e.getHand(), getItemStack(player));
        } else if (event instanceof PlayerUseItemEvent e) {
            player.setItemInHand(e.getHand(), getItemStack(player));
        }
    }
}
