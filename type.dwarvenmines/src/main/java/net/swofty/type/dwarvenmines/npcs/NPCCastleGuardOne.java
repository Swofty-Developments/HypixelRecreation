package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCCastleGuardOne extends HypixelNPC {

	public NPCCastleGuardOne() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Castle Guard", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "jADrA04LTpE3LchH4opgU2WLmf3Tu33mXsrlUEJCei0xSRZY7/yb+yW/IQ5RlweTPGpluNDlS/tW+pgmBN7VBXzWcV61cHMT9ALquS8/LkRivSaSgsEVupp16ot9kne9Ecv2up7UcAJLn7Q7jnMWtF73YqFruTjpsO25xvIL7IBt1H8QxGdaUYiiws3kGuhubZqbCSGj2w4dTdWxJByNMXULoBt0U2tqjDd2rW3vHFLT3dKP+JL3czXHKCMxgvjhZ+GX7DnuiTlUoqO8A/0stbXy4d0IvQsc38PqMN5w103ixsjvAQ8wIvCAqQYX3gfUSPe+xh/JV2gtFZRa6n34VsmkRu8dj36aGjQQlEE7ronYcDFZoWi9lGXn4P5ZeUoX2h2FmGGJcSnTARlp114Ay5buB9hVOqrYCTzc6mbm1ytjIBUjJ2CN7vLgAJfXhpiGQsaP5UMoadmheVgRwY39QAg1LMRONmdP1mCfetr6osZZH8EDv4lJxguNxl3BYn+15OY1mCYEA0e/EqjM2mZKnHuti9AUizZgkWpRgnDnCMNeHd2wsI+vD4dANuBj/gRgZnaWU2qFZ22TTzBVWS5+3Gs0QKsaINNi0E7MzHEJT7Tp0zcE2eQrA28m1yBpLeBLcPZfKC3g7NDTznObeE/lu7brdTkzyIiS+nJ74sqz1B8=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYxMDQ3NTE3NjUzNSwKICAicHJvZmlsZUlkIiA6ICJkZjMxY2M2NTE4M2I0YmUzYmZmZmNjMDVhMDFkYjBmNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ3aWxsaWFtZ2J1cm5zIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJlNTRlZjQzOGYyMWQyMGQwZjZjNjllNjYyZjE3NTM4ZjU3YzFjMTJkZjRmZTgyMmMwMmU4ODlhMTYxNDIxOTgiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(125, 187, 106, -180, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return false;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		setDialogue(player, "idle");
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return Stream.of(
				DialogueSet.builder()
						.key("idle")
						.lines(
								"<f>Last week they promoted me from guarding the exit to guarding the entrance! Still the same spot but I gladly take that promotion!"
						)
						.build()
		).toArray(DialogueSet[]::new);
	}
}