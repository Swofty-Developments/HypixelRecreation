package net.swofty.type.bedwarslobby.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.swofty.commons.ServerType;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarslobby.hologram.LeaderboardHologramManager;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.quest.QuestData;
import net.swofty.type.generic.quest.QuestDefinition;
import net.swofty.type.generic.quest.QuestRegistry;
import net.swofty.type.generic.quest.QuestType;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

public class ActionPlayerDataSpawn implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER_DATA, requireDataLoaded = false, isAsync = true)
    public void run(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        if (!(HypixelConst.getTypeLoader().getType() == ServerType.BEDWARS_LOBBY)) return;

        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        Rank rank = player.getRank();
        if (rank == Rank.DEFAULT) return;

        for (HypixelPlayer onlinePlayer : HypixelGenericLoader.getLoadedPlayers()) {
            onlinePlayer.sendMessage("{} <6>joined the lobby!", player.getFullDisplayName());
        }

        BedWarsDataHandler handler = BedWarsDataHandler.getUser(player.getUuid());
        handler.runOnLoad(player);

		// display the player level progression in the experience bar
        DatapointLeaderboardLong dp = handler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class);
		player.setLevel(BedwarsLevelUtil.calculateLevel(dp.getValue()));
		player.setExp((float) BedwarsLevelUtil.calculateExperienceSinceLastLevel(dp.getValue()) / BedwarsLevelUtil.calculateMaxExperienceFromExperience(dp.getValue()));

		// Spawn leaderboard holograms for this player
		LeaderboardHologramManager.spawnHologramsForPlayer(player);

		// Auto-accept quests if enabled and player is MVP+
		if (player.getRank().isEqualOrHigherThan(Rank.MVP_PLUS) &&
			player.getToggles().get(DatapointToggles.Toggles.ToggleType.AUTO_ACCEPT_QUESTS)) {
			autoAcceptQuests(player, AchievementCategory.BEDWARS);
		}
    }

	private void autoAcceptQuests(HypixelPlayer player, AchievementCategory category) {
		QuestData questData = player.getQuestHandler().getQuestData();
		int startedCount = 0;

		// Auto-accept daily quests
		for (QuestDefinition quest : QuestRegistry.getByCategory(category, QuestType.DAILY)) {
			if (!questData.isActive(quest.getId()) && !questData.isCompleted(quest.getId())) {
				player.getQuestHandler().startQuest(quest.getId());
				startedCount++;
			}
		}

		// Auto-accept weekly quests
		for (QuestDefinition quest : QuestRegistry.getByCategory(category, QuestType.WEEKLY)) {
			if (!questData.isActive(quest.getId()) && !questData.isCompleted(quest.getId())) {
				player.getQuestHandler().startQuest(quest.getId());
				startedCount++;
			}
		}

		if (startedCount > 0) {
			player.sendMessage("<a>You have automatically started {} {} quest{}!",
					startedCount, category.getDisplayName(), startedCount > 1 ? "s" : "");
		}
	}
}