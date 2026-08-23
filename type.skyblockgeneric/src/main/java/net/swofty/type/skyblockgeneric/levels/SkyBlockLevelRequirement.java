package net.swofty.type.skyblockgeneric.levels;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.levels.unlocks.SkyBlockLevelStatisticUnlock;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class SkyBlockLevelRequirement {
    private static final Map<Integer, SkyBlockLevelRequirement> LEVELS = new LinkedHashMap<>();
    private static boolean isLoaded = false;

    private final int level;
    private final int experience;
    private final boolean isMilestone;
    private final List<SkyBlockLevelUnlock> unlocks;
    private final String prefix;
    private final String prefixDisplay;
    private final Material prefixItem;

    public SkyBlockLevelRequirement(int level, int experience, boolean isMilestone,
                                    List<SkyBlockLevelUnlock> unlocks, String prefix,
                                    String prefixDisplay, Material prefixItem) {
        this.level = level;
        this.experience = experience;
        this.isMilestone = isMilestone;
        this.unlocks = unlocks != null ? new ArrayList<>(unlocks) : new ArrayList<>();
        this.prefix = prefix;
        this.prefixDisplay = prefixDisplay;
        this.prefixItem = prefixItem;
    }

    // Static initialization method
    public static void loadFromYaml() {
        if (isLoaded) {
            return; // Prevent double loading
        }

        try {
            SkyBlockLevelRequirement[] levels = SkyBlockLevelLoader.loadFromFile();
            LEVELS.clear();

            for (SkyBlockLevelRequirement level : levels) {
                LEVELS.put(level.getLevel(), level);
            }

            SkyBlockLevelLoader.initializeCustomLevelAwardCache(levels);
            isLoaded = true;

            Logger.info("Loaded " + levels.length + " SkyBlock levels from YAML");
        } catch (Exception e) {
            Logger.error(e, "Failed to load SkyBlock levels from YAML configuration");
        }
    }

    // Static accessor methods (enum-like behavior)
    public static SkyBlockLevelRequirement getLevel(int level) {
        ensureLoaded();
        return LEVELS.get(level);
    }

    public static SkyBlockLevelRequirement[] values() {
        ensureLoaded();
        return LEVELS.values().toArray(new SkyBlockLevelRequirement[0]);
    }

    public static Collection<SkyBlockLevelRequirement> getAllLevels() {
        ensureLoaded();
        return LEVELS.values();
    }

    public static int getMaxLevel() {
        ensureLoaded();
        return LEVELS.keySet().stream().max(Integer::compareTo).orElse(0);
    }

    private static void ensureLoaded() {
        if (!isLoaded) {
            loadFromYaml();
        }
    }

    // Instance methods (equivalent to the original enum methods)
    public int getCumulativeExperience() {
        int cumulative = 0;
        for (SkyBlockLevelRequirement requirement : values()) {
            cumulative += requirement.experience;
            if (requirement.level == this.level) {
                return cumulative;
            }
        }
        return 0;
    }

    public SkyBlockLevelRequirement getNextMilestoneLevel() {
        for (SkyBlockLevelRequirement requirement : values()) {
            if (requirement.isMilestone && requirement.level > this.level) {
                return requirement;
            }
        }
        return null;
    }

    public List<SkyBlockLevelStatisticUnlock> getStatisticUnlocks() {
        return unlocks.stream()
                .filter(unlock -> unlock instanceof SkyBlockLevelStatisticUnlock)
                .map(unlock -> (SkyBlockLevelStatisticUnlock) unlock)
                .collect(Collectors.toList());
    }

    public int asInt() {
        return level;
    }

    public String getColor() {
        return "<7>";
    }

    public @Nullable SkyBlockLevelRequirement getNextLevel() {
        return getLevel(level + 1);
    }

    @Override
    public String toString() {
        return String.valueOf(level);
    }

    public Map<SkyBlockLevelRequirement, String> getPreviousPrefixChanges() {
        Map<SkyBlockLevelRequirement, String> toReturn = new HashMap<>();

        SkyBlockLevelRequirement last = getLevel(0);
        if (last == null) return toReturn;

        for (SkyBlockLevelRequirement requirement : values()) {
            if (requirement.level < this.level && !requirement.prefix.equals(last.prefix)) {
                toReturn.put(requirement, requirement.prefix);
            }
            last = requirement;
        }

        return toReturn;
    }

    public Map.Entry<SkyBlockLevelRequirement, String> getNextPrefixChange() {
        for (SkyBlockLevelRequirement requirement : values()) {
            if (requirement.level > this.level && !requirement.prefix.equals(this.prefix)) {
                return Map.entry(requirement, requirement.prefix);
            }
        }
        return null;
    }

    public int getExperienceOfAllPreviousLevels() {
        return Arrays.stream(values())
                .filter(requirement -> requirement.level < this.level)
                .mapToInt(SkyBlockLevelRequirement::getExperience)
                .sum();
    }

    // Static utility methods
    public static Map<SkyBlockLevelRequirement, String> getAllPrefixChanges() {
        ensureLoaded();
        Map<SkyBlockLevelRequirement, String> toReturn = new HashMap<>();

        SkyBlockLevelRequirement last = getLevel(0);
        if (last == null) return toReturn;

        for (SkyBlockLevelRequirement requirement : values()) {
            if (!requirement.prefix.equals(last.prefix)) {
                toReturn.put(requirement, requirement.prefix);
            }
            last = requirement;
        }

        return toReturn;
    }

    public static SkyBlockLevelRequirement getFromTotalXP(double xp) {
        ensureLoaded();
        SkyBlockLevelRequirement toReturn = getLevel(0);
        if (toReturn == null) return null;

        for (SkyBlockLevelRequirement requirement : values()) {
            if (xp < requirement.experience) {
                return toReturn;
            } else {
                toReturn = requirement;
            }
        }
        return toReturn;
    }

    // Reload method for runtime configuration changes
    public static void reload() {
        isLoaded = false;
        LEVELS.clear();
        loadFromYaml();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SkyBlockLevelRequirement that = (SkyBlockLevelRequirement) obj;
        return level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level);
    }

    public static boolean levelExists(int level) {
        ensureLoaded();
        return LEVELS.containsKey(level);
    }

    public static SkyBlockLevelRequirement getLevelSafe(int level) {
        SkyBlockLevelRequirement result = getLevel(level);
        if (result == null) {
            // Return the highest available level if requested level doesn't exist
            return getLevel(getMaxLevel());
        }
        return result;
    }
}