package net.swofty.service.party;

import net.swofty.commons.party.FullParty;
import net.swofty.commons.party.PartyAction;
import net.swofty.commons.party.PartyBroadcast;
import net.swofty.commons.party.PendingParty;
import net.swofty.commons.protocol.objects.messaging.SendMessagePushProtocol;
import net.swofty.commons.protocol.objects.party.PartyBroadcastPushProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PartyCache {
    private static final long DISCONNECT_TIMEOUT_MS = 300_000;
    private static final long INVITE_TIMEOUT_MS = 60_000;
    private static final long WARP_COOLDOWN_MS = 5_000;
    private static final int BROADCAST_TIMEOUT_MS = 300;
    private static final int WARP_BROADCAST_TIMEOUT_MS = 3_000;

    private static final Map<UUID, FullParty> activeParties = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> playerToParty = new ConcurrentHashMap<>();
    private static final Map<UUID, PendingParty> pendingInvites = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> disconnectTimers = new ConcurrentHashMap<>();
    private static final List<UUID> warpCooldown = new ArrayList<>();

    public static boolean isInParty(UUID playerUUID) {
        return playerToParty.containsKey(playerUUID);
    }

    public static FullParty getPartyFromPlayer(UUID playerUUID) {
        UUID partyUUID = playerToParty.get(playerUUID);
        return partyUUID != null ? activeParties.get(partyUUID) : null;
    }

    public static void handleInvite(PartyAction.Invite action) {
        PendingParty pending = action.party();
        pendingInvites.put(pending.invitee(), pending);
        scheduleInviteExpiration(pending.leader(), pending.invitee());
        broadcast(new PartyBroadcast.Invited(pending));
    }

    public static void handleAcceptInvite(PartyAction.AcceptInvite action) {
        UUID accepter = action.accepter();
        UUID inviter = action.inviter();

        PendingParty pending = pendingInvites.remove(accepter);
        if (pending == null || !pending.leader().equals(inviter)) {
            sendErrorToPlayer(accepter, "<c>You haven't been invited to a party, or the invitation has expired!");
            return;
        }

        FullParty party = getPartyFromPlayer(inviter);
        if (party == null) {
            party = FullParty.create(inviter, accepter);
            activeParties.put(party.getUuid(), party);
            playerToParty.put(inviter, party.getUuid());
        } else {
            party.getMembers().add(new FullParty.Member(accepter, FullParty.Role.MEMBER, true));
        }
        playerToParty.put(accepter, party.getUuid());

        broadcast(new PartyBroadcast.MemberJoined(party, inviter, accepter));
    }

    public static void handleLeave(PartyAction.Leave action) {
        UUID leaver = action.leaver();
        FullParty party = getPartyFromPlayer(leaver);
        if (party == null) {
            sendErrorToPlayer(leaver, "<c>You are not in a party.");
            return;
        }

        cancelDisconnectTimer(leaver);

        FullParty.Member member = getMember(party, leaver);
        if (member.getRole() == FullParty.Role.LEADER) {
            disbandParty(party, leaver, "leader-left");
        } else {
            party.getMembers().remove(member);
            playerToParty.remove(leaver);
            broadcast(new PartyBroadcast.MemberLeft(party, leaver));
        }
    }

    public static void handleDisband(PartyAction.Disband action) {
        UUID disbander = action.disbander();
        FullParty party = getPartyFromPlayer(disbander);
        if (party == null) {
            sendErrorToPlayer(disbander, "<c>You are not in a party.");
            return;
        }

        FullParty.Member member = getMember(party, disbander);
        if (member.getRole() != FullParty.Role.LEADER) {
            sendErrorToPlayer(disbander, "<c>You are not the party leader!");
            return;
        }

        disbandParty(party, disbander, "disbanded");
    }

    public static void handleTransfer(PartyAction.Transfer action) {
        UUID currentLeaderUUID = action.currentLeader();
        UUID newLeaderUUID = action.newLeader();

        FullParty party = getPartyFromPlayer(currentLeaderUUID);
        if (party == null) {
            sendErrorToPlayer(currentLeaderUUID, "<c>You are not in a party!");
            return;
        }

        FullParty.Member currentLeader = getMember(party, currentLeaderUUID);
        FullParty.Member newLeader = getMember(party, newLeaderUUID);

        if (currentLeader.getRole() != FullParty.Role.LEADER) {
            sendErrorToPlayer(currentLeaderUUID, "<c>You are not the party leader!");
            return;
        }
        if (newLeader == null) {
            sendErrorToPlayer(currentLeaderUUID, "<c>That player is not in your party!");
            return;
        }

        currentLeader.setRole(FullParty.Role.MEMBER);
        newLeader.setRole(FullParty.Role.LEADER);
        broadcast(new PartyBroadcast.LeaderTransferred(party, currentLeaderUUID, newLeaderUUID));
    }

    public static void handleKick(PartyAction.Kick action) {
        UUID kickerUUID = action.kicker();
        UUID targetUUID = action.target();

        FullParty party = getPartyFromPlayer(kickerUUID);
        if (party == null) {
            sendErrorToPlayer(kickerUUID, "<c>You are not in a party!");
            return;
        }

        FullParty.Member kicker = getMember(party, kickerUUID);
        FullParty.Member target = getMember(party, targetUUID);

        if (kicker.getRole() == FullParty.Role.MEMBER) {
            sendErrorToPlayer(kickerUUID, "<c>Only party leaders and moderators can kick players!");
            return;
        }
        if (target == null) {
            sendErrorToPlayer(kickerUUID, "<c>That player is not in your party!");
            return;
        }
        if (target.getRole() == FullParty.Role.LEADER) {
            sendErrorToPlayer(kickerUUID, "<c>You cannot kick the party leader!");
            return;
        }

        cancelDisconnectTimer(targetUUID);
        party.getMembers().remove(target);
        playerToParty.remove(targetUUID);
        broadcast(new PartyBroadcast.MemberKicked(party, kickerUUID, targetUUID));
    }

    public static void handlePromote(PartyAction.Promote action) {
        UUID promoterUUID = action.promoter();
        UUID targetUUID = action.target();

        FullParty party = getPartyFromPlayer(promoterUUID);
        if (party == null) return;

        FullParty.Member promoter = getMember(party, promoterUUID);
        FullParty.Member target = getMember(party, targetUUID);

        if (promoter.getRole() != FullParty.Role.LEADER) {
            sendErrorToPlayer(promoterUUID, "<c>Only the party leader can promote players!");
            return;
        }
        if (target == null || target.getRole() == FullParty.Role.MODERATOR) return;

        target.setRole(FullParty.Role.MODERATOR);
        broadcast(new PartyBroadcast.RoleChanged(party, promoterUUID, targetUUID, FullParty.Role.MODERATOR));
    }

    public static void handleDemote(PartyAction.Demote action) {
        UUID demoterUUID = action.demoter();
        UUID targetUUID = action.target();

        FullParty party = getPartyFromPlayer(demoterUUID);
        if (party == null) return;

        FullParty.Member demoter = getMember(party, demoterUUID);
        FullParty.Member target = getMember(party, targetUUID);

        if (demoter.getRole() != FullParty.Role.LEADER) return;
        if (target == null || target.getRole() == FullParty.Role.MEMBER) return;

        target.setRole(FullParty.Role.MEMBER);
        broadcast(new PartyBroadcast.RoleChanged(party, demoterUUID, targetUUID, FullParty.Role.MEMBER));
    }

    public static void handleChat(PartyAction.Chat action) {
        UUID sender = action.player();
        FullParty party = getPartyFromPlayer(sender);
        if (party == null) {
            sendErrorToPlayer(sender, "<c>You are not in a party!");
            return;
        }
        broadcast(new PartyBroadcast.Chat(party, sender, action.message()));
    }

    public static void handleSwitchedServer(PartyAction.SwitchedServer action) {
        UUID mover = action.mover();
        FullParty party = getPartyFromPlayer(mover);
        if (party == null) return;
        broadcast(new PartyBroadcast.MemberSwitchedServer(party, mover));
    }

    public static void handleHijack(PartyAction.Hijack action) {
        UUID hijackerUUID = action.hijacker();
        UUID targetUUID = action.target();

        FullParty targetParty = getPartyFromPlayer(targetUUID);
        if (targetParty == null) {
            sendErrorToPlayer(hijackerUUID, "<c>That player is not in a party!");
            return;
        }

        FullParty currentParty = getPartyFromPlayer(hijackerUUID);
        if (currentParty != null) {
            handleLeave(new PartyAction.Leave(hijackerUUID));
        }

        FullParty.Member currentLeader = targetParty.getMembers().stream()
                .filter(m -> m.getRole() == FullParty.Role.LEADER)
                .findFirst().orElse(null);
        if (currentLeader != null) {
            currentLeader.setRole(FullParty.Role.MEMBER);
        }

        targetParty.getMembers().add(new FullParty.Member(hijackerUUID, FullParty.Role.LEADER, true));
        playerToParty.put(hijackerUUID, targetParty.getUuid());
    }

    public static void handleWarp(PartyAction.Warp action) {
        UUID warperUUID = action.warper();

        FullParty party = getPartyFromPlayer(warperUUID);
        if (party == null) {
            sendErrorToPlayer(warperUUID, "<c>You are not in a party!");
            return;
        }

        FullParty.Member warper = getMember(party, warperUUID);
        if (warper.getRole() != FullParty.Role.LEADER) {
            sendErrorToPlayer(warperUUID, "<c>You are not the party leader!");
            return;
        }
        if (party.getMembers().size() == 1) {
            sendErrorToPlayer(warperUUID, "<c>You need someone else in the party to warp!");
            return;
        }
        if (warpCooldown.contains(warperUUID)) {
            sendErrorToPlayer(warperUUID, "<c>You can only warp once every 5 seconds!");
            return;
        }

        warpCooldown.add(warperUUID);
        CompletableFuture.delayedExecutor(WARP_COOLDOWN_MS, TimeUnit.MILLISECONDS)
                .execute(() -> warpCooldown.remove(warperUUID));

        List<UUID> intendedToWarp = party.getParticipants().stream()
                .filter(uuid -> !uuid.equals(warperUUID))
                .toList();

        Map<UUID, PartyBroadcastPushProtocol.Response> responses = RedisClient.requestAllServersFromService(
                new PartyBroadcastPushProtocol(),
                new PartyBroadcastPushProtocol.Request(new PartyBroadcast.Warp(party, warperUUID)),
                WARP_BROADCAST_TIMEOUT_MS).join();

        List<UUID> warped = new ArrayList<>();
        Map<UUID, String> failures = new HashMap<>();
        for (PartyBroadcastPushProtocol.Response response : responses.values()) {
            if (!response.success()) continue;
            for (UUID uuid : response.playersHandled()) {
                if (intendedToWarp.contains(uuid)) warped.add(uuid);
            }
            if (response.rejectedPlayers() != null) {
                failures.putAll(response.rejectedPlayers());
            }
        }
        List<UUID> didNotWarp = intendedToWarp.stream().filter(uuid -> !warped.contains(uuid)).toList();
        broadcast(new PartyBroadcast.WarpOverview(party, warperUUID, warped, didNotWarp, failures));
    }

    public static void handlePlayerDisconnect(PartyAction.PlayerDisconnect action) {
        UUID playerUUID = action.disconnectedPlayer();
        FullParty party = getPartyFromPlayer(playerUUID);
        if (party == null || disconnectTimers.containsKey(playerUUID)) return;

        UUID timerId = UUID.randomUUID();
        disconnectTimers.put(playerUUID, timerId);

        CompletableFuture.delayedExecutor(DISCONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .execute(() -> resolveDisconnectTimeout(playerUUID, timerId));

        broadcast(new PartyBroadcast.MemberDisconnected(party, playerUUID, DISCONNECT_TIMEOUT_MS / 1000));
    }

    public static void handlePlayerRejoin(PartyAction.PlayerRejoin action) {
        UUID playerUUID = action.rejoinedPlayer();
        if (disconnectTimers.remove(playerUUID) == null) return;

        FullParty party = getPartyFromPlayer(playerUUID);
        if (party == null) return;

        broadcast(new PartyBroadcast.MemberRejoined(party, playerUUID));
    }

    private static void resolveDisconnectTimeout(UUID playerUUID, UUID timerId) {
        UUID currentTimerId = disconnectTimers.get(playerUUID);
        if (currentTimerId == null || !currentTimerId.equals(timerId)) return;

        FullParty party = getPartyFromPlayer(playerUUID);
        if (party == null) {
            disconnectTimers.remove(playerUUID);
            return;
        }
        FullParty.Member member = getMember(party, playerUUID);
        if (member == null) {
            disconnectTimers.remove(playerUUID);
            return;
        }
        disconnectTimers.remove(playerUUID);

        if (member.getRole() == FullParty.Role.LEADER) {
            disbandPartyDueToTimeout(party, playerUUID);
        } else {
            party.getMembers().remove(member);
            playerToParty.remove(playerUUID);
            broadcast(new PartyBroadcast.MemberDisconnectTimedOut(party, playerUUID, false));
        }
    }

    private static void disbandPartyDueToTimeout(FullParty party, UUID timedOutLeader) {
        for (FullParty.Member member : party.getMembers()) {
            disconnectTimers.remove(member.getUuid());
            playerToParty.remove(member.getUuid());
        }
        activeParties.remove(party.getUuid());
        broadcast(new PartyBroadcast.MemberDisconnectTimedOut(party, timedOutLeader, true));
    }

    private static void disbandParty(FullParty party, UUID disbander, String reason) {
        for (FullParty.Member member : party.getMembers()) {
            cancelDisconnectTimer(member.getUuid());
            playerToParty.remove(member.getUuid());
        }
        activeParties.remove(party.getUuid());
        broadcast(new PartyBroadcast.Disbanded(party, disbander, reason));
    }

    private static void cancelDisconnectTimer(UUID playerUUID) {
        disconnectTimers.remove(playerUUID);
    }

    private static void broadcast(PartyBroadcast broadcast) {
        RedisClient.requestAllServersFromService(
                new PartyBroadcastPushProtocol(),
                new PartyBroadcastPushProtocol.Request(broadcast),
                BROADCAST_TIMEOUT_MS);
    }

    private static void sendErrorToPlayer(UUID playerUUID, String markup, Object... arguments) {
        Text message = Text.of("<sep>\n").append(markup, arguments).append("\n<sep>");
        RedisClient.requestAllServersFromService(
                new SendMessagePushProtocol(),
                new SendMessagePushProtocol.Request(playerUUID, message.serialize()),
                BROADCAST_TIMEOUT_MS);
    }

    private static FullParty.Member getMember(FullParty party, UUID playerUUID) {
        return party.getMembers().stream()
                .filter(member -> member.getUuid().equals(playerUUID))
                .findFirst().orElse(null);
    }

    private static void scheduleInviteExpiration(UUID inviter, UUID invitee) {
        CompletableFuture.delayedExecutor(INVITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    PendingParty removed = pendingInvites.remove(invitee);
                    if (removed == null) return;
                    FullParty party = getPartyFromPlayer(invitee);
                    if (party == null || getMember(party, invitee) == null) {
                        broadcast(new PartyBroadcast.InviteExpired(removed, inviter, invitee));
                    }
                });
    }
}
