package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCStJerry extends HypixelNPC {

	public NPCStJerry() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"<c>St. Jerry",
						"<e><l>CLICK"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "tPSYnpLBZNa+zP9AoAajEEmrtaram5+Y62FzmXKeocnEB9lrY1fvlak39zISUu1ismU4SmVj0JBTkamS7RrLYA3KxUp5QiKTSZKnOygvrw6PF8bZqMTHM32/q2CXfdFBYLuEbDCTOtN96x0j5qEKW2P5m27d+KuveriNX9M413h+Lsx8kY6mNzv6slDNLNn6rb2uXABb6eRy2Dzt/qjdEmRZ4ij7pv7IkFgJ65o9kB0f8WuOl7x77u5ejiuOso1IzHTPGcNAHutgaEj00LFDEjDSt0U/mSZQXLhSyDnivfVVrG+YZEzAUWn/gtBnCqD3XDGZ7z+9vrb+vJeWNwPSBmYPtcapVY6IFVRGxKCy+ONOI85HxQLO/h4C/4EjTxuGa+nrSgnrSw0Wapd+XdOy7ZlhHjmjWHwmEwzCkF9QABSY6y7NrBNbd9lfiwKMHvtVw6INCpNOnS7V8RepjoPX5gDJF8f6248c/yj3S+1IOf7vBvHoIGBftQ6M6s9y1ogbMzajBvgzckne09wsTFuUo/suHrrNl36A7U6pQkA9dwWOvdNg01nWAGNXsV8RIjC4gPvTYyTMKnH/wl1F/DcRkhWo3RC2jYTIz9ysxC998hkAKNbUQXama7oqb0W2NLv15aRn8IgHgDXXMsc9Q3Gpr9onw3Aoqc8PlCHugctDZDg=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzYyNzA0NTU2ODUsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE2OTJlNjJiNDExMTRjNWIxYTVlOWUzNmMwNjc5ODI3OGViNmE1MTI2ZGMyNmRkYjE2NmQyMDIxZmUwZmEwMGUifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-22.500, 76.000, 92.500, -60, 0);
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
