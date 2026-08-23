package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCBulvar extends HypixelNPC {

	public NPCBulvar() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<5>Bulvar", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "L96mT0EgH5wKootwvD+UZjsosZYKCl0gYnCjiBqmOLWDFUfVE3izl+Va1A53sgNWn/HWyzd/yaqLnZnBQJvsRIUqUjAfySmQvbOyxN9Lp4BZJN8PFmcg4gYUjpu8Yo/Ykfp0a9X2TyMVKzdK1EpupuFsmuIUSzBuF/gwO+FjZV1WaeuW6wU/QJZD/pYuJw7ZaUlLA5JTVjD/D5ytLC5cQJST4zWez+JGkftf+OHHNidpoWEdi6SqD1CJE1dhDxV6bmwsunLV/z9ZU51k14Ccq9Pe+YsFHXMZANrIv7sCgS3BU9GHVV3t4m/roxZwOwjccDXosC14A6jEnUG8ab2ub5r1Nnfn/duTRxrkR9+e6gxa3tlf960G7jeEE/5TO1I16cklhLugfOus8n1CJDgtCUccDfu/0yOWSX6jtdsE6U7MnZkW716LIJdrDpaIzewTI2z/kuOicXiK/Gtb5yRYwryNEmTuYTKVrU3EuPsV0I3E+fWl3Dj9Z6Vtrt0vvebucy2Tpk9Xc1ATCzMyhokcGtRDaXIBTTAQDdvSKMLlJLMc3awzCRFwxN0er/lA5dCi7qafNfHoHPytIWzrrkvoiPREGXGidqRlY+QiFenUMUDgSc/6cVsUGRppHrBje70o50Frzev2U8caMkBrNaeA8kXwSxpoNeqjO4ztvBWMfK0=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwODMxMzQ0NjA5MiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjEzY2YwY2E3OWEzNjExYjhlMDVmZTllMjY0ZmIyYmY4ZDI3ZTQ2NGRjMTJkYzZlOTVkZDBhZTBjMzM1YTU2MSIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-15.5, 201, -98.5, -135, 0);
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