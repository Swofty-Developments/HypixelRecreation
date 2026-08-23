package net.swofty.type.skyblockgeneric.event.actions.npc;

import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AbiphoneComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

public class ActionPlayerInteractNPC implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
	public void run(NPCInteractEvent event) {
		final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		HypixelNPC npc = event.getNpc();
		SkyBlockItem item = new SkyBlockItem(player.getItemInMainHand());
		if (item.hasComponent(AbiphoneComponent.class)) {
			if (!(npc instanceof NPCAbiphoneTrait trait)) {
				player.sendMessage("<7>[<b>✆<7>] This NPC doesn't own an Abiphone...");
				event.setCancelled(true);
				return;
			}
			AbiphoneNPC abiphoneNPC = AbiphoneRegistry.getFromId(trait.getAbiphoneKey());
			if (abiphoneNPC != null) {
				if (item.getAttributeHandler().hasAbiphoneNPC(abiphoneNPC)) {
					player.sendMessage("<7>[<b>✆<7>] This NPC is already in your contacts list!");
					return;
				}
				abiphoneNPC.onAdd(player, player.getHeldSlot());
			} else {
				Logger.warn("NPC " + npc.getClass().getName() + " has an invalid Abiphone key: " + trait.getAbiphoneKey());
			}
			event.setCancelled(true);
		}
	}

}
