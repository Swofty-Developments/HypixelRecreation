package net.swofty.type.skyblockgeneric.mission.missions.thepark.jungle;


import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class MissionLeaveTheAreaAgain extends SkyBlockMission {

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
	public void run(PlayerRegionChangeEvent event) {
		if (event.getTo() == null) return;
		MissionData data = event.getPlayer().getMissionData();
		if (event.getTo() == RegionType.JUNGLE_ISLAND) return;
		if (data.isCurrentlyActive(this.getClass())) {
			data.endMission(this.getClass());
		}
	}

	@Override
	public String getID() {
		return "leave_the_area_again";
	}

	@Override
	public String getName() {
		return "Leave the area again";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionTalkToMolbertAgainAgainAgain.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.JUNGLE_ISLAND);
	}
}
