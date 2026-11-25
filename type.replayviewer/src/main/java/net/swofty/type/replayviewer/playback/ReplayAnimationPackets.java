package net.swofty.type.replayviewer.playback;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.component.SwingAnimation;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.SwingAnimationPacket;
import net.swofty.type.game.replay.event.ReplayEntityAnimationEvent;

public final class ReplayAnimationPackets {
    private ReplayAnimationPackets() {
    }

    public static ServerPacket of(Entity entity, ReplayEntityAnimationEvent.Animation animation) {
        int id = entity.getEntityId();
        return switch (animation) {
            case SWING_MAIN_HAND -> new SwingAnimationPacket(
                    id, PlayerHand.MAIN, SwingAnimation.DEFAULT);
            case SWING_OFF_HAND -> new SwingAnimationPacket(
                    id, PlayerHand.OFF, SwingAnimation.DEFAULT);
            case TAKE_DAMAGE -> new DamageEventPacket(id, DamageType.GENERIC, null, null, entity.getPosition());
            case LEAVE_BED -> new EntityAnimationPacket(id, EntityAnimationPacket.Animation.LEAVE_BED);
            case CRITICAL_EFFECT -> new EntityAnimationPacket(id, EntityAnimationPacket.Animation.CRITICAL_EFFECT);
            case MAGIC_CRITICAL_EFFECT ->
                    new EntityAnimationPacket(id, EntityAnimationPacket.Animation.MAGICAL_CRITICAL_EFFECT);
        };
    }
}
