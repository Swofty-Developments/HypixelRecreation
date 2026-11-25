package net.swofty.type.island.lifecycle;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleContext;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleStep;

public class SamDefaultsStep implements IslandLifecycleStep {
    public static final Pos DEFAULT_POSITION = new Pos(8.5, 100, 41.5, 180, 0);

    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.CREATE;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        context.island().setSamPosition(DEFAULT_POSITION);
    }
}
