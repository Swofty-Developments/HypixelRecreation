package net.swofty.type.generic.redis.service;

import net.swofty.commons.guild.GuildEvent;
import net.swofty.commons.guild.events.response.*;
import net.swofty.commons.protocol.objects.guild.GuildEventPushProtocol;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RedisPropagateGuildEvent implements RedisMessageHandler<GuildEventPushProtocol.Request, GuildEventPushProtocol.Response> {
    private static final GuildEventPushProtocol PROTOCOL = new GuildEventPushProtocol();

    @Override
    public RedisProtocol<GuildEventPushProtocol.Request, GuildEventPushProtocol.Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public GuildEventPushProtocol.Response handle(GuildEventPushProtocol.Request message, RedisMessageContext context) {
        try {
            GuildEvent event = parseEvent(message.eventType(), message.eventData());
            List<UUID> playersHandled = handleEventForPlayers(event, message.participants());
            return GuildEventPushProtocol.Response.success(playersHandled);
        } catch (Exception e) {
            Logger.error(e, "Failed to handle guild event");
            return GuildEventPushProtocol.Response.failure(e.getMessage());
        }
    }

    private GuildEvent parseEvent(String eventType, String eventData) {
        GuildEvent template = GuildEvent.findFromType(eventType);
        return (GuildEvent) template.getSerializer().deserialize(eventData);
    }

    private List<UUID> handleEventForPlayers(GuildEvent event, List<UUID> participants) {
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
                    Logger.error("Failed to handle guild event for player " + participantUUID + ": " + e.getMessage());
                }
            }
        }

        return playersHandled;
    }

    private void handleEventForPlayer(HypixelPlayer player, GuildEvent event) {
        switch (event) {
            case GuildCreatedResponseEvent e -> handleCreated(player, e);
            case GuildInviteSentResponseEvent e -> handleInviteSent(player, e);
            case GuildMemberJoinedResponseEvent e -> handleMemberJoined(player, e);
            case GuildMemberLeftResponseEvent e -> handleMemberLeft(player, e);
            case GuildMemberKickedResponseEvent e -> handleMemberKicked(player, e);
            case GuildDisbandedResponseEvent e -> handleDisbanded(player, e);
            case GuildRankChangedResponseEvent e -> handleRankChanged(player, e);
            case GuildTransferredResponseEvent e -> handleTransferred(player, e);
            case GuildChatResponseEvent e -> handleChat(player, e);
            case GuildSettingChangedResponseEvent e -> handleSettingChanged(player, e);
            case GuildMuteChangedResponseEvent e -> handleMuteChanged(player, e);
            case GuildInviteExpiredResponseEvent e -> handleInviteExpired(player, e);
            default -> Logger.warn("Unhandled guild event type: " + event.getClass().getSimpleName());
        }
    }

    private void handleCreated(HypixelPlayer player, GuildCreatedResponseEvent event) {
        sendMessage(player, "<a>You created the guild <6>{}<a>!", event.getGuild().getName());
    }

    private void handleInviteSent(HypixelPlayer player, GuildInviteSentResponseEvent event) {
        if (event.getInvitee().equals(player.getUuid())) {
            sendMessage(player, "{} <e>has invited you to join their guild <6>{}<e>!",
                displayName(event.getInviter()), event.getGuild().getName());
            player.sendMessage("""
                <hover:'<e>Click to accept!'><click:run:'/guild accept {}'>\
                <e>You have <c>60 <e>seconds to accept. <6>Click here to join!</click></hover>""",
                HypixelPlayer.getRawName(event.getInviter()));
        } else if (event.getInviter().equals(player.getUuid())) {
            sendMessage(player, "<e>You invited {} <e>to your guild. They have <c>60 <e>seconds to accept.",
                displayName(event.getInvitee()));
        }
    }

    private void handleMemberJoined(HypixelPlayer player, GuildMemberJoinedResponseEvent event) {
        if (event.getJoiner().equals(player.getUuid())) {
            sendMessage(player, "<a>You joined the guild <6>{}<a>!", event.getGuild().getName());
        } else {
            sendMessage(player, "{} <e>joined the guild!", displayName(event.getJoiner()));
        }
    }

    private void handleMemberLeft(HypixelPlayer player, GuildMemberLeftResponseEvent event) {
        if (event.getLeaver().equals(player.getUuid())) {
            sendMessage(player, "<e>You left the guild.");
        } else {
            sendMessage(player, "{} <e>left the guild.", displayName(event.getLeaver()));
        }
    }

    private void handleMemberKicked(HypixelPlayer player, GuildMemberKickedResponseEvent event) {
        if (event.getKicked().equals(player.getUuid())) {
            if (event.getReason() != null && !event.getReason().isEmpty()) {
                sendMessage(player, "<c>You have been kicked from the guild! <7>Reason: <f>{}", event.getReason());
            } else {
                sendMessage(player, "<c>You have been kicked from the guild!");
            }
        } else {
            sendMessage(player, "{} <e>kicked {} <e>from the guild!",
                displayName(event.getKicker()), displayName(event.getKicked()));
        }
    }

    private void handleDisbanded(HypixelPlayer player, GuildDisbandedResponseEvent event) {
        sendMessage(player, "{} <e>disbanded the guild!", displayName(event.getDisbander()));
    }

    private void handleRankChanged(HypixelPlayer player, GuildRankChangedResponseEvent event) {
        if (event.getTarget().equals(player.getUuid())) {
            sendMessage(player, isPrioritized(event.getFromRank(), event.getToRank())
                ? "<e>You were <a>promoted <e>to <6>{}<e>!"
                : "<e>You were <c>demoted <e>to <6>{}<e>!", event.getToRank());
        } else {
            sendMessage(player, "{} <e>changed {}'s <e>rank from <6>{} <e>to <6>{}<e>.",
                displayName(event.getChanger()), displayName(event.getTarget()),
                event.getFromRank(), event.getToRank());
        }
    }

    private void handleTransferred(HypixelPlayer player, GuildTransferredResponseEvent event) {
        if (event.getNewOwner().equals(player.getUuid())) {
            sendMessage(player, "<a>You are now the Guild Master!");
        } else {
            sendMessage(player, "{} <e>transferred guild ownership to {}<e>!",
                displayName(event.getOldOwner()), displayName(event.getNewOwner()));
        }
    }

    private void handleChat(HypixelPlayer player, GuildChatResponseEvent event) {
        player.sendMessage(event.isOfficerChat()
                ? "<3>Officer > {}<f>: {}"
                : "<2>Guild > {}<f>: {}",
            displayName(event.getSender()), event.getMessage());
    }

    private void handleSettingChanged(HypixelPlayer player, GuildSettingChangedResponseEvent event) {
        Text settingDisplay = switch (event.getSetting().toLowerCase()) {
            case "tag" -> Text.of("guild tag to <6>{}", event.getValue());
            case "tagcolor" -> Text.of("tag color to <6>{}", event.getValue());
            case "motd" -> Text.of("the MOTD");
            case "description" -> Text.of("the description");
            case "discord" -> Text.of("the Discord link");
            case "rename" -> Text.of("the guild name to <6>{}", event.getValue());
            case "slow" -> Text.of("slow chat to <6>{}", event.getValue());
            case "finder" -> Text.of("guild finder to <6>{}", event.getValue());
            default -> Text.of("{} to {}", event.getSetting(), event.getValue());
        };
        sendMessage(player, "{} <e>updated {}<e>.", displayName(event.getChanger()), settingDisplay);
    }

    private void handleMuteChanged(HypixelPlayer player, GuildMuteChangedResponseEvent event) {
        Text muterName = displayName(event.getMuter());
        boolean everyone = event.getTarget().equalsIgnoreCase("everyone");
        if (event.isUnmute()) {
            if (everyone) {
                sendMessage(player, "{} <e>unmuted the guild chat.", muterName);
            } else {
                sendMessage(player, "{} <e>unmuted {}<e>.",
                    muterName, displayName(UUID.fromString(event.getTarget())));
            }
        } else {
            long minutes = event.getDuration() / 60000;
            if (everyone) {
                sendMessage(player, "{} <e>muted the guild chat for <c>{} minutes<e>.", muterName, minutes);
            } else {
                sendMessage(player, "{} <e>muted {} <e>for <c>{} minutes<e>.",
                    muterName, displayName(UUID.fromString(event.getTarget())), minutes);
            }
        }
    }

    private void handleInviteExpired(HypixelPlayer player, GuildInviteExpiredResponseEvent event) {
        if (event.getInvitee().equals(player.getUuid())) {
            sendMessage(player, "<e>The guild invite from {} <e>has expired.", displayName(event.getInviter()));
        } else if (event.getInviter().equals(player.getUuid())) {
            sendMessage(player, "<e>The guild invite to {} <e>has expired.", displayName(event.getInvitee()));
        }
    }

    private boolean isPrioritized(String fromRank, String toRank) {
        return switch (toRank) {
            case "Guild Master" -> true;
            case "Officer" -> fromRank.equals("Member");
            default -> false;
        };
    }

    private Text displayName(UUID uuid) {
        return HypixelPlayer.getDisplayName(uuid);
    }

    private void sendMessage(HypixelPlayer player, String markup, Object... arguments) {
        player.sendMessage("<sep>");
        player.sendMessage(markup, arguments);
        player.sendMessage("<sep>");
    }

}
