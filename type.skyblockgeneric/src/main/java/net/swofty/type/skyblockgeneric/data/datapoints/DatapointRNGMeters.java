package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterState;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DatapointRNGMeters extends SkyBlockDatapoint<Map<String, RNGMeterState>> {
    private static final Serializer<Map<String, RNGMeterState>> SERIALIZER = new Serializer<>() {
        @Override
        public String serialize(Map<String, RNGMeterState> value) {
            JSONObject meters = new JSONObject();
            value.forEach((type, state) -> meters.put(type, new JSONObject()
                    .put("selected_reward", state.selectedReward())
                    .put("stored_xp", state.storedXp())));
            return meters.toString();
        }

        @Override
        public Map<String, RNGMeterState> deserialize(String json) {
            Map<String, RNGMeterState> meters = new LinkedHashMap<>();
            if (json == null || json.isBlank()) return meters;

            JSONObject value = new JSONObject(json);
            for (String key : value.keySet()) {
                try {
                    JSONObject meter = value.getJSONObject(key);
                    meters.put(key.toUpperCase(), new RNGMeterState(
                            meter.getString("selected_reward"),
                            meter.optDouble("stored_xp", 0)
                    ));
                } catch (RuntimeException ignored) {
                }
            }
            return meters;
        }

        @Override
        public Map<String, RNGMeterState> clone(Map<String, RNGMeterState> value) {
            return new LinkedHashMap<>(value);
        }
    };

    public DatapointRNGMeters(String key) {
        super(key, new LinkedHashMap<>(), SERIALIZER);
    }
}
