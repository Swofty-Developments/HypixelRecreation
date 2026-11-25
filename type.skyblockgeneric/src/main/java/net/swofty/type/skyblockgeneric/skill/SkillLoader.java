package net.swofty.type.skyblockgeneric.skill;

import lombok.Data;
import net.minestom.server.item.Material;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.region.RegionType;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class SkillLoader {
    private static final File SKILLS_DIR = new File("./configuration/skyblock/skills");

    @Data
    public static class SkillConfig {
        public String name;
        public String displayIcon;
        public List<String> description;
        public List<SkillRewardEntry> rewards;
    }

    @Data
    public static class SkillRewardEntry {
        public int level;
        public int requirement;
        public List<SkillUnlock> unlocks;
    }

    @Data
    public static class SkillUnlock {
        public String type;
        public SkillUnlockData data;
    }

    @Data
    public static class SkillUnlockData {
        // XP/COINS/STATS amount (can be decimal for stats like CRIT_CHANCE)
        public @Nullable Double amount;

        // STATS
        public @Nullable String statistic;
        public @Nullable Double percentage;  // For STATS_ADDITIVE_PERCENTAGE

        // REGION
        public @Nullable String region;

        // RUNE
        public @Nullable Integer runeLevel;
    }

    public static SkillCategory loadFromFile(String name) {
        try {
            // Ensure directory exists
            if (!YamlFileUtils.ensureDirectoryExists(SKILLS_DIR)) {
                throw new IOException("Failed to create skills directory");
            }

            File skillFile = new File(SKILLS_DIR, name.toLowerCase() + ".yml");

            // Load the YAML file
            Yaml yaml = new Yaml();
            SkillConfig config = yaml.loadAs(new FileReader(skillFile), SkillConfig.class);

            return new SkillCategory() {
                @Override
                public Material getDisplayIcon() {
                    return Material.values().stream()
                            .filter(material -> material.key().value().equalsIgnoreCase(config.displayIcon))
                            .findFirst()
                            .orElse(Material.AIR);
                }

                @Override
                public String getName() {
                    return config.name;
                }

                @Override
                public List<String> getDescription() {
                    return config.description;
                }

                private SkillReward[] cachedRewards = null;

                @Override
                public SkillReward[] getRewards() {
                    if (cachedRewards == null) {
                        List<SkillReward> rewards = new ArrayList<>();
                        for (SkillRewardEntry entry : config.rewards) {
                            rewards.add(parseReward(entry));
                        }
                        cachedRewards = rewards.toArray(new SkillReward[0]);
                    }
                    return cachedRewards;
                }
            };
        } catch (Exception e) {
            Logger.error(e, "Failed to load skill from file: " + name);
            return null;
        }
    }

    private static SkillCategory.SkillReward parseReward(SkillRewardEntry entry) {
        List<SkillCategory.Reward> rewards = new ArrayList<>();

        for (SkillUnlock unlock : entry.unlocks) {
            SkillCategory.Reward reward = parseUnlock(unlock);
            if (reward != null) {
                rewards.add(reward);
            }
        }

        return new SkillCategory.SkillReward(
                entry.level,
                entry.requirement,
                rewards.toArray(new SkillCategory.Reward[0])
        );
    }

    private static SkillCategory.Reward parseUnlock(SkillUnlock unlock) {
        switch (unlock.type.toUpperCase()) {
            case "XP" -> {
                return new SkillCategory.XPReward() {
                    @Override
                    public int getXP() {
                        return unlock.data.amount.intValue();
                    }
                };
            }
            case "COINS" -> {
                return new SkillCategory.CoinReward() {
                    @Override
                    public int getCoins() {
                        return unlock.data.amount.intValue();
                    }
                };
            }
            case "STATS_BASE" -> {
                return new SkillCategory.BaseStatisticReward() {
                    @Override
                    public ItemStatistic getStatistic() {
                        return ItemStatistic.valueOf(unlock.data.statistic);
                    }

                    @Override
                    public Double amountAdded() {
                        return unlock.data.amount;
                    }
                };
            }
            case "STATS_ADDITIVE_PERCENTAGE" -> {
                return new SkillCategory.AdditivePercentageStatisticReward() {
                    @Override
                    public ItemStatistic getStatistic() {
                        return ItemStatistic.valueOf(unlock.data.statistic);
                    }

                    @Override
                    public Double amountAdded() {
                        return unlock.data.percentage;
                    }
                };
            }
            case "REGION_ACCESS" -> {
                return new SkillCategory.RegionReward() {
                    @Override
                    public RegionType getRegion() {
                        return RegionType.valueOf(unlock.data.region);
                    }
                };
            }
            case "RUNE" -> {
                return new SkillCategory.RuneReward() {
                    @Override
                    public int getRuneLevel() {
                        return unlock.data.runeLevel;
                    }
                };
            }
            default -> {
                Logger.warn("Unknown skill unlock type: " + unlock.type);
                return null;
            }
        }
    }
}
