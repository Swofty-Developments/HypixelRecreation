package net.swofty.type.dwarvenmines.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIGuide extends HypixelInventoryGUI {

    public GUIGuide() {
        super("Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(FILLER_ITEM);
        set(new GUIItem(11) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.PRISMARINE_CRYSTALS, """
                        <a>Mithril <8>and <9>᠅ Powder
                        <8> ■ <7>All of the veins on this island are
                            <7>mineable.
                        <8> ■ <7>The center of <f>Mithril <7>veins are more
                            <7>pure and thus drop more Mithril,
                            <7>while the outer edges of the vein
                            <7>drop the least!
                        <8> ■ <7>Mining Mithril is the main source of
                            <7>gaining <2>Mithril Powder<7>, which is useful
                            <7>in the <5>Heart of the Mountain<7>.""");
            }
        });
        set(new GUIItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.WRITABLE_BOOK, """
                        <6>Commissions
                        <8> ■ <7>The <6><l>King </l><7>can be found in the <b>Royal
                            <b>Palace<7>. He's pretty famous around
                            <7>here.
                        <8> ■ <7>If you do <9>Commissions <7>for the King
                            <7>you will be rewarded with <5>HOTM Exp<7>!
                        <8> ■ <7>You can always see your active
                            <7>Commissions in the <a>tab list<7>.
                        <8> ■ <7>When you are done with a Commission,
                            <7>talk to the King to get new ones!
                        <8> ■ <7>Your first <a>4 <7>Commissions of the day
                            <7>reward a lot more HOTM Exp!""");
            }
        });
        set(new GUIItem(15) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("86f06eaa3004aeed09b3d5b45d976de584e691c0e9cade133635de93d23b9edb", """
                        <5>Heart of the Mountain
                        <8> ■ <7>You can level up your <5>Heart of the
                            <5>Mountain <7>by doing <9>Commissions<7>.
                        <8> ■ <7>Spend your <5>Token of the Mountain
                            <7>wisely to unlock <a>Perks <7>and <a>Pickaxe
                            <a>Abilities<7>!
                        <8> ■ <7>Most of these Perks can be
                            <7>upgraded using <9>Powder<7>! Some are
                            <7>only one-time unlocks.
                        <8> ■ <7>If you are not happy with your
                            <7>setup, you can reset your HOTM for
                            <7>a price.""");
            }
        });
        set(new GUIItem(28) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DIAMOND_PICKAXE, """
                        <a>Mining
                        <8> ■ <7>Your <6>⸕ Mining Speed <7>stat increases
                            <7>the speed at which you can mine.
                            <7>Increase your <6>⸕ Mining Speed <7>by
                            <7>using better pickaxes or <a>Drills<7>.
                        <8> ■ <7>Certain ores are tougher, requiring
                            <7>pickaxes with a higher <a>Breaking
                            <a>Power <7>to mine!
                        <8> ■ <7>Talk to <5>Bubu <7>at the nearby shop to
                            <7>buy your first <a>Mithril Pickaxe<7>!
                        <8> ■ <7>If you want better pickaxes with
                            <7>higher <6>⸕ Mining Speed<7>, you will need
                            <7>to unlock the <6>Forge <7>down the road.""");
            }
        });
        set(new GUIItem(30) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.FURNACE, """
                        <6>The Forge
                        <8> ■ <7>The <6>Forge <7>allows you to <a>smelt <7>and
                            <a>cast <7>higher quality items.
                        <8> ■ <7>You start with <a>2 <7>Forge slots and can
                            <7>currently unlock up to <a>7<7>!
                        <8> ■ <7>Forging takes time. You'll need to
                            <7>plan things out to get the most
                            <7>efficiency out of it.
                        <8> ■ <7>You can unlock new Forging options
                            <7>- including more slots and faster
                            <7>speeds - by leveling up your <5>HOTM.""");
            }
        });
        set(new GUIItem(32) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.MINECART, """
                        <b>Dwarven Rail Co.
                        <8> ■ <7>The brand new fast travel system
                            <7>that spans the whole of the <6>Dwarven
                            <6>Mines<7>.
                        <8> ■ <7>Many <b>Ticket Masters <7>now dot the
                            <7>cave and will offer to take you to
                            <7>different <a>stations <7>in return for
                            <6>coins<7>.
                        <8> ■ <7>The cost of a <2>journey <7>will vary
                            <7>depending on how far away you are
                            <7>from the <a>station<7>.
                        <8> ■ <7>Once you have paid a <8>Minecart <7>will
                            <7>appear on the tracks to take you to
                            <7>your destination's <a>station<7>.
                        <8> ■ <7>But be careful, the <6>Dwarves <7>run a
                            <7>tight schedule and if you miss your
                            <8>Minecart <7>it will leave without you. Oh
                            <7>and <c>no refunds<7>!""");
            }
        });
        set(new GUIItem(34) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("d159b03243be18a14f3eae763c4565c78f1f339a8742d26fde541be59b7de07", """
                        <5>Crystal Hollows
                        <8> ■ <7>The entrance to the <5>Crystal Hollows
                            <7>is a newly excavated area at the
                            <7>back of the <b>Dwarven Village
                            <7>accessible only via <8>Minecart<7>.
                        <8> ■ <7>Speak to <5>Gwendolyn <7>next to the
                            <7>entrance to gain access to the cave
                            <7>system!
                        <8> ■ <d>Gemstones <7>are a precious resource
                            <7>in the <5>Crystal Hollows <7>so be sure to
                            <7>be on the lookout for them whilst
                            <7>visiting to <b>upgrade <7>your gear at
                            <a>Geo's Shop<7>!
                        <8> ■ <7>Make sure to take lots of <6>Torches
                            <7>and watch out for <9>cave-ins <7>- they
                            <7>are incredibly <c>dangerous <7>and tend
                            <7>to happen every few hours.""");
            }
        });
        set(GUIClickableItem.getCloseItem(49));
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
