package net.swofty.type.skyblockgeneric.mission.missions.thepark.darkthicket;


import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.mission.LocationAssociatedMission;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;

public class MissionCompleteTrialOfFireOne extends SkyBlockMission implements LocationAssociatedMission {

	@Override
	public String getID() {
		return "complete_trial_of_fire_1";
	}

	@Override
	public String getName() {
		return "Complete Trial of Fire I";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionTalkToRyan.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.DARK_THICKET, RegionType.TRIALS_OF_FIRE);
	}

	@Override
	public Pos getLocation() {
		return new Pos(-363, 102, -94);
	}
}
