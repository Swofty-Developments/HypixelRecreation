package net.swofty.type.theend.events;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.custom.DragonHitEvent;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.theend.dragon.EndDragonManager;
import net.swofty.type.theend.dragon.EnderDragonEntity;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionEndDragonHit implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(DragonHitEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (!(event.getDragon() instanceof EnderDragonEntity dragon)) return;
        if (!EndDragonManager.isCurrent(dragon)) return;
        EndDragonManager.recordDamage(player, event.getDamage());
    }
}
