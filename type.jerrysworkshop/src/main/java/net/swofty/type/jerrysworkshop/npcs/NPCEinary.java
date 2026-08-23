package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCEinary extends HypixelNPC {

	public NPCEinary() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"<6>Einary",
						"<e><l>CLICK"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "m68cXsTmpViTHFbRFhvaLw3SEGXzinrDZXz0GtssEB5ps7qmDr0Foo9X0d7ub+H1KYjkE3GvBbzjSaDGokiGnixlubEbWKhwMftbNzdKqFp32Ff7sqzpvgex/wI0eU9G2zl1ovkJjL/oQIHAWSCxoNb0uFILu/bxzEc1YEnuh4xrxlAsWD59D59ycIGvIX0rVyV/gR+TCPzn6HiTD42JfDk63IfwDjgcc2hWRt4ffM6dwW0R94yHsukN7s0IolEGnSCb8dlej/3rFh4Hrtz8jrFRW70ypIcZfKv7hLUnIpMndUWA8a+Jev96s3kH3xWd7dS9s03SHPxtDmuiq1Y74MG0gMrq6/NPjh36hDlrKabxlKR2KBWv6Od+GJq/7b/9oGLMGfrRO/Z+ozJ2hf4hQwK9rGVR5efQyD3q/3m5QYUGkLnwUiGqHDMsGoVQlCqtLATNwbUmCLX9/ykGY7WTMAEjokcFq6nWKZ9+dyHfYIJmWaniKMecpOCyFBaLGVtQBXZwRs1V9FmNKRiCZXTohc99562UH7142WOVyyf6xZvxJl7sGFbEOGM1IiprEAsQzXWickfALd2ZjvN3XYfGYKvcNgHvFk5cguNv0pE7nmLXYZO3k0oErJiDtE4+mHFvTFVjLsMqNiKhd07jV/HpRKXl4+SbnXh+aB0mDK5g0pM=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwNzY1ODkxMDIyMiwKICAicHJvZmlsZUlkIiA6ICI0ZmFkNjk2NTIxNGI0NGQ4YjAxYzlhZTVjZDQ0MDVjOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJoaXBfYXNpYW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGEwMmY0ZmQyZDY4YTc4YjhkYTNhNDZiM2QwMDRhZDk0NzI0NTNjODA4MjhiNzM1NGEyNTkwYTE3OTQ4YzE5OCIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-16.500, 76.000, 63.500, -90, 0);
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
