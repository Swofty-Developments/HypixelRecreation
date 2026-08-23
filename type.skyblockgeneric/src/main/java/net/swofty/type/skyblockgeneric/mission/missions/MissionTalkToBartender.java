package net.swofty.type.skyblockgeneric.mission.missions;


import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.*;

public class MissionTalkToBartender extends SkyBlockMission {

    @Override
    public String getID() {
        return "talk_to_bartender";
    }

    @Override
    public String getName() {
        return "Talk to the Bartender";
    }

    @Override
    public HashMap<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        //TODO move bartender to the bar
        mission.getObjectiveCompleteText(new ArrayList<>(List.of("<6>100 <7>Coins"))).forEach(player::sendMessage);
        player.addCoins(100);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.GRAVEYARD);
    }

}
