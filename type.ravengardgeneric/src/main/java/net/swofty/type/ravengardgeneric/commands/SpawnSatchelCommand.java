package net.swofty.type.ravengardgeneric.commands;

import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.function.Consumer;

@CommandParameters(
        labels = "spawnsatchel",
        description = "Drops a loot satchel at your position",
        usage = "/spawnsatchel",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SpawnSatchelCommand extends HypixelCommand {
    public static volatile Consumer<Player> spawner;

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            Consumer<Player> hook = spawner;
            if (hook == null) {
                player.sendMessage("<c>Satchels only exist on dungeon servers.");
                return;
            }
            hook.accept(player);
            player.sendMessage("<a>Satchel dropped.");
        });
    }
}
