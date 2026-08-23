package net.swofty.type.bedwarslobby.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.gui.GUIGameQuests;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class QuestMasterNPC extends HypixelNPC {

	public QuestMasterNPC() {
		super(new VillagerConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"<b>Quest Master",
						"<e><l>RIGHT CLICK",
				};
			}


			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-23.5, 67, 6.15, 110, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return false;
			}

			@Override
			public VillagerProfession profession() {
				return VillagerProfession.LIBRARIAN;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		new GUIGameQuests(AchievementCategory.BEDWARS).open(event.player());
	}
}
