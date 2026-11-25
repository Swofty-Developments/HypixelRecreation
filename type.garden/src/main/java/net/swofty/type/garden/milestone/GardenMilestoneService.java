package net.swofty.type.garden.milestone;

import net.swofty.commons.StringUtility;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.gui.GardenGuiSupport;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GardenMilestoneService {
    private Map<String, Object> config = Map.of();
    private final Map<String, CropMilestoneDefinition> cropDefinitions = new LinkedHashMap<>();
    private final Map<String, VisitorMilestoneTrack> visitorTracks = new LinkedHashMap<>();

    public void reload() {
        config = GardenConfigRegistry.getConfig("milestones.yml");

        List<MilestoneTier> cropRewardTiers = parseTierList(
            GardenConfigRegistry.getMapList(config, "crop_reward_tiers")
        );

        Map<String, List<Long>> cropGroups = new LinkedHashMap<>();
        GardenConfigRegistry.getSection(config, "crop_groups").forEach((groupId, rawValue) -> {
            if (!(rawValue instanceof Map<?, ?> groupMapRaw)) {
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> groupMap = (Map<String, Object>) groupMapRaw;
            List<Long> thresholds = GardenConfigRegistry.getList(groupMap, "thresholds").stream()
                .map(this::toLong)
                .filter(Objects::nonNull)
                .toList();
            if (!thresholds.isEmpty()) {
                cropGroups.put(normalizeKey(groupId), thresholds);
            }
        });

        cropDefinitions.clear();
        for (Map<String, Object> cropEntry : GardenConfigRegistry.getMapList(config, "crop_milestones")) {
            String cropId = normalizeKey(GardenConfigRegistry.getString(cropEntry, "id", ""));
            String groupId = normalizeKey(GardenConfigRegistry.getString(cropEntry, "group", ""));
            List<Long> thresholds = cropGroups.get(groupId);
            if (cropId.isBlank() || thresholds == null || thresholds.isEmpty()) {
                continue;
            }

            cropDefinitions.put(cropId, new CropMilestoneDefinition(
                cropId,
                GardenConfigRegistry.getString(cropEntry, "display_name", StringUtility.toNormalCase(cropId)),
                GardenConfigRegistry.getString(cropEntry, "icon", cropId),
                combineThresholdsWithRewards(thresholds, cropRewardTiers)
            ));
        }

        visitorTracks.clear();
        GardenConfigRegistry.getSection(config, "visitor_tracks").forEach((trackId, rawValue) -> {
            if (!(rawValue instanceof Map<?, ?> trackMapRaw)) {
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> trackMap = (Map<String, Object>) trackMapRaw;
            List<MilestoneTier> tiers = parseTierList(GardenConfigRegistry.getMapList(trackMap, "tiers"));
            if (tiers.isEmpty()) {
                return;
            }
            String normalizedTrack = normalizeKey(trackId);
            visitorTracks.put(normalizedTrack, new VisitorMilestoneTrack(
                normalizedTrack,
                GardenConfigRegistry.getString(trackMap, "display_name", StringUtility.toNormalCase(normalizedTrack)),
                GardenConfigRegistry.getString(trackMap, "icon", "BOOK"),
                tiers
            ));
        });
    }

    public List<CropMilestoneDefinition> getCropDefinitions() {
        return new ArrayList<>(cropDefinitions.values());
    }

    public CropMilestoneDefinition getCropDefinition(String cropId) {
        return cropDefinitions.get(normalizeKey(cropId));
    }

    public List<VisitorMilestoneTrack> getVisitorTracks() {
        return new ArrayList<>(visitorTracks.values());
    }

    public VisitorMilestoneTrack getVisitorTrack(String trackId) {
        return visitorTracks.get(normalizeKey(trackId));
    }

    public MilestoneAdvanceResult advanceCropMilestone(SkyBlockPlayer player, String cropId, long amount) {
        CropMilestoneDefinition definition = getCropDefinition(cropId);
        if (definition == null || amount <= 0L) {
            return MilestoneAdvanceResult.NONE;
        }

        GardenData.GardenCoreData core = GardenGuiSupport.core(player);
        long updatedProgress = core.getCropMilestoneProgress().getOrDefault(definition.id(), 0L) + amount;
        core.getCropMilestoneProgress().put(definition.id(), updatedProgress);

        int currentTier = core.getCropMilestones().getOrDefault(definition.id(), 0);
        List<MilestoneTier> unlockedTiers = new ArrayList<>();
        while (currentTier < definition.tiers().size()
            && updatedProgress >= definition.tiers().get(currentTier).cumulativeAmount()) {
            MilestoneTier unlocked = definition.tiers().get(currentTier);
            unlockedTiers.add(unlocked);
            awardMilestone(player, unlocked, definition.displayName(), "Crop");
            currentTier++;
        }

        core.getCropMilestones().put(definition.id(), currentTier);
        return new MilestoneAdvanceResult(updatedProgress, currentTier, unlockedTiers);
    }

    public MilestoneAdvanceResult advanceVisitorMilestone(SkyBlockPlayer player, String trackId, long amount) {
        VisitorMilestoneTrack track = getVisitorTrack(trackId);
        if (track == null || amount <= 0L) {
            return MilestoneAdvanceResult.NONE;
        }

        GardenData.GardenCoreData core = GardenGuiSupport.core(player);
        long fallbackProgress = switch (track.id()) {
            case "UNIQUE_SERVED" -> core.getServedUniqueVisitors().size();
            case "OFFERS_ACCEPTED" -> GardenGuiSupport.visitors(player).getServedCounts().values().stream()
                .mapToLong(Integer::longValue)
                .sum();
            default -> 0L;
        };
        long storedProgress = core.getVisitorMilestoneProgress().getOrDefault(track.id(), 0L);
        long baselineProgress = Math.max(storedProgress, Math.max(0L, fallbackProgress - amount));
        long updatedProgress = baselineProgress + amount;
        core.getVisitorMilestoneProgress().put(track.id(), updatedProgress);

        int currentTier = core.getVisitorMilestones().getOrDefault(track.id(), 0);
        List<MilestoneTier> unlockedTiers = new ArrayList<>();
        while (currentTier < track.tiers().size()
            && updatedProgress >= track.tiers().get(currentTier).cumulativeAmount()) {
            MilestoneTier unlocked = track.tiers().get(currentTier);
            unlockedTiers.add(unlocked);
            awardMilestone(player, unlocked, track.displayName(), "Visitor");
            currentTier++;
        }

        core.getVisitorMilestones().put(track.id(), currentTier);
        return new MilestoneAdvanceResult(updatedProgress, currentTier, unlockedTiers);
    }

    public MilestoneProgress getCropProgress(SkyBlockPlayer player, String cropId) {
        CropMilestoneDefinition definition = getCropDefinition(cropId);
        if (definition == null) {
            return MilestoneProgress.EMPTY;
        }

        GardenData.GardenCoreData core = GardenGuiSupport.core(player);
        long progress = core.getCropMilestoneProgress().getOrDefault(definition.id(), 0L);
        int completedTiers = core.getCropMilestones().getOrDefault(definition.id(), 0);
        return buildProgress(progress, completedTiers, definition.tiers());
    }

    public MilestoneProgress getVisitorProgress(SkyBlockPlayer player, String trackId) {
        VisitorMilestoneTrack track = getVisitorTrack(trackId);
        if (track == null) {
            return MilestoneProgress.EMPTY;
        }

        GardenData.GardenCoreData core = GardenGuiSupport.core(player);
        long fallbackProgress = switch (track.id()) {
            case "UNIQUE_SERVED" -> core.getServedUniqueVisitors().size();
            case "OFFERS_ACCEPTED" -> GardenGuiSupport.visitors(player).getServedCounts().values().stream()
                .mapToLong(Integer::longValue)
                .sum();
            default -> 0L;
        };
        long progress = Math.max(core.getVisitorMilestoneProgress().getOrDefault(track.id(), 0L), fallbackProgress);
        int completedTiers = core.getVisitorMilestones().getOrDefault(track.id(), 0);
        return buildProgress(progress, completedTiers, track.tiers());
    }

    private void awardMilestone(SkyBlockPlayer player, MilestoneTier tier, String displayName, String category) {
        GardenData.GardenCoreData core = GardenGuiSupport.core(player);
        core.setExperience(core.getExperience() + tier.gardenXp());
        player.getSkills().increase(player, SkillCategories.FARMING, (double) tier.farmingXp());

        player.sendMessage("§a" + category + " Milestone §e" + displayName + " §7reached Tier §6"
            + StringUtility.getAsRomanNumeral(tier.tier()) + "§7!");
        player.sendMessage("§8+§3" + StringUtility.commaify(tier.farmingXp()) + " Farming XP §8| §2+"
            + tier.gardenXp() + " Garden XP");
        player.playSuccessSound();
    }

    private List<MilestoneTier> combineThresholdsWithRewards(List<Long> thresholds, List<MilestoneTier> rewardTiers) {
        List<MilestoneTier> tiers = new ArrayList<>();
        long cumulative = 0L;
        for (int index = 0; index < Math.min(thresholds.size(), rewardTiers.size()); index++) {
            cumulative += thresholds.get(index);
            MilestoneTier rewardTier = rewardTiers.get(index);
            tiers.add(new MilestoneTier(
                index + 1,
                thresholds.get(index),
                cumulative,
                rewardTier.farmingXp(),
                rewardTier.gardenXp(),
                rewardTier.skyblockXp()
            ));
        }
        return tiers;
    }

    private List<MilestoneTier> parseTierList(List<Map<String, Object>> entries) {
        List<MilestoneTier> tiers = new ArrayList<>();
        long cumulative = 0L;
        for (Map<String, Object> entry : entries) {
            long amount = GardenConfigRegistry.getLong(entry, "amount", 0L);
            if (amount <= 0L) {
                continue;
            }
            cumulative += amount;
            tiers.add(new MilestoneTier(
                GardenConfigRegistry.getInt(entry, "tier", tiers.size() + 1),
                amount,
                cumulative,
                GardenConfigRegistry.getLong(entry, "farming_xp", 0L),
                GardenConfigRegistry.getInt(entry, "garden_xp", 0),
                GardenConfigRegistry.getInt(entry, "skyblock_xp", 0)
            ));
        }
        return tiers;
    }

    private MilestoneProgress buildProgress(long progress, int completedTiers, List<MilestoneTier> tiers) {
        if (tiers.isEmpty()) {
            return MilestoneProgress.EMPTY;
        }

        int clampedCompletedTiers = Math.max(0, Math.min(completedTiers, tiers.size()));
        long previousThreshold = clampedCompletedTiers <= 0 ? 0L : tiers.get(clampedCompletedTiers - 1).cumulativeAmount();
        MilestoneTier nextTier = clampedCompletedTiers >= tiers.size() ? null : tiers.get(clampedCompletedTiers);
        MilestoneTier currentTier = clampedCompletedTiers <= 0 ? null : tiers.get(clampedCompletedTiers - 1);
        return new MilestoneProgress(
            progress,
            clampedCompletedTiers,
            previousThreshold,
            nextTier == null ? previousThreshold : nextTier.cumulativeAmount(),
            currentTier,
            nextTier
        );
    }

    private String normalizeKey(String key) {
        if (key == null) {
            return "";
        }
        return key.trim().replace(' ', '_').toUpperCase();
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public record MilestoneTier(int tier, long amount, long cumulativeAmount, long farmingXp, int gardenXp,
                                int skyblockXp) {
    }

    public record CropMilestoneDefinition(String id, String displayName, String iconItemId, List<MilestoneTier> tiers) {
    }

    public record VisitorMilestoneTrack(String id, String displayName, String iconItemId, List<MilestoneTier> tiers) {
    }

    public record MilestoneProgress(
        long progress,
        int completedTiers,
        long previousThreshold,
        long nextThreshold,
        MilestoneTier currentTier,
        MilestoneTier nextTier
    ) {
        public static final MilestoneProgress EMPTY = new MilestoneProgress(0L, 0, 0L, 0L, null, null);
    }

    public record MilestoneAdvanceResult(long progress, int completedTiers, List<MilestoneTier> unlockedTiers) {
        public static final MilestoneAdvanceResult NONE = new MilestoneAdvanceResult(0L, 0, List.of());
    }
}
