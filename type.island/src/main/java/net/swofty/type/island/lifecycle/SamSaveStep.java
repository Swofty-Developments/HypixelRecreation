package net.swofty.type.island.lifecycle;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleContext;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleStep;

public class SamSaveStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.SAVE;
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        Pos position = context.island().getSamPosition();
        if (position == null) {
            return;
        }

        context.island().getDatabase().insertOrUpdate("sam_position_x", position.x());
        context.island().getDatabase().insertOrUpdate("sam_position_y", position.y());
        context.island().getDatabase().insertOrUpdate("sam_position_z", position.z());
        context.island().getDatabase().insertOrUpdate("sam_position_yaw", (double) position.yaw());
        context.island().getDatabase().insertOrUpdate("sam_position_pitch", (double) position.pitch());
    }
}
