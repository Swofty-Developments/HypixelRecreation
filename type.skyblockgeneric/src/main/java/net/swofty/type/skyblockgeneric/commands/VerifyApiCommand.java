package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.api.APIAuthenticateCodeProtocol;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "allowaccessforapiauthentication verifyapi",
        description = "Allows access for API authentication for the given code",
        usage = "/allowaccessforapiauthentication <code>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class VerifyApiCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString code = new ArgumentString("code");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            sender.sendMessage("<7>Checking if API is online...");

            Thread.startVirtualThread(() -> {
                if (!(new ProxyService(ServiceType.API).isOnline().join())) {
                    sender.sendMessage("<c>API is offline.");
                    return;
                }

                sender.sendMessage("<7>API is online, checking if code is valid...");
                String codeString = context.get(code);

                SkyBlockPlayer player = (SkyBlockPlayer) sender;
                ProxyService apiService = new ProxyService(ServiceType.API);
                apiService.handleRequest(new APIAuthenticateCodeProtocol.AuthenticateCodeMessage(
                        codeString,
                        player.getUsername(),
                        player.getUuid()
                )).thenAccept(nonCastedResponse -> {
                    APIAuthenticateCodeProtocol.AuthenticateCodeResponse response = (APIAuthenticateCodeProtocol.AuthenticateCodeResponse) nonCastedResponse;
                    if (response.success()) {
                        sender.sendMessage(Text.of("<a>Code '{}' has successfully been verified! Check your web browser.", codeString));
                    } else {
                        sender.sendMessage("<c>Invalid code!");
                    }
                });
            });
        }, code);
    }
}
