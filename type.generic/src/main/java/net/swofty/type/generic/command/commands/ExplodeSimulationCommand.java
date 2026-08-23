package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.utility.AnimatedExplosion;

@CommandParameters(labels = "explodesimulation", description = "Simulates an explosion with falling blocks",
        usage = "/explodesimulation <radius> [knockbackStrength]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ExplodeSimulationCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentNumber<Integer> radiusArg = ArgumentType.Integer("radius").min(1).max(20);
        ArgumentNumber<Double> knockbackArg = ArgumentType.Double("knockbackStrength");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            int radius = context.get(radiusArg);
            executeExplosion(player, radius, 0);
        }, radiusArg);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            int radius = context.get(radiusArg);
            double knockback = context.get(knockbackArg);
            executeExplosion(player, radius, knockback);
        }, radiusArg, knockbackArg);
    }

    private void executeExplosion(HypixelPlayer player, int radius, double knockbackStrength) {
        Instance instance = player.getInstance();
        if (instance == null) {
            player.sendMessage("<c>No instance found!");
            return;
        }

        int blockCount = AnimatedExplosion.create(instance, player.getPosition(), radius, knockbackStrength, player);
        player.sendMessage("<a>Explosion simulated! <7>({} blocks affected)", blockCount);
    }
}
