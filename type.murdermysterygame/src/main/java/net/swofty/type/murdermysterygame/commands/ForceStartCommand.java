package net.swofty.type.murdermysterygame.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "forcestart",
        description = "Starts a murder mystery game immediately.",
        usage = "/forcestart [seconds]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceStartCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            MurderMysteryPlayer player = (MurderMysteryPlayer) sender;
            Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
            if (game == null) {
                player.sendMessage("<c>You are not in a game.");
                return;
            }
            if (game.getState() != GameState.WAITING && game.getState() != GameState.COUNTDOWN) {
                player.sendMessage("<c>You can only force start a game that is waiting.");
                return;
            }
            if (game.getPlayers().size() < 2) {
                player.sendMessage("<c>Need at least 2 players to start!");
                return;
            }
            game.forceStart();
        });

        var secondsArg = ArgumentType.Integer("seconds");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            MurderMysteryPlayer player = (MurderMysteryPlayer) sender;
            Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
            if (game == null) {
                player.sendMessage("<c>You are not in a game.");
                return;
            }
            if (game.getState() != GameState.WAITING && game.getState() != GameState.COUNTDOWN) {
                player.sendMessage("<c>You can only force start a game that is waiting.");
                return;
            }
            if (game.getPlayers().size() < 2) {
                player.sendMessage("<c>Need at least 2 players to start!");
                return;
            }
            int seconds = context.get(secondsArg);
            if (seconds < 1 || seconds > 60) {
                player.sendMessage("<c>Seconds must be between 1 and 60!");
                return;
            }
            game.forceStart(seconds);
        }, secondsArg);
    }
}
