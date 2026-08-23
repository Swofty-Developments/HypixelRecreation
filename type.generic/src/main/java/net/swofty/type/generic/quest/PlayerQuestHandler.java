package net.swofty.type.generic.quest;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.sound.SoundEvent;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointQuestData;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;

@RequiredArgsConstructor
public class PlayerQuestHandler {
    private final HypixelPlayer player;

    public QuestData getQuestData() {
        QuestData data = player.getDataHandler()
                .get(HypixelDataHandler.Data.QUEST_DATA, DatapointQuestData.class)
                .getValue();
        data.checkAndResetExpired();
        return data;
    }

    public void startQuest(String questId) {
        QuestData data = getQuestData();
        data.startQuest(questId);
    }

    public boolean addProgress(String questId, int amount) {
        QuestData data = getQuestData();

        if (!data.isActive(questId) && !data.isCompleted(questId)) {
            data.startQuest(questId);
        }

        boolean completed = data.addProgress(questId, amount);

        if (completed) {
            QuestDefinition def = QuestRegistry.get(questId);
            if (def != null) {
                onQuestCompleted(def);
            }
        }

        return completed;
    }

    public void addProgressByTrigger(String trigger, int amount) {
        List<QuestDefinition> quests = QuestRegistry.getByTrigger(trigger);
        QuestData data = getQuestData();

        for (QuestDefinition def : quests) {
            if (data.isActive(def.getId())) {
                addProgress(def.getId(), amount);
            }
        }
    }

    public boolean isQuestCompleted(String questId) {
        return getQuestData().isCompleted(questId);
    }

    public boolean isQuestActive(String questId) {
        return getQuestData().isActive(questId);
    }

    public int getQuestProgress(String questId) {
        return getQuestData().getProgress(questId);
    }

    public int getRemainingChallenges() {
        return getQuestData().getRemainingChallenges();
    }

    public boolean canCompleteChallenge() {
        return getQuestData().canCompleteChallenge();
    }

    public boolean hasCompletedChallengeInGame(String gameId) {
        return getQuestData().hasCompletedChallengeInGame(gameId);
    }

    public void completeChallenge(String gameId, long xpReward) {
        QuestData data = getQuestData();

        if (!data.canCompleteChallenge()) {
            player.sendMessage("<c>You have completed the maximum number of challenges today (15)!");
            return;
        }

        if (data.hasCompletedChallengeInGame(gameId)) {
            return;
        }

        data.markChallengeCompleted(gameId);

        player.getExperienceHandler().addExperience(xpReward);

        player.sendMessage("<a><l>CHALLENGE COMPLETE!");
        player.sendMessage("<7>Challenges remaining today: <a>{}", data.getRemainingChallenges());
    }

    public List<QuestDefinition> getActiveQuests(AchievementCategory category) {
        QuestData data = getQuestData();
        return QuestRegistry.getByCategory(category).stream()
                .filter(q -> data.isActive(q.getId()))
                .toList();
    }

    public List<QuestDefinition> getCompletedQuests(AchievementCategory category) {
        QuestData data = getQuestData();
        return QuestRegistry.getByCategory(category).stream()
                .filter(q -> data.isCompleted(q.getId()))
                .toList();
    }

    private void onQuestCompleted(QuestDefinition def) {
        player.sendMessage("<6><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("<a><l>QUEST COMPLETE: </l><e>{}", def.getName());
        player.sendMessage("");

        QuestReward reward = def.getReward();
        if (reward != null && reward.hasReward()) {
            if (reward.getHypixelExperience() > 0) {
                player.sendMessage("<8>+<3>{} <7>Hypixel Experience", reward.getHypixelExperience());
                player.getExperienceHandler().addExperience(reward.getHypixelExperience());
            }
            if (reward.getCoins() > 0) {
                player.sendMessage("<8>+<6>{} <7>Coins", reward.getCoins());
            }
            if (reward.getGameExperience() > 0) {
                player.sendMessage("<8>+<b>{} <7>Game Experience", reward.getGameExperience());
            }
            if (reward.getTokens() > 0) {
                player.sendMessage("<8>+<2>{} <7>Tokens", reward.getTokens());
            }
        }

        player.sendMessage("<6><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1.0f, 1.2f));
    }
}
