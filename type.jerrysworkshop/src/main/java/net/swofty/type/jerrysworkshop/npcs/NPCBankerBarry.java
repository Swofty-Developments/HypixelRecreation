package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.banker.GUIBanker;

public class NPCBankerBarry extends HypixelNPC {

	public NPCBankerBarry() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"Banker Barry",
						"<e><l>CLICK"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "tXAjzb81VpXCdZ6grdon5HWP0dIJ2iSvROtbjaDdQ0kBjkxbVdj+ja12HDYm2FD9iYAHO6UxdvUmLwHVP+RwbKOW+ZAYg8Vv/PUoO4qxZnEiBQ4Ojii0zjLsUZIzus3Zy1nV3BPmJSDEa9tAbHGFf83aSxp+O2kpUakLkoTMXdNO0P63Lm5DhYNjObhBPZrkBOger6k99fT9GLCjmUqpfaIgKEYt+Hi/UoKjBJ6p3Sg/gTBqmn82WLsRhDNvdiMwNezm0l1BWBOTtkgJtwyW/RcHnGBUgsqoeVni6RAMCc/R0qZmeEcY1Hs35O2HUh1y+mWDc2CRkh86MHf/69R/kWR9qKOdqrMNeEot3V1eT+LfHxzQWVY1IoSMiOjZaCOOYA9+YU7oVwsROrA5/5Tha45qCuwrqs+EWcm60z6zuy8uanFvmdPSF13lrNY0jZ7jyHSzEvakxnG/0vuAwEKg9/kRwEy1SuWqy5Yue9PEfxnguweNOBF8zhua0rYDeleYgOeg+mA9ZokE/5KmEdyNdIi2jI8wkptI0P0zynXr1IEAG9f1MmXEn/ict2Yi/ySa5Y0fC+ffQb6L5DFQpIHT+vEptG2xlcbEsvT/2um8mlrvsSOz+cGX0rNk6e3Bs+98+gG0Ue678EF+IYxqpoet8uLzlNa4fAtq2mL8+sldSLY=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzU1ODMyNjU0NTksInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU5MzY0NzgxMjFmYjc0OTRkNGQwZTRkZmZmMWI1NGZhODg4N2Q1MWJiODk3MjNhODNlYjA5MmQ0ODZmOWViNCJ9fX0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(20.500, 77.000, 44.500, 0, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		new GUIBanker().open(event.player());
	}
}
