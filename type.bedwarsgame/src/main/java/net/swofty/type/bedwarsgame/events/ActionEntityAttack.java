package net.swofty.type.bedwarsgame.events;

import io.github.term4.polyp.api.event.attack.PreAttackEvent;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.item.impl.LuckyCombatEffects;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class ActionEntityAttack implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false)
	public void run(PreAttackEvent event) {
		if (event.attacker() instanceof BedWarsPlayer player) {
			BedWarsGame game = player.getGame();
			if (game == null) {
				event.setCancelled(true);
				return;
			}

			if (game.getState() != GameState.IN_PROGRESS) {
				event.setCancelled(true);
				return;
			}

			for (Entity entity : HypixelNPC.getPerPlayerNPCs().get(player.getUuid()).getEntityImpls().values()) {
				if (event.target() == entity) {
					event.setCancelled(true);
					return;
				}
			}

			if (event.target() != null && game.isSameTeam(player.getUuid(), event.target().getUuid())) {
				player.sendMessage("<c>You cannot attack your teammate!");
				event.setCancelled(true);
				return;
			}

			if (event.target() != null) LuckyCombatEffects.handle(player, event.target());
		}
	}

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
	public void run(EntityDamageEvent event) {
		if (event.getEntity() instanceof BedWarsPlayer player) {
			BedWarsGame game = player.getGame();
			if (game == null) {
				return;
			}

			if (game.getState() == GameState.IN_PROGRESS) {
				player.updateBelowTag();
			}
		}
	}

}
