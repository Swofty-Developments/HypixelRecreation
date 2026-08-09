package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.data.serializer.RoundTripJsonSerializer;
import net.swofty.type.skyblockgeneric.garden.GardenData;

public class DatapointGardenVisitors extends SkyBlockDatapoint<GardenData.GardenVisitorsData> {
    public DatapointGardenVisitors(String key, GardenData.GardenVisitorsData value) {
        super(key, value, new RoundTripJsonSerializer<>(GardenData.GardenVisitorsData.class));
    }

    public DatapointGardenVisitors(String key) {
        this(key, new GardenData.GardenVisitorsData());
    }
}
