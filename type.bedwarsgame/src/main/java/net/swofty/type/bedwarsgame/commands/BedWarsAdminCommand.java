package net.swofty.type.bedwarsgame.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.game.BedWarsTeam;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.game.event.GameTeamWinConditionEvent;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;

import java.util.Optional;

@CommandParameters(labels = "bwadmin",
        description = "Admin commands for BedWars game management.",
        usage = "/bwadmin <breakbed|respawnbed|endgame|info> [team]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class BedWarsAdminCommand extends HypixelCommand {

    @Override
    public void registerUsage(HypixelCommand.MinestomCommand command) {
        // /bwadmin breakbed <team>
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof BedWarsPlayer player)) return;

            String teamName = context.get("team");
            BedWarsGame game = player.getGame();

            if (game == null) {
                player.sendMessage("<c>You are not in a game!");
                return;
            }

            TeamKey teamKey;
            try {
                teamKey = TeamKey.valueOf(teamName.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("<c>Invalid team: {}", teamName);
                return;
            }

            if (!game.isBedAlive(teamKey)) {
                player.sendMessage("<c>That team's bed is already destroyed!");
                return;
            }

            game.onBedDestroyed(teamKey, player);
        }, ArgumentType.Literal("breakbed"), ArgumentType.String("team"));

        // /bwadmin respawnbed <team>
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof BedWarsPlayer player)) return;

            String teamName = context.get("team");
            BedWarsGame game = player.getGame();

            if (game == null) {
                player.sendMessage("<c>You are not in a game!");
                return;
            }

            TeamKey teamKey;
            try {
                teamKey = TeamKey.valueOf(teamName.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("<c>Invalid team: {}", teamName);
                return;
            }

            if (game.isBedAlive(teamKey)) {
                player.sendMessage("<c>That team's bed is already alive!");
                return;
            }

            game.respawnBed(teamKey);

        }, ArgumentType.Literal("respawnbed"), ArgumentType.String("team"));

        // /bwadmin endgame [winner]
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof BedWarsPlayer player)) return;

            BedWarsGame game = player.getGame();

            if (game == null) {
                player.sendMessage("<c>You are not in a game!");
                return;
            }

            String winnerTeam = context.get("winner");
            TeamKey winnerKey = null;

            if (winnerTeam != null && !winnerTeam.isEmpty()) {
                try {
                    winnerKey = TeamKey.valueOf(winnerTeam.toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("<c>Invalid team: {}", winnerTeam);
                    return;
                }
            }

            if (winnerKey != null) {
                BedWarsTeam team = game.getTeam(winnerKey.name()).orElse(null);
                game.getEventDispatcher().accept(
                    new GameTeamWinConditionEvent<>(
                            game,
                        Optional.ofNullable(team)
                    )
                );
            } else {
                game.getEventDispatcher().accept(
                    new GameTeamWinConditionEvent<>(
                            game,
                        Optional.empty()
                    )
                );
            }

        }, ArgumentType.Literal("endgame"), ArgumentType.String("winner").setDefaultValue(""));

        // /bwadmin endgame (no args version)
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof BedWarsPlayer player)) return;

            BedWarsGame game = player.getGame();

            if (game == null) {
                player.sendMessage("<c>You are not in a game!");
                return;
            }

            game.getEventDispatcher().accept(
                new GameTeamWinConditionEvent<>(
                        game,
                    Optional.empty()
                )
            );
        }, ArgumentType.Literal("endgame"));

        // /bwadmin info
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof BedWarsPlayer player)) return;

            BedWarsGame game = player.getGame();

            if (game == null) {
                player.sendMessage("<c>You are not in a game!");
                return;
            }

            player.sendMessage("<6>=== Game Info ===");
            player.sendMessage("<e>Game ID: {}", game.getGameId());
            player.sendMessage("<e>State: {}", game.getState());
            player.sendMessage("<e>Map: {}", game.getMapEntry().getName());
            player.sendMessage("<e>Players: {}/{}", game.getPlayers().size(), game.getMaxPlayers());
            player.sendMessage("<e>Recording: {}", game.getReplayManager().isRecording());

            player.sendMessage("<6>=== Teams ===");
            for (BedWarsTeam team : game.getTeams()) {
                Text bedStatus = team.isBedAlive() ? Text.of("<a>✔") : Text.of("<c>✖");
                player.sendMessage("{} {} <7>({} players)",
                        Text.of("<color:{}>{}", team.getColor(), team.getName()), bedStatus, team.getPlayerCount());
            }
        }, ArgumentType.Literal("info"));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            BedWarsPlayer player = (BedWarsPlayer) sender;
            BedWarsGame game = player.getGame();
            if (game == null) {
                player.sendMessage("<c>You are not in a game.");
                return;
            }

            game.getReplayManager().stopRecording();
        });

        command.addSyntax((sender, _) -> {
                if (!permissionCheck(sender)) return;
                BedWarsPlayer player = (BedWarsPlayer) sender;
                BedWarsGame game = player.getGame();
                if (game == null) {
                    player.sendMessage("<c>You are not in a game.");
                    return;
                }
                if (game.getState() != GameState.WAITING && game.getState() != GameState.COUNTDOWN) {
                    player.sendMessage("<c>You can only force start a game that is waiting.");
                    return;
                }
                game.start();
        }, ArgumentType.Literal("forcestart"));
    }
}
