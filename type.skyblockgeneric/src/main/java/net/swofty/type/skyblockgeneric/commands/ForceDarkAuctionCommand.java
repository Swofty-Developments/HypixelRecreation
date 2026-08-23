package net.swofty.type.skyblockgeneric.commands;

import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.darkauction.TriggerDarkAuctionProtocol;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "forcedarkauction", description = "Force starts a Dark Auction",
        usage = "/forcedarkauction",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceDarkAuctionCommand extends HypixelCommand {
    private static final ProxyService darkAuctionService = new ProxyService(ServiceType.DARK_AUCTION);

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            player.sendMessage("<7>Triggering Dark Auction...");

            if (!darkAuctionService.isOnline().join()) {
                player.sendMessage("<c>Dark Auction service is currently offline.");
                return;
            }

            TriggerDarkAuctionProtocol.TriggerMessage message =
                    new TriggerDarkAuctionProtocol.TriggerMessage(SkyBlockCalendar.getElapsed(), true);

            darkAuctionService.handleRequest(message)
                    .thenAccept(response -> {
                        if (response instanceof TriggerDarkAuctionProtocol.TriggerResponse triggerResponse) {
                            if (triggerResponse.success()) {
                                player.sendMessage("<a>Dark Auction started successfully!");
                            } else {
                                player.sendMessage("<c>Failed to start Dark Auction: {}", triggerResponse.message());
                            }
                        }
                    })
                    .exceptionally(throwable -> {
                        player.sendMessage("<c>Error triggering Dark Auction: {}", throwable.getMessage());
                        return null;
                    });
        });
    }
}
