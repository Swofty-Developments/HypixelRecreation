package net.swofty.type.skyblockgeneric.mission.missions.goldmine.lazyminer;


import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MissionTalkToLazyMiner extends SkyBlockMission {
    @Override
    public String getID() {
        return "talk_to_lazy_miner";
    }

    @Override
    public String getName() {
        return "Talk to Lazy Miner";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        mission.getObjectiveCompleteText(new ArrayList<>(List.of("<b>10 Mining XP"))).forEach(player::sendMessage);
        player.getSkills().increase(player, SkillCategories.MINING, 10D);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.GOLD_MINE);
    }
}
