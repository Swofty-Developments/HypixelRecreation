package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.data.serializer.RoundTripJsonSerializer;
import net.swofty.type.skyblockgeneric.garden.GardenData;

public class DatapointGardenCore extends SkyBlockDatapoint<GardenData.GardenCoreData> {
    public DatapointGardenCore(String key, GardenData.GardenCoreData value) {
        super(key, value, new RoundTripJsonSerializer<>(GardenData.GardenCoreData.class));
    }

    public DatapointGardenCore(String key) {
        this(key, new GardenData.GardenCoreData());
    }
}
