package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(
    allowsConsole = false,
    description = "Skips the time in the current instance",
    permission = Rank.STAFF,
    labels = "worldtime skipworldtime",
    usage = "/worldtime (tick)"
)
public class SkipWorldTimeCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentLong timeArg = ArgumentType.Long("tick");
        command.addSyntax((sender, context) -> {
            long time = context.get(timeArg);
            if (time < 0) {
                sender.sendMessage("<c>Tick must be not negative.");
                return;
            }
            if (time > 24000) {
                sender.sendMessage("<c>Tick must be less than or equal to 24000.");
                return;
            }
            if (sender instanceof Player player) {
                player.getInstance().setTime(time);
                ((HypixelPlayer) player).sendMessage("<a>Set the world time to <e>{}<a> ticks.", time);
            }
        }, timeArg);
    }
}
