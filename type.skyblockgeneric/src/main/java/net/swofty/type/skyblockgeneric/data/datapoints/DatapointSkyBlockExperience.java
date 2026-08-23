package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.event.custom.SkyBlockXPModificationEvent;
import net.swofty.type.skyblockgeneric.levels.SkyBlockEmblems;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.*;

public class DatapointSkyBlockExperience extends SkyBlockDatapoint<DatapointSkyBlockExperience.PlayerSkyBlockExperience> {
    private static final Serializer<PlayerSkyBlockExperience> serializer = new Serializer<>() {
        @Override
        public String serialize(PlayerSkyBlockExperience value) {
            List<String> serialized = new ArrayList<>();

            value.getCompletedExperienceCauses().forEach((cause) -> {
                serialized.add(SkyBlockLevelCause.getKey(cause));
            });

            if (value.getCurrentEmblem() == null) return new JSONObject(new HashMap<>(Map.of(
                    "values", serialized
            ))).toString();

            return new JSONObject(new HashMap<>(Map.of(
                    "values", serialized,
                    "emblem", value.getCurrentEmblem().getKey().name() + ":" + value.getCurrentEmblem().getValue()
            ))).toString();
        }

        @Override
        public PlayerSkyBlockExperience deserialize(String json) {
            List<SkyBlockLevelCauseAbstr> experience = new ArrayList<>();

            JSONObject jsonObject = new JSONObject(json);
            jsonObject.getJSONArray("values").forEach((value) -> {
                if (value instanceof String)
                    experience.add(SkyBlockLevelCause.getCause((String) value));
            });

            if (!jsonObject.has("emblem") || jsonObject.isNull("emblem")) return new PlayerSkyBlockExperience(experience, null);
            String[] emblem = jsonObject.getString("emblem").split(":");
            AbstractMap.SimpleEntry<SkyBlockEmblems, Integer> currentEmblem = new HashMap.SimpleEntry<>(SkyBlockEmblems.valueOf(emblem[0]), Integer.parseInt(emblem[1]));

            return new PlayerSkyBlockExperience(experience, currentEmblem);
        }

        @Override
        public PlayerSkyBlockExperience clone(PlayerSkyBlockExperience value) {
            return new PlayerSkyBlockExperience(value.getCompletedExperienceCauses(), value.getCurrentEmblem());
        }
    };

    public DatapointSkyBlockExperience(String key, PlayerSkyBlockExperience value) {
        super(key, value, serializer);
    }

    public DatapointSkyBlockExperience(String key) {
        this(key, new PlayerSkyBlockExperience());
    }

    @NoArgsConstructor
    @Getter
    public static class PlayerSkyBlockExperience {
        private List<SkyBlockLevelCauseAbstr> completedExperienceCauses = new ArrayList<>();
        private Map.Entry<SkyBlockEmblems, Integer> currentEmblem = null;
        @Setter
        private SkyBlockPlayer attachedPlayer = null;

        public PlayerSkyBlockExperience(List<SkyBlockLevelCauseAbstr> completedExperienceCauses, Map.Entry<SkyBlockEmblems, Integer> currentEmblem) {
            this.completedExperienceCauses = completedExperienceCauses;
            this.currentEmblem = currentEmblem;
        }

        /**
         * Retrieves a list of completed experience causes that are instances of the specified class type.
         *
         * @param type the {@link Class} to filter the completed experience causes by.
         * @return a list of {@link SkyBlockLevelCauseAbstr} instances that match the specified type.
         */
        public List<SkyBlockLevelCauseAbstr> getOfType(Class type) {
            return completedExperienceCauses.stream().filter(type::isInstance).toList();
        }

        /**
         * Gets the current SkyBlock emblem, if available.
         *
         * @return the current {@link SkyBlockEmblems.SkyBlockEmblem}, or null if no emblem is set.
         */
        public @Nullable SkyBlockEmblems.SkyBlockEmblem getEmblem() {
            if (currentEmblem == null) return null;
            return currentEmblem.getKey().getEmblems().get(currentEmblem.getValue());
        }

