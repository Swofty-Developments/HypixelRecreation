package net.swofty.type.lobby;

import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import net.swofty.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public record LobbyOrchestratorConnector(HypixelPlayer player) {
    private static final ProxyService PROXY_SERVICE = new ProxyService(ServiceType.ORCHESTRATOR);
    private static final List<UUID> PLAYERS_SEARCHING = new ArrayList<>();

    public CompletableFuture<Pair<UnderstandableProxyServer, String>> findGameServer(
            ServerType targetServerType,
            String gameType,
            @Nullable String map,
            int neededSlots
    ) {
        if (PLAYERS_SEARCHING.contains(player.getUuid())) {
            return CompletableFuture.completedFuture(null);
        }

        PLAYERS_SEARCHING.add(player.getUuid());

        GetServerForMapProtocol.GetServerForMapMessage message =
                new GetServerForMapProtocol.GetServerForMapMessage(
                        targetServerType,
                        map,
                        gameType,
                        neededSlots
                );

        return PROXY_SERVICE.handleRequest(message)
                .thenApply(response -> {
                    PLAYERS_SEARCHING.remove(player.getUuid());

                    if (response instanceof GetServerForMapProtocol.GetServerForMapResponse(
                            UnderstandableProxyServer server, String gameId, boolean success, String error
                    )) {
                        return new Pair<>(server, gameId);
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    PLAYERS_SEARCHING.remove(player.getUuid());
                    player.sendMessage("<c>Failed to find server: {}", throwable.getMessage());
                    return null;
                });
    }

    public void sendToGame(ServerType targetServerType, String gameType, @Nullable String map) {
        if (PLAYERS_SEARCHING.contains(player.getUuid())) {
            player.sendMessage("<c>Already searching for a game!");
            return;
        }

        if (PartyManager.isInParty(player)) {
            FullParty party = PartyManager.getPartyFromPlayer(player);
            if (party == null) {
                player.sendMessage("<c>Failed to read your party state. Please try again.");
                return;
            }
            if (!party.getLeader().getUuid().equals(player.getUuid())) {
                player.sendMessage("<c>You are in a party! Ask your leader to start the game, or /p leave");
                return;
            }

            sendPartyToGame(targetServerType, gameType, map, party);
            return;
        }

        // Solo queue
        findGameServer(targetServerType, gameType, map, 1).thenAccept(pair -> {
            if (pair != null && pair.first() != null) {
                ChooseGameProtocol.ChooseGameMessage message =
                        new ChooseGameProtocol.ChooseGameMessage(player.getUuid(), pair.first(), pair.second());

                PROXY_SERVICE.handleRequest(message)
                        .exceptionally(throwable -> {
                            player.sendMessage("<c>Failed to choose game: {}", throwable.getMessage());
                            return null;
                        });

                player.sendMessage("<a>Sending you to {}!", pair.first().shortName());
                player.asProxyPlayer().transferToWithIndication(pair.first().uuid());
            } else {
                player.sendMessage("<c>No available servers found! Please try again later.");
            }
        });
    }

    public void sendPartyToGame(ServerType targetServerType, String gameType, @Nullable String map, FullParty party) {
        int partySize = party.getMembers().size();

        player.sendMessage("<a>Searching for a game for your party ({} players)...", partySize);

        findGameServer(targetServerType, gameType, map, partySize).thenAccept(pair -> {
            if (pair != null && pair.first() != null) {
                UnderstandableProxyServer server = pair.first();
                String gameId = pair.second();

                List<UUID> partyMemberUuids = party.getMembers().stream()
                        .map(FullParty.Member::getUuid)
                        .toList();

                List<CompletableFuture<Void>> registrationFutures = new ArrayList<>();
                for (UUID memberUuid : partyMemberUuids) {
                    ChooseGameProtocol.ChooseGameMessage message =
                            new ChooseGameProtocol.ChooseGameMessage(memberUuid, server, gameId);

                    registrationFutures.add(
                            PROXY_SERVICE.handleRequest(message)
                                    .thenAccept(response -> {})
                                    .exceptionally(throwable -> {
                                        Logger.error("Failed to register party member " + memberUuid + " for game: " + throwable.getMessage());
                                        return null;
                                    })
                    );
                }

                try {
                    CompletableFuture.allOf(registrationFutures.toArray(new CompletableFuture[0]))
                            .orTimeout(3, TimeUnit.SECONDS)
                            .join();
                } catch (Exception e) {
                    Logger.warn("Some party member registrations timed out, proceeding with transfer anyway");
                }

                player.sendMessage("<a>Sending your party to {}!", server.shortName());

                player.asProxyPlayer().transferToWithIndication(server.uuid());

                for (UUID memberUuid : partyMemberUuids) {
                    if (!memberUuid.equals(player.getUuid())) {
                        ProxyPlayer memberProxy = new ProxyPlayer(memberUuid);
                        if (memberProxy.isOnline().join()) {
                            memberProxy.sendMessage("<e>Your party leader is starting a game! Joining...");
                            memberProxy.transferToWithIndication(server.uuid());
                        }
                    }
                }
            } else {
                player.sendMessage("<c>No available servers found with enough space for your party! Please try again later.");
            }
        });
    }

    public void sendToGame(ServerType targetServerType, String gameType) {
        sendToGame(targetServerType, gameType, null);
    }

    public static boolean isSearching(UUID playerUuid) {
        return PLAYERS_SEARCHING.contains(playerUuid);
    }

    public static void removeFromSearching(UUID playerUuid) {
        PLAYERS_SEARCHING.remove(playerUuid);
    }

    public record Pair<A, B>(A first, B second) {}
}
