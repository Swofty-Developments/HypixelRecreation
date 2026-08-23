package net.swofty.type.skyblockgeneric.mission.missions.thepark.darkthicket;


import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.mission.LocationAssociatedMission;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.savanna.MissionTravelToTheSavannaWoodland;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.trials.MissionCompleteTrials;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MissionGiveRyanDarkOakLogs extends SkyBlockMission implements LocationAssociatedMission {

	@Override
	public Pos getLocation() {
		return new Pos(-364.5, 102.5, -90.5);
	}

	@Override
	public String getID() {
		return "give_ryan_dark_oak_logs";
	}

	@Override
	public String getName() {
		return "Give Ryan Dark Oak Logs";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public Double getAttachedSkyBlockXP() {
		return 5D;
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.removeItemFromPlayer(ItemType.DARK_OAK_LOG, 256);
		mission.getQuestCompleteText(
				new ArrayList<>(List.of(
						"<f>Campfire Initiate Badge I",
						"<8>+<6>2,000 <7>Coins",
						"<8>+<3>1,000 <7>Foraging Experience",
						"<8>+<b>5 SkyBlock XP"
				))
		).forEach(player::sendMessage);

		player.getSkyBlockExperience().addExperience(SkyBlockLevelCause.getMissionCause(getID()));
		player.addCoins(2000);
		player.getSkills().increase(player, SkillCategories.FORAGING, 1000D);

		player.getMissionData().startMission(MissionTravelToTheSavannaWoodland.class);
		player.getMissionData().startMission(MissionCompleteTrials.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.TRIALS_OF_FIRE, RegionType.DARK_THICKET);
	}
}
