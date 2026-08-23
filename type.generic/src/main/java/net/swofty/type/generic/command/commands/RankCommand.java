package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointRank;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "setrank rank", description = "Rank command", usage = "/rank <player> <rank>", permission = Rank.STAFF, allowsConsole = true)
public class RankCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEntity entityArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
        ArgumentEnum<Rank> rankArgument = ArgumentType.Enum("rank", Rank.class);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final Player player = context.get(entityArgument).findFirstPlayer(sender);
            final Rank rank = context.get(rankArgument);

            if (player == null) {
                sender.sendMessage(Text.key("commands.rank.player_not_found", context.getRaw(entityArgument)));
                return;
            }

            final String playerName = player.getUsername();

            HypixelDataHandler.getUser(player.getUuid()).get(HypixelDataHandler.Data.RANK, DatapointRank.class).setValue(rank);

            sender.sendMessage(Text.key("commands.rank.success", playerName, rank.name()));
        }, entityArgument, rankArgument);
    }
}
