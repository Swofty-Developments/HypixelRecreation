package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCGary extends HypixelNPC {

	public NPCGary() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"Gary",
						"<e><l>CLICK"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "A1FKKDfud3ailHVEb43/j2PxnsuVrSqPWPkfrTYCRnAHkhj/4lH93tX0Bgs+Hikgj8RrCblWs0PqnhiUTLjibfZiOT3d5wMJfqwjXAqdB+6GyP9GXeOprbkoU4rJ5bQ6zGzX7fY0XB87nrxdRrtfAQmFpdfOCF/3Ne1AvL4PKaH3U2tns/JaaaWyOZf8nGBWTRIha2BYXXoeei5+VJN616VNt8M6U17yUMn31BBZTfS43DwEFj/jPHaT6b94GWFVZGqS7bMriIIlkFct3/TK0moGxy6YMSGdvqnBEHyjTtSK14QwELa49L9lpO+D2ZxzSB6vwk6ZX/2WssgaLtz+mW5rRNzz2toNSToEQfgNxdV2yN/LXzYEApKQDYfZU76ruv+yCoV7OBlcUEFgHDzi/Xi+SbyvUdyp8VDroDcjbFchHk120LXVCtWe0BoEm43p3jZBp/wuLOcgHgF6u1wpgC78jnBjKYfvbTE38iq3NLriiT40RXDMAAj13vvZiMYDCRDtu3zHCByNRcDOsgDyeVrhdQ/io2MERXwpprA1pDbNwAOMaeM/ytHUiQxxXsgslgoQVrbrLjDXXHA8qjPhdR8LFKvLRbO8YCrlEa+vRXh844r9vdIMNqoyaTSUbNy60eN4FUGkSf4JbSVbVLhI3BLCf3B5F34MJIVGLP4nmYA=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzU1MDExMjE1ODEsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2IwZTJlZjhiNDdkNjNkYmEzNzU1ODgxMTgxOGQxY2I2MjkzYWQwZTMyODRjMDIwNTFjY2ZjNGQ5MTE4MzliYmYifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(53.500, 103.000, 56.500, -80, 0);
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
