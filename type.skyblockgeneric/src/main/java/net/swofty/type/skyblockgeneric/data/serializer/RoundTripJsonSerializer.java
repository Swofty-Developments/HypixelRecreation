package net.swofty.type.skyblockgeneric.data.serializer;

import lombok.SneakyThrows;
import net.swofty.commons.protocol.Serializer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class RoundTripJsonSerializer<T> implements Serializer<T> {
    private static final JsonMapper MAPPER = JsonMapper.builder()
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();

    private final Class<T> clazz;

    public RoundTripJsonSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    @SneakyThrows
    public String serialize(T value) {
        return MAPPER.writeValueAsString(value);
    }

    @Override
    @SneakyThrows
    public T deserialize(String json) {
        return MAPPER.readValue(json, clazz);
    }

    @Override
    public T clone(T value) {
        if (value == null) {
            return null;
        }
        return deserialize(serialize(value));
    }
}
