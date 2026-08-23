package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.swofty.type.generic.chat.StaffChat;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "sc staffchat",
        description = "Sends a message to staff chat",
        usage = "/sc <message>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class StaffChatCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentStringArray messageArgument = new ArgumentStringArray("message");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            HypixelPlayer player = (HypixelPlayer) sender;
            String[] messageArray = context.get(messageArgument);
            String message = String.join(" ", messageArray);

            if (message.isEmpty()) {
                player.sendMessage("<c>Usage: {}", "/sc <message>");
                return;
            }

            StaffChat.sendMessage(player, message);
        }, messageArgument);
    }
}
