package net.swofty.type.ravengardgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.ravengardgeneric.profile.RavengardProfiles;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

@CommandParameters(
        labels = "crowns",
        description = "Sets your crown balance",
        usage = "/crowns <amount>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class CrownsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentNumber<Integer> amountArgument = ArgumentType.Integer("amount").min(0);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            RavengardPlayer player = (RavengardPlayer) sender;
            RavengardProfiles.setCrowns(player, context.get(amountArgument));

            player.sendMessage("<a>Successfully set crowns to <e>{}<a>.", context.getRaw(amountArgument));
        }, amountArgument);

        command.addSyntax((sender, context) -> {
            RavengardPlayer player = (RavengardPlayer) sender;
            player.sendMessage("<a>You have <e>👑{} Crowns<a>.", RavengardProfiles.getCrowns(player));
        });
    }
}
