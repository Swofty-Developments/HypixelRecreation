package net.swofty.type.ravengarddungeon.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.type.game.game.AbstractGame;
import net.swofty.type.game.game.CountdownConfig;
import net.swofty.type.game.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.ravengarddungeon.config.RavengardDungeonConfig;
import net.swofty.type.ravengarddungeon.user.RavengardDungeonPlayer;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.classes.RavengardSelection;
import net.swofty.type.ravengardgeneric.hud.RavengardHud;

import java.util.List;
import java.util.Optional;

public class RavengardDungeonGame extends AbstractGame<RavengardDungeonPlayer> {
    private static final int TEAM_SIZE = 3;
    public static final List<Pos> SPAWN_POINTS = List.of(
            new Pos(-188.5, 66, -134.5),
            new Pos(-188.5, 66, -81.5),
            new Pos(-187.5, 66, -26.5)
    );
    private final RavengardDungeonConfig config;
    private long startedAt;

    public RavengardDungeonGame(Instance instance, RavengardDungeonConfig config) {
        super(instance, RavengardDungeonGame::onGameEvent);
        this.config = config;
    }

    private static void onGameEvent(Object event) {
        if (!(event instanceof net.swofty.type.game.game.event.CountdownTickEvent tick)
                || !tick.shouldAnnounce() || tick.remainingSeconds() <= 0
                || !(tick.getGame() instanceof RavengardDungeonGame game)) {
            return;
        }
        int seconds = tick.remainingSeconds();
        for (RavengardDungeonPlayer player : game.players.values()) {
            player.sendMessage("<7>The game starts in <e>{}<7> {}!", seconds,
                    seconds == 1 ? "second" : "seconds");
        }
    }

    public RavengardDungeonConfig getConfig() {
        return config;
    }

    @Override
    public Game.JoinResult join(RavengardDungeonPlayer player) {
        Game.JoinResult result = super.join(player);
        if (result instanceof Game.JoinResult.Success) {
            int playerIndex = players.size() - 1;
            player.setDungeonTeam(playerIndex / TEAM_SIZE);
            player.setDungeonDead(false);
            player.setInstance(instance, SPAWN_POINTS.get(playerIndex % SPAWN_POINTS.size()));
            updateHud(player);
        }
        return result;
    }

    @Override
    public int getMaxPlayers() {
        return 21;
    }

    @Override
    public int getMinPlayers() {
        return 16;
    }

    @Override
    protected CountdownConfig getCountdownConfig() {
        return CountdownConfig.simple(30);
    }

    @Override
    public void start() {
        super.start();
        if (state != GameState.IN_PROGRESS) return;
        startedAt = System.currentTimeMillis();
        for (RavengardDungeonPlayer player : players.values()) {
            player.setDungeonDead(false);
            player.setGameMode(GameMode.SURVIVAL);
            updateHud(player);
        }
        MinecraftServer.getSchedulerManager().buildTask(this::enforceSpectators)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    public void eliminate(RavengardDungeonPlayer player) {
        eliminate(player, null);
    }

    public void eliminate(RavengardDungeonPlayer player, String killer) {
        if (state != GameState.IN_PROGRESS || player.isDungeonDead()) return;
        if (killer != null) {
            for (RavengardDungeonPlayer viewer : players.values()) {
                String victimColor = viewer.getDungeonTeam() == player.getDungeonTeam()
                        ? "<e>" : "<f>";
                viewer.sendMessage(victimColor + "{} <7>was defeated by <f>{}<7>!",
                        player.getUsername(), killer);
            }
        }
        player.setDungeonDead(true);
        player.getInventory().clear();
        RavengardClass playerClass = player.getRavengardClass();
        if (playerClass != null) RavengardSelection.giveKit(player, playerClass);
        updateHud(player);
        Optional<RavengardDungeonPlayer> teammate = aliveTeammate(player);
        if (teammate.isEmpty()) {
            routeTeamToLobby(player.getDungeonTeam());
            return;
        }
        lockTo(player, teammate.get());
        for (RavengardDungeonPlayer spectator : teammatesOf(player)) {
            if (spectator.isDungeonDead()) lockTo(spectator, teammate.get());
        }
    }

    public void disconnect(RavengardDungeonPlayer player) {
        if (state == GameState.IN_PROGRESS) {
            player.setDungeonDead(true);
            int team = player.getDungeonTeam();
            players.remove(player.getUuid());
            if (alivePlayers(team).isEmpty()) routeTeamToLobby(team);
            else retargetSpectators(team);
        } else {
            leave(player);
        }
    }

    public int getAlivePlayerCount() {
        return (int) players.values().stream().filter(player -> !player.isDungeonDead()).count();
    }

    public int getAliveTeamCount() {
        return (int) players.values().stream().filter(player -> !player.isDungeonDead())
                .map(RavengardDungeonPlayer::getDungeonTeam).distinct().count();
    }

    public long getElapsedSeconds() {
        return startedAt == 0 ? 0 : (System.currentTimeMillis() - startedAt) / 1000;
    }

    private List<RavengardDungeonPlayer> teammatesOf(RavengardDungeonPlayer player) {
        return players.values().stream()
                .filter(candidate -> candidate.getDungeonTeam() == player.getDungeonTeam())
                .toList();
    }

    private List<RavengardDungeonPlayer> alivePlayers(int team) {
        return players.values().stream()
                .filter(player -> player.getDungeonTeam() == team && !player.isDungeonDead())
                .toList();
    }

    private Optional<RavengardDungeonPlayer> aliveTeammate(RavengardDungeonPlayer player) {
        return alivePlayers(player.getDungeonTeam()).stream()
                .filter(candidate -> !candidate.getUuid().equals(player.getUuid()))
                .findFirst();
    }

    private void lockTo(RavengardDungeonPlayer spectator, RavengardDungeonPlayer teammate) {
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.setSpectatingTeammate(teammate.getUuid());
        spectator.spectate(teammate);
    }

    private void enforceSpectators() {
        if (state != GameState.IN_PROGRESS) return;
        for (RavengardDungeonPlayer player : players.values()) {
            updateHud(player);
            if (!player.isDungeonDead() || player.getSpectatingTeammate() == null) continue;
            RavengardDungeonPlayer target = players.get(player.getSpectatingTeammate());
            if (target == null || target.isDungeonDead()) retargetSpectators(player.getDungeonTeam());
            else player.spectate(target);
        }
    }

    private void retargetSpectators(int team) {
        List<RavengardDungeonPlayer> alive = alivePlayers(team);
        if (alive.isEmpty()) {
            routeTeamToLobby(team);
            return;
        }
        for (RavengardDungeonPlayer player : players.values()) {
            if (player.getDungeonTeam() == team && player.isDungeonDead()) lockTo(player, alive.getFirst());
        }
    }

    private void routeTeamToLobby(int team) {
        players.values().stream()
                .filter(player -> player.getDungeonTeam() == team)
                .toList()
                .forEach(player -> player.sendTo(ServerType.RAVENGARD_LOBBY));
    }

    private void updateHud(RavengardDungeonPlayer player) {
        var hud = RavengardHud.state(player);
        long seconds = getElapsedSeconds();
        hud.setClock(String.format("%02d:%02d", seconds / 60, seconds % 60));
        hud.setPlayerCount(getAlivePlayerCount());
        hud.setTeamCount(getAliveTeamCount());
        hud.setSpectating(player.isDungeonDead());
        hud.setDungeon(true);
        hud.setLocation("Dungeon");
    }
}
