package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.data.serializer.RoundTripJsonSerializer;
import net.swofty.type.skyblockgeneric.garden.GardenData;

public class DatapointGardenPests extends SkyBlockDatapoint<GardenData.GardenPestsData> {
    public DatapointGardenPests(String key, GardenData.GardenPestsData value) {
        super(key, value, new RoundTripJsonSerializer<>(GardenData.GardenPestsData.class));
    }

    public DatapointGardenPests(String key) {
        this(key, new GardenData.GardenPestsData());
    }
}
