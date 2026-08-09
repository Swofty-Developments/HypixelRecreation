package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.dwarvenmines.gui.GUIGuide;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCRhys extends HypixelNPC {

	public NPCRhys() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"§aGuide", "§fRhys", "§e§lCLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "cEhw4KPnjOLHodZV5sBF0cDMUcmzFklYNbv8zR3G1+IwdkEfLxnXeSOeZRu3OrGOvxn6WwZWl/P4cZbfAxYEoVXvTYFesNM8lI0VNwuqt0QGz5sjkKMlQlEbdQilnw2Ki77SAqFqd8XHknYuUku/XpMEFCvDaS96SwTkQRwitE8EhJb5/X9AS6m/Wslu/F/AuKcZKc5UIXFr6geGV0XhJUpW8rBnWPxf4so5XKdQLXPvM8aActwAecXV4MkIPTsP1N6nOywEOeV5qs56AgoNgx44u+38Xg+0RV0wr9hzYHENPos7ZSfdVwd/tqAMTbyrKj4AGppl7MnSynEdgiBBk22qiF3g8vYgGj4O9w3h5185U9Ow7f2DihIy3mDAiFSvc77Hx/BGYkIxZ4HXWrKRj3INTxVSalB1RG7tuCHs4J/xAaDu2yI5zSedAX+L6VXeudIzpUPym5JaRc3alL1TW94ZQZ4W7QKw5fViLVORr4lve2fdDaz9KRUVkGeQx9L7Q81LaCV84E/8j0juzyHMtVdm+fmDERr+TSoa7REk6KIg0bpTqvyWiqJyygL8/sw02SuhqAupFdf3QOTLbLrh4qU8LtMvykl9V7n36uSTRZi0EVJasODGEVF2Qyt+toFSRdIvc6YUY+C2UJf3jI1C7E9hAuz42ayleKdGoezAsds=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwODMxNDY5NDY2OSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmIyMGIyM2MxYWEyYmUwMjcwZjAxNmI0YzkwZDZlZTZiODMzMGExN2NmZWY4Nzg2OWQ2YWQ2MGIyZmZiZjNiNSIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-38, 200, -119, 280, -3);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		new GUIGuide().open(event.player());
	}
}
