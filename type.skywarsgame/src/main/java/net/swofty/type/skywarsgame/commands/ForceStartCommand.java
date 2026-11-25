package net.swofty.type.skywarsgame.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

@CommandParameters(
        description = "Force starts the skywars game",
        usage = "/forcestart [seconds]",
        permission = Rank.STAFF,
        allowsConsole = false,
        labels = "forcestart"
)
public class ForceStartCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof SkywarsPlayer player)) return;

            SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
            if (game == null) {
                player.sendMessage("<c>You are not in a game!");
                return;
            }

            if (!game.getState().isWaiting()) {
                player.sendMessage("<c>The game has already started!");
                return;
            }
            if (!game.hasMinimumPlayers()) {
                player.sendMessage("<c>Need at least {} players to start!", game.getMinPlayers());
                return;
            }

            game.forceStart(5);
            player.sendMessage("<a>Force starting the game in 5 seconds!");
        });

        var secondsArg = ArgumentType.Integer("seconds").min(1).max(60);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof SkywarsPlayer player)) return;

            SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
            if (game == null) {
                player.sendMessage("<c>You are not in a game!");
                return;
            }

            if (!game.getState().isWaiting()) {
                player.sendMessage("<c>The game has already started!");
                return;
            }
            if (!game.hasMinimumPlayers()) {
                player.sendMessage("<c>Need at least {} players to start!", game.getMinPlayers());
                return;
            }

            int seconds = context.get(secondsArg);
            game.forceStart(seconds);
            player.sendMessage("<a>Force starting the game in {} seconds!", seconds);
        }, secondsArg);
    }
}
