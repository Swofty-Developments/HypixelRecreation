package net.swofty.service.guild;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.swofty.commons.guild.GuildData;
import net.swofty.commons.guild.GuildEvent;
import net.swofty.commons.guild.GuildMember;
import net.swofty.commons.guild.GuildPermission;
import net.swofty.commons.guild.GuildRank;
import net.swofty.commons.guild.events.*;
import net.swofty.commons.guild.events.response.*;
import net.swofty.commons.protocol.objects.guild.GuildEventPushProtocol;
import net.swofty.commons.protocol.objects.messaging.SendMessagePushProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.text.Text;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GuildCache {
    private static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm z");
    private static final Cache<UUID, GuildData> guildCache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build();

    private static final Map<UUID, PendingGuildInvite> pendingInvites = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastChatTimestamps = new ConcurrentHashMap<>();

    public static boolean isInGuild(final @NotNull UUID playerUUID) {
        Objects.requireNonNull(playerUUID, "playerUUID cannot be null");

        return getGuildFromPlayer(playerUUID) != null;
    }

    public static GuildData getGuildFromPlayer(final @NotNull UUID playerUUID) {
        Objects.requireNonNull(playerUUID, "playerUUID cannot be null");

        UUID guildId = GuildDatabase.getPlayerGuildId(playerUUID);
        if (guildId == null) return null;
        GuildData guild = getGuild(guildId);
        if (guild == null) {
            GuildDatabase.removePlayerMapping(playerUUID);
        }
        return guild;
    }

    public static GuildData getGuild(final @NotNull UUID guildId) {
        return guildCache.get(guildId, GuildDatabase::getGuild);
    }

    public static void handleCreateRequest(final GuildCreateRequestEvent event) {
        final UUID creator = event.getCreator();
        final String guildName = event.getGuildName();

        if (isInGuild(creator)) {
            sendErrorToPlayer(creator, "<c>You are already in a guild! Leave your current guild to create a new one.");
            return;
        }

        if (guildName.length() < 3 || guildName.length() > 30) {
            sendErrorToPlayer(creator, "<c>Guild name must be between 3 and 30 characters.");
            return;
        }

        if (GuildDatabase.guildNameExists(guildName)) {
            sendErrorToPlayer(creator, "<c>A guild with that name already exists!");
            return;
        }

        final GuildData guild = new GuildData(UUID.randomUUID(), guildName, creator);
        persistGuild(guild);
        GuildDatabase.mapPlayerToGuild(creator, guild.getGuildId());

        GuildCreatedResponseEvent response = new GuildCreatedResponseEvent(guild, creator);
        sendEvent(response);
    }

    public static void handleInviteRequest(GuildInviteRequestEvent event) {
        UUID inviter = event.getInviter();
        UUID invitee = event.getInvitee();

        GuildData guild = getGuildFromPlayer(inviter);
        if (guild == null) {
            sendErrorToPlayer(inviter, "<c>You are not in a guild!");
            return;
        }

        GuildRank inviterRank = guild.getMemberRank(inviter);
        if (inviterRank == null || !inviterRank.hasPermission(GuildPermission.INVITE)) {
            sendErrorToPlayer(inviter, "<c>You do not have permission to invite players!");
            return;
        }

        if (guild.isFull()) {
            sendErrorToPlayer(inviter, "<c>Your guild is full!");
            return;
        }

        if (isInGuild(invitee)) {
            sendErrorToPlayer(inviter, "<c>That player is already in a guild!");
            return;
        }

        if (pendingInvites.containsKey(invitee)) {
            sendErrorToPlayer(inviter, "<c>That player already has a pending guild invite!");
            return;
        }

        PendingGuildInvite invite = new PendingGuildInvite(guild.getGuildId(), inviter, invitee);
        pendingInvites.put(invitee, invite);
        scheduleInviteExpiration(guild.getGuildId(), inviter, invitee, 60000);
        log(guild, "<a>{}<7> invited <e>{}", inviter, invitee);
        persistGuild(guild);

        GuildInviteSentResponseEvent response = new GuildInviteSentResponseEvent(guild, inviter, invitee);
        sendEvent(response);
    }

    public static void handleAcceptInvite(GuildAcceptInviteRequestEvent event) {
        UUID accepter = event.getAccepter();
        UUID inviter = event.getInviter();

        PendingGuildInvite invite = pendingInvites.get(accepter);
        if (invite == null || !invite.inviter().equals(inviter)) {
            sendErrorToPlayer(accepter, "<c>You don't have a pending invite from that player, or it has expired!");
            return;
        }

        if (isInGuild(accepter)) {
            sendErrorToPlayer(accepter, "<c>You must leave your current guild before joining another!");
            return;
        }

        GuildData guild = getGuild(invite.guildId());
        if (guild == null) {
            sendErrorToPlayer(accepter, "<c>The guild no longer exists!");
            pendingInvites.remove(accepter);
            return;
        }

        if (guild.isFull()) {
            sendErrorToPlayer(accepter, "<c>The guild is now full!");
            pendingInvites.remove(accepter);
            return;
        }

        GuildMember newMember = new GuildMember(accepter, "Member", System.currentTimeMillis());
        guild.getMembers().add(newMember);
        log(guild, "<a>{}<7> joined", accepter);
        pendingInvites.remove(accepter);

        persistGuild(guild);
        GuildDatabase.mapPlayerToGuild(accepter, guild.getGuildId());

        GuildMemberJoinedResponseEvent response = new GuildMemberJoinedResponseEvent(guild, accepter);
        sendEvent(response);
    }

    public static void handleLeaveRequest(GuildLeaveRequestEvent event) {
        UUID leaver = event.getLeaver();

        GuildData guild = getGuildFromPlayer(leaver);
        if (guild == null) {
            sendErrorToPlayer(leaver, "<c>You are not in a guild!");
            return;
        }

        if (guild.getMasterUuid().equals(leaver)) {
            sendErrorToPlayer(leaver, "<c>You cannot leave a guild as the Guild Master! Transfer ownership first or disband the guild.");
            return;
        }

        GuildMember member = guild.getMember(leaver);
        guild.getMembers().remove(member);
        log(guild, "<a>{}<7> left", leaver);

        persistGuild(guild);
        GuildDatabase.removePlayerMapping(leaver);

        GuildMemberLeftResponseEvent response = new GuildMemberLeftResponseEvent(guild, leaver);
        sendEvent(response);
    }

    public static void handleKickRequest(GuildKickRequestEvent event) {
        UUID kicker = event.getKicker();
        UUID target = event.getTarget();
        String reason = event.getReason();

        GuildData guild = getGuildFromPlayer(kicker);
        if (guild == null) {
            sendErrorToPlayer(kicker, "<c>You are not in a guild!");
            return;
        }

        GuildRank kickerRank = guild.getMemberRank(kicker);
        if (kickerRank == null || !kickerRank.hasPermission(GuildPermission.KICK)) {
            sendErrorToPlayer(kicker, "<c>You do not have permission to kick members!");
            return;
        }

        GuildMember targetMember = guild.getMember(target);
        if (targetMember == null) {
            sendErrorToPlayer(kicker, "<c>That player is not in your guild!");
            return;
        }

        GuildRank targetRank = guild.getRank(targetMember.getRankName());
        if (targetRank != null && !kickerRank.isHigherThan(targetRank)) {
            sendErrorToPlayer(kicker, "<c>You cannot kick someone of equal or higher rank!");
            return;
        }

        guild.getMembers().remove(targetMember);
        log(guild, Text.of("<a>{}<7> kicked <e>{}", kicker, target)
                .appendIf(!reason.isBlank(), " <7>for <e>{}", reason));

        persistGuild(guild);
        GuildDatabase.removePlayerMapping(target);

        GuildMemberKickedResponseEvent response = new GuildMemberKickedResponseEvent(guild, kicker, target, reason);
        sendEvent(response);
    }

    public static void handleDisbandRequest(GuildDisbandRequestEvent event) {
        UUID disbander = event.getDisbander();

        GuildData guild = getGuildFromPlayer(disbander);
        if (guild == null) {
            sendErrorToPlayer(disbander, "<c>You are not in a guild!");
            return;
        }

        if (!guild.getMasterUuid().equals(disbander)) {
            sendErrorToPlayer(disbander, "<c>Only the Guild Master can disband the guild!");
            return;
        }

        disbandGuild(guild, disbander);
    }

    public static void handlePromoteRequest(GuildPromoteRequestEvent event) {
        UUID promoter = event.getPromoter();
        UUID target = event.getTarget();

        GuildData guild = getGuildFromPlayer(promoter);
        if (guild == null) {
            sendErrorToPlayer(promoter, "<c>You are not in a guild!");
            return;
        }

        GuildRank promoterRank = guild.getMemberRank(promoter);
        if (promoterRank == null || !promoterRank.hasPermission(GuildPermission.PROMOTE)) {
            sendErrorToPlayer(promoter, "<c>You do not have permission to promote members!");
            return;
        }

        GuildMember targetMember = guild.getMember(target);
        if (targetMember == null) {
            sendErrorToPlayer(promoter, "<c>That player is not in your guild!");
            return;
        }

        GuildRank currentRank = guild.getRank(targetMember.getRankName());
        if (currentRank == null) return;

        GuildRank nextRank = guild.getNextRankUp(currentRank);
        if (nextRank == null || nextRank.getName().equals("Guild Master")) {
            sendErrorToPlayer(promoter, "<c>That player cannot be promoted any further!");
            return;
        }

        if (!promoterRank.isHigherThan(nextRank)) {
            sendErrorToPlayer(promoter, "<c>You can only promote members to ranks below yours!");
            return;
        }

        String oldRank = targetMember.getRankName();
        targetMember.setRankName(nextRank.getName());
        log(guild, "<a>{}<7> set rank of <e>{} <7>to <e>{}", promoter, target, nextRank.getName());
        persistGuild(guild);

        GuildRankChangedResponseEvent response = new GuildRankChangedResponseEvent(guild, promoter, target, oldRank, nextRank.getName());
        sendEvent(response);
    }

    public static void handleDemoteRequest(GuildDemoteRequestEvent event) {
        UUID demoter = event.getDemoter();
        UUID target = event.getTarget();

        GuildData guild = getGuildFromPlayer(demoter);
        if (guild == null) {
            sendErrorToPlayer(demoter, "<c>You are not in a guild!");
            return;
        }

        GuildRank demoterRank = guild.getMemberRank(demoter);
        if (demoterRank == null || !demoterRank.hasPermission(GuildPermission.DEMOTE)) {
            sendErrorToPlayer(demoter, "<c>You do not have permission to demote members!");
            return;
        }

        GuildMember targetMember = guild.getMember(target);
        if (targetMember == null) {
            sendErrorToPlayer(demoter, "<c>That player is not in your guild!");
            return;
        }

        GuildRank currentRank = guild.getRank(targetMember.getRankName());
        if (currentRank == null) return;

        GuildRank nextRank = guild.getNextRankDown(currentRank);
        if (nextRank == null) {
            sendErrorToPlayer(demoter, "<c>That player cannot be demoted any further!");
            return;
        }

        if (!demoterRank.isHigherThan(currentRank)) {
            sendErrorToPlayer(demoter, "<c>You can only demote members of lower rank!");
            return;
        }

        String oldRank = targetMember.getRankName();
        targetMember.setRankName(nextRank.getName());
        log(guild, "<a>{}<7> set rank of <e>{} <7>to <e>{}", demoter, target, nextRank.getName());
        persistGuild(guild);

        GuildRankChangedResponseEvent response = new GuildRankChangedResponseEvent(guild, demoter, target, oldRank, nextRank.getName());
        sendEvent(response);
    }

    public static void handleTransferRequest(GuildTransferRequestEvent event) {
        UUID currentOwner = event.getCurrentOwner();
        UUID newOwner = event.getNewOwner();

        GuildData guild = getGuildFromPlayer(currentOwner);
        if (guild == null) {
            sendErrorToPlayer(currentOwner, "<c>You are not in a guild!");
            return;
        }

        if (!guild.getMasterUuid().equals(currentOwner)) {
            sendErrorToPlayer(currentOwner, "<c>Only the Guild Master can transfer ownership!");
            return;
        }

        GuildMember newOwnerMember = guild.getMember(newOwner);
        if (newOwnerMember == null) {
            sendErrorToPlayer(currentOwner, "<c>That player is not in your guild!");
            return;
        }

        GuildMember currentOwnerMember = guild.getMember(currentOwner);
        currentOwnerMember.setRankName("Officer");
        newOwnerMember.setRankName("Guild Master");
        log(guild, "<a>{}<7> transferred the guild to <e>{}", currentOwner, newOwner);
        persistGuild(guild);

        GuildTransferredResponseEvent response = new GuildTransferredResponseEvent(guild, currentOwner, newOwner);
        sendEvent(response);
    }

    public static void handleProgressRequest(GuildProgressRequestEvent event) {
        GuildData guild = getGuildFromPlayer(event.getPlayer());
        if (guild == null) return;
        guild.resetDailyProgressIfNeeded();
        GuildMember member = guild.getMember(event.getPlayer());
        if (member == null) return;
        if (event.getGexp() > 0) {
            guild.addGexp(event.getGexp());
            guild.addDailyGexp(event.getGexp());
            member.setWeeklyGexp(member.getWeeklyGexp() + event.getGexp());
            member.setTotalGexp(member.getTotalGexp() + event.getGexp());
        }
        if (event.isWin()) guild.addDailyWin();
        persistGuild(guild);
    }

    public static void handleChatRequest(GuildChatRequestEvent event) {
        UUID sender = event.getSender();
        String message = event.getMessage();
        boolean officerChat = event.isOfficerChat();

        GuildData guild = getGuildFromPlayer(sender);
        if (guild == null) {
            sendErrorToPlayer(sender, "<c>You are not in a guild!");
            return;
        }

        if (officerChat) {
            GuildRank rank = guild.getMemberRank(sender);
            if (rank == null || !rank.hasPermission(GuildPermission.OFFICER_CHAT)) {
                sendErrorToPlayer(sender, "<c>You do not have permission to use officer chat!");
                return;
            }
        }

        GuildMember member = guild.getMember(sender);
        if (member != null && member.isMuted()) {
            sendErrorToPlayer(sender, "<c>You are muted in this guild!");
            return;
        }

        if (guild.isEveryoneMuted() && guild.getEveryoneMutedExpiry() > System.currentTimeMillis()) {
            GuildRank rank = guild.getMemberRank(sender);
            if (rank == null || !rank.hasPermission(GuildPermission.MUTE_MEMBERS)) {
                sendErrorToPlayer(sender, "<c>Guild chat is currently muted!");
                return;
            }
        }

        if (guild.isSlowChat()) {
            Long lastChat = lastChatTimestamps.get(sender);
            if (lastChat != null && System.currentTimeMillis() - lastChat < 10000) {
                sendErrorToPlayer(sender, "<c>Slow mode is enabled. Please wait before sending another message.");
                return;
            }
            lastChatTimestamps.put(sender, System.currentTimeMillis());
        }

        GuildChatResponseEvent response = new GuildChatResponseEvent(guild, sender, message, officerChat);
        sendEvent(response);
    }

    public static void handleSettingRequest(GuildSettingRequestEvent event) {
        UUID changer = event.getChanger();
        String setting = event.getSetting();
        String value = event.getValue();

        GuildData guild = getGuildFromPlayer(changer);
        if (guild == null) {
            sendErrorToPlayer(changer, "<c>You are not in a guild!");
            return;
        }

        GuildRank rank = guild.getMemberRank(changer);
        if (rank == null) return;

        switch (setting.toLowerCase()) {
            case "tag" -> {
                if (!rank.hasPermission(GuildPermission.MODIFY_TAG)) {
                    sendErrorToPlayer(changer, "<c>You do not have permission to change the guild tag!");
                    return;
                }
                if (!guild.canSetTag()) {
                    sendErrorToPlayer(changer, "<c>Your guild must be Level 5 to set a tag!");
                    return;
                }
                if (value.length() > guild.getMaxTagLength()) {
                    sendErrorToPlayer(changer, "<c>Tag cannot be longer than {} characters!", guild.getMaxTagLength());
                    return;
                }
                guild.setTag(value);
            }
            case "tagcolor" -> {
                if (!rank.hasPermission(GuildPermission.MODIFY_TAG)) {
                    sendErrorToPlayer(changer, "<c>You do not have permission to change the tag color!");
                    return;
                }
                int requiredLevel = switch (value) {
                    case "§7" -> 5;
                    case "§3" -> 15;
                    case "§2" -> 25;
                    default -> Integer.MAX_VALUE;
                };
                if (guild.getLevel() < requiredLevel) {
                    sendErrorToPlayer(changer, "<c>That guild tag color has not been unlocked!");
                    return;
                }
                guild.setTagColor(value);
            }
            case "motd" -> {
                if (!rank.hasPermission(GuildPermission.MODIFY_MOTD)) {
                    sendErrorToPlayer(changer, "<c>You do not have permission to change the MOTD!");
                    return;
                }
                guild.setMotd(value);
            }
            case "description" -> {
                if (!rank.hasPermission(GuildPermission.MODIFY_DESCRIPTION)) {
                    sendErrorToPlayer(changer, "<c>You do not have permission to change the description!");
                    return;
                }
                guild.setDescription(value);
            }
            case "discord" -> {
                if (!rank.hasPermission(GuildPermission.MODIFY_DISCORD)) {
                    sendErrorToPlayer(changer, "<c>You do not have permission to change the discord link!");
                    return;
                }
                guild.setDiscordLink(value);
            }
            case "rename" -> {
                if (!guild.getMasterUuid().equals(changer)) {
                    sendErrorToPlayer(changer, "<c>Only the Guild Master can rename the guild!");
                    return;
                }
                if (value.length() < 3 || value.length() > 30) {
                    sendErrorToPlayer(changer, "<c>Guild name must be between 3 and 30 characters.");
                    return;
                }
                if (GuildDatabase.guildNameExists(value)) {
                    sendErrorToPlayer(changer, "<c>A guild with that name already exists!");
                    return;
                }
                guild.setName(value);
            }
            case "slow" -> {
                if (!rank.hasPermission(GuildPermission.MODIFY_SETTINGS)) {
                    sendErrorToPlayer(changer, "<c>You do not have permission to modify settings!");
                    return;
                }
                guild.setSlowChat(!guild.isSlowChat());
                value = guild.isSlowChat() ? "enabled" : "disabled";
            }
            case "finder" -> {
                if (!rank.hasPermission(GuildPermission.MODIFY_SETTINGS)) {
                    sendErrorToPlayer(changer, "<c>You do not have permission to modify settings!");
                    return;
                }
                guild.setListedInFinder(!guild.isListedInFinder());
                value = guild.isListedInFinder() ? "visible" : "hidden";
            }
            case "notifications" -> {
                GuildMember member = guild.getMember(changer);
                member.setNotificationsEnabled(!member.isNotificationsEnabled());
                value = member.isNotificationsEnabled() ? "enabled" : "disabled";
            }
            case "toggle" -> {
                GuildMember member = guild.getMember(changer);
                member.setGuildChatEnabled(!member.isGuildChatEnabled());
                value = member.isGuildChatEnabled() ? "enabled" : "disabled";
            }
            case "onlinemode" -> {
                guild.setOnlineMode(!guild.isOnlineMode());
                value = guild.isOnlineMode() ? "enabled" : "disabled";
            }
            default -> {
                sendErrorToPlayer(changer, "<c>Unknown setting: {}", setting);
                return;
            }
        }

        log(guild, "<a>{}<7> changed guild setting <e>{}", changer, setting);
        persistGuild(guild);

        GuildSettingChangedResponseEvent response = new GuildSettingChangedResponseEvent(guild, changer, setting, value);
        sendEvent(response);
    }

    public static void handleMuteRequest(GuildMuteRequestEvent event) {
        UUID muter = event.getMuter();
        String target = event.getTarget();
        long duration = event.getDuration();

        GuildData guild = getGuildFromPlayer(muter);
        if (guild == null) {
            sendErrorToPlayer(muter, "<c>You are not in a guild!");
            return;
        }

        GuildRank muterRank = guild.getMemberRank(muter);
        if (muterRank == null || !muterRank.hasPermission(GuildPermission.MUTE_MEMBERS)) {
            sendErrorToPlayer(muter, "<c>You do not have permission to mute guild members!");
            return;
        }

        if (target.equalsIgnoreCase("everyone")) {
            guild.setEveryoneMuted(true);
            guild.setEveryoneMutedExpiry(System.currentTimeMillis() + duration);
        } else {
            UUID targetUUID = UUID.fromString(target);
            GuildMember member = guild.getMember(targetUUID);
            if (member == null) {
                sendErrorToPlayer(muter, "<c>That player is not in your guild!");
                return;
            }
            member.setMutedUntil(System.currentTimeMillis() + duration);
        }

        log(guild, "<a>{}<7> muted <e>{} <7>for <e>{}ms", muter, target, duration);
        persistGuild(guild);

        GuildMuteChangedResponseEvent response = new GuildMuteChangedResponseEvent(guild, muter, target, duration, false);
        sendEvent(response);
    }

    public static void handleUnmuteRequest(GuildUnmuteRequestEvent event) {
        UUID actor = event.getActor();
        String target = event.getTarget();

        GuildData guild = getGuildFromPlayer(actor);
        if (guild == null) {
            sendErrorToPlayer(actor, "<c>You are not in a guild!");
            return;
        }

        GuildRank actorRank = guild.getMemberRank(actor);
        if (actorRank == null || !actorRank.hasPermission(GuildPermission.MUTE_MEMBERS)) {
            sendErrorToPlayer(actor, "<c>You do not have permission to unmute guild members!");
            return;
        }

        if (target.equalsIgnoreCase("everyone")) {
            guild.setEveryoneMuted(false);
            guild.setEveryoneMutedExpiry(0);
        } else {
            UUID targetUUID = UUID.fromString(target);
            GuildMember member = guild.getMember(targetUUID);
            if (member == null) {
                sendErrorToPlayer(actor, "<c>That player is not in your guild!");
                return;
            }
            member.setMutedUntil(0);
        }

        log(guild, "<a>{}<7> unmuted <e>{}", actor, target);
        persistGuild(guild);

        GuildMuteChangedResponseEvent response = new GuildMuteChangedResponseEvent(guild, actor, target, 0, true);
        sendEvent(response);
    }

    public static void handleSetRankRequest(GuildSetRankRequestEvent event) {
        UUID setter = event.getSetter();
        UUID target = event.getTarget();
        String rankName = event.getRankName();

        GuildData guild = getGuildFromPlayer(setter);
        if (guild == null) {
            sendErrorToPlayer(setter, "<c>You are not in a guild!");
            return;
        }

        if (!guild.getMasterUuid().equals(setter)) {
            sendErrorToPlayer(setter, "<c>Only the Guild Master can set ranks directly!");
            return;
        }

        GuildMember targetMember = guild.getMember(target);
        if (targetMember == null) {
            sendErrorToPlayer(setter, "<c>That player is not in your guild!");
            return;
        }

        GuildRank newRank = guild.getRank(rankName);
        if (newRank == null) {
            sendErrorToPlayer(setter, "<c>That rank does not exist!");
            return;
        }

        if (newRank.getName().equals("Guild Master")) {
            sendErrorToPlayer(setter, "<c>Use /guild transfer to transfer Guild Master!");
            return;
        }

        String oldRank = targetMember.getRankName();
        targetMember.setRankName(newRank.getName());
        log(guild, "<a>{}<7> set rank of <e>{} <7>to <e>{}", setter, target, newRank.getName());
        persistGuild(guild);

        GuildRankChangedResponseEvent response = new GuildRankChangedResponseEvent(guild, setter, target, oldRank, newRank.getName());
        sendEvent(response);
    }

    private static void disbandGuild(GuildData guild, UUID disbander) {
        GuildDisbandedResponseEvent response = new GuildDisbandedResponseEvent(guild, disbander);
        sendEvent(response);

        guildCache.invalidate(guild.getGuildId());
        GuildDatabase.deleteGuild(guild.getGuildId());
    }

    private static void sendEvent(final @NotNull GuildEvent event) {
        RedisClient.requestAllServersFromService(
            new GuildEventPushProtocol(),
            new GuildEventPushProtocol.Request(
                event.getClass().getSimpleName(),
                event.getSerializer().serialize(event),
                event.getParticipants()
            ),
            300
        );
    }

    private static void sendErrorToPlayer(UUID playerUUID, String markup, Object... arguments) {
        Text message = Text.of("<sep>\n").append(markup, arguments).append("\n<sep>");
        sendMessageToPlayer(playerUUID, message);
    }

    private static void sendMessageToPlayer(final @NotNull UUID playerUUID, final @NotNull Text message) {
        RedisClient.requestAllServersFromService(
            new SendMessagePushProtocol(),
            new SendMessagePushProtocol.Request(playerUUID, message.serialize()),
            300
        );
    }

    private static void scheduleInviteExpiration(UUID guildId, UUID inviter, UUID invitee, long delayMs) {
        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS).execute(() -> {
            PendingGuildInvite invite = pendingInvites.remove(invitee);
            if (invite != null) {
                GuildData guild = getGuild(guildId);
                if (guild != null) {
                    GuildInviteExpiredResponseEvent response = new GuildInviteExpiredResponseEvent(guild, inviter, invitee);
                    sendEvent(response);
                }
            }
        });
    }

    private static void persistGuild(final @NotNull GuildData guild) {
        guildCache.put(guild.getGuildId(), guild);
        GuildDatabase.saveGuild(guild);
    }

    private static void log(GuildData guild, String markup, Object... arguments) {
        log(guild, Text.of(markup, arguments));
    }

    private static void log(GuildData guild, Text message) {
        Text entry = Text.of("<7>{}: ", LOG_DATE.format(ZonedDateTime.now())).append(message);
        guild.addAuditLog(entry.serialize());
    }

    public record PendingGuildInvite(@NotNull UUID guildId, @NotNull UUID inviter, @NotNull UUID invitee) {
    }
}
