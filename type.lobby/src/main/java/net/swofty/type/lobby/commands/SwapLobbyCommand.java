package net.swofty.type.lobby.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.ServerType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.lobby.LobbyServerOrder;
import net.swofty.type.lobby.ServerInfoCache;

import java.util.List;

@CommandParameters(
    labels = "sl",
    description = "Switch to a numbered lobby",
    usage = "/swaplobby <number>",
    permission = Rank.DEFAULT,
    allowsConsole = false
)
public class SwapLobbyCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        var lobbyNumberArg = ArgumentType.Integer("number");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            HypixelPlayer player = (HypixelPlayer) sender;
            int requestedLobby = context.get(lobbyNumberArg);

            if (requestedLobby < 1) {
                player.sendMessage("<c>Lobby number must be at least 1.");
                return;
            }

            ServerType currentType = HypixelConst.getTypeLoader().getType();
            if (!currentType.name().endsWith("_LOBBY")) {
                player.sendMessage("<c>This command can only be used in lobbies.");
                return;
            }

            ServerInfoCache.getServersByType(currentType).thenAccept(servers -> {
                List<UnderstandableProxyServer> ordered = LobbyServerOrder.sortBySelectorOrder(servers);
                if (ordered.isEmpty()) {
                    player.sendMessage("<c>No lobbies are currently available.");
                    return;
                }

                UnderstandableProxyServer target = LobbyServerOrder.getBySelectorIndex(ordered, requestedLobby);
                if (target == null) {
                    player.sendMessage("<c>Invalid lobby number. Pick between 1 and {}.", ordered.size());
                    return;
                }

                if (target.uuid().equals(HypixelConst.getServerUUID())) {
                    player.sendMessage("<c>You are already connected to this lobby!");
                    return;
                }

                if (target.players().size() >= target.maxPlayers()) {
                    player.sendMessage("<c>That lobby is full.");
                    return;
                }

                player.sendMessage("<a>Sending you to lobby #{}...", requestedLobby);
                player.asProxyPlayer().transferToWithIndication(target.uuid());
            }).exceptionally(throwable -> {
                player.sendMessage("<c>Failed to load lobbies. Please try again.");
                return null;
            });
        }, lobbyNumberArg);
    }
}
