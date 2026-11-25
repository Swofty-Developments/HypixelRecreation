package net.swofty.type.skyblockgeneric.event.actions.entity;

import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class ActionChunkUnload implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void run(InstanceChunkUnloadEvent event) {
        Instance instance = event.getInstance();
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();

        if (HypixelConst.isIslandServer() || HypixelConst.isGarden()) return;

        instance.loadChunk(chunkX, chunkZ).join();
    }
}
