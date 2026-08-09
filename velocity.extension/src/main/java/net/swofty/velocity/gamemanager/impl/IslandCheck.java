package net.swofty.velocity.gamemanager.impl;

import com.velocitypowered.api.proxy.Player;
import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.proxy.from.DoesServerHaveIslandProtocol;
import net.swofty.velocity.data.ProfilesDatabase;
import net.swofty.velocity.data.UserDatabase;
import net.swofty.velocity.gamemanager.BalanceConfiguration;
import net.swofty.velocity.gamemanager.GameManager;
import net.swofty.commons.redis.RedisClient;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class IslandCheck extends BalanceConfiguration {

    @Override
    public GameManager.GameServer getServer(Player player, List<GameManager.GameServer> servers) {
        Document userDatabase = new UserDatabase(player.getUniqueId()).getDocument();
        if (userDatabase == null || !userDatabase.containsKey("selected")) {
            return null;
        }

        UUID activeProfile = UUID.fromString(userDatabase.getString("selected"));
        Document document = new ProfilesDatabase(activeProfile.toString()).getDocument();
        if (document == null || !document.containsKey("island_uuid") ||
            document.getString("island_uuid").equals("null")) {
            return null;
        }

        UUID islandUUID = UUID.fromString(document.getString("island_uuid").replace("\"", ""));
        AtomicReference<GameManager.GameServer> toSendTo = getGameServerAtomicReference(islandUUID);

        return toSendTo.get();
    }

    @NotNull
    private static AtomicReference<GameManager.GameServer> getGameServerAtomicReference(UUID islandUUID) {
        AtomicReference<GameManager.GameServer> toSendTo = new AtomicReference<>(null);

        for (Map.Entry<ServerType, ArrayList<GameManager.GameServer>> entry : GameManager.getServers().entrySet()) {
            ServerType serverType = entry.getKey();
            if (serverType == ServerType.SKYBLOCK_ISLAND) {
                ArrayList<GameManager.GameServer> gameServers = entry.getValue();

                new ArrayList<>(gameServers).forEach(gameServer -> {
                    DoesServerHaveIslandProtocol.Response response = RedisClient.requestServer(
                        gameServer.internalID(),
                        new DoesServerHaveIslandProtocol(),
                        new DoesServerHaveIslandProtocol.Request(islandUUID.toString())).join();

                    if (response.serverHasIt()) {
                        toSendTo.set(gameServer);
                    }
                });
            }
        }
        return toSendTo;
    }
}
