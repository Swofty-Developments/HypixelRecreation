package net.swofty.type.skyblockgeneric.gui.inventories.abiphone;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;


public class GUIContactManagement extends HypixelInventoryGUI {

	private final SkyBlockItem abiphone;
	private final AbiphoneNPC npc;

	public GUIContactManagement(SkyBlockItem abiphone, AbiphoneNPC npc) {
		super(Text.key("gui_abiphone.management.title"), InventoryType.CHEST_6_ROW);
		this.abiphone = abiphone;
		this.npc = npc;
	}

	@Override
	public void onOpen(InventoryGUIOpenEvent e) {
		fill(FILLER_ITEM);

		set(new GUIItem(4) {
			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				return ItemStacks.lore(
						ItemStacks.name(npc.getIcon(), "<f>{}", npc.getName()),
						"<7>{}", npc.getDescription()
				);
			}
		});

		set(new GUIClickableItem(31) {
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer player) {
				new GUIConfirmAbiphone(npc, () -> {
					abiphone.getAttributeHandler().removeAbiphoneNPC(npc);
					player.closeInventory();
					new GUIAbiphone(abiphone).open(player);
					player.sendMessage(Text.key("gui_abiphone.management.removed_message", npc.getName()));
				}).open(player);
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				return ItemStacks.item(Material.FEATHER, 1, Text.key("gui_abiphone.management.remove_contact"),
						Text.keyLines("gui_abiphone.management.remove_contact.lore"));
			}
		});

		GUIClickableItem.getGoBackItem(48, new GUIAbiphone(abiphone));
		GUIClickableItem.getCloseItem(49);
		updateItemStacks(getInventory(), getPlayer());
	}

	@Override
	public boolean allowHotkeying() {
		return false;
	}

	@Override
	public void onBottomClick(InventoryPreClickEvent e) {

	}
}
