package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCFrosty extends HypixelNPC {

	public NPCFrosty() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"Frosty"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "pSocoChCCNb8dv3syEVm3kolZaVkf6qR70ngHQdDRw6/rA2tVIZbvBWS6n99Nx8MxfMpRi9mcSTGuKGuOq7lHradijwTrHIn8pcMD2k3JwzXyVQ+ipxqAxJChJTffASJFFL7v/o9SzY89ullKfVl5xIgJHmw00Z77qDDn9ZnWLaazwlfpMR443kOVge/3DlRp62ZFbLBvvyc5locPIUm6Fbl9kbKMBOeEJLFzw+1jvBlkiBbsSemzL7i4ro3Jvd0LUxtPoZ+GONTfdAYE3Go/6zSrBwtVloGYbbtdvyH8rijD9nbLUVWbZV3YeCO+0oNlrxpCVksAEAAHUpDhoakz/vJe+2QEtMsckv6ahjemDnZ3Mo2oewjvL0ENNqClBnp6g8pgu+8P4Cv3qJozs8+Ftk8Rwum9z51bSNxApvz5mnR9dyHbysRYeVrWqULHcWrBt1eeL7AyxNR1JwExF8/3hXpqXdABwgLf3vGCtjafVjcfeAlf3sjQ2voDnQQeULQhoOqVlSqz8s8tlNUJYmIGm9MhS5kBwGUvE6s/82if7dYLL9zGA0WqmmPs2PuOby4a3BBWG4dDgmd/jqkNLFhLDm28o1+SCIYscLwEdJ341BoJIXD20xJBtmRLTsC2HhIXXvmCNxSH9rQyPhmThbf2Hd3GB3zMY0c3my4sN9HJKI=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzQ4NzQ2OTcyMzMsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2UxNmY5NmM3NTZkZWZlZTYzOTA4OWI3MDI5MDE0MzEwNzI0ZGYwZWYzMGM3NTEwZjJkNzJhYzllMGRhY2Y3ZDMifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-1.500, 76.000, 92.500, 124, 0);
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
