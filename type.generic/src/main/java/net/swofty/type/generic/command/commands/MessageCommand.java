package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@CommandParameters(labels = "msg message whipser",
        description = "Sends a message to another player",
        usage = "/msg <player> <message>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class MessageCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString playerArgument = ArgumentType.String("player");
        ArgumentStringArray messageArgument = new ArgumentStringArray("message");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String playerName = context.get(playerArgument);
            String[] message = context.get(messageArgument);
            HypixelPlayer player = (HypixelPlayer) sender;

            @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(playerName);
            if (targetUUID == null) {
                player.sendMessage(Text.key("commands.message.player_not_found", playerName));
                return;
            }

            ProxyPlayer target = new ProxyPlayer(targetUUID);
            if (!target.isOnline().join()) {
                player.sendMessage(Text.key("commands.message.player_not_online", playerName));
                return;
            }
            Text targetName = HypixelPlayer.getDisplayName(targetUUID);
            Text ourName = player.getFullDisplayName();

            String joinedMessage = String.join(" ", message);
            player.sendMessage(Text.key("commands.message.outgoing", targetName, joinedMessage));
            target.sendMessage(Text.key("commands.message.incoming", ourName, joinedMessage));
        }, playerArgument, messageArgument);
    }
}
