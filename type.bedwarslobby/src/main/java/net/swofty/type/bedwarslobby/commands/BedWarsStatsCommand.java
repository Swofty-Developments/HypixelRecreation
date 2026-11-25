package net.swofty.type.bedwarslobby.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(
        description = "Modify your bw stats",
        usage = "/bwstats",
        permission = Rank.STAFF,
        allowsConsole = false,
        labels = "bwstats"
)
public class BedWarsStatsCommand extends HypixelCommand {


    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentLong experience = ArgumentType.Long("exp");
        ArgumentLong tokens = ArgumentType.Long("tks");

        command.addSyntax((sender, context) -> {
            long l = context.get(experience);
            if (sender instanceof HypixelPlayer player) {
                BedWarsDataHandler handler = BedWarsDataHandler.getUser(player);
                handler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class).setValue(l);
                player.sendMessage(Component.text("Set experience to " + l, NamedTextColor.GREEN));
            }
        }, ArgumentType.Literal("experience"), experience);

        command.addSyntax((sender, context) -> {
            long tks = context.get(tokens);
            if (sender instanceof HypixelPlayer player) {
                BedWarsDataHandler handler = BedWarsDataHandler.getUser(player);
                handler.get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class).setValue(tks);
                player.sendMessage(Component.text("Set tokens to " + tks, NamedTextColor.GREEN));
            }
        }, ArgumentType.Literal("tokens"), tokens);
    }
}
