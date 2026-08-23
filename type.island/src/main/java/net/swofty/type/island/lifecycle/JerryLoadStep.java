package net.swofty.type.island.lifecycle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.generic.utility.MathUtility;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleContext;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleStep;
import net.swofty.type.skyblockgeneric.utility.JerryInformation;
import org.bson.Document;

public class JerryLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        Document document = context.island().getDatabase().getDocument();
        JerryInformation jerryInformation = context.island().getJerryInformation();

        if (jerryInformation == null) {
            jerryInformation = new JerryInformation(null, null, null);
        }

        if (document != null && document.getDouble("jerry_position_x") != null) {
            jerryInformation.setJerryPosition(
                    new Pos(
                            document.getDouble("jerry_position_x"),
                            document.getDouble("jerry_position_y"),
                            document.getDouble("jerry_position_z"),
                            MathUtility.fromDouble(document.getDouble("jerry_position_yaw")),
                            MathUtility.fromDouble(document.getDouble("jerry_position_pitch"))
                    )
            );
        } else {
            jerryInformation.setJerryPosition(new Pos(9.5, 100, 33.5, 145, 0));
        }

        Entity jerry = new Entity(EntityType.VILLAGER);
        jerry.setAutoViewable(true);
        jerry.setInstance(context.island().getIslandInstance(), jerryInformation.getJerryPosition());
        jerry.setNoGravity(true);

        jerryInformation.setJerry(jerry);

        ServerHolograms.ExternalHologram hologram = ServerHolograms.ExternalHologram.builder()
                .text(new String[]{"<6><l>NEW UPDATE", "Jerry", "<e><l>CLICK"})
                .instance(context.island().getIslandInstance())
                .pos(jerryInformation.getJerryPosition().add(0, 1.8, 0))
                .build();

        ServerHolograms.addExternalHologram(hologram);

        jerryInformation.setHologram(hologram);
        context.island().setJerryInformation(jerryInformation);
    }
}
