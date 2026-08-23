package net.swofty.type.dwarvenmines.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.dwarvenmines.commission.CommissionMilestone;
import net.swofty.type.dwarvenmines.commission.Commissions;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCommissions;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICommissionMilestones extends HypixelInventoryGUI {

	private static final int[] MILESTONE_SLOTS = {19, 20, 21, 23, 24, 25};

	public GUICommissionMilestones() {
		super("<e>Commission Milestones", InventoryType.CHEST_6_ROW);
	}

	@Override
	public void onOpen(InventoryGUIOpenEvent e) {
		fill(FILLER_ITEM);

		SkyBlockPlayer player = (SkyBlockPlayer) getPlayer();
		DatapointCommissions.PlayerCommissionData commissionData = Commissions.getCommissionData(player);
		int totalCompleted = commissionData.getTotalCompleted();
		int currentTier = commissionData.getMilestoneTier();

		set(new GUIItem(4) {
			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				List<Text> lore = new ArrayList<>();
				lore.add(Text.of("<7>View milestone progress and rewards!"));
				lore.add(Text.empty());

				CommissionMilestone nextMilestone = null;
				for (CommissionMilestone m : CommissionMilestone.values()) {
					if (totalCompleted < m.getCommissionsRequired()) {
						nextMilestone = m;
						break;
					}
				}

				if (nextMilestone != null) {
					double progress = commissionData.getMilestoneProgress(nextMilestone.getTier());
					lore.add(Text.of("<7>Progress to milestone {:roman}: <e>{}<6>%",
							nextMilestone.getTier(), String.format("%.1f", progress)));
					lore.add(buildProgressBar(progress).append("<e>{}<6>/<e>{}",
							totalCompleted, nextMilestone.getCommissionsRequired()));
					lore.add(Text.empty());
					lore.add(Text.of("<7>Tier {:roman} Rewards:", nextMilestone.getTier()));
					for (Text reward : nextMilestone.getRewardDescriptions()) {
						lore.add(Text.of("  {}", reward));
					}
				} else {
					lore.add(Text.of("<a><l>ALL MILESTONES COMPLETED!"));
					lore.add(Text.empty());
					lore.add(Text.of("<7>Total Commissions: <e>{}", totalCompleted));
				}

				return ItemStacks.item(
						Material.FILLED_MAP,
						1,
						Text.of("<e>Commission Milestones"),
						lore
				);
			}
		});

		for (int i = 0; i < CommissionMilestone.values().length; i++) {
			CommissionMilestone milestone = CommissionMilestone.values()[i];
			int slot = MILESTONE_SLOTS[i];

			set(new GUIClickableItem(slot) {
				@Override
				public void run(InventoryPreClickEvent e, HypixelPlayer player) {
					SkyBlockPlayer sbPlayer = (SkyBlockPlayer) player;
					DatapointCommissions.PlayerCommissionData data = Commissions.getCommissionData(sbPlayer);

					if (data.getTotalCompleted() >= milestone.getCommissionsRequired()
							&& !data.isMilestoneClaimed(milestone.getTier())) {
						claimMilestone(sbPlayer, milestone, data);
						new GUICommissionMilestones().open(sbPlayer);
					}
				}

				@Override
				public ItemStack.Builder getItem(HypixelPlayer player) {
					SkyBlockPlayer sbPlayer = (SkyBlockPlayer) player;
					DatapointCommissions.PlayerCommissionData data = Commissions.getCommissionData(sbPlayer);

					boolean reached = data.getTotalCompleted() >= milestone.getCommissionsRequired();
					boolean claimed = data.isMilestoneClaimed(milestone.getTier());
					double progress = data.getMilestoneProgress(milestone.getTier());

					List<Text> lore = new ArrayList<>();
					lore.add(Text.empty());
					lore.add(reached
							? Text.of("<7>Progress: <a>100%")
							: Text.of("<7>Progress: <e>{}<6>%", String.format("%.1f", progress)));

					lore.add(buildProgressBar(progress).append("<e>{}<6>/<e>{}",
							data.getTotalCompleted(), milestone.getCommissionsRequired()));
					lore.add(Text.empty());
					lore.add(Text.of("<7>Rewards:"));

					for (Text reward : getMilestoneRewardLines(milestone)) {
						if (claimed) {
							lore.add(Text.of("<a> ✔ {}", reward));
						} else {
							lore.add(Text.of("  {}", reward));
						}
					}

					lore.add(Text.empty());
					if (claimed) {
						lore.add(Text.of("<a><l>CLAIMED"));
					} else if (reached) {
						lore.add(Text.of("<e><l>CLICK TO CLAIM!"));
					}

					Material material;
					String title;
					if (claimed) {
						material = Material.LIME_STAINED_GLASS_PANE;
						title = "<a>Milestone {:roman} Rewards";
					} else if (reached) {
						material = Material.YELLOW_STAINED_GLASS_PANE;
						title = "<e>Milestone {:roman} Rewards";
					} else {
						material = Material.RED_STAINED_GLASS_PANE;
						title = "<c>Milestone {:roman} Rewards";
					}

					return ItemStacks.item(
							material,
							milestone.getTier(),
							Text.of(title, milestone.getTier()),
							lore
					);
				}
			});
		}

		set(new GUIClickableItem(48) {
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer player) {
				new GUICommisions().open((SkyBlockPlayer) player);
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				return ItemStacks.item(Material.ARROW, """
						<a>Go Back
						<7>To Commissions""");
			}
		});

		set(GUIClickableItem.getCloseItem(49));
		updateItemStacks(getInventory(), getPlayer());
	}

	private void claimMilestone(SkyBlockPlayer player, CommissionMilestone milestone,
								DatapointCommissions.PlayerCommissionData data) {
		data.claimMilestone(milestone.getTier());
		Commissions.saveCommissionData(player, data);

		player.getSkills().increase(player, SkillCategories.MINING, (double) milestone.getMiningXpReward());
		player.getExperienceHandler().addExperience(milestone.getSkyBlockXpReward());


		player.sendMessage("<6><l> MILESTONE CLAIMED! <r><e>Tier {:roman}", milestone.getTier());
		player.sendMessage("<7>Rewards:");
		player.sendMessage("<7> - <3>+{:,} Mining XP", milestone.getMiningXpReward());
		player.sendMessage("<7> - <b>+{} SkyBlock XP", milestone.getSkyBlockXpReward());

		if (milestone.isUnlocksEmissaries()) {
			player.addAndUpdateItem(ItemType.ROYAL_COMPASS);
		}
		if (milestone.isUnlocksExtraSlot()) {
		}
		if (milestone.isUnlocksDwarvenMinesScroll()) {
			// TODO: Give Travel Scroll item
		}
		if (milestone.isUnlocksRoyalPigeon()) {
			player.addAndUpdateItem(ItemType.ROYAL_PIGEON);
		}
		if (milestone.isUnlocksCrystalNucleusScroll()) {
			// TODO: Give Travel Scroll item
		}
	}

	private List<Text> getMilestoneRewardLines(CommissionMilestone milestone) {
		List<Text> rewards = new ArrayList<>();

		if (milestone.isUnlocksEmissaries()) {
			rewards.add(Text.of("<6>Emissaries"));
			rewards.add(Text.of("<9>Royal Compass"));
		}
		if (milestone.isUnlocksExtraSlot()) {
			rewards.add(Text.of("<a>+1 Commission Slot"));
		}
		if (milestone.isUnlocksDwarvenMinesScroll()) {
			rewards.add(Text.of("<9>Travel Scroll to Dwarven Mines"));
		}
		if (milestone.isUnlocksRoyalPigeon()) {
			rewards.add(Text.of("<6>Royal Pigeon"));
		}
		if (milestone.isUnlocksCrystalNucleusScroll()) {
			rewards.add(Text.of("<9>Travel Scroll to the Crystal Nucleus"));
		}

		rewards.add(Text.of("<8>+<3>{:,} <7>Mining Experience", milestone.getMiningXpReward()));
		rewards.add(Text.of("<8>+<b>{} SkyBlock XP", milestone.getSkyBlockXpReward()));

		return rewards;
	}

	private Text buildProgressBar(double percentage) {
		int filled = (int) (percentage / 5); // 20 segments total
		int empty = 20 - filled;
		return Text.of("<l><m><2>{}<f>{} ",
				" ".repeat(Math.max(0, filled)),
				" ".repeat(Math.max(0, empty)));
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
