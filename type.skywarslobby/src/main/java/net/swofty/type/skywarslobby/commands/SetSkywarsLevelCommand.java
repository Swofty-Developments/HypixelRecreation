package net.swofty.type.skywarslobby.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skywarslobby.level.SkywarsLevelRegistry;

@CommandParameters(
        labels = "setswlevel",
        description = "Set your SkyWars level for testing",
        usage = "/setskywarlevel <level>",
        permission = Rank.STAFF,
        allowsConsole = false
)
public class SetSkywarsLevelCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentNumber<Integer> levelArgument = ArgumentType.Integer("level").between(1, SkywarsLevelRegistry.getMaxLevel());

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            HypixelPlayer player = (HypixelPlayer) sender;
            int targetLevel = context.get(levelArgument);

            SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
            if (handler == null) {
                player.sendMessage("<c>Could not find your SkyWars data. Please try again.");
                return;
            }

            // Calculate the cumulative XP required to reach this level
            long requiredXP = SkywarsLevelRegistry.getCumulativeXPForLevel(targetLevel);

            // Set the player's experience
            DatapointLong experienceDatapoint = handler.get(SkywarsDataHandler.Data.EXPERIENCE, DatapointLong.class);
            experienceDatapoint.setValue(requiredXP);

            player.sendMessage("<a>Successfully set your SkyWars level to <e>{}<a>!", targetLevel);
            player.sendMessage("<7>Total XP: <b>{:,}", requiredXP);
        }, levelArgument);
    }
}
