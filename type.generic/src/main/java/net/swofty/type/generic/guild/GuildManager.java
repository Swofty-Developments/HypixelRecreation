package net.swofty.type.generic.guild;

import net.swofty.commons.ServiceType;
import net.swofty.commons.guild.GuildData;
import net.swofty.commons.guild.GuildEvent;
import net.swofty.commons.guild.events.*;
import net.swofty.commons.protocol.objects.guild.GetGuildProtocolObject;
import net.swofty.commons.protocol.objects.guild.IsPlayerInGuildProtocolObject;
import net.swofty.commons.protocol.objects.guild.SendGuildEventToServiceProtocolObject;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GuildManager {
    private static final ProxyService guildService = new ProxyService(ServiceType.GUILD);

    public static boolean isInGuild(HypixelPlayer player) {
        if (!guildService.isOnline().join()) return false;
        return guildService.<IsPlayerInGuildProtocolObject.IsPlayerInGuildMessage,
                IsPlayerInGuildProtocolObject.IsPlayerInGuildResponse>handleRequest(
                new IsPlayerInGuildProtocolObject.IsPlayerInGuildMessage(player.getUuid()))
            .thenApply(IsPlayerInGuildProtocolObject.IsPlayerInGuildResponse::isInGuild)
            .join();
    }

    public static @Nullable GuildData getGuildFromPlayer(HypixelPlayer player) {
        if (!guildService.isOnline().join()) return null;
        return guildService.<GetGuildProtocolObject.GetGuildMessage,
                GetGuildProtocolObject.GetGuildResponse>handleRequest(
                new GetGuildProtocolObject.GetGuildMessage(player.getUuid()))
            .thenApply(GetGuildProtocolObject.GetGuildResponse::guild)
            .join();
    }

    public static void createGuild(HypixelPlayer creator, String guildName) {
        GuildCreateRequestEvent event = new GuildCreateRequestEvent(creator.getUuid(), guildName);
        sendEventToService(event);
    }

    public static void invitePlayer(HypixelPlayer inviter, String targetName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(inviter, "<c>Couldn't find a player with that name!");
            return;
        }
        GuildInviteRequestEvent event = new GuildInviteRequestEvent(inviter.getUuid(), targetUUID);
        sendEventToService(event);
    }

    public static void acceptInvite(HypixelPlayer player, String inviterName) {
        @Nullable UUID inviterUUID = HypixelDataHandler.getPotentialUUIDFromName(inviterName);
        if (inviterUUID == null) {
            sendError(player, "<c>Couldn't find a player with that name!");
            return;
        }
        GuildAcceptInviteRequestEvent event = new GuildAcceptInviteRequestEvent(player.getUuid(), inviterUUID);
        sendEventToService(event);
    }

    public static void leaveGuild(HypixelPlayer player) {
        GuildLeaveRequestEvent event = new GuildLeaveRequestEvent(player.getUuid());
        sendEventToService(event);
    }

    public static void kickPlayer(HypixelPlayer kicker, String targetName, String reason) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(kicker, "<c>Couldn't find a player with that name!");
            return;
        }
        GuildKickRequestEvent event = new GuildKickRequestEvent(kicker.getUuid(), targetUUID, reason);
        sendEventToService(event);
    }

    public static void disbandGuild(HypixelPlayer player) {
        GuildDisbandRequestEvent event = new GuildDisbandRequestEvent(player.getUuid());
        sendEventToService(event);
    }

    public static void promotePlayer(HypixelPlayer promoter, String targetName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(promoter, "<c>Couldn't find a player with that name!");
            return;
        }
        GuildPromoteRequestEvent event = new GuildPromoteRequestEvent(promoter.getUuid(), targetUUID);
        sendEventToService(event);
    }

    public static void demotePlayer(HypixelPlayer demoter, String targetName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(demoter, "<c>Couldn't find a player with that name!");
            return;
        }
        GuildDemoteRequestEvent event = new GuildDemoteRequestEvent(demoter.getUuid(), targetUUID);
        sendEventToService(event);
    }

    public static void transferOwnership(HypixelPlayer owner, String targetName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(owner, "<c>Couldn't find a player with that name!");
            return;
        }
        GuildTransferRequestEvent event = new GuildTransferRequestEvent(owner.getUuid(), targetUUID);
        sendEventToService(event);
    }

    public static void sendChat(HypixelPlayer player, String message, boolean officerChat) {
        GuildChatRequestEvent event = new GuildChatRequestEvent(player.getUuid(), message, officerChat);
        sendEventToService(event);
    }

    public static void changeSetting(HypixelPlayer player, String setting, String value) {
        GuildSettingRequestEvent event = new GuildSettingRequestEvent(player.getUuid(), setting, value);
        sendEventToService(event);
    }

    public static void mutePlayer(HypixelPlayer muter, String target, long duration) {
        String resolvedTarget = resolveTarget(muter, target);
        if (resolvedTarget == null) return;
        GuildMuteRequestEvent event = new GuildMuteRequestEvent(muter.getUuid(), resolvedTarget, duration);
        sendEventToService(event);
    }

    public static void unmutePlayer(HypixelPlayer unmuter, String target) {
        String resolvedTarget = resolveTarget(unmuter, target);
        if (resolvedTarget == null) return;
        GuildUnmuteRequestEvent event = new GuildUnmuteRequestEvent(unmuter.getUuid(), resolvedTarget);
        sendEventToService(event);
    }

    public static void setRank(HypixelPlayer setter, String targetName, String rankName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(setter, "<c>Couldn't find a player with that name!");
            return;
        }
        GuildSetRankRequestEvent event = new GuildSetRankRequestEvent(setter.getUuid(), targetUUID, rankName);
        sendEventToService(event);
    }

    // TODO: make less stupid
    public static void recordProgress(HypixelPlayer player, long gexp, boolean win) {
        sendEventToService(new GuildProgressRequestEvent(player.getUuid(), gexp, win));
    }

    private static void sendEventToService(GuildEvent event) {
        var message = new SendGuildEventToServiceProtocolObject.SendGuildEventToServiceMessage(
            event.getClass().getSimpleName(),
            event.getSerializer().serialize(event)
        );
        guildService.handleRequest(message);
    }

    private static void sendError(HypixelPlayer player, String markup) {
        player.sendMessage("<sep>");
        player.sendMessage(markup);
        player.sendMessage("<sep>");
    }

    private static @Nullable String resolveTarget(HypixelPlayer player, String target) {
        if (target.equalsIgnoreCase("everyone")) return "everyone";
        UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(target);
        if (targetUUID == null) {
            sendError(player, "<c>Couldn't find a player with that name!");
            return null;
        }
        return targetUUID.toString();
    }
}
