package net.swofty.type.skyblockgeneric.experimentation;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class ExperimentRules {
    private static final List<SuperPairItem> GRAND_BOOKS = List.of(
            SuperPairItem.SCAVENGER_V, SuperPairItem.SHARPNESS_VI, SuperPairItem.LIFE_STEAL_IV,
            SuperPairItem.POWER_VI, SuperPairItem.ENDER_SLAYER_VI, SuperPairItem.THUNDERBOLT_VI,
            SuperPairItem.GROWTH_VI, SuperPairItem.CHANCE_IV, SuperPairItem.BLAST_PROTECTION_VI,
            SuperPairItem.RESPITE_III, SuperPairItem.VENOMOUS_VI, SuperPairItem.PROJECTILE_PROTECTION_VI,
            SuperPairItem.FIRE_PROTECTION_VI, SuperPairItem.WOODSPLITTER_VI, SuperPairItem.GIANT_KILLER_VI,
            SuperPairItem.DRAIN_IV, SuperPairItem.PROTECTION_VI, SuperPairItem.TITAN_KILLER_VI);
    private static final List<SuperPairItem> ULTRA_RARE_REWARDS = List.of(
            SuperPairItem.A_BEGINNERS_GUIDE_TO_PESTHUNTING, SuperPairItem.SEVERED_PINCER,
            SuperPairItem.CHANCE_V, SuperPairItem.THUNDERLORD_VII, SuperPairItem.ENSNARED_SNAIL,
            SuperPairItem.GIANT_KILLER_VII, SuperPairItem.GRAVITY_VI, SuperPairItem.GOLDEN_BOUNTY,
            SuperPairItem.SEVERED_HAND, SuperPairItem.CRITICAL_VII, SuperPairItem.VIBRANT_CORAL,
            SuperPairItem.SNIPE_IV, SuperPairItem.LIFE_STEAL_V, SuperPairItem.GOLD_BOTTLE_CAP,
            SuperPairItem.LOOTING_V, SuperPairItem.FIRST_STRIKE_V, SuperPairItem.FIRE_PROTECTION_VII,
            SuperPairItem.THUNDERBOLT_VII, SuperPairItem.CUBISM_VI, SuperPairItem.TRIPLE_STRIKE_V,
            SuperPairItem.CHAIN_OF_THE_END_TIMES, SuperPairItem.DRAIN_V, SuperPairItem.FATEFUL_STINGER,
            SuperPairItem.BLAST_PROTECTION_VII, SuperPairItem.CLEAVE_VI, SuperPairItem.OCTOPUS_TENDRIL,
            SuperPairItem.TITAN_KILLER_VII, SuperPairItem.LUCK_VII, SuperPairItem.END_STONE_IDOL,
            SuperPairItem.EXECUTE_VI, SuperPairItem.POWER_VII, SuperPairItem.TROUBLED_BUBBLE,
            SuperPairItem.PROJECTILE_PROTECTION_VII, SuperPairItem.GROWTH_VII, SuperPairItem.SHARPNESS_VII,
            SuperPairItem.PROTECTION_VII, SuperPairItem.PROSECUTE_VI);
    private static final Map<ExperimentType, Map<ExperimentTier, Rule>> RULES = createRules();

    private ExperimentRules() {
    }

    public static Rule forExperiment(ExperimentType type, ExperimentTier tier) {
        Rule rule = RULES.getOrDefault(type, Map.of()).get(tier);
        if (rule == null) {
            throw new IllegalArgumentException(tier.displayName() + " is not available for " + type.displayName());
        }
        return rule;
    }

    public static boolean isUnlocked(SkyBlockPlayer player, ExperimentType type, ExperimentTier tier) {
        return player.getSkills().getCurrentLevel(net.swofty.type.skyblockgeneric.skill.SkillCategories.ENCHANTING)
                >= forExperiment(type, tier).requiredEnchantingLevel();
    }

    private static Map<ExperimentType, Map<ExperimentTier, Rule>> createRules() {
        Map<ExperimentType, Map<ExperimentTier, Rule>> rules = new EnumMap<>(ExperimentType.class);

        Map<ExperimentTier, Rule> superpairs = new EnumMap<>(ExperimentTier.class);
        superpairs.put(ExperimentTier.BEGINNER, superpairs(ExperimentTier.BEGINNER, 10, 25, 10, 7, 2,
                50, 75_000, 1, List.of(SuperPairItem.EXPERIENCE, SuperPairItem.EXPERIENCE_BOTTLE,
                        SuperPairItem.GUARDIAN_PET), List.of(SuperPairItem.EXTRA_CLICKS, SuperPairItem.EXTRA_CLICKS)));
        superpairs.put(ExperimentTier.HIGH, superpairs(ExperimentTier.HIGH, 20, 50, 12, 5, 4,
                100, 200_000, 1, List.of(SuperPairItem.EXPERIENCE, SuperPairItem.EXPERIENCE_BOTTLE,
                        SuperPairItem.GRAND_EXPERIENCE_BOTTLE,
                        SuperPairItem.TITANIC_EXPERIENCE_BOTTLE, SuperPairItem.GUARDIAN_PET),
                List.of(SuperPairItem.EXTRA_CLICK, SuperPairItem.EXTRA_CLICKS)));
        superpairs.put(ExperimentTier.GRAND, superpairs(ExperimentTier.GRAND, 25, 75, 12, 5, 4,
                150, 300_000, 1, grandRewards(),
                List.of(SuperPairItem.EXTRA_CLICKS, SuperPairItem.EXPERIENCE_POWERUP)));
        superpairs.put(ExperimentTier.SUPREME, superpairs(ExperimentTier.SUPREME, 30, 100, 14, 7, 4,
                200, 400_000, 1, endgameRewards(false),
                List.of(SuperPairItem.INSTANT_FIND, SuperPairItem.NEXT_CLICK_FREE)));
        superpairs.put(ExperimentTier.TRANSCENDENT, superpairs(ExperimentTier.TRANSCENDENT, 40, 200, 16, 7, 4,
                250, 500_000, 1, endgameRewards(false),
                List.of(SuperPairItem.INSTANT_FIND, SuperPairItem.NEXT_CLICK_FREE)));
        superpairs.put(ExperimentTier.METAPHYSICAL, superpairs(ExperimentTier.METAPHYSICAL, 50, 350, 12, 7, 4,
                300, 600_000, 2, endgameRewards(true),
                List.of(SuperPairItem.INSTANT_FIND, SuperPairItem.NEXT_CLICK_FREE)));
        rules.put(ExperimentType.SUPERPAIRS, Map.copyOf(superpairs));

        Map<ExperimentTier, Rule> chronomatron = new EnumMap<>(ExperimentTier.class);
        chronomatron.put(ExperimentTier.HIGH, chronomatron(ExperimentTier.HIGH, 20, 3, 1_500, 20));
        chronomatron.put(ExperimentTier.GRAND, chronomatron(ExperimentTier.GRAND, 25, 5, 2_500, 18));
        chronomatron.put(ExperimentTier.SUPREME, chronomatron(ExperimentTier.SUPREME, 30, 7, 3_500, 16));
        chronomatron.put(ExperimentTier.TRANSCENDENT, chronomatron(ExperimentTier.TRANSCENDENT, 35, 8, 4_500, 14));
        chronomatron.put(ExperimentTier.METAPHYSICAL, chronomatron(ExperimentTier.METAPHYSICAL, 40, 10, 6_000, 12));
        rules.put(ExperimentType.CHRONOMATRON, Map.copyOf(chronomatron));

        Map<ExperimentTier, Rule> ultrasequencer = new EnumMap<>(ExperimentTier.class);
        ultrasequencer.put(ExperimentTier.SUPREME, ultrasequencer(ExperimentTier.SUPREME, 25, 7, 3, 3_500));
        ultrasequencer.put(ExperimentTier.TRANSCENDENT, ultrasequencer(ExperimentTier.TRANSCENDENT, 30, 7, 4, 5_000));
        ultrasequencer.put(ExperimentTier.METAPHYSICAL, ultrasequencer(ExperimentTier.METAPHYSICAL, 40, 9, 4, 7_000));
        rules.put(ExperimentType.ULTRASEQUENCER, Map.copyOf(ultrasequencer));
        return Map.copyOf(rules);
    }

    private static List<SuperPairItem> grandRewards() {
        List<SuperPairItem> rewards = new ArrayList<>(List.of(
                SuperPairItem.EXPERIENCE, SuperPairItem.GRAND_EXPERIENCE_BOTTLE,
                SuperPairItem.TITANIC_EXPERIENCE_BOTTLE, SuperPairItem.GUARDIAN_PET,
                SuperPairItem.METAPHYSICAL_SERUM));
        rewards.addAll(GRAND_BOOKS);
        return List.copyOf(rewards);
    }

    private static List<SuperPairItem> endgameRewards(boolean includeFish) {
        List<SuperPairItem> rewards = new ArrayList<>(grandRewards());
        rewards.addAll(ULTRA_RARE_REWARDS);
        rewards.add(SuperPairItem.NADESHIKO_DYE);
        if (includeFish) rewards.add(SuperPairItem.EXPERIMENT_THE_FISH);
        return List.copyOf(rewards);
    }

    private static Rule superpairs(ExperimentTier tier, int requiredLevel, int startingCost, int baseClicks,
                                   int width, int height, int pairXp, int completionXp,
                                   int bonusClickMultiplier,
                                   List<SuperPairItem> rewards, List<SuperPairItem> powerUps) {
        return new Rule(ExperimentType.SUPERPAIRS, tier, requiredLevel, startingCost, 0, pairXp,
                completionXp, baseClicks, bonusClickMultiplier, 0, width, height, 0, 0, 90,
                List.of(), rewards, powerUps);
    }

    private static Rule chronomatron(ExperimentTier tier, int requiredLevel, int colors, int xpPerStep,
                                     int seconds) {
        return new Rule(ExperimentType.CHRONOMATRON, tier, requiredLevel, 0, repeatCost(tier), xpPerStep,
                0, 0, 1, colors, 0, 0, 49, 15, seconds, List.of(), List.of(), List.of());
    }

    private static Rule ultrasequencer(ExperimentTier tier, int requiredLevel, int width, int height,
                                       int xpPerStep) {
        return new Rule(ExperimentType.ULTRASEQUENCER, tier, requiredLevel, 0, repeatCost(tier), xpPerStep,
                0, 0, 1, 0, width, height, 20, 20, 20, List.of(), List.of(), List.of());
    }

    private static int repeatCost(ExperimentTier tier) {
        return switch (tier) {
            case HIGH -> 5;
            case GRAND -> 10;
            case SUPREME -> 15;
            case TRANSCENDENT -> 20;
            case METAPHYSICAL -> 40;
            case BEGINNER -> throw new IllegalArgumentException("Beginner has no add-on experiment");
        };
    }

    public record Rule(ExperimentType type, ExperimentTier tier, int requiredEnchantingLevel,
                       int startingCostLevels, int repeatAttemptCostLevels, int xpPerStep,
                       int completionXp, int baseClicks, int bonusClickMultiplier, int colorCount,
                       int boardWidth, int boardHeight,
                       int maximumScore, int xpCap, int deadlineSeconds, List<Integer> reservedSlots,
                       List<SuperPairItem> rewardPool, List<SuperPairItem> powerUps) {
        public Rule {
            reservedSlots = List.copyOf(reservedSlots);
            rewardPool = List.copyOf(rewardPool);
            powerUps = List.copyOf(powerUps);
            if (requiredEnchantingLevel < 0 || startingCostLevels < 0 || repeatAttemptCostLevels < 0
                    || xpPerStep < 0 || completionXp < 0 || baseClicks < 0 || bonusClickMultiplier < 1
                    || colorCount < 0 || deadlineSeconds <= 0) {
                throw new IllegalArgumentException("Experiment rule values must be non-negative");
            }
            if (type == ExperimentType.CHRONOMATRON && colorCount <= 0) {
                throw new IllegalArgumentException("Chronomatron must have at least one color");
            }
            if (type == ExperimentType.SUPERPAIRS
                    && (boardWidth <= 0 || boardHeight <= 0 || powerUps.size() != 2
                    || (boardWidth * boardHeight - powerUps.size()) % 2 != 0)) {
                throw new IllegalArgumentException("Superpairs board must contain two power-ups and complete pairs");
            }
            if (type == ExperimentType.SUPERPAIRS && rewardPool.stream().anyMatch(SuperPairItem::isPowerUp)) {
                throw new IllegalArgumentException("Superpairs reward pool cannot contain power-ups");
            }
            if (type == ExperimentType.SUPERPAIRS && powerUps.stream().anyMatch(item -> !item.isPowerUp())) {
                throw new IllegalArgumentException("Superpairs power-up pool must contain power-ups");
            }
            if (type == ExperimentType.SUPERPAIRS && new HashSet<>(rewardPool).size() != rewardPool.size()) {
                throw new IllegalArgumentException("Superpairs rewards must be unique");
            }
            if (type == ExperimentType.ULTRASEQUENCER && (boardWidth <= 0 || boardHeight <= 0)) {
                throw new IllegalArgumentException("Ultrasequencer board dimensions must be positive");
            }
            if ((type == ExperimentType.SUPERPAIRS || type == ExperimentType.ULTRASEQUENCER)
                    && boardWidth > 9) {
                throw new IllegalArgumentException("Experiment board width cannot exceed the inventory width");
            }
        }

        public boolean isUnlocked(SkyBlockPlayer player) {
            return player.getSkills().getCurrentLevel(net.swofty.type.skyblockgeneric.skill.SkillCategories.ENCHANTING)
                    >= requiredEnchantingLevel;
        }

        public int boardSize() {
            return boardWidth * boardHeight;
        }

        public int pairCount() {
            if (type != ExperimentType.SUPERPAIRS) return 0;
            return (boardSize() - powerUps.size()) / 2;
        }

        public int xpPerPair() {
            return xpPerStep;
        }

        public int superpairsBonusClicks(int earnedClicks) {
            if (type != ExperimentType.SUPERPAIRS) throw new IllegalStateException("Not a Superpairs rule");
            if (earnedClicks < 0) throw new IllegalArgumentException("Earned clicks cannot be negative");
            return earnedClicks * bonusClickMultiplier;
        }

        public boolean canContain(ExperimentReward reward) {
            if (type != ExperimentType.SUPERPAIRS || reward == null) return false;
            return rewardPool.stream().anyMatch(item -> item.reward() == reward);
        }

        public List<Integer> boardSlots() {
            if (type != ExperimentType.SUPERPAIRS && type != ExperimentType.ULTRASEQUENCER) return List.of();
            List<Integer> slots = new ArrayList<>(boardSize());
            int firstColumn = (9 - boardWidth) / 2;
            for (int row = 0; row < boardHeight; row++) {
                int start = row * 9 + 9 + firstColumn;
                for (int column = 0; column < boardWidth; column++) slots.add(start + column);
            }
            return List.copyOf(slots);
        }

        public List<Integer> slotsForColor(int color) {
            if (type != ExperimentType.CHRONOMATRON) throw new IllegalStateException("Not a Chronomatron rule");
            if (color < 0 || color >= colorCount) throw new IllegalArgumentException("Invalid experiment color: " + color);
            int colors = colorCount;
            if (colors <= 7) {
                int[] rows = switch (colors) {
                    case 3 -> new int[]{12, 21, 30};
                    case 5 -> new int[]{11, 20, 29};
                    default -> new int[]{10, 19, 28};
                };
                return java.util.Arrays.stream(rows).map(row -> row + color).boxed().toList();
            }
            int columns = colors == 8 ? 4 : 5;
            int group = color / columns;
            int localColor = color % columns;
            int[] rows = colors == 8
                    ? (group == 0 ? new int[]{11, 20} : new int[]{30, 39})
                    : (group == 0 ? new int[]{11, 20} : new int[]{29, 38});
            return java.util.Arrays.stream(rows).map(row -> row + localColor).boxed().toList();
        }

        public int bonusClicksForScore(int score) {
            return bonusClicksForScore(score, 0);
        }

        public int bonusClicksForScore(int score, int metaphysicalSerums) {
            if (metaphysicalSerums < 0 || metaphysicalSerums > 3) {
                throw new IllegalArgumentException("Serum count must be between 0 and 3");
            }
            if (type == ExperimentType.CHRONOMATRON) {
                if (score >= threshold(12, metaphysicalSerums) && tier == ExperimentTier.METAPHYSICAL) return 3;
                if (score >= threshold(9, metaphysicalSerums)) return 2;
                return score >= threshold(5, metaphysicalSerums) ? 1 : 0;
            }
            if (type == ExperimentType.ULTRASEQUENCER) {
                if (score >= threshold(9, metaphysicalSerums) && tier == ExperimentTier.METAPHYSICAL) return 3;
                if (score >= threshold(7, metaphysicalSerums)) return 2;
                return score >= threshold(5, metaphysicalSerums) ? 1 : 0;
            }
            return 0;
        }

        private static int threshold(int base, int metaphysicalSerums) {
            return Math.max(1, base - metaphysicalSerums);
        }
    }
}
