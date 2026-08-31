package net.swofty.type.replayviewer.command;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.playback.ReplaySession;

@CommandParameters(
        description = "Controls replay playback",
        usage = "/viewer <play|pause|speed|skip|goto|restart|leave>",
        permission = Rank.DEFAULT,
        allowsConsole = false,
        labels = "viewer"
)
public class ViewerCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        var valueArg = ArgumentType.String("value");

        // /viewer play
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
                    ReplaySession::play,
                    () -> player.sendMessage(Text.key("replays.no_active_session"))
            );
        }, ArgumentType.Literal("play"));

        // /viewer pause
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
                    ReplaySession::pause,
                    () -> player.sendMessage(Text.key("replays.no_active_session"))
            );
        }, ArgumentType.Literal("pause"));

        // /viewer speed <value>
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            String value = context.get(valueArg);
            try {
                float speed = Float.parseFloat(value);
                TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
                        session -> session.setPlaybackSpeed(speed),
                        () -> player.sendMessage(Text.key("replays.no_active_session"))
                );
            } catch (NumberFormatException e) {
                player.sendMessage(Text.key("replays.invalid_speed"));
            }
        }, ArgumentType.Literal("speed"), valueArg);

        // /viewer skip <seconds>
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            String value = context.get(valueArg);
            try {
                int seconds = Integer.parseInt(value);
                TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
                        session -> {
                            if (seconds > 0) {
                                session.skipForward(seconds);
                            } else {
                                session.skipBackward(-seconds);
                            }
                        },
                        () -> player.sendMessage(Text.key("replays.no_active_session"))
                );
            } catch (NumberFormatException e) {
                player.sendMessage(Text.key("replays.invalid_seconds"));
            }
        }, ArgumentType.Literal("skip"), valueArg);

        // /viewer goto <tick or time>
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            String value = context.get(valueArg);
            TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
                    session -> {
                        int tick = parseTimeOrTick(value);
                        if (tick >= 0) {
                            session.seekTo(tick);
                        } else {
                            player.sendMessage(Text.key("replays.invalid_time"));
                        }
                    },
                    () -> player.sendMessage(Text.key("replays.no_active_session"))
            );
        }, ArgumentType.Literal("goto"), valueArg);

        // /viewer restart
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
                    session -> session.seekTo(0),
                    () -> player.sendMessage(Text.key("replays.no_active_session"))
            );
        }, ArgumentType.Literal("restart"));

        // /viewer leave
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            TypeReplayViewerLoader.removeSession(player.getUuid());
            player.sendMessage(Text.key("replays.leaving_replay"));
            // Would send player back to lobby
        }, ArgumentType.Literal("leave"));
    }

    private int parseTimeOrTick(String value) {
        // Try parsing as mm:ss format
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
