package net.swofty.type.murdermysterygame.user;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;
import net.swofty.type.game.game.GameParticipant;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.murdermysterygame.role.GameRole;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MurderMysteryPlayer extends HypixelPlayer implements GameParticipant {
    private boolean eliminated = false;
    private int goldCollected = 0;
    private int tokensEarnedThisGame = 0;
    private int killsThisGame = 0;

    // Achievement tracking fields
    private int arrowsFiredThisGame = 0;
    private int bowKillsThisGame = 0;
    private int consecutiveBowKills = 0;
    private int murdererBowKillsThisGame = 0;
    private long lastKillTimestamp = 0;
    private List<Long> recentKillTimestamps = new ArrayList<>();
    private boolean hasCollectedGoldThisGame = false;
    private int goldCollectedInFirstMinute = 0;
    private boolean hasFiredArrowThisGame = false;
    private GameRole lastGameRole = null;
    private int totalGoldCollectedThisGame = 0;

    public MurderMysteryPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    @Override
    public String getGameId() {
        return getTag(Tag.String("gameId"));
    }

    @Override
    public void setGameId(String gameId) {
        if (gameId == null) {
            removeTag(Tag.String("gameId"));
        } else {
            setTag(Tag.String("gameId"), gameId);
        }
    }

    @Override
    public Player getServerPlayer() {
        return this;
    }

    public Game getGame() {
        String gameId = getGameId();
        return gameId == null ? null : TypeMurderMysteryGameLoader.getGameById(gameId);
    }

    public void addGold(int amount) {
        this.goldCollected += amount;
    }

    public void resetGold() {
        this.goldCollected = 0;
    }

    public void addTokens(int amount) {
        this.tokensEarnedThisGame += amount;
    }

    public void addKill() {
        this.killsThisGame++;
    }

    public void addArrowFired() {
        this.arrowsFiredThisGame++;
        this.hasFiredArrowThisGame = true;
    }

    public void addBowKill() {
        this.bowKillsThisGame++;
        this.consecutiveBowKills++;
    }

    public void resetBowKillStreak() {
        this.consecutiveBowKills = 0;
    }

    public void recordKillTimestamp() {
        long now = System.currentTimeMillis();
        this.lastKillTimestamp = now;
        this.recentKillTimestamps.add(now);
        // Keep only last 5 seconds of kills
        this.recentKillTimestamps.removeIf(t -> now - t > 5000);
    }

    public int getKillsInLast5Seconds() {
        long now = System.currentTimeMillis();
        return (int) recentKillTimestamps.stream()
                .filter(t -> now - t <= 5000)
                .count();
    }

    public void addGoldCollectedThisGame(int amount) {
        this.totalGoldCollectedThisGame += amount;
        this.hasCollectedGoldThisGame = true;
    }

    public void addGoldInFirstMinute(int amount) {
        this.goldCollectedInFirstMinute += amount;
    }

    public void resetGameStats() {
        this.tokensEarnedThisGame = 0;
        this.killsThisGame = 0;
        this.goldCollected = 0;
        this.eliminated = false;
        // Reset achievement tracking fields
        this.arrowsFiredThisGame = 0;
        this.bowKillsThisGame = 0;
        this.consecutiveBowKills = 0;
        this.murdererBowKillsThisGame = 0;
        this.lastKillTimestamp = 0;
        this.recentKillTimestamps.clear();
        this.hasCollectedGoldThisGame = false;
        this.goldCollectedInFirstMinute = 0;
        this.hasFiredArrowThisGame = false;
        this.totalGoldCollectedThisGame = 0;
    }
}
