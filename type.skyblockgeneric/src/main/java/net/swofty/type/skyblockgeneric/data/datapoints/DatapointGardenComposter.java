package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.data.serializer.RoundTripJsonSerializer;
import net.swofty.type.skyblockgeneric.garden.GardenData;

public class DatapointGardenComposter extends SkyBlockDatapoint<GardenData.GardenComposterData> {
    public DatapointGardenComposter(String key, GardenData.GardenComposterData value) {
        super(key, value, new RoundTripJsonSerializer<>(GardenData.GardenComposterData.class));
    }

    public DatapointGardenComposter(String key) {
        this(key, new GardenData.GardenComposterData());
    }
}
