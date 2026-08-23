package net.swofty.type.generic.redis;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.from.BroadcastStaffChatProtocol;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.command.commands.ChatCommand;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointRank;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.user.categories.Rank;

import java.util.UUID;
import net.swofty.commons.redis.RedisMessageContext;

public class RedisStaffChatBroadcast implements RedisMessageHandler<BroadcastStaffChatProtocol.Request, BroadcastStaffChatProtocol.Response> {
    @Override
    public RedisProtocol<BroadcastStaffChatProtocol.Request, BroadcastStaffChatProtocol.Response> protocol() {
        return new BroadcastStaffChatProtocol();
    }

    @Override
    public BroadcastStaffChatProtocol.Response handle(BroadcastStaffChatProtocol.Request message, RedisMessageContext context) {
        String type = message.type();

        switch (type) {
            case "message" -> {
                String formattedMessage = message.formattedMessage();
                sendToLocalStaff(Text.read(formattedMessage), null);
            }
            case "join" -> {
                UUID playerUuid = UUID.fromString(message.uuid());
                handleJoinLeave(playerUuid, true);
            }
            case "leave" -> {
                UUID playerUuid = UUID.fromString(message.uuid());
                handleJoinLeave(playerUuid, false);
            }
        }

        return new BroadcastStaffChatProtocol.Response();
    }

    private void handleJoinLeave(UUID playerUuid, boolean isJoin) {
        try {
            HypixelDataHandler handler = HypixelDataHandler.getOfOfflinePlayer(playerUuid);
            Rank rank = handler.get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue();

            if (!rank.isStaff()) {
                return;
            }

            String ign = handler.get(HypixelDataHandler.Data.IGN, DatapointString.class).getValue();
            Text displayName = Text.of("{}", rank.displayName(ign));
            String action = isJoin ? "joined" : "left";

            sendToLocalStaff(Text.of("<b>[STAFF] <7>{} <7>{}.", displayName, action), playerUuid);
        } catch (Exception e) {
        }
    }

    private void sendToLocalStaff(Text message, UUID senderUuid) {
        HypixelGenericLoader.getLoadedPlayers().stream()
                .filter(player -> player.getRank().isStaff())
                .filter(player -> ChatCommand.isStaffViewEnabled(player.getUuid()) ||
                        (player.getUuid().equals(senderUuid)))
                .forEach(player -> player.sendMessage(message));
    }
}
