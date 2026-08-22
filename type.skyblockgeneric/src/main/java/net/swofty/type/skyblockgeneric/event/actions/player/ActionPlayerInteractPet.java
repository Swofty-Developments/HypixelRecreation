package net.swofty.type.skyblockgeneric.event.actions.player;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.PetEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetSkinComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionPlayerInteractPet implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        if (!(event.getTarget() instanceof PetEntityImpl target)) return;

        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockItem skinItem = new SkyBlockItem(player.getItemInMainHand());
        if (!skinItem.hasComponent(PetSkinComponent.class)) return;

        skinItem.getComponent(PetSkinComponent.class).apply(player, skinItem, target);
    }
}
