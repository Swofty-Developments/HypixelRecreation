package net.swofty.type.skyblockgeneric.mission;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class MissionData {
    private static final HashMap<String, SkyBlockMission> missionClassCache = new HashMap<>();

    private List<ActiveMission> activeMissions = new ArrayList<>();
    private List<ActiveMission> completedMissions = new ArrayList<>();
    @Setter
    private SkyBlockPlayer skyBlockPlayer;

    public Map.Entry<ActiveMission, Boolean> getMission(String missionID) {
        for (ActiveMission mission : activeMissions) {
            if (mission.getMissionID().equalsIgnoreCase(missionID)) {
                return Map.entry(mission, false);
            }
        }

        for (ActiveMission mission : completedMissions) {
            if (mission.getMissionID().equalsIgnoreCase(missionID)) {
                return Map.entry(mission, true);
            }
        }

        return null;
    }

    @SneakyThrows
    public Map.Entry<ActiveMission, Boolean> getMission(Class<? extends SkyBlockMission> skyBlockMission) {
        return getMission(getMissionIDFromClass(skyBlockMission));
    }

    public List<ActiveMission> getActiveMissions(RegionType regionType) {
        return activeMissions.stream().filter(mission -> {
            SkyBlockMission skyBlockMission = getMissionFromCache(mission.getMissionID());
            return skyBlockMission.getValidRegions().contains(regionType);
        }).collect(Collectors.toList());
    }

    public boolean isCurrentlyActive(String missionID) {
        return activeMissions.stream().anyMatch(mission -> mission.getMissionID().equals(missionID));
    }

    public boolean isCurrentlyActive(Class<? extends SkyBlockMission> skyBlockMission) {
        return isCurrentlyActive(getMissionIDFromClass(skyBlockMission));
    }

    public boolean hasCompleted(String missionID) {
        return completedMissions.stream().anyMatch(mission -> mission.getMissionID().equals(missionID));
    }

    public boolean hasCompleted(Class<? extends SkyBlockMission> skyBlockMission) {
        return hasCompleted(getMissionIDFromClass(skyBlockMission));
    }

    @SneakyThrows
    public void startMission(Class<? extends SkyBlockMission> skyBlockMission) {
        String missionID = getMissionIDFromClass(skyBlockMission);
        if (activeMissions.stream().anyMatch(mission -> mission.getMissionID().equals(missionID))) {
            throw new RuntimeException("Mission already started");
        }
        if (completedMissions.stream().anyMatch(mission -> mission.getMissionID().equals(missionID))) {
            throw new RuntimeException("Mission already started, was previously completed");
        }

        SkyBlockMission mission = getMissionFromCache(missionID);
        ActiveMission activeMission = new ActiveMission(missionID, 0, mission instanceof SkyBlockProgressMission);

        Map<String, Object> data = mission.onStart(getSkyBlockPlayer(), activeMission);
        if (data != null) {
            activeMission.setCustomData(data);
        }

        activeMissions.add(activeMission);
    }

    public void endMission(String missionID) {
        if (activeMissions.stream().noneMatch(mission -> mission.getMissionID().equals(missionID))) {
            throw new RuntimeException("Mission not started");
        }

        ActiveMission activeMission = activeMissions.stream().filter(mission -> mission.getMissionID().equals(missionID)).findFirst().get();
        getMissionFromCache(missionID).onEnd(getSkyBlockPlayer(), activeMission.getCustomData(), activeMission);
        getSkyBlockPlayer().getSkyBlockExperience().addExperience(SkyBlockLevelCause.getMissionCause(activeMission.getMissionID()));
        activeMission.setMissionEnded((int) SkyBlockCalendar.getElapsed());
        activeMissions.remove(activeMission);
        completedMissions.add(activeMission);
    }

    public void endMission(Class<? extends SkyBlockMission> skyBlockMission) {
        endMission(getMissionIDFromClass(skyBlockMission));
    }

    public @Nullable SkyBlockProgressMission getAsProgressMission(String missionID) {
        SkyBlockMission mission = getMissionFromCache(missionID);
        return (mission instanceof SkyBlockProgressMission) ? (SkyBlockProgressMission) mission : null;
    }

    public @Nullable SkyBlockProgressMission getAsProgressMission(Class<? extends SkyBlockMission> skyBlockMission) {
        return getAsProgressMission(getMissionIDFromClass(skyBlockMission));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();

        result.put("activeMissions", activeMissions.stream().map(mission -> {
            Map<String, Object> m = new HashMap<>();
            m.put("missionID", mission.getMissionID());
            m.put("missionProgress", mission.getMissionProgress());
            m.put("isDynamic", mission.isProgress());
            m.put("missionStarted", mission.getMissionStarted());
            m.put("customData", mission.getCustomData());
            m.put("missionEnded", mission.getMissionEnded());
            return m;
        }).collect(Collectors.toList()));

        result.put("completedMissions", completedMissions.stream().map(mission -> {
            Map<String, Object> m = new HashMap<>();
            m.put("missionID", mission.getMissionID());
            m.put("missionProgress", mission.getMissionProgress());
            m.put("isDynamic", mission.isProgress());
            m.put("missionStarted", mission.getMissionStarted());
            m.put("customData", mission.getCustomData());
            m.put("missionEnded", mission.getMissionEnded());
            return m;
        }).collect(Collectors.toList()));

        return result;
    }

    public void deserialize(Map<String, Object> map) {
        List<Map<String, Object>> serializedActiveMissions = (List<Map<String, Object>>) map.get("activeMissions");
        List<Map<String, Object>> serializedCompletedMissions = (List<Map<String, Object>>) map.get("completedMissions");

        activeMissions = serializedActiveMissions.stream().map(m -> new ActiveMission(
                (String) m.get("missionID"),
                (int) m.get("missionProgress"),
                (boolean) m.get("isDynamic"),
                (int) m.get("missionStarted"),
                (Map<String, Object>) m.get("customData"),
                (int) m.get("missionEnded")
        )).collect(Collectors.toList());

        completedMissions = serializedCompletedMissions.stream().map(m -> new ActiveMission(
                (String) m.get("missionID"),
                (int) m.get("missionProgress"),
                (boolean) m.get("isDynamic"),
                (int) m.get("missionStarted"),
                (Map<String, Object>) m.get("customData"),
                (int) m.get("missionEnded")
        )).collect(Collectors.toList());
    }

    @Getter
    @Setter
    public static class ActiveMission {
        private String missionID;
        private int missionStarted;
        private int missionEnded;
        private int missionProgress;
        private boolean isProgress;
        private Map<String, Object> customData;

        public ActiveMission(String missionID, int missionProgress, boolean isProgress) {
            this.missionID = missionID;
            this.missionProgress = missionProgress;
            this.isProgress = isProgress;
            this.missionStarted = (int) SkyBlockCalendar.getElapsed();
            this.customData = new HashMap<>();
            this.missionEnded = 0;
        }

        public ActiveMission(String missionID, int missionProgress, boolean isProgress, int missionStarted, Map<String, Object> customData, int missionEnded) {
            this.missionID = missionID;
            this.missionProgress = missionProgress;
            this.isProgress = isProgress;
            this.missionStarted = missionStarted;
            this.customData = customData;
            this.missionEnded = missionEnded;
        }

        @Override
        public String toString() {
            return getMissionFromCache(missionID).getName();
        }

        public void checkIfMissionEnded(SkyBlockPlayer player) {
            SkyBlockProgressMission mission = (SkyBlockProgressMission) getMissionFromCache(missionID);

            if (missionProgress >= mission.getMaxProgress()) {
                player.getMissionData().endMission(missionID);
            }
        }

        public List<Text> getObjectiveCompleteText(ArrayList<String> rewards) {
            return getObjectiveCompleteText("OBJECTIVE COMPLETE", rewards);
        }

        public List<Text> getObjectiveCompleteText(String title, ArrayList<String> rewards) {
            SkyBlockMission mission = getMissionFromCache(missionID);

            if (rewards == null || rewards.isEmpty())
                return List.of(
                        Text.of("<7> "),
                        Text.of("<6><l>  {}", title),
                        Text.of("<f>  {}", mission.getName()),
                        Text.of("<7> "));

            List<Text> display = new ArrayList<>(List.of(
                    Text.of("<7> "),
                    Text.of("<6><l>  {}", title),
                    Text.of("<f>  {}", mission.getName()),
                    Text.of("<7> "),
                    Text.of("<a><l>    REWARDS")
            ));
            display.addAll(rewards.stream().map(reward -> Text.parse("<8>    +" + reward)).toList());
            display.add(Text.of("<7> "));
            return display;
        }

        public List<Text> getQuestCompleteText(ArrayList<String> rewards) {
            List<Text> display = new ArrayList<>(List.of(
                    Text.of("<7> "),
                    Text.of("<6><l>  QUEST COMPLETE"),
                    Text.of("<7> "),
                    Text.of("<a><l>    REWARDS")
            ));
            display.addAll(rewards.stream().map(reward -> Text.parse("<8>    +" + reward)).toList());
            display.add(Text.of("<7> "));
            return display;
        }

        public List<Text> getNewObjectiveText() {
            SkyBlockMission mission = getMissionFromCache(missionID);

            return List.of(
                    Text.of("<7> "),
                    Text.of("<6><l>  NEW OBJECTIVE"),
                    Text.of("<f>  {}", mission.getName()),
                    Text.of("<7> "));
        }
    }

    private static SkyBlockMission getMissionFromCache(String missionID) {
        SkyBlockMission mission = missionClassCache.get(missionID);
        if (mission == null) {
            throw new IllegalArgumentException("Mission not found in cache: " + missionID);
        }
        return mission;
    }

    @SneakyThrows
    private static String getMissionIDFromClass(Class<? extends SkyBlockMission> skyBlockMission) {
        return skyBlockMission.newInstance().getID();
    }

    public static SkyBlockMission getMissionClass(String missionID) {
        return missionClassCache.get(missionID);
    }

    public static SkyBlockMission getMissionClass(ActiveMission skyBlockMission) {
        return missionClassCache.get(skyBlockMission.getMissionID());
    }

    public static List<String> getAllMissionIDs() {
        return new ArrayList<>(missionClassCache.keySet());
    }

    public static void registerMission(Class<? extends SkyBlockMission> skyBlockMission) {
        try {
            SkyBlockMission mission = skyBlockMission.newInstance();
            missionClassCache.put(mission.getID(), mission);
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.info(e.getStackTrace());
        }
    }
}
