package net.swofty.type.skyblockgeneric.commands;

import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.TrackedItem;
import net.swofty.commons.protocol.objects.itemtracker.TrackedItemRetrieveProtocol;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandParameters(labels = "itemtrackedinformation",
        description = "Gets the information of a tracked item in the players hand",
        usage = "/itemtrackedinformation",
        permission = Rank.STAFF,
        allowsConsole = false)
public class GetTrackedItemInformationCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockItem item = new SkyBlockItem(((SkyBlockPlayer) sender).getItemInMainHand());
            if (item.getAttributeHandler().getUniqueTrackedID() == null) {
                sender.sendMessage("<c>This item is not tracked.");
                return;
            }

            ProxyService service = new ProxyService(ServiceType.ITEM_TRACKER);
            if (!service.isOnline().join()) {
                sender.sendMessage("<c>The item tracker service is currently offline.");
                return;
            }
            long start = System.currentTimeMillis();

            CompletableFuture<TrackedItemRetrieveProtocol.TrackedItemResponse> trackedItemFuture = service.handleRequest(
                    new TrackedItemRetrieveProtocol.TrackedItemRetrieveMessage(
                            UUID.fromString(item.getAttributeHandler().getUniqueTrackedID())
            ));

            TrackedItem trackedItem = trackedItemFuture.join().trackedItem();

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            player.sendMessage("<a>Information for tracked item in hand:");
            player.sendMessage("<7>- <e>Item Type: <7>{}", trackedItem.getItemType());
            player.sendMessage("<7>- <e>Item UUID: <7>{}", trackedItem.getItemUUID());
            player.sendMessage("<7>- <e>Number Made: <7>{}", trackedItem.getNumberMade());
            player.sendMessage("<7>- <e>Made: <7>{}", StringUtility.formatTimeAsAgo(trackedItem.getCreated()));
            player.sendMessage("<8>Attached Players: <7>{}", trackedItem.getAttachedPlayers().size());

            trackedItem.getAttachedPlayers().forEach(log -> {
                player.sendMessage("<7>- <e>Player UUID: <7>{}", log.playerUUID());
                player.sendMessage("<7>- <e>Profile UUID: <7>{}", log.playerProfileUUID());
                player.sendMessage("<7>- <e>First Seen: <7>{}", StringUtility.formatTimeAsAgo(log.firstSeen()));
                player.sendMessage("<7>- <e>Last Seen: <7>{}", StringUtility.formatTimeAsAgo(log.lastSeen()));
            });

            player.sendMessage("<c>Took {}ms to get information.", System.currentTimeMillis() - start);
        });
    }
}
