package net.swofty.type.ravengardgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.text.Text;
import net.swofty.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import net.swofty.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import net.swofty.commons.protocol.objects.orchestrator.ListGamesProtocol;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.concurrent.ThreadLocalRandom;

@CommandParameters(labels = "dungeon",
        description = "Ravengard dungeon administration",
        usage = "/dungeon <join|list|generate [seed] [rooms]>",
        permission = Rank.STAFF, allowsConsole = false)
public class DungeonCommand extends HypixelCommand {
    /** Set by the dungeon server so joining an instance hosted locally skips the proxy hop. */
    public static volatile java.util.function.BiFunction<RavengardPlayer, String, Boolean> localJoin;

    private static final ProxyService ORCHESTRATOR = new ProxyService(ServiceType.ORCHESTRATOR);
    private static final int DEFAULT_ADMIN_ROOMS = 150;

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            RavengardPlayer player = (RavengardPlayer) sender;
            player.sendMessage("<e>/dungeon join <7>- queue into a dungeon");
            player.sendMessage("<e>/dungeon join \\<instance> <7>- fly over an instance");
            player.sendMessage("<e>/dungeon list <7>- every dungeon server and its instances");
            player.sendMessage("<e>/dungeon generate [seed] [rooms] <7>- dedicated instance");
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            queue((RavengardPlayer) sender, "STANDARD");
        }, ArgumentType.Literal("join"));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            list((RavengardPlayer) sender);
        }, ArgumentType.Literal("list"));

        ArgumentNumber<Long> seedArg = ArgumentType.Long("seed");
        ArgumentNumber<Integer> roomsArg = ArgumentType.Integer("rooms").min(3).max(200);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            adminGenerate((RavengardPlayer) sender,
                    "ADMIN:" + ThreadLocalRandom.current().nextLong() + ":" + DEFAULT_ADMIN_ROOMS);
        }, ArgumentType.Literal("generate"));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            adminGenerate((RavengardPlayer) sender,
                    "ADMIN:" + context.get(seedArg) + ":" + DEFAULT_ADMIN_ROOMS);
        }, ArgumentType.Literal("generate"), seedArg);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            adminGenerate((RavengardPlayer) sender,
                    "ADMIN:" + context.get(seedArg) + ":" + context.get(roomsArg));
        }, ArgumentType.Literal("generate"), seedArg, roomsArg);

        var instanceArg = ArgumentType.Word("instance");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            adminJoin((RavengardPlayer) sender, context.get(instanceArg));
        }, ArgumentType.Literal("join"), instanceArg);
    }

    private static void adminGenerate(RavengardPlayer player, String mode) {
        player.sendMessage("<7>Allocating a dungeon instance...");
        GetServerForMapProtocol.GetServerForMapMessage request =
                new GetServerForMapProtocol.GetServerForMapMessage(
                        ServerType.RAVENGARD_DUNGEON, null, mode, 1);
        ORCHESTRATOR.handleRequest(request).thenAccept(response -> {
            if (!(response instanceof GetServerForMapProtocol.GetServerForMapResponse(
                    UnderstandableProxyServer server, String gameId, boolean success, String error))
                    || server == null || gameId == null) {
                String reason = response instanceof GetServerForMapProtocol.GetServerForMapResponse r
                        && r.error() != null ? r.error() : "no servers available";
                player.sendMessage("<c>Could not allocate an instance: {}", reason);
                return;
            }
            player.sendMessage("<a>Instance <f>{}<a> created on <f>{}<a>. It self-destructs after 30s with nobody inside.",
                    gameId, server.shortName());
            player.sendMessage("<click:run:'/dungeon join {0}'><e><l>[CLICK TO JOIN] <7>or run /dungeon join {1}</click>",
                    gameId, gameId.substring(0, 8));
        }).exceptionally(throwable -> {
            player.sendMessage("<c>Allocation failed: {}", throwable.getMessage());
            return null;
        });
    }

    private static void adminJoin(RavengardPlayer player, String instanceId) {
        if (localJoin != null && localJoin.apply(player, instanceId)) {
            return;
        }
        ORCHESTRATOR.handleRequest(new ListGamesProtocol.ListGamesMessage(ServerType.RAVENGARD_DUNGEON))
                .thenAccept(response -> {
                    if (!(response instanceof ListGamesProtocol.ListGamesResponse listing)
                            || !listing.success()) {
                        player.sendMessage("<c>Failed to look the instance up.");
                        return;
                    }
                    for (ListGamesProtocol.ServerGames server : listing.servers()) {
                        for (ListGamesProtocol.GameSummary game : server.games()) {
                            if (!game.gameId().equals(instanceId)
                                    && !game.gameId().startsWith(instanceId)) continue;
                            UnderstandableProxyServer proxy = new UnderstandableProxyServer(
                                    server.shortName(),
                                    java.util.UUID.fromString(server.serverUuid()),
                                    ServerType.RAVENGARD_DUNGEON, -1,
                                    new java.util.ArrayList<>(), server.maxPlayers(),
                                    server.shortName());
                            ORCHESTRATOR.handleRequest(new ChooseGameProtocol.ChooseGameMessage(
                                    player.getUuid(), proxy, game.gameId())).thenRun(() -> {
                                player.sendMessage("<a>Sending you to <f>{}<a>!", server.shortName());
                                player.asProxyPlayer().transferToWithIndication(proxy.uuid());
                            });
                            return;
                        }
                    }
                    player.sendMessage("<c>No instance found matching <f>{}<c>.", instanceId);
                }).exceptionally(throwable -> {
                    player.sendMessage("<c>Lookup failed: {}", throwable.getMessage());
                    return null;
                });
    }

    private static void queue(RavengardPlayer player, String mode) {
        player.sendMessage("<7>Finding a dungeon server...");
        GetServerForMapProtocol.GetServerForMapMessage request =
                new GetServerForMapProtocol.GetServerForMapMessage(
                        ServerType.RAVENGARD_DUNGEON, null, mode, 1);

        ORCHESTRATOR.handleRequest(request).thenAccept(response -> {
            if (!(response instanceof GetServerForMapProtocol.GetServerForMapResponse(
                    UnderstandableProxyServer server, String gameId, boolean success, String error))
                    || server == null || gameId == null) {
                String reason = response instanceof GetServerForMapProtocol.GetServerForMapResponse r
                        && r.error() != null ? r.error() : "no servers available";
                player.sendMessage("<c>Could not find a dungeon: {}", reason);
                return;
            }

            ORCHESTRATOR.handleRequest(new ChooseGameProtocol.ChooseGameMessage(
                    player.getUuid(), server, gameId)).thenRun(() -> {
                player.sendMessage("<a>Sending you to <f>{}<a>!", server.shortName());
                player.asProxyPlayer().transferToWithIndication(server.uuid());
            }).exceptionally(throwable -> {
                player.sendMessage("<c>Failed to register for the dungeon: {}", throwable.getMessage());
                return null;
            });
        }).exceptionally(throwable -> {
            player.sendMessage("<c>Dungeon search failed: {}", throwable.getMessage());
            return null;
        });
    }

    private static void list(RavengardPlayer player) {
        ORCHESTRATOR.handleRequest(new ListGamesProtocol.ListGamesMessage(ServerType.RAVENGARD_DUNGEON))
                .thenAccept(response -> {
                    if (!(response instanceof ListGamesProtocol.ListGamesResponse listing)
                            || !listing.success()) {
                        player.sendMessage("<c>Failed to list dungeon servers.");
                        return;
                    }
                    if (listing.servers().isEmpty()) {
                        player.sendMessage("<c>No dungeon servers are online.");
                        return;
                    }
                    for (ListGamesProtocol.ServerGames server : listing.servers()) {
                        player.sendMessage("<e>{} <7>- <f>{}<7>/<f>{} players<7>, <f>{}<7> free instance slots",
                                server.shortName(), server.onlinePlayers(), server.maxPlayers(),
                                server.remainingGameSlots() == null ? "?" : server.remainingGameSlots());
                        for (ListGamesProtocol.GameSummary game : server.games()) {
                            boolean dying = game.map() != null && game.map().startsWith("empty:");
                            Text line = Text.of("  <8>- <f>{} <7>{} <f>{} players ",
                                            game.gameId().substring(0, 8), game.gameTypeName(), game.playerCount())
                                    .append(game.acceptingJoins() ? "<a>open" : "<c>closed")
                                    .appendIf(dying, " <c>(dies in {}s)", dying ? game.map().substring(6) : "");
                            player.sendMessage(line);
                        }
                        if (server.games().isEmpty()) {
                            player.sendMessage("  <8>- <7>no instances");
                        }
                    }
                }).exceptionally(throwable -> {
                    player.sendMessage("<c>Listing failed: {}", throwable.getMessage());
                    return null;
                });
    }
}
