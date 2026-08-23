package net.swofty.type.skyblockgeneric.mission.missions.goldmine.lazyminer;


import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MissionFindLazyMinerPickaxe extends SkyBlockMission {
    @Override
    public String getID() {
        return "find_lazy_miner_pickaxe";
    }

    @Override
    public String getName() {
        return "Find Lazy Miner's Pickaxe";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        player.getMissionData().startMission(MissionTalkToLazyMiner.class);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.GOLD_MINE);
    }
}
