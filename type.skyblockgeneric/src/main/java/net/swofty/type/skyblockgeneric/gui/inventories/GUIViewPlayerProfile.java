package net.swofty.type.skyblockgeneric.gui.inventories;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIViewPlayerProfile extends HypixelInventoryGUI {
	private final SkyBlockPlayer viewedPlayer;

	public GUIViewPlayerProfile(SkyBlockPlayer viewedPlayer) {
		super(Text.key("gui_profile.title", viewedPlayer.getUsername()), InventoryType.CHEST_6_ROW);
		this.viewedPlayer = viewedPlayer;
	}

	@Override
	public void onOpen(InventoryGUIOpenEvent e) {
		fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
		set(GUIClickableItem.getCloseItem(49));

		set(new GUIItem(2) { //Held Item
			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				if (!viewedPlayer.getItemInMainHand().isAir()) {
					return ItemStacks.copy(viewedPlayer.getItemInMainHand());
				} else {
					return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, Text.key("gui_profile.empty_held_item"), List.of());
				}
			}
		});
		set(new GUIItem(11) { //Helmet
			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				if (!viewedPlayer.getHelmet().isAir()) {
					return ItemStacks.copy(viewedPlayer.getHelmet());
				} else {
					return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, Text.key("gui_profile.empty_helmet"), List.of());
				}
			}
		});
		set(new GUIItem(20) { //Chestplate
			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				if (!viewedPlayer.getChestplate().isAir()) {
					return ItemStacks.copy(viewedPlayer.getChestplate());
				} else {
					return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, Text.key("gui_profile.empty_chestplate"), List.of());
				}
			}
		});
		set(new GUIItem(29) { //Leggings
			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				if (!viewedPlayer.getLeggings().isAir()) {
					return ItemStacks.copy(viewedPlayer.getLeggings());
				} else {
					return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, Text.key("gui_profile.empty_leggings"), List.of());
				}
			}
		});
		set(new GUIItem(38) { //Boots
			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				if (!viewedPlayer.getBoots().isAir()) {
					return ItemStacks.copy(viewedPlayer.getBoots());
				} else {
					return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, Text.key("gui_profile.empty_boots"), List.of());
				}
			}
		});
		set(new GUIItem(47) { //Pet
			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				if (viewedPlayer.getPetData().getEnabledPet() != null && !viewedPlayer.getPetData().getEnabledPet().getItemStack().isAir()) {
					SkyBlockItem pet = viewedPlayer.getPetData().getEnabledPet();
					return new NonPlayerItemUpdater(pet).getUpdatedItem();
				} else {
					return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, Text.key("gui_profile.empty_pet"), List.of());
				}
			}
		});
		set(new GUIItem(22) { //Player Stats
			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				SkyBlockDataHandler dataHandler = viewedPlayer.getSkyblockDataHandler();
				String age = StringUtility.profileAge(System.currentTimeMillis() - dataHandler.get(SkyBlockDataHandler.Data.CREATED, DatapointLong.class).getValue());
				List<Text> lore = new ArrayList<>();

				lore.add(Text.of("<7> "));
				lore.add(Text.key("gui_profile.skyblock_level", Text.parse(
						viewedPlayer.getSkyBlockExperience().getLevel().getColor()
								+ viewedPlayer.getSkyBlockExperience().getLevel().toString())));
				lore.add(Text.of("<7> "));
				lore.add(Text.key("gui_profile.oldest_profile", age));

				return ItemStacks.head(PlayerSkin.fromUuid(viewedPlayer.getUuid().toString()), 1,
						Text.literal(viewedPlayer.getShortenedDisplayName()),
						lore);
			}
		});
		set(new GUIClickableItem(31) { //Emblem
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				player.sendMessage(Text.key("gui_profile.feature_not_added"));
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				List<Text> lore = new ArrayList<>();
				SkyBlockDataHandler dataHandler = viewedPlayer.getSkyblockDataHandler();
				Text name;
				Material material;
				if (dataHandler.get(SkyBlockDataHandler.Data.SKYBLOCK_EXPERIENCE, DatapointSkyBlockExperience.class).getValue().getCurrentEmblem() != null) {
					name = Text.key("gui_profile.emblem_selected", dataHandler.get(SkyBlockDataHandler.Data.SKYBLOCK_EXPERIENCE, DatapointSkyBlockExperience.class).getValue().getCurrentEmblem().toString());
					material = dataHandler.get(SkyBlockDataHandler.Data.SKYBLOCK_EXPERIENCE, DatapointSkyBlockExperience.class).getValue().getEmblem().displayMaterial();
				} else {
					name = Text.key("gui_profile.no_emblem");
					material = Material.BARRIER;
					lore.addAll(Text.keyLines("gui_profile.no_emblem.lore"));
				}
				lore.add(Text.literal(" "));
				lore.add(Text.key("gui_profile.click_view_emblems"));
				return ItemStacks.item(material, 1, name, lore);
			}
		});
        set(new GUIClickableItem(15) { // Visit Island
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				player.sendMessage(Text.key("gui_profile.feature_not_added"));
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				return ItemStacks.item(Material.FEATHER, 1, Text.key("gui_profile.visit_island"),
						List.of(Text.key("gui_profile.visit_island.lore")));
			}
		});
		set(new GUIClickableItem(16) { //Trade Request
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				player.sendMessage(Text.key("gui_profile.feature_not_added"));
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				return ItemStacks.item(Material.EMERALD, 1, Text.key("gui_profile.trade_request"),
						List.of(Text.key("gui_profile.trade_request.lore")));
			}
		});
		set(new GUIClickableItem(24) { //Invite to Island
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				player.sendMessage(Text.key("gui_profile.feature_not_added"));
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				return ItemStacks.item(Material.POPPY, 1, Text.key("gui_profile.invite_to_island"),
						List.of(Text.key("gui_profile.invite_to_island.lore")));
			}
		});
		set(new GUIClickableItem(25) { //Coop Request
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				player.sendMessage(Text.key("gui_profile.feature_not_added"));
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				return ItemStacks.item(Material.DIAMOND, 1, Text.key("gui_profile.coop_request"),
						List.of(Text.key("gui_profile.coop_request.lore")));
			}
		});
		set(new GUIClickableItem(33) { //Personal Vault
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				player.sendMessage(Text.key("gui_profile.feature_not_added"));
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				return ItemStacks.item(Material.ENDER_CHEST, 1, Text.key("gui_profile.personal_vault"),
						List.of(Text.key("gui_profile.personal_vault.lore")));
			}
		});
		set(new GUIClickableItem(34) { //Museum
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				player.sendMessage(Text.key("gui_profile.feature_not_added"));
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer p) {
				SkyBlockDataHandler dataHandler = viewedPlayer.getSkyblockDataHandler();
				String profileName = dataHandler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue();
				return ItemStacks.head(PlayerSkin.fromUuid(viewedPlayer.getUuid().toString()), 1,
						Text.of("{}'s Museum", viewedPlayer.getUsername()),
						Text.keyLines("gui_profile.museum.lore", profileName));
			}
		});
		updateItemStacks(getInventory(), getPlayer());
	}

	@Override
	public boolean allowHotkeying() {
		return false;
	}

	@Override
	public void onClose(InventoryCloseEvent e, CloseReason reason) {
	}

	@Override
	public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
	}

	@Override
	public void onBottomClick(InventoryPreClickEvent e) {
		e.setCancelled(true);
	}
}
