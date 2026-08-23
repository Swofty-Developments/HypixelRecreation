package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "setvelocity",
		description = "Sets the velocity of a player",
		usage = "/velocity <player> <x> <y> <z>",
		permission = Rank.STAFF,
		allowsConsole = false)
public class SetVelocityCommand extends HypixelCommand {

	@Override
	public void registerUsage(MinestomCommand command) {
		ArgumentEntity entityArg = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
		ArgumentFloat xArg = ArgumentType.Float("x");
		ArgumentFloat yArg = ArgumentType.Float("y");
		ArgumentFloat zArg = ArgumentType.Float("z");
		command.addSyntax((sender, context) -> {
			if (!permissionCheck(sender)) return;

			var target = context.get(entityArg).findFirstEntity(sender);
			float x = context.get(xArg);
			float y = context.get(yArg);
			float z = context.get(zArg);

			if (target == null) {
				((HypixelPlayer) sender).sendMessage("<c>Player not found.");
				return;
			}

			target.setVelocity(target.getVelocity().withX(x).withY(y).withZ(z));
		}, entityArg, xArg, yArg, zArg);
	}
}
