package net.swofty.type.island.lifecycle;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.swofty.type.skyblockgeneric.user.island.IslandLifecycle;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IslandLifecycleSteps {
    private static boolean registered = false;

    public static void register() {
        if (registered) return;

        IslandLifecycle.register(new JerryDefaultsStep());
        IslandLifecycle.register(new SamDefaultsStep());
        IslandLifecycle.register(new StarterChestStep());
        IslandLifecycle.register(new FirstMinionStep());
        IslandLifecycle.register(new JerryLoadStep());
        IslandLifecycle.register(new SamLoadStep());
        IslandLifecycle.register(new MinionLoadStep());
        IslandLifecycle.register(new PortalBuildStep());
        IslandLifecycle.register(new MinionSaveStep());
        IslandLifecycle.register(new JerrySaveStep());
        IslandLifecycle.register(new SamSaveStep());

        registered = true;
    }
}
