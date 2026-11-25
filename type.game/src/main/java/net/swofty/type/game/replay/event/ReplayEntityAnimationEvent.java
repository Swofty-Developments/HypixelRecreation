package net.swofty.type.game.replay.event;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayEvent;

import java.io.IOException;

public record ReplayEntityAnimationEvent(int entityId, Animation animation) implements ReplayEvent {
    public static final int TYPE_ID = 1002;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeVarInt(entityId);
        writer.writeByte(animation.ordinal());
    }

    public static ReplayEntityAnimationEvent read(ReplayDataReader reader) throws IOException {
        int entityId = reader.readVarInt();
        int animationId = reader.readUnsignedByte();
        if (animationId >= Animation.values().length) throw new IOException("Unknown entity animation: " + animationId);
        return new ReplayEntityAnimationEvent(entityId, Animation.values()[animationId]);
    }

    public enum Animation {
        SWING_MAIN_HAND,
        SWING_OFF_HAND,
        TAKE_DAMAGE,
        LEAVE_BED,
        CRITICAL_EFFECT,
        MAGIC_CRITICAL_EFFECT
    }
}
