package net.swofty.type.generic.redis.service;

import net.swofty.commons.ServerType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.party.PartyBroadcast;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.party.PartyBroadcastPushProtocol;
import net.swofty.commons.protocol.objects.party.PartyBroadcastPushProtocol.Request;
import net.swofty.commons.protocol.objects.party.PartyBroadcastPushProtocol.Response;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.swofty.commons.redis.RedisMessageContext;

public class PartyBroadcastHandler implements RedisMessageHandler<Request, Response> {

    private static final PartyBroadcastPushProtocol PROTOCOL = new PartyBroadcastPushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request message, RedisMessageContext context) {
        try {
            PartyBroadcast broadcast = message.broadcast();
            List<UUID> handled = new ArrayList<>();
            Map<UUID, String> rejected = new HashMap<>();

            for (UUID participant : broadcast.participants()) {
                HypixelPlayer player = findPlayer(participant);
                if (player == null) continue;
                try {
                    DispatchResult result = render(player, broadcast);
                    if (result.handled) handled.add(participant);
                    if (result.rejection != null) rejected.put(participant, result.rejection);
                } catch (Exception e) {
                    Logger.error(e, "Failed to render party broadcast {} for {}",
                            broadcast.getClass().getSimpleName(), participant);
                }
            }
            return Response.success(handled, rejected);
        } catch (Exception e) {
            Logger.error(e, "Failed to handle party broadcast");
            return Response.failure(e.getMessage());
        }
    }

    private static HypixelPlayer findPlayer(UUID uuid) {
        return HypixelGenericLoader.getLoadedPlayers().stream()
                .filter(p -> p.getUuid().equals(uuid))
                .findFirst().orElse(null);
    }

    private static DispatchResult render(HypixelPlayer player, PartyBroadcast broadcast) {
        return switch (broadcast) {
            case PartyBroadcast.Invited b -> renderInvited(player, b);
            case PartyBroadcast.InviteExpired b -> renderInviteExpired(player, b);
            case PartyBroadcast.MemberJoined b -> renderJoined(player, b);
            case PartyBroadcast.MemberLeft b -> renderLeft(player, b);
            case PartyBroadcast.MemberKicked b -> renderKicked(player, b);
            case PartyBroadcast.LeaderTransferred b -> renderTransferred(player, b);
            case PartyBroadcast.RoleChanged b -> renderRoleChanged(player, b);
            case PartyBroadcast.Disbanded b -> renderDisbanded(player, b);
            case PartyBroadcast.Chat b -> renderChat(player, b);
            case PartyBroadcast.Warp b -> renderWarp(player, b);
            case PartyBroadcast.WarpOverview b -> renderWarpOverview(player, b);
            case PartyBroadcast.MemberSwitchedServer b -> renderSwitchedServer(player, b);
            case PartyBroadcast.MemberDisconnected b -> renderDisconnected(player, b);
            case PartyBroadcast.MemberRejoined b -> renderRejoined(player, b);
            case PartyBroadcast.MemberDisconnectTimedOut b -> renderDisconnectTimedOut(player, b);
        };
    }

    private static DispatchResult renderInvited(HypixelPlayer player, PartyBroadcast.Invited b) {
        UUID inviter = b.party().leader();
        UUID invitee = b.party().invitee();
        if (invitee.equals(player.getUuid())) {
            String inviterName = HypixelPlayer.getRawName(inviter);
            player.sendMessage("<sep>");
            player.sendMessage("{} <e>has invited you to join their party!", displayName(inviter));
            player.sendMessage("""
                    <hover:'<e>Click here to join!'><click:run:'/p accept {}'>\
                    <e>You have <c>60 <e>seconds to accept. <6>Click here to join!</click></hover>""",
                    inviterName);
            player.sendMessage("<sep>");
        } else {
            sendBoxed(player, "{} <e>invited {} <e>to join the party! They have <c>60 <e>seconds to accept.",
                    displayName(inviter), displayName(invitee));
        }
        return DispatchResult.ok();
    }

    private static DispatchResult renderInviteExpired(HypixelPlayer player, PartyBroadcast.InviteExpired b) {
        if (b.invitee().equals(player.getUuid())) {
            sendBoxed(player, "<e>The party invite from {} <e>has expired!", displayName(b.inviter()));
        } else if (b.inviter().equals(player.getUuid())) {
            sendBoxed(player, "<e>The party invite to {} <e>has expired.", displayName(b.invitee()));
        }
        return DispatchResult.ok();
    }

    private static DispatchResult renderJoined(HypixelPlayer player, PartyBroadcast.MemberJoined b) {
        UUID leaderUUID = b.party().getLeader().getUuid();
        if (!b.joiner().equals(player.getUuid())) {
            if (leaderUUID.equals(b.inviter())) {
                sendBoxed(player, "{} <e>joined the party.", displayName(b.joiner()));
            } else {
                sendBoxed(player, "{} <e>joined the party using an invite from {}!",
                        displayName(b.joiner()), displayName(b.inviter()));
            }
        } else if (leaderUUID.equals(b.inviter())) {
            sendBoxed(player, "<e>You have joined {}'s <e>party!", displayName(leaderUUID));
        } else {
            sendBoxed(player, "<e>You have joined {}'s <e>party using an invite from {}!",
                    displayName(leaderUUID), displayName(b.inviter()));
        }
        return DispatchResult.ok();
    }

    private static DispatchResult renderLeft(HypixelPlayer player, PartyBroadcast.MemberLeft b) {
        if (b.leaver().equals(player.getUuid())) {
            sendBoxed(player, "<e>You left the party.");
        } else {
            sendBoxed(player, "{} <e>has left the party.", displayName(b.leaver()));
        }
        return DispatchResult.ok();
    }

    private static DispatchResult renderKicked(HypixelPlayer player, PartyBroadcast.MemberKicked b) {
        if (b.kicked().equals(player.getUuid())) {
            sendBoxed(player, "<c>You have been kicked from the party!");
        } else {
            sendBoxed(player, "{} <e>has kicked {} <e>from the party!",
                    displayName(b.kicker()), displayName(b.kicked()));
        }
        return DispatchResult.ok();
    }

    private static DispatchResult renderTransferred(HypixelPlayer player, PartyBroadcast.LeaderTransferred b) {
        if (b.newLeader().equals(player.getUuid())) {
            sendBoxed(player, "<e>You are now the party leader!");
        } else {
            sendBoxed(player, "<e>The party was transferred to {}", displayName(b.newLeader()));
        }
        return DispatchResult.ok();
    }

    private static DispatchResult renderRoleChanged(HypixelPlayer player, PartyBroadcast.RoleChanged b) {
        boolean isDemotion = b.newRole() == FullParty.Role.MEMBER;
        String roleName = b.newRole().name().toLowerCase();
        if (b.promoted().equals(player.getUuid())) {
            if (isDemotion) {
                sendBoxed(player, "<c>You have been demoted to member!");
            } else {
                sendBoxed(player, "<a>You have been promoted to {}!", roleName);
            }
        } else {
            sendBoxed(player, "{} <e>{} {} <e>to {}!", displayName(b.promoter()),
                    isDemotion ? "demoted" : "promoted", displayName(b.promoted()), roleName);
        }
        return DispatchResult.ok();
    }

    private static DispatchResult renderDisbanded(HypixelPlayer player, PartyBroadcast.Disbanded b) {
        sendBoxed(player, "{} <e>has disbanded the party!", displayName(b.disbander()));
        return DispatchResult.ok();
    }

    private static DispatchResult renderChat(HypixelPlayer player, PartyBroadcast.Chat b) {
        player.sendMessage("<9>Party <8>> {}<f>: {}", displayName(b.sender()), b.message());
        return DispatchResult.ok();
    }

    private static DispatchResult renderWarp(HypixelPlayer player, PartyBroadcast.Warp b) {
        if (b.warper().equals(player.getUuid())) {
            player.sendMessage("<7>Warping party...");
            return DispatchResult.ok();
        }

        UUID warper = b.warper();
        Text warperName = displayName(warper);
        FullParty.Member warperMember = b.party().getFromUuid(warper);

        sendBoxed(player, "<e>Party {}, {}<e>, summoned you to their server.",
                warperMember.getRole(), warperName);

        ProxyPlayer warperProxy = new ProxyPlayer(warper);
        if (!warperProxy.isOnline().join()) {
            player.sendMessage("<c>Couldn't find a proxy for {}!", warperName);
            return DispatchResult.rejected("Warper offline");
        }

        UnderstandableProxyServer warperServer = warperProxy.getServer().join();
        if (warperServer.uuid().equals(HypixelConst.getServerUUID())) {
            return DispatchResult.ok();
        }

        new ProxyPlayer(player.getUuid()).transferToWithIndication(warperServer.uuid())
                .orTimeout(2, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    if (player.isOnline()) {
                        throw new RuntimeException(throwable);
                    }
                    return null;
                }).join();
        return DispatchResult.ok();
    }

    private static DispatchResult renderWarpOverview(HypixelPlayer player, PartyBroadcast.WarpOverview b) {
        if (!b.warper().equals(player.getUuid())) return DispatchResult.ok();
        int total = b.warped().size() + b.failed().size();
        player.sendMessage("<sep>");
        player.sendMessage(HypixelConst.getTypeLoader().getType().isSkyBlock()
                        ? "<e>SkyBlock Party Warp <7>({} {})"
                        : "<e>Party Warp <7>({} {})",
                total, total > 1 ? "players" : "player");
        for (UUID uuid : b.warped()) {
            player.sendMessage("<a><l>✔ </l>{} <a>warped to your server", displayName(uuid));
        }
        for (UUID uuid : b.failed()) {
            String reason = b.failureReasons() != null
                    ? b.failureReasons().getOrDefault(uuid, "Unable to warp")
                    : "Unable to warp";
            player.sendMessage("<c><l>✖ </l>{} <c>- {}", displayName(uuid), reason);
        }
        player.sendMessage("<sep>");
        return DispatchResult.ok();
    }

    private static DispatchResult renderSwitchedServer(HypixelPlayer player, PartyBroadcast.MemberSwitchedServer b) {
        if (b.mover().equals(player.getUuid())) return DispatchResult.ok();

        ProxyPlayer mover = new ProxyPlayer(b.mover());
        if (!mover.isOnline().join()) return DispatchResult.ok();

        UnderstandableProxyServer moverServer = mover.getServer().join();
        ServerType moverServerType = moverServer.type();
        Text hover = Text.of("""
                <e>{}
                <9>Party Member
                \s
                <e>Click to follow!""",
                moverServerType.isSkyBlock() ? "SkyBlock Travel" : "Hypixel Travel");

        player.sendMessage("""
                <hover:'{2}'><click:run:'/p movetoserver {3}'>\
                <9><l>» </l>{0} <e>is traveling to <a>{1} <e><l>FOLLOW""",
                displayName(b.mover()), moverServerType.formatName(), hover, moverServer.uuid());
        return DispatchResult.ok();
    }

    private static DispatchResult renderDisconnected(HypixelPlayer player, PartyBroadcast.MemberDisconnected b) {
        if (b.disconnectedPlayer().equals(player.getUuid())) return DispatchResult.ok();
        int minutes = (int) (b.timeoutSeconds() / 60);
        sendBoxed(player, "{} <e>has disconnected. They have <c>{} minutes <e>to rejoin before being removed.",
                displayName(b.disconnectedPlayer()), minutes);
        return DispatchResult.ok();
    }

    private static DispatchResult renderRejoined(HypixelPlayer player, PartyBroadcast.MemberRejoined b) {
        if (b.rejoinedPlayer().equals(player.getUuid())) return DispatchResult.ok();
        sendBoxed(player, "{} <e>has reconnected to the party.", displayName(b.rejoinedPlayer()));
        return DispatchResult.ok();
    }

    private static DispatchResult renderDisconnectTimedOut(HypixelPlayer player, PartyBroadcast.MemberDisconnectTimedOut b) {
        Text name = displayName(b.timedOutPlayer());
        if (b.wasLeader()) {
            sendBoxed(player, "<c>The party leader {} <c>timed out. The party has been disbanded.", name);
        } else if (!b.timedOutPlayer().equals(player.getUuid())) {
            sendBoxed(player, "{} <e>has been removed from the party due to disconnect timeout.", name);
        }
        return DispatchResult.ok();
    }

    private static Text displayName(UUID uuid) {
        return HypixelPlayer.getDisplayName(uuid);
    }

    private static void sendBoxed(HypixelPlayer player, String markup, Object... arguments) {
        player.sendMessage("<sep>");
        player.sendMessage(markup, arguments);
        player.sendMessage("<sep>");
    }

    private record DispatchResult(boolean handled, String rejection) {
        static DispatchResult ok() { return new DispatchResult(true, null); }
        static DispatchResult rejected(String reason) { return new DispatchResult(false, reason); }
    }
}
