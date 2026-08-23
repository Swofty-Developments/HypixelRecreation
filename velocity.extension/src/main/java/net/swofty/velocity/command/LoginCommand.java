package net.swofty.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.swofty.commons.ServerType;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.commons.text.Text;
import net.swofty.velocity.SkyBlockVelocity;
import net.swofty.velocity.data.AuthenticationDatabase;
import net.swofty.velocity.gamemanager.TransferHandler;

public class LoginCommand implements SimpleCommand {

    @Override
    public boolean hasPermission(Invocation invocation) {
        return ConfigProvider.settings().isRequireAuth();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            return;
        }

        if (!ConfigProvider.settings().isRequireAuth()) return;

        if (!SkyBlockVelocity.getUnauthenticated().contains(player.getUniqueId())) return;

        String[] args = invocation.arguments();
        if (args.length != 1) {
            player.sendMessage(Text.of("<c>In the Minecraft chat, type <6>/login \\<password><c>."));
            player.sendMessage(Text.of("<c>It is not a command, it's just a message. Nobody else can see it."));
            return;
        }

        AuthenticationDatabase.AuthenticationData data = new AuthenticationDatabase(player.getUniqueId()).getAuthenticationData();
        if (data == null) {
            player.sendMessage(Text.of("<c>You must do <6>/register \\<password> \\<password> <c>first!"));
            return;
        }

        if (data.matches(args[0])) {
            SkyBlockVelocity.getUnauthenticated().remove(player.getUniqueId());
            player.sendMessage(Text.of("<a>You have successfully logged in!"));
            new TransferHandler(player).transferTo(ServerType.PROTOTYPE_LOBBY);
        } else {
            player.sendMessage(Text.of("<c>Your password is incorrect."));
        }
    }
}
