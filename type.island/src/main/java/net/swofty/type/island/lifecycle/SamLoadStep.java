package net.swofty.type.island.lifecycle;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleContext;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleStep;
import org.bson.Document;

public class SamLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        Document document = context.island().getDatabase().getDocument();
        if (document == null || !document.containsKey("sam_position_x")) {
            context.island().setSamPosition(SamDefaultsStep.DEFAULT_POSITION);
            return;
        }

        context.island().setSamPosition(new Pos(
            number(document, "sam_position_x", SamDefaultsStep.DEFAULT_POSITION.x()),
            number(document, "sam_position_y", SamDefaultsStep.DEFAULT_POSITION.y()),
            number(document, "sam_position_z", SamDefaultsStep.DEFAULT_POSITION.z()),
            (float) number(document, "sam_position_yaw", SamDefaultsStep.DEFAULT_POSITION.yaw()),
            (float) number(document, "sam_position_pitch", SamDefaultsStep.DEFAULT_POSITION.pitch())
        ));
    }

    private double number(Document document, String key, double fallback) {
        Object value = document.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }
}
