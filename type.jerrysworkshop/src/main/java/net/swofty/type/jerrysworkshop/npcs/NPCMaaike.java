package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCMaaike extends HypixelNPC {

	public NPCMaaike() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"Maaike"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "U6xuqs9BTNXhwwoRSgDJcCkqcvgKu6w144PpGpx/4OQqq5WQZ3F8ytjofpE6YBqf1FWYQpIgFLaT+VX7N7rQD2O8JUnC2FJVko3glVWx0KDLudvbe4mo/0a7Air2rTIpNEMbcjRc3GO4eCQlf4pHudh6Cd88tFA2WQa9XRcSiU3pN8+nspS2tdFJByJFtcOKYyzNQQTM6eMSxrQeqcIlC7DZ4PKdLmYstHNWMKIcVLYdu3RErspp8VHOoIX6XdMV3UGVkoTjuuNjk3/356JbkebxYjZcgxtZTvUjNLAU8o86scBiHQ0crsDHo3kCkBfq4FSo4kD3Lh74D26MeC6I5gDL+rXNt/mH14UUbvfBE88x5Sd0pFLjAqQTM1DQBOCb36IWcqwRPWvmtsAj+jq05Foj3ztUf4dpozmgZU+kRwFGY1vcBUnud2VUkFaDeolXe1IAHhCfiBDEEbGB6wy4Ww8FMJ4fshaZWoH2vNUBEIlufZydW8c8O03Z02Bb60JpArBqCjF1/QKjTPJBQV3OyqE3o/0qdcXukj6aBHE2MJ3iY6Aqzbl5Xq5uwbd6vRDQ3dTG91N0/V+kM6lL+lqAGynjQEKATakIOYvMIscxPyZj3oozlgU4kPI1xqQ9xDXVOP2BoFnV2ZVRyL1Gs1H20BQ19KiWwUV/268Ot783B+E=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzY4NjIzMzY4MjAsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2ZlYTg2NDdhNjFkYTI1MDkxZTcxNzdlYWQ4ODI5OWQ3ZDdiOTRiYjQzOTI0MDQwMWIyZTdhMmRlODNkNjkzODUifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(49.500, 81.000, 50.500, 24, 0);
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
