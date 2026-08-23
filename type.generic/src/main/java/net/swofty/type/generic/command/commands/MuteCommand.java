package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.protocol.objects.punishment.PunishPlayerServiceProtocol;
import net.swofty.commons.punishment.PunishmentReason;
import net.swofty.commons.punishment.PunishmentType;
import net.swofty.commons.punishment.template.MuteType;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@CommandParameters(
        labels = "mute tempmute",
        permission = Rank.STAFF,
        description = "Mute a player from the server.",
        usage = "/mute <player> [duration] <reason>",
        allowsConsole = false
)
public class MuteCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString playerArg = ArgumentType.String("player");
        ArgumentString durationArg = ArgumentType.String("duration");
        Argument<String> reasonArg = ArgumentType.String("reason").setSuggestionCallback((sender, context, suggestion) -> {
            for (MuteType type : MuteType.values()) {
                suggest(suggestion, type.name(), "<c>{}", type.getReason());
            }
        });

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            String playerName = context.get(playerArg);
            String duration = context.get(durationArg);

            MuteType type;
            try {
                type = MuteType.valueOf(context.get(reasonArg));
            } catch (IllegalArgumentException e) {
                player.sendMessage(Text.key("commands.mute.invalid_reason"));
                return;
            }

            CompletableFuture.runAsync(() -> {
                try {
                    UUID targetUuid = net.minestom.server.utils.mojang.MojangUtils.getUUID(playerName);
                    long actualTime = StringUtility.parseDuration(duration);
                    long expiryTime = System.currentTimeMillis() + actualTime;
                    mutePlayer(player, targetUuid, type, player.getUuid(), actualTime, expiryTime, playerName);
                } catch (IOException e) {
                    player.sendMessage(Text.key("commands.common.player_not_found_short", playerName));
                }
            });
        }, playerArg, durationArg, reasonArg);

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            String playerName = context.get(playerArg);

            MuteType reason;
            try {
                reason = MuteType.valueOf(context.get(reasonArg));
            } catch (IllegalArgumentException e) {
                player.sendMessage(Text.key("commands.mute.invalid_reason"));
                return;
            }

            CompletableFuture.runAsync(() -> {
                try {
                    mutePlayer(player, net.minestom.server.utils.mojang.MojangUtils.getUUID(playerName), reason,
                            player.getUuid(), 0, -1, playerName);
                } catch (IOException e) {
                    player.sendMessage(Text.key("commands.common.player_not_found_short", playerName));
                }
            });
        }, playerArg, reasonArg);
    }

    private void mutePlayer(HypixelPlayer sender, UUID targetUuid, MuteType type, UUID senderUuid,
                            long actualTime, long expiryTime, String playerName) {
        ProxyService punishmentService = new ProxyService(ServiceType.PUNISHMENT);
        PunishmentReason reason = new PunishmentReason(type);
        PunishPlayerServiceProtocol.PunishPlayerMessage message = new PunishPlayerServiceProtocol.PunishPlayerMessage(
                targetUuid,
                PunishmentType.MUTE.name(),
                reason,
                senderUuid,
                List.of(),
                actualTime > 0 ? expiryTime : -1
        );

        punishmentService.handleRequest(message).thenAccept(result -> {
            if (result instanceof PunishPlayerServiceProtocol.PunishPlayerResponse response) {
                if (response.success()) {
                    sender.sendMessage(Text.key("commands.mute.success", playerName, response.punishmentId()));
                } else if (response.errorCode() == PunishPlayerServiceProtocol.ErrorCode.ALREADY_PUNISHED) {
                    sender.sendMessage(Text.key("commands.mute.already_muted", response.errorMessage()));
                } else {
                    sender.sendMessage(Text.key("commands.mute.failed", response.errorMessage()));
                }
            }
        }).orTimeout(5, TimeUnit.SECONDS).exceptionally(_ -> {
            sender.sendMessage(Text.key("commands.mute.service_offline"));
            return null;
        });
    }
}
