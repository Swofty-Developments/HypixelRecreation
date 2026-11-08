package net.swofty.type.skyblockgeneric.event.actions.entity;

import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class ActionAnimateEntityDamage implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityDamageEvent event) {
        event.setAnimation(true);
        event.setCancelled(false);

        event.getEntity().sendPacketToViewersAndSelf(new DamageEventPacket(event.getEntity().getEntityId(),
                        event.getDamage().getType(),
                        event.getDamage().getAttacker() == null ? null : event.getDamage().getAttacker().getEntityId(),
                        event.getDamage().getSource() == null ? null : event.getDamage().getSource().getEntityId(),
                        null));
    }
}
