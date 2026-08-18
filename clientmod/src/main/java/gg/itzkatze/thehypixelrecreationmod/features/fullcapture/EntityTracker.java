package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.itzkatze.thehypixelrecreationmod.utils.StringUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class EntityTracker {
    private static final int TRACK_INTERVAL_TICKS = 10;
    private static final int FULL_TRACK_EVERY = 20;
    private static final int MAX_TRACKED = 400;
    private static final double TRACK_RADIUS = 96.0;
    private static final double MOVED_THRESHOLD = 0.02;

    private final EventSink sink;
    private final Map<Integer, double[]> lastPositions = new HashMap<>();

    private int trackCountdown;
    private int samples;
    private int spawnCount;

    EntityTracker(EventSink sink) {
        this.sink = sink;
    }

    int spawnCount() {
        return spawnCount;
    }

    void reset() {
        lastPositions.clear();
    }

    void onAdd(ClientboundAddEntityPacket packet) {
        spawnCount++;
        sink.emit("entities", "entity:add", CaptureJson.fields(packet));
    }

    void onRemove(ClientboundRemoveEntitiesPacket packet) {
        sink.emit("entities", "entity:remove", CaptureJson.fields(packet));
    }

    void onEquipment(ClientboundSetEquipmentPacket packet) {
        sink.emit("entities", "entity:equipment", CaptureJson.fields(packet));
    }

    void onData(ClientboundSetEntityDataPacket packet) {
        JsonObject payload = new JsonObject();
        payload.addProperty("id", packet.id());

        Entity entity = entity(packet.id());
        if (entity != null) {
            payload.addProperty("type", entity.getType().toShortString());
            payload.addProperty("uuid", entity.getUUID().toString());
        }

        JsonObject values = new JsonObject();
        for (SynchedEntityData.DataValue<?> value : packet.packedItems()) {
            values.add(String.valueOf(value.id()), CaptureJson.encode(value.value()));
        }
        payload.add("values", values);
        sink.emit("entities", "entity:data", payload);
    }

    void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }
        if (--trackCountdown > 0) {
            return;
        }
        trackCountdown = TRACK_INTERVAL_TICKS;

        boolean full = samples++ % FULL_TRACK_EVERY == 0;
        Set<Integer> seen = new HashSet<>();
        JsonArray entities = new JsonArray();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entities.size() >= MAX_TRACKED) {
                break;
            }
            if (entity.distanceToSqr(client.player) > TRACK_RADIUS * TRACK_RADIUS) {
                continue;
            }

            int id = entity.getId();
            seen.add(id);
            double[] position = {entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot()};
            double[] previous = lastPositions.put(id, position);
            if (!full && previous != null && !moved(previous, position)) {
                continue;
            }

            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            json.addProperty("type", entity.getType().toShortString());
            json.addProperty("x", CaptureJson.round(position[0]));
            json.addProperty("y", CaptureJson.round(position[1]));
            json.addProperty("z", CaptureJson.round(position[2]));
            json.addProperty("yaw", CaptureJson.round(position[3]));
            json.addProperty("pitch", CaptureJson.round(position[4]));
            if (entity.getCustomName() != null) {
                json.addProperty("name", StringUtility.toLegacyString(entity.getCustomName()));
            }
            entities.add(json);
        }

        lastPositions.keySet().retainAll(seen);
        if (entities.isEmpty() && !full) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("full", full);
        payload.addProperty("count", entities.size());
        payload.add("entities", entities);
        sink.emit("entities", "entity:track", payload);
    }

    private static boolean moved(double[] previous, double[] position) {
        for (int index = 0; index < previous.length; index++) {
            if (Math.abs(previous[index] - position[index]) > MOVED_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private static Entity entity(int id) {
        Minecraft client = Minecraft.getInstance();
        return client.level == null ? null : client.level.getEntity(id);
    }
}
