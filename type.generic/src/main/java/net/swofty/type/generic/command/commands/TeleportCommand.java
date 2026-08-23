package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "tp teleport",
        description = "teleports to a player",
        usage = "/teleport",
        permission = Rank.STAFF,
        allowsConsole = false)
public class TeleportCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEntity entityArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            final Player target = context.get(entityArgument).findFirstPlayer(sender);

            if (target == null) {
                player.sendMessage("<c>Couldn't find a player by the name of <e>{}<c>.", context.getRaw(entityArgument));
                return;
            }
            player.teleport(target.getPosition());
        });

        ArgumentInteger xArgument = ArgumentType.Integer("x");
        ArgumentInteger yArgument = ArgumentType.Integer("y");
        ArgumentInteger zArgument = ArgumentType.Integer("z");
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            int x = context.get(xArgument);
            int y = context.get(yArgument);
            int z = context.get(zArgument);
            player.teleport(player.getPosition().withX(x).withY(y).withZ(z));
        }, xArgument, yArgument, zArgument);
    }
}