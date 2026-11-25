package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.data.serializer.RoundTripJsonSerializer;
import net.swofty.type.skyblockgeneric.garden.GardenData;

public class DatapointGardenPersonal extends SkyBlockDatapoint<GardenData.GardenPersonalData> {
    public DatapointGardenPersonal(String key, GardenData.GardenPersonalData value) {
        super(key, value, new RoundTripJsonSerializer<>(GardenData.GardenPersonalData.class));
    }

    public DatapointGardenPersonal(String key) {
        this(key, new GardenData.GardenPersonalData());
    }
}
