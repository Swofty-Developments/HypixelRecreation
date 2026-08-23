package net.swofty.type.skyblockgeneric.gui.inventories.abiphone;

import lombok.Setter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelPaginatedGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

import java.util.List;

public class GUIAbiphone extends HypixelPaginatedGUI<AbiphoneNPC> {
	@Setter
	private SortType sortType = SortType.ALPHABETICAL;
	private final SkyBlockItem abiphone;

	public GUIAbiphone(SkyBlockItem abiphone) {
		super(InventoryType.CHEST_6_ROW);
		this.abiphone = abiphone;
	}

	@Override
	protected int[] getPaginatedSlots() {
		return new int[]{
				10, 11, 12, 13, 14, 15, 16,
				19, 20, 21, 22, 23, 24, 25,
				28, 29, 30, 31, 32, 33, 34,
				37, 38, 39, 40, 41, 42, 43
		};
	}

	@Override
	protected PaginationList<AbiphoneNPC> fillPaged(HypixelPlayer player, PaginationList<AbiphoneNPC> paged) {
		paged.addAll(abiphone.getAttributeHandler().getAbiphoneNPCs());
		return paged;
	}

	@Override
	protected boolean shouldFilterFromSearch(String query, AbiphoneNPC item) {
		return false;
	}

	@Override
	protected void performSearch(HypixelPlayer player, String query, int page, int maxPage) {
		border(FILLER_ITEM);
		List<AbiphoneNPC> contacts = abiphone.getAttributeHandler().getAbiphoneNPCs();

		set(GUIClickableItem.getCloseItem(49));
		set(new GUIItem(50) {
			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				return ItemStacks.item(Material.HOPPER, 1, Text.key("gui_abiphone.sort_button"),
					Text.keyLines("gui_abiphone.sort_button.lore"));
			}
		});
		set(new GUIItem(51) {
			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				return ItemStacks.item(Material.BOOK, 1, Text.key("gui_abiphone.contacts_directory"),
					Text.keyLines("gui_abiphone.contacts_directory.lore",
						contacts.size(),
						AbiphoneRegistry.getRegisteredContactNPCs().size()));
			}
		});

		if (page > 1) {
			set(createNavigationButton(this, 45, query, page, false));
		}
		if (page < maxPage) {
			set(createNavigationButton(this, 53, query, page, true));
		}
	}

	@Override
	protected String getTitle(HypixelPlayer player, String query, int page, PaginationList<AbiphoneNPC> paged) {
		return abiphone.getCleanName();
	}

	@Override
	protected GUIClickableItem createItemFor(AbiphoneNPC npc, int slot, HypixelPlayer player) {
		return new GUIClickableItem(slot) {
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer player) {
				Click click = e.getClick();
				if (click instanceof Click.Left) {
					player.closeInventory();
					player.sendMessage(Text.key("gui_abiphone.ring_1"));
					MinecraftServer.getSchedulerManager().buildTask(() -> {
						player.sendMessage(Text.key("gui_abiphone.ring_2"));
						MinecraftServer.getSchedulerManager().buildTask(() -> {
							player.sendMessage(Text.key("gui_abiphone.ring_3"));
							MinecraftServer.getSchedulerManager().buildTask(() -> {
								npc.onCall(player);
							}).delay(TaskSchedule.seconds(1)).schedule();
						}).delay(TaskSchedule.seconds(1)).schedule();
					}).delay(TaskSchedule.seconds(1)).schedule();
				} else if (click instanceof Click.Right) {
					new GUIContactManagement(abiphone, npc).open(player);
				}
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				return ItemStacks.lore(
						ItemStacks.name(npc.getIcon(), "<f>{}", npc.getName()),
						List.of(
								Text.of("<7>{}", npc.getDescription()),
								Text.empty(),
								Text.key("gui_abiphone.contact_manage_hint"),
								Text.key("gui_abiphone.contact_call_hint")
						)
				);
			}

		};
	}

	@Override
	public boolean allowHotkeying() {
		return false;
	}

	@Override
	public void onBottomClick(InventoryPreClickEvent e) {
		e.setCancelled(true);
	}

	public enum SortType {
		FIRST_ADDED,
		ALPHABETICAL,
		LAST_CALLED,
		MOST_CALLED,
		DO_NOT_DISTURB_FIRST
	}
}
