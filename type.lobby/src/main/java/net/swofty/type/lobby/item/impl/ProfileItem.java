package net.swofty.type.lobby.item.impl;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.network.player.ResolvableProfile;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.gui.GUIMyProfile;
import net.swofty.type.lobby.item.LobbyItem;

import java.util.Arrays;

public class ProfileItem extends LobbyItem {

    public ProfileItem() {
        super("profile");
    }

    @Override
    public ItemStack getBlandItem() {
        return null;
    }

    @Override
    public ItemStack getItemStack(HypixelPlayer... p) {
        HypixelPlayer player = Arrays.stream(p).findFirst().orElseThrow();
        return ItemStacks.named(Material.PLAYER_HEAD, "<a>My Profile <7>(Right Click)")
                .set(DataComponents.PROFILE, new ResolvableProfile(player.getPlayerSkin()))
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
        new GUIMyProfile().open((HypixelPlayer) event.getPlayer());
    }
}
