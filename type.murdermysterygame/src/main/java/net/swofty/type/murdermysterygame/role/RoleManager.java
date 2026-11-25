package net.swofty.type.murdermysterygame.role;

import net.swofty.commons.murdermystery.MurderMysteryGameType;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

import java.util.*;

public class RoleManager {
    private final Game game;
    private final Map<UUID, GameRole> playerRoles = new HashMap<>();
    private final Map<UUID, UUID> assassinTargets = new HashMap<>();

    public RoleManager(Game game) {
        this.game = game;
    }

    public void assignRoles() {
        List<MurderMysteryPlayer> players = new ArrayList<>(game.getPlayers());
        Collections.shuffle(players);

        MurderMysteryGameType gameType = game.getGameType();

        if (gameType == MurderMysteryGameType.ASSASSINS) {
            assignAssassinsMode(players);
        } else {
            assignClassicMode(players, gameType);
        }
    }

    private void assignClassicMode(List<MurderMysteryPlayer> players, MurderMysteryGameType type) {
        int murdererCount = type.getMurdererCount();
        int detectiveCount = type.getDetectiveCount();

        int index = 0;
        for (int i = 0; i < murdererCount && index < players.size(); i++, index++) {
            playerRoles.put(players.get(index).getUuid(), GameRole.MURDERER);
        }
        for (int i = 0; i < detectiveCount && index < players.size(); i++, index++) {
            playerRoles.put(players.get(index).getUuid(), GameRole.DETECTIVE);
        }
        for (; index < players.size(); index++) {
            playerRoles.put(players.get(index).getUuid(), GameRole.INNOCENT);
        }
    }

    private void assignAssassinsMode(List<MurderMysteryPlayer> players) {
        for (MurderMysteryPlayer player : players) {
            playerRoles.put(player.getUuid(), GameRole.ASSASSIN);
        }
        for (int i = 0; i < players.size(); i++) {
            UUID hunter = players.get(i).getUuid();
            UUID target = players.get((i + 1) % players.size()).getUuid();
            assassinTargets.put(hunter, target);
        }
    }

    public GameRole getRole(UUID playerUuid) {
        return playerRoles.get(playerUuid);
    }

    public UUID getAssassinTarget(UUID hunterUuid) {
        return assassinTargets.get(hunterUuid);
    }

    public Map<UUID, GameRole> getRoles() {
        return Map.copyOf(playerRoles);
    }

    public Map<UUID, UUID> getAssassinTargets() {
        return Map.copyOf(assassinTargets);
    }

    public void reassignTarget(UUID hunterUuid, UUID newTarget) {
        assassinTargets.put(hunterUuid, newTarget);
    }

    public List<MurderMysteryPlayer> getPlayersWithRole(GameRole role) {
        return game.getPlayers().stream()
                .filter(p -> playerRoles.get(p.getUuid()) == role)
                .toList();
    }

    public int countAliveWithRole(GameRole role) {
        return (int) game.getPlayers().stream()
                .filter(p -> playerRoles.get(p.getUuid()) == role)
                .filter(p -> !p.isEliminated())
                .count();
    }

    public void clear() {
        playerRoles.clear();
        assassinTargets.clear();
    }
}
