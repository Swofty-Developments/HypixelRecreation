package net.swofty.type.skyblockgeneric.mission.missions.thepark.jungle;


import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.mission.LocationAssociatedMission;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MissionTalkToMolbertFourth extends SkyBlockMission implements LocationAssociatedMission {

	@Override
	public String getID() {
		return "talk_to_molbert_fourth";
	}

	@Override
	public String getName() {
		return "Talk to Molbert";
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
		mission.getQuestCompleteText(
				new ArrayList<>(List.of(
						"<8>+<6>1,500 <7>Coins",
						"<8>+<3>500 <7>Foraging Experience",
						"<8>+<b>5 SkyBlock XP",
						"<f>Mole Hat"
				))
		).forEach(player::sendMessage);

		player.getSkyBlockExperience().addExperience(SkyBlockLevelCause.getMissionCause(getID()));
		player.addCoins(1500);
		player.getSkills().increase(player, SkillCategories.FORAGING, 500D);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.SAVANNA_WOODLAND, RegionType.JUNGLE_ISLAND);
	}

	@Override
	public Pos getLocation() {
		return new Pos(-448.500, 119.281, -64.125);
	}
}
