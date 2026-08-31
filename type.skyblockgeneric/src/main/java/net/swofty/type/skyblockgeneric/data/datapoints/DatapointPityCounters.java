package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class DatapointPityCounters extends SkyBlockDatapoint<Map<String, Long>> {
    private static final Serializer<Map<String, Long>> SERIALIZER = new Serializer<>() {
        @Override
        public String serialize(Map<String, Long> value) {
            return new JSONObject(value).toString();
        }

        @Override
        public Map<String, Long> deserialize(String json) {
            Map<String, Long> counters = new HashMap<>();
            if (json == null || json.isBlank()) return counters;
            JSONObject object = new JSONObject(json);
            for (String key : object.keySet()) counters.put(key, Math.max(0, object.optLong(key, 0)));
            return counters;
        }

        @Override
        public Map<String, Long> clone(Map<String, Long> value) {
            return new HashMap<>(value);
        }
    };

    public DatapointPityCounters(String key) {
        super(key, new HashMap<>(), SERIALIZER);
    }
}
