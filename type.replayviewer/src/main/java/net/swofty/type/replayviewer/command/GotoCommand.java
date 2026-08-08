package net.swofty.type.replayviewer.command;

import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;

@CommandParameters(
        description = "Seeks to a specific time in the replay",
        usage = "/goto <time|tick>",
        permission = Rank.DEFAULT,
        allowsConsole = false
)
public class GotoCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        var timeArg = ArgumentType.String("time");

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            String time = context.get(timeArg);

            TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
                    session -> {
                        int tick = parseTime(time);
                        if (tick >= 0) {
                            session.seekTo(tick);
                            player.sendMessage(I18n.t("replays.going_to_seconds",
                                    Argument.string("seconds", String.valueOf(tick / 20 * 50))));
                        } else {
                            player.sendMessage(I18n.t("replays.invalid_time_format"));
                        }
                    },
                    () -> player.sendMessage(I18n.t("replays.no_active_session"))
            );
        }, timeArg);

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
                    session -> session.seekTo(0),
                    () -> player.sendMessage(I18n.t("replays.no_active_session"))
            );
        }, ArgumentType.Literal("start"));

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
                    session -> session.seekTo(session.getTotalTicks() - 1),
                    () -> player.sendMessage(I18n.t("replays.no_active_session"))
            );
        }, ArgumentType.Literal("end"));
    }

    private int parseTime(String value) {
        // Try parsing as mm:ss or m:ss format
        if (value.contains(":")) {
            String[] parts = value.split(":");
            try {
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return (minutes * 60 + seconds) * 20;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        // Try parsing as tick number
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
