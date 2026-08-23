package net.swofty.type.game.game;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.instance.InstanceContainer;
import net.swofty.commons.text.Text;
import net.swofty.type.game.game.event.GameTeamWinConditionEvent;
import net.swofty.type.game.game.event.PlayerAssignedTeamEvent;
import net.swofty.type.game.game.event.TeamEliminatedEvent;
import net.swofty.type.game.game.team.GameTeam;
import net.swofty.type.game.game.team.TeamManager;
import org.tinylog.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Base game implementation with team assignment and team-based win conditions.
 *
 * @param <P> The player type
 * @param <T> The team type
 */
public abstract class AbstractTeamGame<P extends GameParticipant, T extends GameTeam>
    extends AbstractGame<P> implements TeamManager<T, P> {

    protected final Map<String, T> teams = new LinkedHashMap<>();
    protected final Map<UUID, String> playerTeams = new HashMap<>();

    protected AbstractTeamGame(InstanceContainer instance, Consumer<Object> eventDispatcher) {
        super(instance, eventDispatcher);
    }

    /**
     * Registers teams for this game.
     * Call this during initialization.
     */
    protected void registerTeam(T team) {
        teams.put(team.getId(), team);
    }

    @Override
    public Collection<T> getTeams() {
        // teams can be null if an overridable method (e.g. getMaxPlayers) is invoked
        // from a super-constructor before this field's initializer runs.
        if (teams == null) {
            return java.util.List.of();
        }
        return Collections.unmodifiableCollection(teams.values());
    }

    @Override
    public Optional<T> getTeam(String teamId) {
        return Optional.ofNullable(teams.get(teamId));
    }

    @Override
    public Optional<T> getPlayerTeam(UUID playerId) {
        String teamId = playerTeams.get(playerId);
        return teamId != null ? getTeam(teamId) : Optional.empty();
    }

    @Override
    public void removeFromTeam(P player) {
        String currentTeamId = playerTeams.remove(player.getUuid());
        if (currentTeamId != null) {
            getTeam(currentTeamId).ifPresent(team -> team.removePlayer(player.getUuid()));
        }
    }

    @Override
    public Collection<T> getActiveTeams() {
        return teams.values().stream()
            .filter(GameTeam::hasPlayers)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<T> getViableTeams() {
        return teams.values().stream()
            .filter(this::isTeamViable)
            .collect(Collectors.toList());
    }

    /**
     * Determines if a team can still win the game.
     */
    protected abstract boolean isTeamViable(T team);

    protected void onTeamEliminated(T team) {
        eventDispatcher.accept(new TeamEliminatedEvent<>(
            this,
            team
        ));
    }

    public void broadcastMessage(Text message) {
        Audience.audience(getPlayers().stream().map(GameParticipant::getServerPlayer).toList()).sendMessage(message);
    }

    public void broadcastMessage(String markup, Object... arguments) {
        broadcastMessage(Text.of(markup, arguments));
    }

    /**
     * Checks if two players are on the same team.
     *
     * @param first  uuid of the first player
     * @param second uuid of the second player
     * @return true if both players are on the same team, false otherwise
     */
    public boolean isSameTeam(UUID first, UUID second) {
        String team1 = playerTeams.get(first);
        String team2 = playerTeams.get(second);
        return team1 != null && team1.equals(team2);
    }

    /**
     * Auto-assigns all unassigned players to teams.
     * Override for custom assignment logic.
     */
    public void autoAssignTeams() {
        List<P> unassignedPlayers = players.values().stream()
            .filter(p -> !playerTeams.containsKey(p.getUuid()))
            .collect(Collectors.toList());

        if (unassignedPlayers.isEmpty() || teams.isEmpty()) {
            return;
        }

        Collections.shuffle(unassignedPlayers);
        int teamSize = getTeamSize();

        List<T> shuffledTeams = new ArrayList<>(teams.values());
        Collections.shuffle(shuffledTeams);
        Map<String, Integer> teamPriority = new HashMap<>();
        for (int i = 0; i < shuffledTeams.size(); i++) {
            teamPriority.put(shuffledTeams.get(i).getId(), i);
        }

        for (P player : unassignedPlayers) {
            T targetTeam = teams.values().stream()
                .filter(team -> team.getPlayerCount() < teamSize)
                .min((left, right) -> {
                    int byCount = Integer.compare(left.getPlayerCount(), right.getPlayerCount());
                    if (byCount != 0) {
                        return byCount;
                    }

                    int leftPriority = teamPriority.getOrDefault(left.getId(), Integer.MAX_VALUE);
                    int rightPriority = teamPriority.getOrDefault(right.getId(), Integer.MAX_VALUE);
                    return Integer.compare(leftPriority, rightPriority);
                })
                .orElse(null);

            if (targetTeam != null) {
                removeFromTeam(player);
                targetTeam.addPlayer(player.getUuid());
                playerTeams.put(player.getUuid(), targetTeam.getId());

                eventDispatcher.accept(new PlayerAssignedTeamEvent<>(
                        this,
                    player.getServerPlayer(),
                    targetTeam
                ));
            }
        }
    }

    /**
     * @return The maximum team size for this game
     */
    protected abstract int getTeamSize();

    @Override
    protected void checkWinConditions() {
        if (state != GameState.IN_PROGRESS) return;

        List<T> viableTeams = getViableTeams().stream().toList();
        Logger.info("Viable teams: {}", viableTeams.stream().map(T::getName).toList());
        if (viableTeams.size() <= 1) {
            T winner = viableTeams.isEmpty() ? null : viableTeams.getFirst();

            Logger.info("Game {} ended. Winner: {}", gameId, winner != null ? winner.getName() : "None");

            eventDispatcher.accept(
                new GameTeamWinConditionEvent<>(
                        this,
                    Optional.ofNullable(winner)
                )
            );
        }
    }
}
