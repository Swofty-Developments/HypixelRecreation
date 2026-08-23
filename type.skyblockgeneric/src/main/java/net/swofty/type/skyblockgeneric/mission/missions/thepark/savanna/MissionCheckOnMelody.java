package net.swofty.type.skyblockgeneric.mission.missions.thepark.savanna;


import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.mission.LocationAssociatedMission;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;

public class MissionCheckOnMelody extends SkyBlockMission implements LocationAssociatedMission {

	@Override
	public String getID() {
		return "check_on_melody";
	}

	@Override
	public String getName() {
		return "Check on Melody";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionCollectAcaciaLogs.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.SAVANNA_WOODLAND, RegionType.MELODY_PLATEAU);
	}

	@Override
	public Pos getLocation() {
		return new Pos(-411.5, 109, 71.5);
	}
}
