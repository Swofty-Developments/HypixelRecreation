package net.swofty.type.goldmine.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.goldmine.gui.GUIShopIronForger;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCIronForger extends HypixelNPC {

	public NPCIronForger() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Iron Forger", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "Cbf/zZ46MDpeuN1bUtSfJzrQvklkf22+0TjuSNzdVY9Kg1MatTxw1XfQTcad5r3TfPMmKwEh9x/OdnyiuFK7XuxmZaSnGg9QzT8oGtRThINfEbZQh7ezUsAK/f/J2b8soAA5NXmQmG1C3JG1urUk6uIB7WgIF7X536rlxhAMZ+TnXS0PKOMhoHZCxdgtsI8QBeGnT9szkBd+2Krg+Gcm5242ygKjfXcUKzPWhvv4HLv1FNY0hBYQAtTuWb1pT9E5WRBuKVI6qZbqNaiHleQpgjE2Qad2HsUb+IHryWIB6UvMgYgG8BIge0Fjf25hAzZ5oTXZPCPpqYnY2PrP0qU6xy4cPvZ0b4UqtkSyCGr7eldb2a8P96DClPuJEbmC+7ThnUwTGwIJhbNIXrU5xIbyr0/vJux843FbaRscNhmhmmbEuUo7BJlhQQDLZFqC+EeB8n/N47hqIWDBYZWEiTF19w7uO9+sdAAar02gru58hKpN2EALRbDvyjsWl+iEn3wgqlsPcNTbccQflvD+2enxLVAF4OmS/hOI/4nX4plu/CO107ICZxZ+WLJWHAhuNTFf3xuO71+4eZvKIl99PnqEJGWlerKHmJW9dEsplB7Rlzu7737+XrlTTmCx6s/v/3GHQBFMp1AZQ0UXBsWtcU/oidv2IcAMVuVVY9fbJ64l3T0=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NTk1OTU4NzUyNDgsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY2MWUxYTNhOTRiZjIyY2FmMjgyNWIyZjE2NzZjYWQzN2ExNjRlNDc3NGYyNzgzNzZmNzI5M2I0MzE0MzRmMTgifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-1.5, 75, -307.5, 0, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent e) {
		SkyBlockPlayer player = (SkyBlockPlayer) e.player();
		if (isInDialogue(player)) return;
		boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_IRON_FORGER);

		if (!hasSpokenBefore) {
			setDialogue(player, "hello").thenRun(() -> {
				player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_IRON_FORGER, true);
			});
			return;
		}

		player.openView(new GUIShopIronForger());
	}

	@Override
	public DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[] {
				DialogueSet.builder()
						.key("hello").lines(
						"For my wares, you'll have to pay the iron price!",
						"Seriously though, I accept Coins.",
						"Click me again to open the Iron Forger Shop!"
						).build(),
		};
	}
}