package net.swofty.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.commons.text.Text;
import net.swofty.velocity.SkyBlockVelocity;
import net.swofty.velocity.data.AuthenticationDatabase;

public class RegisterCommand implements SimpleCommand {

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

        AuthenticationDatabase.AuthenticationData data = new AuthenticationDatabase(player.getUniqueId()).getAuthenticationData();
        if (data != null) {
            player.sendMessage(Text.of("<c>You have already registered your account!"));
        }

        String[] args = invocation.arguments();
        if (args.length != 2) {
            player.sendMessage(Text.of("<c>You must first register to play this server!"));
            player.sendMessage(Text.of("<c>In the Minecraft chat, type <6>/register \\<password> \\<password><c>."));
            return;
        }

        if (!args[0].equals(args[1])) {
            player.sendMessage(Text.of("<c>Your passwords do not match."));
            return;
        }

        AuthenticationDatabase.AuthenticationData newData = AuthenticationDatabase.makeFromPassword(args[1]);
        new AuthenticationDatabase(player.getUniqueId()).setAuthenticationData(newData);

        player.sendMessage(Text.of("<a>You have successfully registered your account!"));
        player.sendMessage(Text.of("<a>Now, in the Minecraft chat, type <6>/login \\<password><a>."));
    }
}
