package net.swofty.type.thepark.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCMasterTacticianFunk extends HypixelNPC {

	public NPCMasterTacticianFunk() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Master Tactician Funk", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "Bq0N2vw7ls4BI3wpkk9G8aAqrML70WWBC9baoeOqwOnfEQw29jzuHG5+YnnPR+zN9YT4FslLL6FqbMp5WHzt3SOKpJ9wjKhOFy0rucgBQaYH5P5j6ffeglb8on3iqcP0JwQ28F0FH3JzowA0LR2rBL0I6hvdq6Bvar2H8r0zUCxss7WDe9iYV+TakzIqfPstNJrehWZSMDFHM8BCuRbP+cxMW8PoGOsMDvyvWYk9aIySGT+81cKLnkeoyP2YBVDygctHJMoiSnUTE/Ecd3XU5cxGFhgFqiTxycGTK6yull0k/JR83+yStlIxfWpKN1Po6m5d2TtE9fMV1Y5GEmXUm2IHWz2uscfTPbYEoZSE0aDDEMw00L73xIdpHRHP04Fwv/JUhEyYsbUp9GA5nb/OC/w5ePWLMGC5k3FkdLFzLpmG5I2PpQRXbeGFsG+FyQOxMN0B9PoJI7BLRqwBKeHf6FxAdspl+6hNdB1iICYsCo7xJkFLS0IVKWVRiHZ3pTYD+g7fSfwplJMgU19ZlENdicD/liih3zEBVspkINA6FHXQZNFMVTU+5hvd20f/GrSAxsT1ypB/i2noY9FYL2jOIdvU2WG6nSHtrVkGc+KKFybqvjd8rjGiWeHzgzaxn4Uy8rAF83XMufNaTYKfJmYmvKecAFcGfRm9ZdoQShXVsv0=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzI0MDk2NzY0OTIsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzkyYjdhY2JmYTkzMDhhMjJlNzUwNmEzNmJjY2FmZDAxNjBhZmEwZDUwMDgxOGVmZWU5ZmE5ODM1MjE0YzE0ZmEifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-452.5, 110, 29.5, -105, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		if (isInDialogue(player)) return;

		if(!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MASTER_TACTICIAN_FUNK)) {
			setDialogue(player, "intro").thenRun(() -> {
				player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MASTER_TACTICIAN_FUNK, true);
			});
			return;
		}

	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return List.of(
				DialogueSet.builder().key("intro").lines(
						"Welcome to the Woodlands!",
						"I use all sorts of wood types to create strong and useful items!",
						"Here. Check theme out!"
				).build()
		).toArray(DialogueSet[]::new);
	}
}
