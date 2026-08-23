package net.swofty.type.generic.command.commands;

import net.swofty.commons.ServerType;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.PlayerWipeService;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.utility.ScheduleUtility;
import org.tinylog.Logger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@CommandParameters(description = "Allows the player to wipe themselves",
        usage = "/wipeme",
        permission = Rank.STAFF,
        labels = "deletemyprofiles wipeme",
        allowsConsole = false)
public class WipeMeCommand extends HypixelCommand {
    private static final ServerType DESTINATION = ServerType.PROTOTYPE_LOBBY;
    private static final long TRANSFER_TIMEOUT_SECONDS = 20;

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;

            HypixelPlayer player = (HypixelPlayer) sender;
            UUID playerUuid = player.getUuid();
            if (!PlayerWipeService.begin(playerUuid)) {
                player.sendMessage("<c>You are already being wiped.");
                return;
            }

            ServerType type = HypixelConst.getTypeLoader().getType();
            player.sendMessage("<c>Wiping everything you own on every server.");
            Thread.startVirtualThread(() -> wipe(player, playerUuid, type));
        });
    }

    private void wipe(HypixelPlayer player, UUID playerUuid, ServerType type) {
        try {
            PlayerDataService.discardAll(type, playerUuid);
            PlayerWipeService.Result result = PlayerWipeService.wipe(playerUuid);
            Logger.info("Wiped {} profile(s), {} island(s), {} leaderboard entr(ies) and {} tracked row(s) of {}",
                    result.profiles().size(), result.islands(), result.leaderboards(), result.ownedRows(), playerUuid);

            if (type == DESTINATION) {
                reload(player, playerUuid, type);
                return;
            }

            new ProxyPlayer(playerUuid).transferWithoutDataTo(DESTINATION)
                    .orTimeout(TRANSFER_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .exceptionally(error -> {
                        Logger.error(error, "Failed to move wiped user {} to the prototype lobby", playerUuid);
                        kick(player);
                        return null;
                    });
        } catch (Exception e) {
            Logger.error(e, "Failed to wipe user {}", playerUuid);
            kick(player);
        } finally {
            PlayerWipeService.finish(playerUuid);
        }
    }

    private void reload(HypixelPlayer player, UUID playerUuid, ServerType type) {
        try {
            PlayerDataService.loadAll(type, playerUuid);
            PlayerDataService.attachAll(type, player);
            PlayerDataService.applyAll(type, player);
            player.sendMessage("<a>You have been wiped.");
        } catch (Exception e) {
            Logger.error(e, "Failed to reload wiped user {}", playerUuid);
            kick(player);
        }
    }

    private void kick(HypixelPlayer player) {
        ScheduleUtility.nextTick(() -> {
            if (player.isOnline()) player.kick("<c>You have been wiped");
        });
    }
}