        /**
         * Sets the current emblem by specifying the {@link SkyBlockEmblems} category and the associated emblem.
         *
         * @param emblems the {@link SkyBlockEmblems} category.
         * @param emblem the {@link SkyBlockEmblems.SkyBlockEmblem} to be set.
         */
        public void setEmblem(SkyBlockEmblems emblems, SkyBlockEmblems.SkyBlockEmblem emblem) {
            currentEmblem = new HashMap.SimpleEntry<>(SkyBlockEmblems.getCategoryFromEmblem(emblem),
                    emblems.getEmblems().indexOf(emblem));
        }

        /**
         * Checks if the player has already gained experience for the specified cause.
         *
         * @param cause the {@link SkyBlockLevelCauseAbstr} representing the experience cause.
         * @return true if the cause has been completed, false otherwise.
         */
        public boolean hasExperienceFor(SkyBlockLevelCauseAbstr cause) {
            return completedExperienceCauses.contains(cause);
        }

        /**
         * Adds a new experience cause to the completed causes, and triggers an event if the player is online.
         * If the experience cause has already been completed, it does nothing.
         *
         * @param cause the {@link SkyBlockLevelCauseAbstr} to be added.
         */
        public void addExperience(@NonNull SkyBlockLevelCauseAbstr cause) {
            if (completedExperienceCauses.contains(cause)) return;
            String causeKey = SkyBlockLevelCause.getKey(cause);
            if (completedExperienceCauses.stream().anyMatch((c) -> SkyBlockLevelCause.getKey(c).equals(causeKey))) return;

            double oldXP = getTotalXP();
            completedExperienceCauses.add(cause);
            double newXP = getTotalXP();

            if (getAttachedPlayer() != null)
                HypixelEventHandler.callCustomEvent(new SkyBlockXPModificationEvent(
                        getAttachedPlayer(), cause, oldXP, newXP));
        }

        /**
         * Returns a formatted display showing the player's progress to the next level.
         * This includes a visual loading bar based on the experience gained.
         *
         * @return a String representation of the next level's progress or "<c>MAX" if the maximum level is reached.
         */
        public String getNextLevelDisplay() {
            SkyBlockLevelRequirement nextLevel = getLevel().getNextLevel();
            if (nextLevel == null) return "<c>MAX";

            int nextLevelXP = nextLevel.getExperience() - nextLevel.getExperienceOfAllPreviousLevels();
            double totalXP = getTotalXP() - nextLevel.getExperienceOfAllPreviousLevels();

            String baseLoadingBar = "─────────────────";
            int maxBarLength = baseLoadingBar.length();
            int completedLength = (int) ((totalXP / (double) nextLevelXP) * maxBarLength);

            String completedLoadingBar = baseLoadingBar.substring(0, Math.min(completedLength, maxBarLength));
            String uncompletedLoadingBar = baseLoadingBar.substring(Math.min(completedLoadingBar.length(), maxBarLength));

            return "<7><3><m>" + completedLoadingBar + "<f>" + uncompletedLoadingBar + "<r> <b>" + Math.round(totalXP) + "<3>/<b>" + nextLevelXP;
        }

        /**
         * Calculates and returns the total experience earned from all completed experience causes.
         *
         * @return the total experience points as a {@link Double}.
         */
        public Double getTotalXP() {
            if (completedExperienceCauses.isEmpty()) return 0.0;
            return completedExperienceCauses.stream().mapToDouble(SkyBlockLevelCauseAbstr::xpReward).sum();
        }

        /**
         * Determines and returns the player's current level based on their total experience points.
         *
         * @return the current {@link SkyBlockLevelRequirement} for the player.
         */
        public SkyBlockLevelRequirement getLevel() {
            return SkyBlockLevelRequirement.getFromTotalXP(getTotalXP());
        }
    }
}
