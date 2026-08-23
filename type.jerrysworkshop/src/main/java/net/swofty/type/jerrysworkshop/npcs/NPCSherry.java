package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.AnimalConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCSherry extends HypixelNPC {

	public NPCSherry() {
		super(new AnimalConfiguration() {
			@Override
			public EntityType entityType() {
				return EntityType.SNOW_GOLEM;
			}

			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"<c>Sherry",
						"<e><l>CLICK"
				};
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(6.500, 76.000, 95.500, 100, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {

	}
}
