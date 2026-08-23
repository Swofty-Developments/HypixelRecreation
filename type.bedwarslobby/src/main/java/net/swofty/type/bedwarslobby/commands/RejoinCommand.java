package net.swofty.type.bedwarslobby.commands;

import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import net.swofty.commons.protocol.objects.orchestrator.RejoinGameProtocol;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(
        description = "Rejoin an active BedWars game",
        usage = "/rejoin",
        permission = Rank.DEFAULT,
        allowsConsole = false,
        labels = "rejoin"
)
public class RejoinCommand extends HypixelCommand {
    private static final ProxyService ORCHESTRATOR = new ProxyService(ServiceType.ORCHESTRATOR);

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            HypixelPlayer player = (HypixelPlayer) sender;

            // Query orchestrator for active game
            RejoinGameProtocol.RejoinGameRequest request =
                    new RejoinGameProtocol.RejoinGameRequest(player.getUuid());

            ORCHESTRATOR.handleRequest(request).thenAccept(response -> {
                if (response instanceof RejoinGameProtocol.RejoinGameResponse resp) {
                    if (resp.hasActiveGame() && resp.server() != null) {
                        // Send the player to the game
                        player.sendMessage("<a>Rejoining your game...");

                        // Notify the game server about this player
                        ChooseGameProtocol.ChooseGameMessage chooseMsg =
                                new ChooseGameProtocol.ChooseGameMessage(
                                        player.getUuid(),
                                        resp.server(),
                                        resp.gameId()
                                );
                        ORCHESTRATOR.handleRequest(chooseMsg);

                        // Transfer player to the game server
                        player.asProxyPlayer().transferToWithIndication(resp.server().uuid());
                        player.getAchievementHandler().completeAchievement("bedwars.rejoining_the_dream");
                    } else {
                        player.sendMessage("<c>You don't have an active game to rejoin!");
                    }
                } else {
                    player.sendMessage("<c>Failed to check for active games. Please try again.");
                }
            }).exceptionally(throwable -> {
                player.sendMessage("<c>Failed to rejoin: {}", throwable.getMessage());
                return null;
            });
        });
    }
}
