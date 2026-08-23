package net.swofty.type.generic.achievement;

import lombok.RequiredArgsConstructor;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointAchievementData;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;

@RequiredArgsConstructor
public class PlayerAchievementHandler {
    private final HypixelPlayer player;

    public AchievementData getAchievementData() {
        return player.getDataHandler()
                .get(HypixelDataHandler.Data.ACHIEVEMENT_DATA, DatapointAchievementData.class)
                .getValue();
    }

    public boolean addProgress(String achievementId, int amount) {
        AchievementDefinition def = AchievementRegistry.get(achievementId);
        if (def == null) return false;

        AchievementData data = getAchievementData();
        AchievementData.AchievementProgress progress = data.getOrCreate(achievementId);

        boolean unlocked = progress.addProgress(def, amount);

        if (unlocked) {
            onAchievementUnlocked(def, progress);
        }

        return unlocked;
    }

    public void addProgressByTrigger(String trigger, int amount) {
        List<AchievementDefinition> achievements = AchievementRegistry.getByTrigger(trigger);
        AchievementData data = getAchievementData();

        for (AchievementDefinition def : achievements) {
            if (def.isPerGame()) {
                continue;
            }

            if (def.getType() == AchievementType.TIERED) {
                if (!data.isTracked(def.getId())) {
                    continue;
                }
            }

            addProgress(def.getId(), amount);
        }
    }

    public void completeAchievement(String achievementId) {
        AchievementDefinition def = AchievementRegistry.get(achievementId);
        if (def == null) return;

        AchievementData data = getAchievementData();
        AchievementData.AchievementProgress progress = data.getOrCreate(achievementId);

        if (!progress.isCompleted()) {
            progress.complete();
            onAchievementUnlocked(def, progress);
        }
    }

    public boolean hasAchievement(String achievementId) {
        return getAchievementData().isCompleted(achievementId);
    }

    public boolean hasFullyCompletedAchievement(String achievementId) {
        return getAchievementData().isFullyCompleted(achievementId);
    }

    public int getAchievementTier(String achievementId) {
        return getAchievementData().getCurrentTier(achievementId);
    }

    public int getProgress(String achievementId) {
        return getAchievementData().getProgress(achievementId);
    }

    public AchievementData.AchievementProgress getProgressData(String achievementId) {
        return getAchievementData().get(achievementId);
    }

    public int getTotalPoints() {
        return getAchievementData().getTotalPoints();
    }

    public int getTotalPoints(AchievementCategory category) {
        return getAchievementData().getTotalPoints(category);
    }

    public int getPoints(AchievementCategory category, AchievementType type) {
        return getAchievementData().getPoints(category, type);
    }

    public int getUnlockedCount(AchievementCategory category) {
        return getAchievementData().getUnlockedCount(category);
    }

    public int getUnlockedCount(AchievementCategory category, AchievementType type) {
        return getAchievementData().getUnlockedCount(category, type);
    }

    public double getCompletionPercentage(AchievementCategory category) {
        return getAchievementData().getCompletionPercentage(category);
    }

    public int getTotalUnlockedCount() {
        return getAchievementData().getTotalUnlockedCount();
    }

    public boolean isTracked(String achievementId) {
        return getAchievementData().isTracked(achievementId);
    }

    public String getTrackedAchievement(AchievementCategory category) {
        return getAchievementData().getTrackedAchievement(category);
    }

    public boolean toggleTracking(String achievementId) {
        AchievementDefinition def = AchievementRegistry.get(achievementId);
        if (def == null || def.getType() != AchievementType.TIERED) {
            player.sendMessage("<c>Only tiered achievements can be tracked!");
            return false;
        }

        if (hasFullyCompletedAchievement(achievementId)) {
            player.sendMessage("<c>You have already completed all tiers of this achievement!");
            return false;
        }

        boolean nowTracking = getAchievementData().toggleTracking(achievementId);

        if (nowTracking) {
            player.sendMessage("<a>Now tracking: <e>{}", def.getName());
            player.playSound(net.kyori.adventure.sound.Sound.sound(
                    net.minestom.server.sound.SoundEvent.BLOCK_NOTE_BLOCK_PLING,
                    net.kyori.adventure.sound.Sound.Source.MASTER, 1.0f, 2.0f));
        } else {
            player.sendMessage("<c>Stopped tracking: <e>{}", def.getName());
        }

        return nowTracking;
    }

    private void onAchievementUnlocked(AchievementDefinition def, AchievementData.AchievementProgress progress) {
        AchievementStatisticsService.recordUnlock(def, progress);

        String tierText = "";
        if (def.getType() == AchievementType.TIERED) {
            tierText = " " + toRoman(progress.getCurrentTier());
        }

        // TODO: make this actually clickable to open the achievements menu
        Text tierHover = Text.of("""
                <a>{}{}
                <7>{}

                <7>Reward:
                 <8>+<e>5 <7>Achievement Points

                <e>Click to open achievements menu!""",
                def.getName(), tierText, def.getDescription());

        player.sendMessage("<e><k>A</k><a>>>   Achievement Unlocked: <6><hover:'{0}'>{1}{2}</hover><a>   \\<\\<<e><k>A",
                tierHover, def.getName(), tierText);

        player.playSound(net.kyori.adventure.sound.Sound.sound(
                net.minestom.server.sound.SoundEvent.ENTITY_PLAYER_LEVELUP,
                net.kyori.adventure.sound.Sound.Source.MASTER, 1.0f, 1.0f));
    }

    private String toRoman(int tier) {
        return AchievementTier.toRomanNumeral(tier);
    }
}
