package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.datapoints.DatapointChatType;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(description = "Changes chat mode",
        usage = "/chat <type>",
        permission = Rank.DEFAULT,
    labels = "chatmode chat",
        allowsConsole = false)
public class ChatCommand extends HypixelCommand {
    private static final java.util.concurrent.ConcurrentHashMap<java.util.UUID, Boolean> staffView = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord chatType = ArgumentType.Word("type");
        chatType.setSuggestionCallback((sender, context, suggestion) -> {
            boolean isStaff = sender instanceof HypixelPlayer hp && hp.getRank().isStaff();
            suggestion.addEntry(new SuggestionEntry("p"));
            suggestion.addEntry(new SuggestionEntry("party"));
            suggestion.addEntry(new SuggestionEntry("a"));
            suggestion.addEntry(new SuggestionEntry("all"));
            if (isStaff) {
                suggestion.addEntry(new SuggestionEntry("s"));
                suggestion.addEntry(new SuggestionEntry("staff"));
                suggestion.addEntry(new SuggestionEntry("staffview"));
                suggestion.addEntry(new SuggestionEntry("sv"));
            }
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            HypixelPlayer player = (HypixelPlayer) sender;
            String raw = context.get(chatType).toLowerCase();

            if ((raw.equals("staffview") || raw.equals("sv"))) {
                if (!player.getRank().isStaff()) {
                    player.sendMessage("<c>Unknown chat type.");
                    return;
                }
                boolean enabled = staffView.getOrDefault(player.getUuid(), true);
                staffView.put(player.getUuid(), !enabled);
                player.sendMessage("<a>Staff chat viewing is now {}", !enabled ? Text.of("<a>enabled") : Text.of("<c>disabled"));
                return;
            }

            PickerChatType type = PickerChatType.fromString(raw);
            if (type == null) {
                player.sendMessage("<c>Unknown chat type.");
                return;
            }

            if (type.getChatType() == DatapointChatType.Chats.STAFF && !player.getRank().isStaff()) {
                player.sendMessage("<c>Unknown chat type.");
                return;
            }

            player.getChatType().switchTo(type.getChatType());
            player.sendMessage("<a>You are now in the <6>{} <a>channel", type.chatType.name());
        }, chatType);
    }

    enum PickerChatType {
        p(DatapointChatType.Chats.PARTY),
        party(DatapointChatType.Chats.PARTY),
        a(DatapointChatType.Chats.ALL),
        all(DatapointChatType.Chats.ALL),
        s(DatapointChatType.Chats.STAFF),
        staff(DatapointChatType.Chats.STAFF);

        private final DatapointChatType.Chats chatType;

        PickerChatType(DatapointChatType.Chats chatType) {
            this.chatType = chatType;
        }

        public DatapointChatType.Chats getChatType() {
            return chatType;
        }

        public static PickerChatType fromString(String input) {
            for (PickerChatType val : values()) {
                if (val.name().equalsIgnoreCase(input)) return val;
            }
            return null;
        }
    }

    public static boolean isStaffViewEnabled(java.util.UUID uuid) {
        return staffView.getOrDefault(uuid, true);
    }
}
