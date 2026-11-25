package net.swofty.type.replayviewer.event;

import net.minestom.server.event.player.PlayerInputEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;

public final class PlayerIndicateLeaveSpectate implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerInputEvent event) {
        if (!(event.getPlayer() instanceof HypixelPlayer player)) {
            return;
        }

        if (!shouldLeaveSpectate(player, event)) {
            return;
        }

        TypeReplayViewerLoader.getSession(player.getUuid()).ifPresent(session -> {
            Integer followedEntityId = session.getFollowedEntityId(player);
            if (followedEntityId == null) {
                return;
            }

            var followedEntity = session.getEntityManager().getEntity(followedEntityId);
            if (followedEntity == null) {
                return;
            }

            session.stopFollowing(player);
            player.teleport(followedEntity.getPosition());
        });
    }

    private static boolean shouldLeaveSpectate(HypixelPlayer player, PlayerInputEvent event) {
        return player.isSpectating() && (event.hasPressedShiftKey() || event.hasPressedJumpKey());
    }
}
