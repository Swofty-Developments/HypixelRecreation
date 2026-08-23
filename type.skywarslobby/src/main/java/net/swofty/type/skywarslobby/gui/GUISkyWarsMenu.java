package net.swofty.type.skywarslobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarslobby.level.SkywarsLevelCategory;
import net.swofty.type.skywarslobby.level.SkywarsLevelRegistry;

import java.util.List;

/**
 * Main SkyWars Menu GUI accessible from the hotbar emerald item.
 */
public class GUISkyWarsMenu extends HypixelInventoryGUI {

    public GUISkyWarsMenu() {
        super("SkyWars Menu", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        long playerXP = handler != null ? handler.get(SkywarsDataHandler.Data.EXPERIENCE, DatapointLong.class).getValue() : 0;
        int playerLevel = SkywarsLevelRegistry.calculateLevel(playerXP);
        long souls = handler != null ? handler.get(SkywarsDataHandler.Data.SOULS, DatapointLong.class).getValue() : 0;

        // Kits & Perks (slot 10)
        set(new GUIClickableItem(10) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ENDER_EYE, 1, """
                        <a>Kits & Perks
                        <7>Change the way you play by picking
                        <7>kits and perks!

                        <7>Win kits and perks in the <b>Soul Well</b> or
                        <7>buy them directly using <6>coins</6>.

                        <e>Click to browse!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIKitsPerks().open(player);
            }
        });

        // My Cosmetics (slot 11) - placeholder for now
        set(new GUIClickableItem(11) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARMOR_STAND, 1, """
                        <a>My Cosmetics
                        <7>Browse and equip all the available
                        <7>in-game SkyWars cosmetics.

                        <e>Click to browse!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.sendMessage("<c>Cosmetics browser coming soon!");
            }
        });

        // SkyWars Level Progression (slot 12)
        set(new GUIClickableItem(12) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                // Get progress info for display
                long xpProgress = SkywarsLevelRegistry.getXPIntoCurrentLevel(playerXP);
                long xpNeeded = SkywarsLevelRegistry.getXPForNextLevel(playerXP);
                double progress = SkywarsLevelRegistry.getProgressToNextLevel(playerXP);
                int filled = (int) (progress * 10);
                int nextLevel = Math.min(playerLevel + 1, SkywarsLevelRegistry.getMaxLevel());

                // Get current and next prestige info
                SkywarsLevelCategory.SkywarsLevel currentPrestige = SkywarsLevelRegistry.getCurrentPrestige(playerXP);
                Text currentEmblem = currentPrestige != null
                        ? currentPrestige.getEmblem() : Text.of("<7>[1✯]");

                SkywarsLevelCategory.SkywarsLevel nextLevelData = SkywarsLevelRegistry.getLevel(nextLevel);
                Text nextEmblem = nextLevelData != null
                        ? Text.of("<f>[{}✯]", nextLevel) : Text.empty();

                Text progressBar = Text.of("<8>[<b>{}<7>{}<8>]",
                        "■".repeat(filled), "■".repeat(10 - filled));

                return ItemStacks.item(Material.NETHER_STAR, 1,
                        Text.of("<d>SkyWars Level Progression"), List.of(
                                Text.of("<7>View information about your SkyWars"),
                                Text.of("<7>Level progression, select your"),
                                Text.of("<7>Emblem, and view level rewards."),
                                Text.empty(),
                                Text.of("<7>Progress: <b>{}<7>/<a>{}",
                                        SkywarsLevelCategory.formatXPRequirement(xpProgress),
                                        SkywarsLevelCategory.formatXPRequirement(xpNeeded)),
                                Text.of("{} {} {}", currentEmblem, progressBar, nextEmblem),
                                Text.empty(),
                                Text.of("<e>Click to view!")
                        ));
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUISkyWarsLevelProgression().open(player);
            }
        });

        // Soul Well (slot 14)
        set(new GUIClickableItem(14) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.END_PORTAL_FRAME, 1, """
                        <b>Soul Well
                        <7>Test your luck by spending <b>Souls</b> to
                        <7>earn random Kits and Perks!

                        <b>Souls <7>are earned by killing players
                        <7>in games of SkyWars!

                        <7>Your Souls: <b>{}

                        <e>Click to open!""", souls);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.openView(new GUISoulWell());
            }
        });

        // Angel's Descent (slot 15) - locked until level 15
        set(new GUIItem(15) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                if (playerLevel >= 15) {
                    return ItemStacks.item(Material.FEATHER, 1, """
                            <b>Angel's Descent
                            <7>Spend <9>Opals</9> to unlock exclusive
                            <7>perks, upgrades, kits, and cosmetics!

                            <7>Large amounts of <b>Souls</b> can be
                            <7>fused into <9>Opals</9> at the <5>Fallen Forge</5>.

                            <e>Click to enter!""");
                } else {
                    return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1, """
                            <b>Angel's Descent
                            <7>Spend <9>Opals</9> to unlock exclusive
                            <7>perks, upgrades, kits, and cosmetics!

                            <7>Large amounts of <b>Souls</b> can be
                            <7>fused into <9>Opals</9> at the <5>Fallen Forge</5>.

                            <c><l>!! Requires SkyWars Level 15!""");
                }
            }
        });

        // Angel's Brewery (slot 16) - locked until level 25
        set(new GUIItem(16) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                if (playerLevel >= 25) {
                    return ItemStacks.item(Material.BREWING_STAND, 1, """
                            <c>Angel's Brewery
                            <7>Brew Potions using <6>Coins</6> and <9>Opals
                            <7>which grant buffs for the next <a>50
                            <7>games you play.

                            <e>Click to brew!""");
                } else {
                    return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1, """
                            <c>Angel's Brewery
                            <7>Brew Potions using <6>Coins</6> and <9>Opals
                            <7>which grant buffs for the next <a>50
                            <7>games you play.

                            <c><l>!! Requires SkyWars Level 25!""");
                }
            }
        });

        // Close button (slot 31)
        set(GUIClickableItem.getCloseItem(31));

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
