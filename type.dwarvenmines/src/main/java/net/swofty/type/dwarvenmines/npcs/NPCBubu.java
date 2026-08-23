package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCBubu extends HypixelNPC {

	public NPCBubu() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<5>Bubu", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "Tu4M+oWlNBsaoOukmq/Zx0zbq/xnCxOFFGOsVnk6Rw38djwURcKWUp5xz4N3IBGfLGCaeZdUu3FwEwNn4qeQ58lEO+MWc0pOhphP4CI1qsMK5fx+I42PKlJSGX12gUwHTJWJtIgKtVgcXMSNpP24p3HrJqpk/K4oDIP2MO3pQ+UwMn0b0QkRcpGK1JqyPMiIM2gdDnn/dtfZswrNQ1KkvZgeGWt8mcvt/iMK8/67br6bFJV8ql7k1H5Gk+Qx5s2RQ2U2cp2u0x4CDxqr41xZqqXLIrjpzUkah8EvalSBW6yNnLnAQXgALwKsiG+X9MX1MY1xXbBbTbkKDPR8qUy4DS+FujHvt3FL4W0PP/5uhzaKxazDEqXOk0m6xhvzLLhcz/86lou6LWba67yhECJvLcSiTMLTQa87IG1PGozOg8czPX36F73WxOu/FClsLw5bLTsUA3aBAtCUG8MVWOMuqG6sCmibPSVfr/YJKIib4veZrHGa6zCEj7gl75yF1A3lj0wYWSr4NsuYR7JxzMgv7HmujeYMgrx7CimcTQWCu1ReGqTMQziF03tRCc2V2h5/1uhFOiAr0Q0FMEmF/vXPrxq6Bt4TD7XhVUsDNvYZ8piUwKL19YKtcS79hIOCzu3+SsDFk9dKXIEVO0aqBQGfVvpsAayiIVPpdCF1/JQGWUw=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwODMxMzM3MzE3MywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjVmMzFjMWMyMTVhNTdlOTM3ZmQ3NWFiMzU3ODJmODVlYzI0MmExYjFmOTUwYTI2YTQyYmI1ZTBhYTVjYmVkYSIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-10.5, 201, -103.5, 45, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		player.notImplemented();
	}
}