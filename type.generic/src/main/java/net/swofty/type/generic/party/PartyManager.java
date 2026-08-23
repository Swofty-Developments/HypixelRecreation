package net.swofty.type.generic.party;

import net.swofty.commons.ServiceType;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.party.PartyAction;
import net.swofty.commons.party.PendingParty;
import net.swofty.commons.protocol.objects.party.GetPartyProtocol;
import net.swofty.commons.protocol.objects.party.IsPlayerInPartyProtocol;
import net.swofty.commons.protocol.objects.party.SendPartyActionProtocol;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PartyManager {
    private static final ProxyService partyService = new ProxyService(ServiceType.PARTY);

    public static boolean isInParty(HypixelPlayer player) {
        if (!partyService.isOnline().join()) return false;
        return partyService.<IsPlayerInPartyProtocol.IsPlayerInPartyMessage,
                        IsPlayerInPartyProtocol.IsPlayerInPartyResponse>handleRequest(
                        new IsPlayerInPartyProtocol.IsPlayerInPartyMessage(player.getUuid()))
                .thenApply(IsPlayerInPartyProtocol.IsPlayerInPartyResponse::isInParty)
                .join();
    }

    public static @Nullable FullParty getPartyFromPlayer(HypixelPlayer player) {
        if (!partyService.isOnline().join()) return null;
        return (FullParty) partyService.<GetPartyProtocol.GetPartyMessage,
                        GetPartyProtocol.GetPartyResponse>handleRequest(
                        new GetPartyProtocol.GetPartyMessage(player.getUuid()))
                .thenApply(GetPartyProtocol.GetPartyResponse::party)
                .join();
    }

    public static void invitePlayer(HypixelPlayer inviter, String targetName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(inviter, "<c>Couldn't find a player with that name!");
            return;
        }
        if (!new ProxyPlayer(targetUUID).isOnline().join()) {
            sendError(inviter, "<c>That player is not online!");
            return;
        }
        if (targetUUID.equals(inviter.getUuid())) {
            sendError(inviter, "<c>You cannot invite yourself!");
            return;
        }
        if (isInParty(inviter)) {
            FullParty party = getPartyFromPlayer(inviter);
            if (party != null && party.getMembers().stream().anyMatch(m -> m.getUuid().equals(targetUUID))) {
                sendError(inviter, "<c>That player is already in your party!");
                return;
            }
        }

        send(new PartyAction.Invite(PendingParty.create(targetUUID, inviter.getUuid())));
    }

    public static void acceptInvite(HypixelPlayer player, String inviterName) {
        @Nullable UUID inviterUUID = HypixelDataHandler.getPotentialUUIDFromName(inviterName);
        if (inviterUUID == null) {
            sendError(player, "<c>Couldn't find a player with that name!");
            return;
        }
        if (isInParty(player)) {
            sendError(player, "<c>You must leave your current party before accepting an invite!");
            return;
        }
        send(new PartyAction.AcceptInvite(player.getUuid(), inviterUUID));
    }

    public static void leaveParty(HypixelPlayer player) {
        send(new PartyAction.Leave(player.getUuid()));
    }

    public static void disbandParty(HypixelPlayer player) {
        send(new PartyAction.Disband(player.getUuid()));
    }

    public static void transferLeadership(HypixelPlayer leader, String targetName) {
        UUID targetUUID = resolveTarget(leader, targetName);
        if (targetUUID == null) return;
        send(new PartyAction.Transfer(leader.getUuid(), targetUUID));
    }

    public static void kickPlayer(HypixelPlayer kicker, String targetName) {
        UUID targetUUID = resolveTarget(kicker, targetName);
        if (targetUUID == null) return;
        send(new PartyAction.Kick(kicker.getUuid(), targetUUID));
    }

    public static void promotePlayer(HypixelPlayer promoter, String targetName) {
        UUID targetUUID = resolveTarget(promoter, targetName);
        if (targetUUID == null) return;
        send(new PartyAction.Promote(promoter.getUuid(), targetUUID));
    }

    public static void demotePlayer(HypixelPlayer demoter, String targetName) {
        UUID targetUUID = resolveTarget(demoter, targetName);
        if (targetUUID == null) return;
        send(new PartyAction.Demote(demoter.getUuid(), targetUUID));
    }

    public static void warpParty(HypixelPlayer warper) {
        send(new PartyAction.Warp(warper.getUuid()));
    }

    public static void sendChat(HypixelPlayer player, String message) {
        send(new PartyAction.Chat(player.getUuid(), message));
    }

    public static void switchPartyServer(HypixelPlayer player) {
        send(new PartyAction.SwitchedServer(player.getUuid()));
    }

    public static void notifyPlayerDisconnect(UUID playerUUID) {
        if (!partyService.isOnline().join()) return;
        send(new PartyAction.PlayerDisconnect(playerUUID));
    }

    public static void notifyPlayerRejoin(UUID playerUUID) {
        if (!partyService.isOnline().join()) return;
        send(new PartyAction.PlayerRejoin(playerUUID));
    }

    public static void hijackParty(HypixelPlayer hijacker, String targetName) {
        if (!hijacker.getRank().isEqualOrHigherThan(Rank.STAFF)) {
            hijacker.sendMessage("<c>You need STAFF to do this command");
            return;
        }
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(hijacker, "<c>Couldn't find a player with that name!");
            return;
        }
        if (!new ProxyPlayer(targetUUID).isOnline().join()) {
            sendError(hijacker, "<c>That player is not online!");
            return;
        }
        send(new PartyAction.Hijack(hijacker.getUuid(), targetUUID));
    }

    private static @Nullable UUID resolveTarget(HypixelPlayer caller, String targetName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(caller, "<c>Couldn't find a player with that name!");
        }
        return targetUUID;
    }

    private static void send(PartyAction action) {
        partyService.handleRequest(new SendPartyActionProtocol.Request(action));
    }

    private static void sendError(HypixelPlayer player, String markup) {
        player.sendMessage("<sep>");
        player.sendMessage(markup);
        player.sendMessage("<sep>");
    }
}
