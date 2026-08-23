package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.banker.GUIBanker;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCBankerBroadjaw extends HypixelNPC {

	public NPCBankerBroadjaw() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Banker Broadjaw", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "PaUxiTsjUMkZYK0pG/asB1a6/FALKLHWL5JbS1j1cGTW+6iTRD6JES6mQlOsJtf1/KJ0CX+ONfmvMjRN+x27s/ufQGDI4BLtem/UFnxu82OBzhVT4FDEUn792yi+YkVyb1gDFQW3UkkgMahSLBoe+I4JtFl+4OMUTBQZF8eYsWlr8ViwAs0a3gr5JbnLrtPxbhUwmgtcBrw2Hk+WipSgcWoZyhvG+17SHZ2zqIzu8tOi7k0lp92VZs1GFRs50ZpMV7/IHB3pEwp1Peh8DlMDtpIDdOOxmiNtJBZVGIyXXwoQ/wmg2ma4TUaanwBkJh+Sdd1yT0aRwdMGEzl0wAzHhEyr/L17SKSdUaK+29un+4pTV2nqTO3dLRSCnRVOmJtMnrfc+K0aoivJhCc3MRIaSB5Ib1bP86l57wNVomTeca+ywl+M4oF62Gq1QQnDmZHERB5i0TzP49DVKJy7sU/8mnfmah5+mfpIM7C5p+IB6ALWJ7nn/5QV4jqP+sUDw0+0XN/1KeXwnGGyhHn2B7ysYkqA8AUpNi/ygYhEeqUlJCDzKAP7GrkE9CktbQdH8HXb0KpS9hUmweKmL7VR9aJiqcK5LTCVctZfPA6H9fXTagIk7tPKth41GBnSjDJazj61MlUazNpYJIBQnDuErllKecr0Gft2E/jxrezWlaw+fmg=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwOTI0NTM4MDg1MCwKICAicHJvZmlsZUlkIiA6ICI3MmNiMDYyMWU1MTA0MDdjOWRlMDA1OTRmNjAxNTIyZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNb3M5OTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTU5ZjhjNGRmZDFjNTgzYThhNTRmYTVjYzdmOGViNmMyNGQyNjlhZjI3Yzk5ZmQ5ZjJiMmJiZjQwZTcwZmVkMiIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(13.5, 201, -148.5, 44, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		new GUIBanker().open(event.player());
	}
}