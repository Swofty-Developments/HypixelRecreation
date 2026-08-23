package net.swofty.type.dwarvenmines.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.dwarvenmines.commission.Commission;
import net.swofty.type.dwarvenmines.commission.CommissionMilestone;
import net.swofty.type.dwarvenmines.commission.CommissionReward;
import net.swofty.type.dwarvenmines.commission.Commissions;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCommissions;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICommisions extends HypixelInventoryGUI {

	public GUICommisions() {
		super("Commissions", InventoryType.CHEST_4_ROW);
	}

	@Override
	public void onOpen(InventoryGUIOpenEvent e) {
		fill(FILLER_ITEM);

		SkyBlockPlayer player = (SkyBlockPlayer) getPlayer();
		DatapointCommissions.PlayerCommissionData commissionData = Commissions.getCommissionData(player);
		commissionData.checkAndResetDaily();

		if (Commissions.needsGeneration(player)) {
			Commissions.generateCommissions(player);
			commissionData = Commissions.getCommissionData(player); // refresh
		}

		int slotCount = commissionData.getCommissionSlots();
		int[] slots = Commissions.getGuiSlots(slotCount);

		List<DatapointCommissions.ActiveCommission> activeCommissions = commissionData.getActiveCommissions();

		DatapointHOTM.PlayerHOTMData hotmData = player.getSkyblockDataHandler()
				.get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).getValue();
		int hotmTier = hotmData.getTier();
		boolean isHotmMaxed = hotmData.isMaxed();
		int remainingDailyBonus = commissionData.getRemainingDailyBonus();

		for (int i = 0; i < slots.length && i < activeCommissions.size(); i++) {
			int index = i;
			DatapointCommissions.ActiveCommission activeCommission = activeCommissions.get(i);
			Commission commission = Commissions.getCommissionByName(activeCommission.getCommissionName());
			boolean willGetDailyBonus = index < remainingDailyBonus;

			set(new GUIClickableItem(slots[i]) {
				@Override
				public void run(InventoryPreClickEvent e, HypixelPlayer p) {
					SkyBlockPlayer player = (SkyBlockPlayer) p;
					DatapointCommissions.PlayerCommissionData commissionData = Commissions.getCommissionData(player);
					if (activeCommission.isCompleted() && !activeCommission.isClaimed()) {
						SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();

						DatapointHOTM.PlayerHOTMData hotmData =
								dataHandler.get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).getValue();

						int hotmTier = hotmData.getTier();
						boolean isHotmMaxed = hotmData.isMaxed();
						boolean isDailyBonus = commissionData.hasDailyBonus();

						CommissionReward reward = CommissionReward.calculate(hotmTier, isHotmMaxed, isDailyBonus);

						if (reward.hotmXp() > 0) {
							hotmData.addExperience(reward.hotmXp());
							dataHandler.get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).setValue(hotmData);
						}

						if (reward.mithrilPowder() > 0) {
							hotmData.addMithrilPowder(reward.mithrilPowder());
							dataHandler.get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).setValue(hotmData);
						}

						if (reward.miningXp() > 0) {
							player.getSkills().increase(player, SkillCategories.MINING, (double) reward.miningXp());
						}

						activeCommission.setClaimed(true);
						commissionData.completeCommission();
						Commissions.saveCommissionData(player, commissionData);
						Commissions.replaceCommissionAt(player, index);
						new GUICommisions().open(player);
					}
				}

				@Override
				public ItemStack.Builder getItem(HypixelPlayer player) {
					List<Text> lore = new ArrayList<>();
					lore.add(Text.of("<7>Commissions are tasks given directly"));
					lore.add(Text.of("<7>by the king which give bountiful"));
					lore.add(Text.of("<7>rewards."));
					lore.add(Text.literal(" "));

					if (commission != null) {
						lore.add(Text.of("<9>{}", commission.name));
						lore.add(Text.of("<7>{}", commission.generateDescription()));
					} else {
						lore.add(Text.of("<9>Unknown Commission"));
						lore.add(Text.of("<7>Complete this task for rewards."));
					}

					lore.add(Text.empty());
					lore.add(Text.of("<9>Rewards"));
					lore.addAll(CommissionReward.getRewardLore(hotmTier, isHotmMaxed, willGetDailyBonus));

					lore.add(Text.literal(" "));

					if (activeCommission.isCompleted()) {
						lore.add(Text.of("<a><l>COMPLETED!"));
						lore.add(Text.empty());
						lore.add(Text.of("<e>Click to claim rewards!"));
					} else {
						lore.add(Text.of("<9>Progress"));
						int progress = activeCommission.getProgress();
						int target = commission != null ? commission.objective.amount : 100;
						int percentage = Math.min(100, (int) ((progress / (double) target) * 100));
						lore.add(buildProgressBar(percentage).append("<9>{}%", percentage));
					}

					return ItemStacks.item(Material.WRITABLE_BOOK, 1,
							Text.of("<6>Commission #{}", index + 1), lore);
				}
			});
		}

		set(new GUIClickableItem(30) {
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer player) {
				new GUICommissionMilestones().open(player);
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				DatapointCommissions.PlayerCommissionData data = Commissions.getCommissionData((SkyBlockPlayer) player);
				int totalCompleted = data.getTotalCompleted();

				List<Text> lore = new ArrayList<>();
				lore.add(Text.of("<7>View milestone progress and rewards!"));
				lore.add(Text.literal(" "));

				CommissionMilestone nextMilestone = CommissionMilestone.getNextMilestone(totalCompleted);
				if (nextMilestone != null) {
					double progress = data.getMilestoneProgress(nextMilestone.getTier());

					lore.add(Text.of("<7>Progress to milestone {:roman}: <e>{}<6>%",
							nextMilestone.getTier(), String.format("%.1f", progress)));
					lore.add(buildMilestoneProgressBar(progress).append("<e>{}<6>/<e>{}",
							totalCompleted, nextMilestone.getCommissionsRequired()));
					lore.add(Text.literal(" "));
					lore.add(Text.of("<7>Tier {:roman} Rewards:", nextMilestone.getTier()));
					for (Text reward : nextMilestone.getRewardDescriptions()) {
						lore.add(Text.of("  {}", reward));
					}
				} else {
					lore.add(Text.of("<a><l>ALL MILESTONES COMPLETED!"));
				}

				lore.add(Text.literal(" "));
				lore.add(Text.of("<e>Click to view!"));

				return ItemStacks.item(Material.FILLED_MAP, 1,
						Text.of("<e>Commission Milestones"), lore);
			}
		});

		set(GUIClickableItem.getCloseItem(31));
		updateItemStacks(getInventory(), getPlayer());
	}

	private Text buildProgressBar(int percentage) {
		int filled = percentage / 5; // 20 segments total
		int empty = 20 - filled;

		return Text.of("<l><m><f>{}<7>{} ",
				" ".repeat(Math.max(0, filled)),
				" ".repeat(Math.max(0, empty)));
	}

	private Text buildMilestoneProgressBar(double percentage) {
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

	}
}
