package net.swofty.type.murdermysterygame.weapon;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

public class WeaponManager {
    private final Game game;

    public WeaponManager(Game game) {
        this.game = game;
    }

    public void giveMurdererKnife(MurderMysteryPlayer player) {
        ItemStack knife = ItemStacks.raw(Material.IRON_SWORD, """
                        <c>Murderer's Knife
                        <7>Right-click to throw
                        <7>Left-click for melee attack
                        """)
                .build();
        player.getInventory().addItemStack(knife);
    }

    public void giveDetectiveBow(MurderMysteryPlayer player) {
        ItemStack bow = ItemStacks.raw(Material.BOW, """
                        <9>Detective's Bow
                        <7>One shot kill
                        <7>Use it wisely!
                        """)
                .build();
        ItemStack arrow = ItemStack.of(Material.ARROW, 1);

        player.getInventory().addItemStack(bow);
        player.getInventory().addItemStack(arrow);
    }

    public void giveInnocentBow(MurderMysteryPlayer player) {
        ItemStack bow = ItemStacks.raw(Material.BOW, """
                        <a>Bow
                        <7>Collected enough gold!
                        <7>One shot - make it count!
                        """)
                .build();
        ItemStack arrow = ItemStack.of(Material.ARROW, 1);

        player.getInventory().addItemStack(bow);
        player.getInventory().addItemStack(arrow);

        player.sendMessage("<a>You collected enough gold for a bow!");
    }
}
