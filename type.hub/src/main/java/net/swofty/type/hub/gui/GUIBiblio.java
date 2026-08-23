package net.swofty.type.hub.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.Wiki;

public class GUIBiblio extends HypixelInventoryGUI {
	public GUIBiblio() {
		super("Skyblock Wiki", InventoryType.CHEST_4_ROW);
	}

	@Override
	public void onOpen(InventoryGUIOpenEvent e) {
		fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
		set(GUIClickableItem.getCloseItem(31));
		set(new GUIClickableItem(11) {
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				player.closeInventory();
				Wiki.wiki(player);
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				return ItemStacks.item(Material.PAINTING, """
						<d>Wiki Command
						<7>Visit the Wiki using <a>/wiki </a>and browse
						<7>the many pages and utilities.

						<7>You can also specify an extra
						<7>argument when using <6>/wiki \\<id> </6>to
						<7>search via an item ID.

						<e>Click to visit the Wiki!""");
			}
		});
		set(new GUIClickableItem(13) {
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				return ItemStacks.item(Material.WRITABLE_BOOK, """
						<6>The Skyblock Wiki
						<7>The newly finished <a>Official SkyBlock
						<a>Wiki <7>has launched and contains lots
						<7>of useful information on items, mobs,
						<7>drop rates, areas, trivia, and more.
						<7>This is a <6>Hypixel-led</6>, <d>community
						<d>maintained <7>Wiki which aims to provide
						<7>the most accurate information in the
						<7>best way possible.
						<8>Edits
						 <6>> 100,000+

						<8>Pages
						 <d>> 27,000+

						<8>Files
						 <a>> 15,000+

						<e>See more @ <f>wiki.hypixel.net""");
			}
		});
		set(new GUIClickableItem(15) {
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				player.closeInventory();
				Wiki.wikiThis(player);
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				return ItemStacks.item(Material.OAK_SIGN, """
						<a>Wikithis Command
						<7>Want to view more information about
						<7>the item you are currently <d>holding </d>?
						<7>Then this is the command for <e>you</e>!

						<7>Running <6>/wikithis </6>whilst <a>holding an
						<a>item <7>will attempt to find a Wiki page
						<7>for the item and then link you to it
						<7>in-game.

						<e>Click to search your held item!""");
			}
		});
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
