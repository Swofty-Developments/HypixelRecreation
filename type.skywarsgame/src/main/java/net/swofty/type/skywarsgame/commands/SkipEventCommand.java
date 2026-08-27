package net.swofty.type.skywarsgame.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.game.game.GameState;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

@CommandParameters(
        description = "Skips to the next game event (refill, dragon spawn)",
        usage = "/skipevent",
        permission = Rank.STAFF,
        allowsConsole = false,
        labels = "skipevent"
)
public class SkipEventCommand extends HypixelCommand {

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

            if (game.getState() != GameState.IN_PROGRESS) {
                player.sendMessage("<c>The game is not in progress!");
                return;
            }

            SkywarsGame.GameEvent currentEvent = game.getCurrentEvent();
            SkywarsGame.GameEvent nextEvent = currentEvent.getNext();

            if (nextEvent == SkywarsGame.GameEvent.GAME_END) {
                player.sendMessage("<c>No more events to skip to! Current event: {}", currentEvent.getDisplayName());
                return;
            }

            SkywarsGame.GameEvent triggeredEvent = game.skipToNextEvent();
            if (triggeredEvent != null) {
                player.sendMessage("<a>Skipped to: <e>{}", triggeredEvent.getDisplayName());
            } else {
                player.sendMessage("<c>Failed to skip to next event.");
            }
        });
    }
}
