package net.swofty.type.generic.redis.service;

import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.commons.friend.FriendEvent;
import net.swofty.commons.friend.FriendSettingType;
import net.swofty.commons.friend.events.response.*;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.friend.FriendEventPushProtocol;
import net.swofty.commons.protocol.objects.friend.FriendEventPushProtocol.Request;
import net.swofty.commons.protocol.objects.friend.FriendEventPushProtocol.Response;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.swofty.commons.redis.RedisMessageContext;

public class FriendEventHandler implements RedisMessageHandler<Request, Response> {

    private static final FriendEventPushProtocol PROTOCOL = new FriendEventPushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request message, RedisMessageContext context) {
        try {
            FriendEvent event = parseEvent(message.eventType(), message.eventData());
            if (event == null) {
                Logger.error("Failed to parse friend event of type: " + message.eventType());
                return Response.failure("Failed to parse event of type: " + message.eventType());
            }

            List<UUID> playersHandled = handleEventForPlayers(event, message.participants());
            return Response.success(playersHandled.size(), playersHandled);
        } catch (Exception e) {
            Logger.error("Failed to handle friend event: " + e.getMessage());
            return Response.failure("Exception occurred: " + e.getMessage());
        }
    }

    private FriendEvent parseEvent(String eventType, String eventData) {
        try {
            FriendEvent templateEvent = FriendEvent.findFromType(eventType);
            return (FriendEvent) templateEvent.getSerializer().deserialize(eventData);
        } catch (Exception e) {
            Logger.error(e, "Failed to parse friend event of type: {}", eventType);
            return null;
        }
    }

    private List<UUID> handleEventForPlayers(FriendEvent event, List<UUID> participants) {
        List<UUID> playersHandled = new ArrayList<>();

        for (UUID participantUUID : participants) {
            HypixelPlayer player = HypixelGenericLoader.getLoadedPlayers().stream()
                    .filter(p -> p.getUuid().equals(participantUUID))
                    .findFirst()
                    .orElse(null);

            if (player != null) {
                try {
                    handleEventForPlayer(player, event);
                    playersHandled.add(participantUUID);
                } catch (Exception e) {
                    Logger.error("Failed to handle friend event for player " + participantUUID + ": " + e.getMessage());
                }
            }
        }

        return playersHandled;
    }

    private void handleEventForPlayer(HypixelPlayer player, FriendEvent event) {
        switch (event) {
            case FriendRequestSentResponseEvent e -> handleRequestSent(player, e);
            case FriendRequestReceivedResponseEvent e -> handleRequestReceived(player, e);
            case FriendAddedResponseEvent e -> handleFriendAdded(player, e);
            case FriendDeniedResponseEvent e -> handleRequestDenied(player, e);
            case FriendRemovedResponseEvent e -> handleFriendRemoved(player, e);
            case FriendRemoveAllResponseEvent e -> handleRemoveAll(player, e);
            case FriendBestToggledResponseEvent e -> handleBestToggled(player, e);
            case FriendNicknameSetResponseEvent e -> handleNicknameSet(player, e);
            case FriendSettingToggledResponseEvent e -> handleSettingToggled(player, e);
            case FriendJoinNotificationEvent e -> handleJoinNotification(player, e);
            case FriendLeaveNotificationEvent e -> handleLeaveNotification(player, e);
            case FriendRequestExpiredResponseEvent e -> handleRequestExpired(player, e);
            case FriendListResponseEvent e -> handleFriendList(player, e);
            case FriendRequestsListResponseEvent e -> handleRequestsList(player, e);
            default -> Logger.warn("Unhandled friend event type: " + event.getClass().getSimpleName());
        }
    }

    private void handleRequestSent(HypixelPlayer player, FriendRequestSentResponseEvent event) {
        sendMessage(player, "<a>Friend request sent to <e>{}<a>!", event.getTargetName());
    }

    private void handleRequestReceived(HypixelPlayer player, FriendRequestReceivedResponseEvent event) {
        String senderName = event.getSenderName();

        player.sendMessage("<sep>");
        player.sendMessage("<a>Friend request from <e>{}", senderName);
        player.sendMessage("""
                <e>Click <6><l>ACCEPT </l><e>or <c><l>DENY</l>\
                <a><l><hover:'<e>Click to accept friend request'><click:run:'/f accept {0}'> [ACCEPT]</click></hover></l>\
                <c><l><hover:'<e>Click to deny friend request'><click:run:'/f deny {0}'> [DENY]</click></hover>""",
                senderName);
        player.sendMessage("<sep>");
    }

    private void handleFriendAdded(HypixelPlayer player, FriendAddedResponseEvent event) {
        String otherName = player.getUuid().equals(event.getPlayer1())
                ? event.getPlayer2Name()
                : event.getPlayer1Name();
        sendMessage(player, "<a>You are now friends with <e>{}<a>!", otherName);
    }

    private void handleRequestDenied(HypixelPlayer player, FriendDeniedResponseEvent event) {
        sendMessage(player, "<e>{} <c>denied your friend request.", event.getDenierName());
    }

    private void handleFriendRemoved(HypixelPlayer player, FriendRemovedResponseEvent event) {
        if (player.getUuid().equals(event.getRemover())) {
            sendMessage(player, "<c>Removed <e>{} <c>from your friends list.",
                    HypixelPlayer.getDisplayName(event.getRemoved()));
        } else {
            sendMessage(player, "<e>{} <c>removed you from their friends list.", event.getRemoverName());
        }
    }

    private void handleRemoveAll(HypixelPlayer player, FriendRemoveAllResponseEvent event) {
        if (event.getRemovedCount() == 0) {
            sendMessage(player, "<c>You have no friends to remove! (Best friends are kept)");
        } else {
            sendMessage(player, "<c>Removed <e>{} <c>friends from your list. Best friends were kept.",
                    event.getRemovedCount());
        }
    }

    private void handleBestToggled(HypixelPlayer player, FriendBestToggledResponseEvent event) {
        if (event.isBest()) {
            sendMessage(player, "<e>{} <a>is now your best friend!", event.getTargetName());
        } else {
            sendMessage(player, "<e>{} <c>is no longer your best friend.", event.getTargetName());
        }
    }

    private void handleNicknameSet(HypixelPlayer player, FriendNicknameSetResponseEvent event) {
        if (event.getNickname() == null || event.getNickname().isEmpty()) {
            sendMessage(player, "<a>Cleared nickname for <e>{}<a>.", event.getTargetName());
        } else {
            sendMessage(player, "<a>Set nickname for <e>{} <a>to <e>{}<a>.",
                    event.getTargetName(), event.getNickname());
        }
    }

    private void handleSettingToggled(HypixelPlayer player, FriendSettingToggledResponseEvent event) {
        String settingName = event.getSettingType() == FriendSettingType.ACCEPTING_REQUESTS
                ? "Friend requests"
                : "Friend join/leave notifications";
        sendMessage(player, event.isNewValue()
                ? "<e>{} <7>are now <a>enabled<7>."
                : "<e>{} <7>are now <c>disabled<7>.", settingName);
    }

    private void handleJoinNotification(HypixelPlayer player, FriendJoinNotificationEvent event) {
        player.sendMessage("<a>Friend > {} <7>joined.", HypixelPlayer.getDisplayName(event.getFriend()));
    }

    private void handleLeaveNotification(HypixelPlayer player, FriendLeaveNotificationEvent event) {
        player.sendMessage("<a>Friend > {} <7>left.", HypixelPlayer.getDisplayName(event.getFriend()));
    }

    private void handleRequestExpired(HypixelPlayer player, FriendRequestExpiredResponseEvent event) {
        if (player.getUuid().equals(event.getSender())) {
            sendMessage(player, "<e>Your friend request to {} <e>has expired.",
                    HypixelPlayer.getDisplayName(event.getTarget()));
        } else {
            sendMessage(player, "<e>The friend request from {} <e>has expired.",
                    HypixelPlayer.getDisplayName(event.getSender()));
        }
    }

    private void handleFriendList(HypixelPlayer player, FriendListResponseEvent event) {
        player.sendMessage("<sep>");
        player.sendMessage("<6>{} <7>(Page {}/{})", event.isBestOnly() ? "Best Friends" : "Friends",
                event.getPage(), event.getTotalPages());

        if (event.getFriends().isEmpty()) {
            player.sendMessage("<7>You have no {}friends to display.", event.isBestOnly() ? "best " : "");
        } else {
            for (FriendListResponseEvent.FriendListEntry friend : event.getFriends()) {
                Text line = Text.of(friend.isOnline() ? "<a>● " : "<c>● ")
                        .appendIf(friend.isBest(), "<6>✦ ")
                        .append(displayNameOf(friend));

                if (friend.getNickname() != null && !friend.getNickname().isEmpty()) {
                    line = line.append(" <7>({})", friend.getNickname());
                }
                if (friend.isOnline() && friend.getServer() != null && !friend.getServer().isEmpty()) {
                    line = line.append(" <7>- <e>{}", StringUtility.toNormalCase(friend.getServer()));
                }

                Text friendsSince = friend.getFriendSince() > 0
                        ? Text.of("<7>Friends for {}", formatDuration(secondsSince(friend.getFriendSince())))
                        : Text.of("<7>Friends since: Unknown");

                Text hover;
                if (friend.isOnline()) {
                    hover = friendsSince;
                } else {
                    Text lastSeen = friend.getLastSeen() > 0
                            ? Text.of("<7>Last seen {} ago", formatDuration(secondsSince(friend.getLastSeen())))
                            : Text.of("<7>Last seen: Unknown");
                    hover = Text.of("{}\n{}", lastSeen, friendsSince);
                }

                player.sendMessage("<hover:'{1}'>{0}", line, hover);
            }
        }

        if (event.getTotalPages() > 1) {
            player.sendMessage("<7>Use <e>/f list {}\\<page> <7>to navigate.", event.isBestOnly() ? "best " : "");
        }

        player.sendMessage("<sep>");
    }

    private void handleRequestsList(HypixelPlayer player, FriendRequestsListResponseEvent event) {
        player.sendMessage("<sep>");
        player.sendMessage("<6>Pending Friend Requests <7>(Page {}/{})", event.getPage(), event.getTotalPages());

        if (event.getRequests().isEmpty()) {
            player.sendMessage("<7>You have no pending friend requests.");
        } else {
            for (FriendRequestsListResponseEvent.FriendRequestEntry request : event.getRequests()) {
                player.sendMessage("""
                        <e>{0} \
                        <a><hover:'<e>Click to accept'><click:run:'/f accept {0}'>[ACCEPT]</click></hover>\
                        <c><hover:'<e>Click to deny'><click:run:'/f deny {0}'> [DENY]</click></hover>""",
                        request.getSenderName());
            }
        }

        if (event.getTotalPages() > 1) {
            player.sendMessage("<7>Use <e>/f requests \\<page> <7>to navigate.");
        }

        player.sendMessage("<sep>");
    }

    private Text displayNameOf(FriendListResponseEvent.FriendListEntry friend) {
        Text displayName;
        try {
            displayName = HypixelPlayer.getDisplayName(friend.getUuid());
        } catch (Exception e) {
            displayName = null;
        }
        return displayName == null || displayName.isEmpty()
                ? Text.of("<e>{}", friend.getName())
                : displayName;
    }

    private long secondsSince(long timestamp) {
        return Math.max(0, (System.currentTimeMillis() - timestamp) / 1000);
    }

    private void sendMessage(HypixelPlayer player, String markup, Object... arguments) {
        player.sendMessage("<sep>");
        player.sendMessage(markup, arguments);
        player.sendMessage("<sep>");
    }

    private String formatDuration(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        if (days > 0) {
            return days + "d " + hours + "h";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m";
        }
        return seconds + "s";
    }
}
