package net.swofty.type.skyblockgeneric.mission.missions.thepark.jungle;


import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.item.Material;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockProgressMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class MissionCollectJungleLogs extends SkyBlockProgressMission {

	private final Map<UUID, Long> testTimes = new HashMap<>();

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
	public void onTick(PlayerTickEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		if (testTimes.containsKey(player.getUuid())) {
			long lastTime = testTimes.get(player.getUuid());
			if (System.currentTimeMillis() - lastTime < 650) {
				return;
			}
		}

		MissionData data = player.getMissionData();

		if (!data.isCurrentlyActive(this.getClass()) || data.hasCompleted(this.getClass())) {
			testTimes.remove(player.getUuid());
			return;
		}

		testTimes.put(player.getUuid(), System.currentTimeMillis());

		int amount = 0;
		for (SkyBlockItem item : player.getAllInventoryItems()) {
			if (item.getMaterial() == Material.JUNGLE_LOG || item.getMaterial() == Material.JUNGLE_WOOD) {
				amount += item.getAmount();
			}
		}

		for (SkyBlockItem item : player.getAllSacks()) {
			if (item.getMaterial() == Material.JUNGLE_LOG || item.getMaterial() == Material.JUNGLE_WOOD) {
				amount += item.getAmount();
			}
		}

		MissionData.ActiveMission mission = data.getMission(this.getClass()).getKey();
		mission.setMissionProgress(amount);
		mission.checkIfMissionEnded(player);
	}

	@Override
	public int getMaxProgress() {
		return 512;
	}

	@Override
	public String getID() {
		return "collect_jungle_logs";
	}

	@Override
	public String getName() {
		return "Collect Jungle Logs";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionGiveMolbertJungleLogs.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.JUNGLE_ISLAND);
	}
}
