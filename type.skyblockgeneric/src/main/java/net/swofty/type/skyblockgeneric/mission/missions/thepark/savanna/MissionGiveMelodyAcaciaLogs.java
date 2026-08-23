package net.swofty.type.skyblockgeneric.mission.missions.thepark.savanna;


import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.mission.LocationAssociatedMission;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.jungle.MissionTalkToMolbert;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MissionGiveMelodyAcaciaLogs extends SkyBlockMission implements LocationAssociatedMission {

	@Override
	public Pos getLocation() {
		return new Pos(-411.5, 109, 71.5);
	}

	@Override
	public String getID() {
		return "give_melody_acacia_logs";
	}

	@Override
	public String getName() {
		return "Give Melody Acacia Logs";
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
		mission.getObjectiveCompleteText(
				"QUEST COMPLETE",
				new ArrayList<>(List.of(
						"<8>+<7>Access to <d>Melody's Harp",
						"<8>+<6>2,000 <7>Coins",
						"<8>+<3>1,500 <7>Foraging Experience",
						"<8>+<b>5 SkyBlock XP",
						"<f>Melody's Shoes"
				))
		).forEach(player::sendMessage);

		player.getSkyBlockExperience().addExperience(SkyBlockLevelCause.getMissionCause(getID()));
		player.addCoins(2000);
		player.getSkills().increase(player, SkillCategories.FORAGING, 1500D);

		player.getMissionData().startMission(MissionTalkToMolbert.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.SAVANNA_WOODLAND, RegionType.MELODY_PLATEAU);
	}
}
