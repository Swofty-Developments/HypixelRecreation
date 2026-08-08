package net.swofty.type.replayviewer.playback.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.swofty.type.game.replay.api.ReplayPlaybackContext;
import net.swofty.type.replayviewer.playback.ReplaySession;

import java.util.List;

public interface ReplayScoreboard extends net.swofty.type.game.replay.api.ReplayScoreboard {

    /**
     * Creates the scoreboard for a viewer.
     *
     * @param viewer the player viewing the replay
     */
    void create(Player viewer);

    /**
     * Updates the scoreboard with current replay state.
     *
     * @param session the replay session containing current state
     */
    void update(ReplaySession session);

    @Override
    default void update(ReplayPlaybackContext context) {
        update((ReplaySession) context);
    }

    /**
     * Removes the scoreboard from a viewer.
     *
     * @param viewer the player to remove the scoreboard from
     */
    void remove(Player viewer);

    /**
     * Gets the title for the scoreboard.
     *
     * @return the scoreboard title
     */
    Component getTitle();

    /**
     * Gets the current lines to display.
     *
     * @param session the replay session
     * @return list of lines for the scoreboard
     */
    List<Component> getLines(ReplaySession session);

}
