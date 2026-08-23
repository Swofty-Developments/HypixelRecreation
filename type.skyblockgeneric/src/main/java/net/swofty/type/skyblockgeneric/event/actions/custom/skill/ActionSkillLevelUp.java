package net.swofty.type.skyblockgeneric.event.actions.custom.skill;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.skill.SkillCategory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;

public class ActionSkillLevelUp implements HypixelEventClass {


	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
	public void run(SkillUpdateEvent event) {
		if (event.getNewValueRaw() <= event.getOldValueRaw()) return;

		SkyBlockPlayer player = event.getPlayer();
		SkillCategories skillCategory = event.getSkillCategory();

		int oldLevel = player.getSkills().getLevelAt(skillCategory, event.getOldValueRaw());
		int newLevel = player.getSkills().getLevelAt(skillCategory, event.getNewValueRaw());

		if (oldLevel == newLevel) return;

		player.sendMessage("<3><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

		Text transition = oldLevel == 0
				? Text.of("<3>{} <e>{:roman}", skillCategory, newLevel)
				: Text.of("<3>{} <8>{:roman}➜<e>{:roman}", skillCategory, oldLevel, newLevel);

		player.sendMessage(
				"<hover:'<e>Click to view your {0} Skill progress!'><click:run:'/viewskill {1}'>  <b><l>SKILL LEVEL UP </l>{2}</click></hover>",
				skillCategory, skillCategory.toString().toUpperCase(), transition
		);

		SkillCategory.SkillReward reward = skillCategory.asCategory().getReward(newLevel);

		if (reward.unlocks().length != 0) {
			player.sendMessage(" ");
			player.sendMessage("  <a><l>REWARDS");
			for (int level = oldLevel + 1; level <= newLevel; level++) {
				Arrays.stream(skillCategory.asCategory().getReward(level).unlocks()).forEach(unlock -> {
				switch (unlock.type()) {
					case XP ->
							player.sendMessage("    <8>+<b>{} SkyBlock XP", ((SkillCategory.XPReward) unlock).getXP());
					case COINS ->
							player.sendMessage("    <8>+<6>{} <7>Coins", ((SkillCategory.CoinReward) unlock).getCoins());
					case STATS_BASE -> {
						ItemStatistic statistic = ((SkillCategory.BaseStatisticReward) unlock).getStatistic();
						player.sendMessage("    <8>+<a>{:.1}{} {}", ((SkillCategory.BaseStatisticReward) unlock).amountAdded(),
								statistic.getSuffix(), statistic.getCompleteDisplayName());
					}
					case STATS_ADDITIVE_PERCENTAGE -> {
						ItemStatistic statistic = ((SkillCategory.AdditivePercentageStatisticReward) unlock).getStatistic();
						player.sendMessage("    <8>+<a>{:.1}% {}", ((SkillCategory.AdditivePercentageStatisticReward) unlock).amountAdded(),
								statistic.getCompleteDisplayName());
					}
					case REGION_ACCESS -> {
						RegionType region = ((SkillCategory.RegionReward) unlock).getRegion();
						player.sendMessage("    <8>+<a>Access to <color:{}>{}", region.getColor(), region.getName());
					}
				}

				unlock.onUnlock(player);
				});
			}
		}

		player.sendMessage("<3><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}
}
