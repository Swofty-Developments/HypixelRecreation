package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import org.tinylog.Logger;

@CommandParameters(labels = "signgraphicaluserinterface signgui",
        description = "Opens a graphical user interface",
        usage = "/signgui <text>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SignGUICommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString text = ArgumentType.String("text");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String signContent = context.get(text);
            HypixelPlayer player = (HypixelPlayer) sender;

            new HypixelSignGUI(player).open(new String[]{"Test1", "Test2"}).thenAccept(line -> {
                if (line == null) {
                    Logger.debug("{} left server while sign GUI was open", player.getDisplayName());
                    return;
                }

                player.sendMessage("<7>You wrote: <a>{}", line);
                Logger.debug("Sign GUI input: {}", line);
            });
        }, text);
    }
}
