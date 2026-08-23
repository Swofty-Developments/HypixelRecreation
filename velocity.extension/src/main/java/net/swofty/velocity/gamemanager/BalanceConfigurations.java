package net.swofty.velocity.gamemanager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.velocitypowered.api.proxy.Player;
import net.swofty.commons.ServerType;
import net.swofty.commons.text.Text;
import net.swofty.velocity.gamemanager.impl.IslandCheck;
import net.swofty.velocity.gamemanager.impl.LowestPlayerCount;
import net.swofty.velocity.testflow.TestFlowManager;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BalanceConfigurations {

    // Shared singletons — the strategies are stateless, so there's no reason to
    // hold 25+ separate instances when one per kind suffices.
    private static final BalanceConfiguration ISLAND_CHECK = new IslandCheck();
    private static final BalanceConfiguration LOWEST_PLAYER_COUNT = new LowestPlayerCount();
    private static final List<BalanceConfiguration> DEFAULT_CHAIN = List.of(LOWEST_PLAYER_COUNT);

    public static final Map<ServerType, List<BalanceConfiguration>> CONFIGURATIONS = Map.ofEntries(
            Map.entry(ServerType.SKYBLOCK_ISLAND, List.of(ISLAND_CHECK, LOWEST_PLAYER_COUNT)),
            Map.entry(ServerType.SKYBLOCK_HUB, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_DUNGEON_HUB, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_THE_FARMING_ISLANDS, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_SPIDERS_DEN, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_THE_END, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_CRIMSON_ISLE, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_GOLD_MINE, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_DEEP_CAVERNS, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_DWARVEN_MINES, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_THE_PARK, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_GALATEA, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_BACKWATER_BAYOU, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYBLOCK_JERRYS_WORKSHOP, DEFAULT_CHAIN),
            Map.entry(ServerType.PROTOTYPE_LOBBY, DEFAULT_CHAIN),
            Map.entry(ServerType.BEDWARS_LOBBY, DEFAULT_CHAIN),
            Map.entry(ServerType.BEDWARS_GAME, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYWARS_LOBBY, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYWARS_GAME, DEFAULT_CHAIN),
            Map.entry(ServerType.MURDER_MYSTERY_LOBBY, DEFAULT_CHAIN),
            Map.entry(ServerType.MURDER_MYSTERY_GAME, DEFAULT_CHAIN),
            Map.entry(ServerType.SKYWARS_CONFIGURATOR, DEFAULT_CHAIN),
            Map.entry(ServerType.BEDWARS_CONFIGURATOR, DEFAULT_CHAIN),
            Map.entry(ServerType.MURDER_MYSTERY_CONFIGURATOR, DEFAULT_CHAIN),
            Map.entry(ServerType.RAVENGARD_LOBBY, DEFAULT_CHAIN)
    );

    public static @Nullable GameManager.GameServer getServerFor(Player player, ServerType type) {
        final boolean inTestFlow = TestFlowManager.isPlayerInTestFlow(player.getUsername());
        if (inTestFlow) {
            player.sendMessage(Text.of("<e>You are currently in a network-isolated test flow, load balancing will be restricted to test flow servers!"));
            player.sendMessage(Text.of("<8>Executing test flow {}...",
                    TestFlowManager.getTestFlowForPlayer(player.getUsername()).getName()));
        }

        try {
            for (BalanceConfiguration configuration : CONFIGURATIONS.getOrDefault(type, DEFAULT_CHAIN)) {
                List<GameManager.GameServer> serversToConsider = GameManager.getFromType(type);
                if (inTestFlow) {
                    serversToConsider.removeIf(server -> !isEligibleForTestFlowPlayer(server, player));
                } else {
                    serversToConsider.removeIf(server -> !isEligibleForRegularPlayer(server));
                }

                GameManager.GameServer server = configuration.getServer(player, serversToConsider);
                if (server != null) {
                    if (inTestFlow) {
                        player.sendMessage(Text.of("<8>Done overriding the server manager for your test flow."));
                    }
                    return server;
                }
            }
            return null;
        } catch (Exception e) {
            Logger.error(e, "Error in trying to balance type {} for player {}",
                    type.name(), player.getUsername());
            throw e;
        }
    }

    private static boolean hasCapacity(GameManager.GameServer server) {
        return server.maxPlayers() > server.registeredServer().getPlayersConnected().size();
    }

    private static boolean isEligibleForRegularPlayer(GameManager.GameServer server) {
        return hasCapacity(server) && !TestFlowManager.isServerInTestFlow(server.internalID());
    }

    private static boolean isEligibleForTestFlowPlayer(GameManager.GameServer server, Player player) {
        if (!hasCapacity(server)) return false;
        if (!TestFlowManager.isServerInTestFlow(server.internalID())) return false;

        TestFlowManager.ProxyTestFlowInstance instance =
                TestFlowManager.getFromServerUUID(server.internalID());
        return instance != null && instance.hasPlayer(player.getUsername());
    }
}
