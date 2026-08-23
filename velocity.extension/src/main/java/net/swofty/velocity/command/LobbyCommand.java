package net.swofty.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.swofty.commons.text.Text;
import net.swofty.commons.LobbyDestination;
import net.swofty.commons.ServerType;
import net.swofty.velocity.gamemanager.GameManager;
import net.swofty.velocity.gamemanager.TransferHandler;

public class LobbyCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(Text.of("<c>This command can only be used by players."));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length > 1) {
            player.sendMessage(Text.of("<c>Usage: /lobby [game]"));
            return;
        }

        ServerType currentType = player.getCurrentServer()
            .map(connection -> GameManager.getTypeFromRegisteredServer(connection.getServer()))
            .orElse(null);

        ServerType destination;
        if (args.length == 0) {
            destination = LobbyDestination.resolveDefaultDestination(currentType);
        } else {
            destination = LobbyDestination.resolveFromAlias(args[0]);
            if (destination == null) {
                player.sendMessage(Text.of("<c>Unknown lobby destination. Try /lobby bw, /lobby sw, /lobby bedwars, /lobby skywars."));
                return;
            }
        }

        new TransferHandler(player).transferTo(destination);
    }
}
