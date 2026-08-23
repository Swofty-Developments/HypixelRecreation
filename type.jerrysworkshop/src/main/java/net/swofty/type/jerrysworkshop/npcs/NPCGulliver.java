package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCGulliver extends HypixelNPC {

	public NPCGulliver() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"Gulliver",
						"<e><l>CLICK"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "TUUGLCH9HGzfw3oC4LGEYYhCoPNa5YWAiW8XUjy5wIch9MVcMdwHYxnnphazo0OOnFutHjucI2/sv6Fq8w3VCPDYqo9yIw7RNizJK8Q9dG6H1NUOu87G9YiWygF3EutSrARXjPBuFieENVxPbkpcPvzfPGrCJUkUWSGyJVpO3pK/zzA2zy9QUg7l03lm1o0pd3baVhHZSS3OOrN9i6MfO0b6SwhsI/s4gCFoDddGYR7lMYfxUNiSI5NbhvLk3lQYYdv6YMolZx4XonJ+quB8Oq0SOcZv3Wt3dBpfMBXyDWqUjSME8KZ5D9H4u0Y1dRo9bHYjqAvLD7M+BljUqzlR9xlD8Q6TpaelqXIAUsG9CQk1Y+/sjv4SXetvWlJKL1Gqd/VL6EKHV5YUL5XZ8WeUVuY10+bPjXuWH81DvhZEGMe7ADntgbktG8CLwp1kDmXO0kyy6TgCbzkvROvhDFqRP0etYegNE/IdBDeph/2dZqkyL6dvQucoTEd+qXq5wFy/kvDC01oCB08/HIkqsm+xjFaImFvEW711s/6JD/qiBO1VECQ8xQnQ8UoH120bibH0XPS0AXqVamr/dwcUR0BdQKP1Txaq7rKtJIqPXkjRHJCv5s1QvPxGuM44629FpT6xCCoiqPwbMDZXQI7OWuq+RpZi56zxAvWKuPW//4D/dcE=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwMjcxNTY0MTU4MywKICAicHJvZmlsZUlkIiA6ICJhYmQyYTNjNDMwYmM0NWZhODY3NmViNzYyZTMwOTI0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJYSVRzYWx2YSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84Nzc5MWFjZDQ4NjhiNWYyYjJjMTc1NmFmOTNiNTNkZTEyMGJkZjczMmIwNTZiYWQ2MDI3MjY1ZGJiMTEzNWY1IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(68.500, 105.000, 33.500, 91, 6);
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
