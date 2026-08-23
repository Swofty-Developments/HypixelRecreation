package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.RegenerationValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.util.ArrayList;
import java.util.List;

public class LeafletSet extends SkyBlockValueEvent implements ArmorSet {
    @Override
    public String getName() {
        return "Energy of the Forest";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "While in a Forest zone you regain <a>5.0",
                "<c>♥ Health <7>every second."
        ));
    }

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return RegenerationValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        float value = (float) event.getValue();

        if (!isWearingSet(event.getPlayer())) return;

        SkyBlockRegion region = event.getPlayer().getRegion();

        if (region == null || region.getType() != RegionType.FOREST) {
            return;
        }

        event.setValue(value + 5);
    }
}
