package net.swofty.type.skyblockgeneric.garden;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GardenData {
    private GardenData() {
    }

    public static class GardenCoreData {
        private long experience = 0;
        private int level = 1;
        private Set<String> unlockedPlots = new LinkedHashSet<>(Set.of("central"));
        private Map<String, Double> cleanedPlots = new HashMap<>();
        private Map<String, String> plotPresets = new HashMap<>();
        private Map<String, Integer> cropUpgrades = new HashMap<>();
        private String selectedBarnSkin = "default";
        private Set<String> ownedBarnSkins = new LinkedHashSet<>(Set.of("default"));
        private long copper = 0;
        private Set<String> servedUniqueVisitors = new LinkedHashSet<>();
        private Map<String, Integer> gardenMilestones = new HashMap<>();
        private Map<String, Integer> cropMilestones = new HashMap<>();
        private Map<String, Long> cropMilestoneProgress = new HashMap<>();
        private Map<String, Integer> visitorMilestones = new HashMap<>();
        private Map<String, Long> visitorMilestoneProgress = new HashMap<>();
        private Set<String> skyMartPurchases = new LinkedHashSet<>();
        private Set<String> tutorialFlags = new LinkedHashSet<>();
        private Set<String> visitorRequirementFlags = new LinkedHashSet<>();
        private Map<String, Long> visitorRequirementCounters = new HashMap<>();
        private String selectedTimeMode = "DYNAMIC";
        private long lastActiveAt = 0;

        public long getExperience() {
            return experience;
        }

        public void setExperience(long experience) {
            this.experience = experience;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public Set<String> getUnlockedPlots() {
            return unlockedPlots;
        }

        public void setUnlockedPlots(Set<String> unlockedPlots) {
            this.unlockedPlots = unlockedPlots;
        }

        public Map<String, Double> getCleanedPlots() {
            return cleanedPlots;
        }

        public void setCleanedPlots(Map<String, Double> cleanedPlots) {
            this.cleanedPlots = cleanedPlots;
        }

        public Map<String, String> getPlotPresets() {
            return plotPresets;
        }

        public void setPlotPresets(Map<String, String> plotPresets) {
            this.plotPresets = plotPresets;
        }

        public Map<String, Integer> getCropUpgrades() {
            return cropUpgrades;
        }

        public void setCropUpgrades(Map<String, Integer> cropUpgrades) {
            this.cropUpgrades = cropUpgrades;
        }

        public String getSelectedBarnSkin() {
            return selectedBarnSkin;
        }

        public void setSelectedBarnSkin(String selectedBarnSkin) {
            this.selectedBarnSkin = selectedBarnSkin;
        }

        public Set<String> getOwnedBarnSkins() {
            return ownedBarnSkins;
        }

        public void setOwnedBarnSkins(Set<String> ownedBarnSkins) {
            this.ownedBarnSkins = ownedBarnSkins;
        }

        public long getCopper() {
            return copper;
        }

        public void setCopper(long copper) {
            this.copper = copper;
        }

        public Set<String> getServedUniqueVisitors() {
            return servedUniqueVisitors;
        }

        public void setServedUniqueVisitors(Set<String> servedUniqueVisitors) {
            this.servedUniqueVisitors = servedUniqueVisitors;
        }

        public Map<String, Integer> getGardenMilestones() {
            return gardenMilestones;
        }

        public void setGardenMilestones(Map<String, Integer> gardenMilestones) {
            this.gardenMilestones = gardenMilestones;
        }

        public Map<String, Integer> getCropMilestones() {
            return cropMilestones;
        }

        public void setCropMilestones(Map<String, Integer> cropMilestones) {
            this.cropMilestones = cropMilestones;
        }

        public Map<String, Long> getCropMilestoneProgress() {
            return cropMilestoneProgress;
        }

        public void setCropMilestoneProgress(Map<String, Long> cropMilestoneProgress) {
            this.cropMilestoneProgress = cropMilestoneProgress;
        }

        public Map<String, Integer> getVisitorMilestones() {
            return visitorMilestones;
        }

        public void setVisitorMilestones(Map<String, Integer> visitorMilestones) {
            this.visitorMilestones = visitorMilestones;
        }

        public Map<String, Long> getVisitorMilestoneProgress() {
            return visitorMilestoneProgress;
        }

        public void setVisitorMilestoneProgress(Map<String, Long> visitorMilestoneProgress) {
            this.visitorMilestoneProgress = visitorMilestoneProgress;
        }

        public Set<String> getSkyMartPurchases() {
            return skyMartPurchases;
        }

        public void setSkyMartPurchases(Set<String> skyMartPurchases) {
            this.skyMartPurchases = skyMartPurchases;
        }

        public Set<String> getTutorialFlags() {
            return tutorialFlags;
        }

        public void setTutorialFlags(Set<String> tutorialFlags) {
            this.tutorialFlags = tutorialFlags;
        }

        public Set<String> getVisitorRequirementFlags() {
            return visitorRequirementFlags;
        }

        public void setVisitorRequirementFlags(Set<String> visitorRequirementFlags) {
            this.visitorRequirementFlags = visitorRequirementFlags;
        }

        public Map<String, Long> getVisitorRequirementCounters() {
            return visitorRequirementCounters;
        }

        public void setVisitorRequirementCounters(Map<String, Long> visitorRequirementCounters) {
            this.visitorRequirementCounters = visitorRequirementCounters;
        }

        public String getSelectedTimeMode() {
            return selectedTimeMode;
        }

        public void setSelectedTimeMode(String selectedTimeMode) {
            this.selectedTimeMode = selectedTimeMode;
        }

        public long getLastActiveAt() {
            return lastActiveAt;
        }

        public void setLastActiveAt(long lastActiveAt) {
            this.lastActiveAt = lastActiveAt;
        }
    }

    public static class GardenVisitorsData {
        private List<GardenVisitorState> activeVisitors = new ArrayList<>();
        private List<GardenVisitorState> queuedVisitors = new ArrayList<>();
        private Map<String, Integer> visitCounts = new HashMap<>();
        private Map<String, Integer> servedCounts = new HashMap<>();
        private Set<String> logbookEntries = new LinkedHashSet<>();
        private long nextArrivalAt = 0;
        private long lastProcessedAt = 0;
        private long lastFarmingActivityAt = 0;

        public List<GardenVisitorState> getActiveVisitors() {
            return activeVisitors;
        }

        public void setActiveVisitors(List<GardenVisitorState> activeVisitors) {
            this.activeVisitors = activeVisitors;
        }

        public List<GardenVisitorState> getQueuedVisitors() {
            return queuedVisitors;
        }

        public void setQueuedVisitors(List<GardenVisitorState> queuedVisitors) {
            this.queuedVisitors = queuedVisitors;
        }

        public Map<String, Integer> getServedCounts() {
            return servedCounts;
        }

        public void setServedCounts(Map<String, Integer> servedCounts) {
            this.servedCounts = servedCounts;
        }

        public Map<String, Integer> getVisitCounts() {
            return visitCounts;
        }

        public void setVisitCounts(Map<String, Integer> visitCounts) {
            this.visitCounts = visitCounts;
        }

        public Set<String> getLogbookEntries() {
            return logbookEntries;
        }

        public void setLogbookEntries(Set<String> logbookEntries) {
            this.logbookEntries = logbookEntries;
        }

        public long getNextArrivalAt() {
            return nextArrivalAt;
        }

        public void setNextArrivalAt(long nextArrivalAt) {
            this.nextArrivalAt = nextArrivalAt;
        }

        public long getLastProcessedAt() {
            return lastProcessedAt;
        }

        public void setLastProcessedAt(long lastProcessedAt) {
            this.lastProcessedAt = lastProcessedAt;
        }

        public long getLastFarmingActivityAt() {
            return lastFarmingActivityAt;
        }

        public void setLastFarmingActivityAt(long lastFarmingActivityAt) {
            this.lastFarmingActivityAt = lastFarmingActivityAt;
        }
    }

    public static class GardenVisitorState {
        private String visitorId = "";
        private String rarity = "UNCOMMON";
        private List<GardenRequest> requests = new ArrayList<>();
        private long farmingXp = 0;
        private int gardenXp = 0;
        private int copper = 0;
        private int bits = 0;
        private List<Object> guaranteedRewards = new ArrayList<>();
        private List<Object> bonusRewards = new ArrayList<>();
        private long arrivedAt = 0;
        private boolean queued = false;

        public String getVisitorId() {
            return visitorId;
        }

        public void setVisitorId(String visitorId) {
            this.visitorId = visitorId;
        }

        public String getRarity() {
            return rarity;
        }

        public void setRarity(String rarity) {
            this.rarity = rarity;
        }

        public List<GardenRequest> getRequests() {
            return requests;
        }

        public void setRequests(List<GardenRequest> requests) {
            this.requests = requests;
        }

        public long getFarmingXp() {
            return farmingXp;
        }

        public void setFarmingXp(long farmingXp) {
            this.farmingXp = farmingXp;
        }

        public int getGardenXp() {
            return gardenXp;
        }

        public void setGardenXp(int gardenXp) {
            this.gardenXp = gardenXp;
        }

        public int getCopper() {
            return copper;
        }

        public void setCopper(int copper) {
            this.copper = copper;
        }

        public int getBits() {
            return bits;
        }

        public void setBits(int bits) {
            this.bits = bits;
        }

        public List<GardenRewardState> getGuaranteedRewards() {
            return normalizeRewards(guaranteedRewards);
        }

        public void setGuaranteedRewards(List<?> guaranteedRewards) {
            this.guaranteedRewards = guaranteedRewards == null ? new ArrayList<>() : new ArrayList<>(guaranteedRewards);
        }

        public List<GardenRewardState> getBonusRewards() {
            return normalizeRewards(bonusRewards);
        }

        public void setBonusRewards(List<?> bonusRewards) {
            this.bonusRewards = bonusRewards == null ? new ArrayList<>() : new ArrayList<>(bonusRewards);
        }

        public long getArrivedAt() {
            return arrivedAt;
        }

        public void setArrivedAt(long arrivedAt) {
            this.arrivedAt = arrivedAt;
        }

        public boolean isQueued() {
            return queued;
        }

        public void setQueued(boolean queued) {
            this.queued = queued;
        }

        private static List<GardenRewardState> normalizeRewards(List<Object> rawRewards) {
            List<GardenRewardState> normalized = new ArrayList<>();
            if (rawRewards == null) {
                return normalized;
            }
            for (Object rawReward : rawRewards) {
                if (rawReward instanceof GardenRewardState rewardState) {
                    normalized.add(rewardState);
                    continue;
                }
                if (rawReward instanceof Map<?, ?> rewardMap) {
                    GardenRewardState rewardState = new GardenRewardState();
                    rewardState.setType(String.valueOf(rewardMap.containsKey("type") ? rewardMap.get("type") : "PROFILE_FLAG"));
                    rewardState.setKey(String.valueOf(rewardMap.containsKey("key") ? rewardMap.get("key") : ""));
                    rewardState.setAmount(parseLong(rewardMap.get("amount"), 0L));
                    rewardState.setMin(parseLong(rewardMap.get("min"), 0L));
                    rewardState.setMax(parseLong(rewardMap.get("max"), 0L));
                    rewardState.setPoolId(String.valueOf(rewardMap.containsKey("poolId") ? rewardMap.get("poolId") : ""));
                    rewardState.setFirstVisitOnly(Boolean.parseBoolean(String.valueOf(
                        rewardMap.containsKey("firstVisitOnly") ? rewardMap.get("firstVisitOnly") : false
                    )));
                    rewardState.setDisplayOverride(String.valueOf(
                        rewardMap.containsKey("displayOverride") ? rewardMap.get("displayOverride") : ""
                    ));
                    Object metadata = rewardMap.get("metadata");
                    if (metadata instanceof Map<?, ?> metadataMap) {
                        Map<String, String> normalizedMetadata = new LinkedHashMap<>();
                        metadataMap.forEach((metadataKey, metadataValue) -> normalizedMetadata.put(
                            String.valueOf(metadataKey),
                            String.valueOf(metadataValue)
                        ));
                        rewardState.setMetadata(normalizedMetadata);
                    }
                    normalized.add(rewardState);
                    continue;
                }

                GardenRewardState rewardState = new GardenRewardState();
                rewardState.setType("LEGACY");
                rewardState.setKey(String.valueOf(rawReward));
                normalized.add(rewardState);
            }
            return normalized;
        }

        private static long parseLong(Object rawValue, long defaultValue) {
            if (rawValue instanceof Number number) {
                return number.longValue();
            }
            if (rawValue == null) {
                return defaultValue;
            }
            try {
                return Long.parseLong(String.valueOf(rawValue));
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
    }

    public static class GardenRequest {
        private String itemId = "";
        private int amount = 0;
        private double itemQuantityMultiplier = 1;

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public double getItemQuantityMultiplier() {
            return itemQuantityMultiplier;
        }

        public void setItemQuantityMultiplier(double itemQuantityMultiplier) {
            this.itemQuantityMultiplier = itemQuantityMultiplier;
        }
    }

    public static class GardenRewardState {
        private String type = "";
        private String key = "";
        private long amount = 0;
        private long min = 0;
        private long max = 0;
        private String poolId = "";
        private boolean firstVisitOnly = false;
        private String displayOverride = "";
        private Map<String, String> metadata = new LinkedHashMap<>();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public long getMin() {
            return min;
        }

        public void setMin(long min) {
            this.min = min;
        }

        public long getMax() {
            return max;
        }

        public void setMax(long max) {
            this.max = max;
        }

        public String getPoolId() {
            return poolId;
        }

        public void setPoolId(String poolId) {
            this.poolId = poolId;
        }

        public boolean isFirstVisitOnly() {
            return firstVisitOnly;
        }

        public void setFirstVisitOnly(boolean firstVisitOnly) {
            this.firstVisitOnly = firstVisitOnly;
        }

        public String getDisplayOverride() {
            return displayOverride;
        }

        public void setDisplayOverride(String displayOverride) {
            this.displayOverride = displayOverride;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }
    }

    public static class GardenPestsData {
        private List<GardenPestState> activePests = new ArrayList<>();
        private int storedPests = 0;
        private long cooldownEndsAt = 0;
        private long lastSpawnCheckAt = 0;
        private long offlineAccumulatorMs = 0;
        private long repellentEndsAt = 0;
        private boolean repellentMax = false;

        public List<GardenPestState> getActivePests() {
            return activePests;
        }

        public void setActivePests(List<GardenPestState> activePests) {
            this.activePests = activePests;
        }

        public int getStoredPests() {
            return storedPests;
        }

        public void setStoredPests(int storedPests) {
            this.storedPests = storedPests;
        }

        public long getCooldownEndsAt() {
            return cooldownEndsAt;
        }

        public void setCooldownEndsAt(long cooldownEndsAt) {
            this.cooldownEndsAt = cooldownEndsAt;
        }

        public long getLastSpawnCheckAt() {
            return lastSpawnCheckAt;
        }

        public void setLastSpawnCheckAt(long lastSpawnCheckAt) {
            this.lastSpawnCheckAt = lastSpawnCheckAt;
        }

        public long getOfflineAccumulatorMs() {
            return offlineAccumulatorMs;
        }

        public void setOfflineAccumulatorMs(long offlineAccumulatorMs) {
            this.offlineAccumulatorMs = offlineAccumulatorMs;
        }

        public long getRepellentEndsAt() {
            return repellentEndsAt;
        }

        public void setRepellentEndsAt(long repellentEndsAt) {
            this.repellentEndsAt = repellentEndsAt;
        }

        public boolean isRepellentMax() {
            return repellentMax;
        }

        public void setRepellentMax(boolean repellentMax) {
            this.repellentMax = repellentMax;
        }
    }

    public static class GardenPestState {
        private String pestId = "";
        private String plotId = "";
        private long spawnedAt = 0;
        private boolean offlineSpawned = false;
        private boolean trapSpawned = false;

        public String getPestId() {
            return pestId;
        }

        public void setPestId(String pestId) {
            this.pestId = pestId;
        }

        public String getPlotId() {
            return plotId;
        }

        public void setPlotId(String plotId) {
            this.plotId = plotId;
        }

        public long getSpawnedAt() {
            return spawnedAt;
        }

        public void setSpawnedAt(long spawnedAt) {
            this.spawnedAt = spawnedAt;
        }

        public boolean isOfflineSpawned() {
            return offlineSpawned;
        }

        public void setOfflineSpawned(boolean offlineSpawned) {
            this.offlineSpawned = offlineSpawned;
        }

        public boolean isTrapSpawned() {
            return trapSpawned;
        }

        public void setTrapSpawned(boolean trapSpawned) {
            this.trapSpawned = trapSpawned;
        }
    }

    public static class GardenComposterData {
        private double organicMatter = 0;
        private double fuel = 0;
        private int compostAvailable = 0;
        private long lastUpdatedAt = 0;
        private Map<String, Integer> upgrades = new HashMap<>();
        private Map<String, Long> insertedMatter = new HashMap<>();
        private Map<String, Long> insertedFuel = new HashMap<>();

        public double getOrganicMatter() {
            return organicMatter;
        }

        public void setOrganicMatter(double organicMatter) {
            this.organicMatter = organicMatter;
        }

        public double getFuel() {
            return fuel;
        }

        public void setFuel(double fuel) {
            this.fuel = fuel;
        }

        public int getCompostAvailable() {
            return compostAvailable;
        }

        public void setCompostAvailable(int compostAvailable) {
            this.compostAvailable = compostAvailable;
        }

        public long getLastUpdatedAt() {
            return lastUpdatedAt;
        }

        public void setLastUpdatedAt(long lastUpdatedAt) {
            this.lastUpdatedAt = lastUpdatedAt;
        }

        public Map<String, Integer> getUpgrades() {
            return upgrades;
        }

        public void setUpgrades(Map<String, Integer> upgrades) {
            this.upgrades = upgrades;
        }

        public Map<String, Long> getInsertedMatter() {
            return insertedMatter;
        }

        public void setInsertedMatter(Map<String, Long> insertedMatter) {
            this.insertedMatter = insertedMatter;
        }

        public Map<String, Long> getInsertedFuel() {
            return insertedFuel;
        }

        public void setInsertedFuel(Map<String, Long> insertedFuel) {
            this.insertedFuel = insertedFuel;
        }
    }

    public static class GardenGreenhouseData {
        private boolean blueprintUnlocked = false;
        private boolean greenhouseUnlocked = false;
        private int unlockedGreenhouses = 0;
        private int unlockedPlots = 0;
        private int spentEtherealVines = 0;
        private Set<String> unlockedSlots = new LinkedHashSet<>();
        private Map<String, Integer> upgrades = new HashMap<>();
        private Set<String> analyzedMutations = new LinkedHashSet<>();
        private Map<String, Integer> mutationHarvests = new HashMap<>();
        private int dnaMilestone = 0;
        private Set<String> ownedGreenhouseSkins = new LinkedHashSet<>(Set.of("default"));
        private String selectedGreenhouseSkin = "default";

        public boolean isBlueprintUnlocked() {
            return blueprintUnlocked;
        }

        public void setBlueprintUnlocked(boolean blueprintUnlocked) {
            this.blueprintUnlocked = blueprintUnlocked;
        }

        public boolean isGreenhouseUnlocked() {
            return greenhouseUnlocked;
        }

        public void setGreenhouseUnlocked(boolean greenhouseUnlocked) {
            this.greenhouseUnlocked = greenhouseUnlocked;
        }

        public int getUnlockedGreenhouses() {
            return unlockedGreenhouses;
        }

        public void setUnlockedGreenhouses(int unlockedGreenhouses) {
            this.unlockedGreenhouses = unlockedGreenhouses;
        }

        public int getUnlockedPlots() {
            return unlockedPlots;
        }

        public void setUnlockedPlots(int unlockedPlots) {
            this.unlockedPlots = unlockedPlots;
        }

        public int getSpentEtherealVines() {
            return spentEtherealVines;
        }

        public void setSpentEtherealVines(int spentEtherealVines) {
            this.spentEtherealVines = spentEtherealVines;
        }

        public Set<String> getUnlockedSlots() {
            return unlockedSlots;
        }

        public void setUnlockedSlots(Set<String> unlockedSlots) {
            this.unlockedSlots = unlockedSlots;
        }

        public Map<String, Integer> getUpgrades() {
            return upgrades;
        }

        public void setUpgrades(Map<String, Integer> upgrades) {
            this.upgrades = upgrades;
        }

        public Set<String> getAnalyzedMutations() {
            return analyzedMutations;
        }

        public void setAnalyzedMutations(Set<String> analyzedMutations) {
            this.analyzedMutations = analyzedMutations;
        }

        public Map<String, Integer> getMutationHarvests() {
            return mutationHarvests;
        }

        public void setMutationHarvests(Map<String, Integer> mutationHarvests) {
            this.mutationHarvests = mutationHarvests;
        }

        public int getDnaMilestone() {
            return dnaMilestone;
        }

        public void setDnaMilestone(int dnaMilestone) {
            this.dnaMilestone = dnaMilestone;
        }

        public Set<String> getOwnedGreenhouseSkins() {
            return ownedGreenhouseSkins;
        }

        public void setOwnedGreenhouseSkins(Set<String> ownedGreenhouseSkins) {
            this.ownedGreenhouseSkins = ownedGreenhouseSkins;
        }

        public String getSelectedGreenhouseSkin() {
            return selectedGreenhouseSkin;
        }

        public void setSelectedGreenhouseSkin(String selectedGreenhouseSkin) {
            this.selectedGreenhouseSkin = selectedGreenhouseSkin;
        }
    }

    public static class GardenPersonalData {
        private double sowdust = 0;
        private int jacobsTickets = 0;
        private Map<String, Integer> medals = new HashMap<>();
        private Map<String, GardenChipProgress> chips = new HashMap<>();
        private Map<String, Long> contestPersonalBests = new HashMap<>();
        private Set<String> claimedContestRewards = new LinkedHashSet<>();
        private Set<String> spokenNpcFlags = new LinkedHashSet<>();
        private Set<String> anitaPurchases = new LinkedHashSet<>();
        private Set<String> tutorialFlags = new LinkedHashSet<>();
        private Set<String> visitorRequirementFlags = new LinkedHashSet<>();
        private Map<String, Long> visitorRequirementCounters = new HashMap<>();
        private Set<String> donatedItems = new LinkedHashSet<>();
        private Map<String, Long> exportedItems = new HashMap<>();
        private Set<String> accessFlags = new LinkedHashSet<>();

        public double getSowdust() {
            return sowdust;
        }

        public void setSowdust(double sowdust) {
            this.sowdust = sowdust;
        }

        public int getJacobsTickets() {
            return jacobsTickets;
        }

        public void setJacobsTickets(int jacobsTickets) {
            this.jacobsTickets = jacobsTickets;
        }

        public Map<String, Integer> getMedals() {
            return medals;
        }

        public void setMedals(Map<String, Integer> medals) {
            this.medals = medals;
        }

        public Map<String, GardenChipProgress> getChips() {
            return chips;
        }

        public void setChips(Map<String, GardenChipProgress> chips) {
            this.chips = chips;
        }

        public Map<String, Long> getContestPersonalBests() {
            return contestPersonalBests;
        }

        public void setContestPersonalBests(Map<String, Long> contestPersonalBests) {
            this.contestPersonalBests = contestPersonalBests;
        }

        public Set<String> getClaimedContestRewards() {
            return claimedContestRewards;
        }

        public void setClaimedContestRewards(Set<String> claimedContestRewards) {
            this.claimedContestRewards = claimedContestRewards;
        }

        public Set<String> getSpokenNpcFlags() {
            return spokenNpcFlags;
        }

        public void setSpokenNpcFlags(Set<String> spokenNpcFlags) {
            this.spokenNpcFlags = spokenNpcFlags;
        }

        public Set<String> getAnitaPurchases() {
            return anitaPurchases;
        }

        public void setAnitaPurchases(Set<String> anitaPurchases) {
            this.anitaPurchases = anitaPurchases;
        }

        public Set<String> getTutorialFlags() {
            return tutorialFlags;
        }

        public void setTutorialFlags(Set<String> tutorialFlags) {
            this.tutorialFlags = tutorialFlags;
        }

        public Set<String> getVisitorRequirementFlags() {
            return visitorRequirementFlags;
        }

        public void setVisitorRequirementFlags(Set<String> visitorRequirementFlags) {
            this.visitorRequirementFlags = visitorRequirementFlags;
        }

        public Map<String, Long> getVisitorRequirementCounters() {
            return visitorRequirementCounters;
        }

        public void setVisitorRequirementCounters(Map<String, Long> visitorRequirementCounters) {
            this.visitorRequirementCounters = visitorRequirementCounters;
        }

        public Set<String> getDonatedItems() {
            return donatedItems;
        }

        public void setDonatedItems(Set<String> donatedItems) {
            this.donatedItems = donatedItems;
        }

        public Map<String, Long> getExportedItems() {
            return exportedItems;
        }

        public void setExportedItems(Map<String, Long> exportedItems) {
            this.exportedItems = exportedItems;
        }

        public Set<String> getAccessFlags() {
            return accessFlags;
        }

        public void setAccessFlags(Set<String> accessFlags) {
            this.accessFlags = accessFlags;
        }
    }

    public static class GardenChipProgress {
        private int consumed = 0;
        private String rarity = "LOCKED";
        private int level = 0;

        public int getConsumed() {
            return consumed;
        }

        public void setConsumed(int consumed) {
            this.consumed = consumed;
        }

        public String getRarity() {
            return rarity;
        }

        public void setRarity(String rarity) {
            this.rarity = rarity;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }
}
