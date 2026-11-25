package net.swofty.type.game.replay.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayEvent;

import java.io.IOException;
import java.util.UUID;

public record ReplayBookmarkEvent(Component title, UUID participantUuid) implements ReplayEvent {
    public static final int TYPE_ID = 1001;
    private static final GsonComponentSerializer COMPONENTS = GsonComponentSerializer.gson();

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeString(COMPONENTS.serialize(title));
        writer.writeBoolean(participantUuid != null);
        if (participantUuid != null) writer.writeUUID(participantUuid);
    }

    public static ReplayBookmarkEvent read(ReplayDataReader reader) throws IOException {
        Component title = COMPONENTS.deserialize(reader.readString());
        return new ReplayBookmarkEvent(title, reader.readBoolean() ? reader.readUUID() : null);
    }
}
