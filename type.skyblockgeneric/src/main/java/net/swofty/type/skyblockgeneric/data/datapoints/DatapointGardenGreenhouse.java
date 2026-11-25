package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.data.serializer.RoundTripJsonSerializer;
import net.swofty.type.skyblockgeneric.garden.GardenData;

public class DatapointGardenGreenhouse extends SkyBlockDatapoint<GardenData.GardenGreenhouseData> {
    public DatapointGardenGreenhouse(String key, GardenData.GardenGreenhouseData value) {
        super(key, value, new RoundTripJsonSerializer<>(GardenData.GardenGreenhouseData.class));
    }

    public DatapointGardenGreenhouse(String key) {
        this(key, new GardenData.GardenGreenhouseData());
    }
}
