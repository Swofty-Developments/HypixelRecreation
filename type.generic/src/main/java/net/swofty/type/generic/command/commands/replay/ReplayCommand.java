package net.swofty.type.generic.command.commands.replay;

import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.replay.ChooseReplayProtocolObject;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.utility.ScheduleUtility;

import java.util.UUID;

@CommandParameters(
        description = "Watch a specific replay by ID",
        usage = "/replay <uuid> [hex]",
        permission = Rank.DEFAULT,
        allowsConsole = false,
        labels = "replay"
)
public class ReplayCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        var replayIdArg = ArgumentType.String("replayId");
        var hexArg = ArgumentType.String("hex");

        command.setDefaultExecutor((sender, _) ->
                sender.sendMessage(I18n.t("replays.replay_usage")));

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            String replayIdStr = context.get(replayIdArg);

            UUID replayId = parseUuid(player, replayIdStr);
            if (replayId == null) return;

            sendToReplayViewer(player, replayId, null);
        }, replayIdArg);

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            String replayIdStr = context.get(replayIdArg);
            String hex = context.get(hexArg);

            UUID replayId = parseUuid(player, replayIdStr);
            if (replayId == null) return;

            String cleanHex = hex.startsWith("#") ? hex : "#" + hex;
            if (cleanHex.length() != 9) {
                player.sendMessage(I18n.t("replays.invalid_share_code_format"));
                return;
            }

            sendToReplayViewer(player, replayId, cleanHex);
        }, replayIdArg, hexArg);
    }

    private UUID parseUuid(HypixelPlayer player, String uuidStr) {
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(I18n.t("replays.invalid_replay_id"));
            return null;
        }
    }

    private void sendToReplayViewer(HypixelPlayer player, UUID replayId, String shareCode) {
        player.sendMessage(I18n.t("replays.loading_replay"));

        ProxyService replayService = new ProxyService(ServiceType.REPLAY);
        var request = new ChooseReplayProtocolObject.ChooseReplayMessage(player.getUuid(), replayId.toString(), shareCode);
        replayService.<ChooseReplayProtocolObject.ChooseReplayMessage, ChooseReplayProtocolObject.ChooseReplayResponse>handleRequest(request).thenAccept(response -> {
            ScheduleUtility.nextTick(() -> {
                if (!response.error()) {
                    player.sendMessage(I18n.t("replays.sending_to_viewer"));
                    player.sendTo(ServerType.REPLAY_VIEWER);
                } else {
                    player.sendMessage(I18n.t("replays.replay_not_found"));
                }
            });
        }).exceptionally(e -> {
            ScheduleUtility.nextTick(() -> player.sendMessage(I18n.t("replays.replay_load_failed_with_error",
                    Argument.string("error", String.valueOf(e.getMessage())))));
            return null;
        });
    }
}
