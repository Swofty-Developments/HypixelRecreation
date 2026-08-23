package net.swofty.type.replayviewer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.playback.ReplaySession;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplayShareUtil {

    public static String buildShareCommand(ReplaySession session, Pos position, int tick) {
        int maxTick = Math.max(0, session.getTotalTicks() - 1);
        int clampedTick = Math.max(0, Math.min(tick, maxTick));

        String shareCode = ReplayShareCodec.encode(
            position,
            clampedTick,
                session.getMetadata().descriptor().mapCenterX(),
                session.getMetadata().descriptor().mapCenterZ()
        );

        return "/replay " + session.getReplayId() + " " + shareCode;
    }

    public static void sendShareCommandMessage(HypixelPlayer player, ReplaySession session) {
        sendShareCommandMessage(player, session, session.getCurrentTick(), player.getPosition());
    }

    public static void sendShareCommandMessage(HypixelPlayer player, ReplaySession session, int tick) {
        sendShareCommandMessage(player, session, tick, player.getPosition());
    }

    public static void sendShareCommandMessage(HypixelPlayer player, ReplaySession session, int tick, Pos position) {
        String fullCommand = buildShareCommand(session, position, tick);

        player.sendMessage(Text.of("<hover:'{}'><click:suggest:'{}'>{}",
                Text.key("replays.share_command_hover"), fullCommand,
                Text.key("replays.share_command_message")));
    }
}
