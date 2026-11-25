package net.swofty.type.game.replay.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayEvent;

import java.io.IOException;

public record ReplayComponentEvent(Kind kind, Component component) implements ReplayEvent {
    public static final int TYPE_ID = 1000;
    private static final GsonComponentSerializer SERIALIZER = GsonComponentSerializer.gson();

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeByte(kind.ordinal());
        writer.writeString(SERIALIZER.serialize(component));
    }

    public static ReplayComponentEvent read(ReplayDataReader reader) throws IOException {
        int kindId = reader.readUnsignedByte();
        if (kindId >= Kind.values().length) throw new IOException("Unknown component event kind: " + kindId);
        return new ReplayComponentEvent(Kind.values()[kindId], SERIALIZER.deserialize(reader.readString()));
    }

    public enum Kind {
        CHAT,
        DEATH_MESSAGE,
        ANNOUNCEMENT,
        TITLE,
        ACTION_BAR
    }
}
