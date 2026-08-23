package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.dwarvenmines.gui.GUICommisions;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCKingName extends HypixelNPC {

	public NPCKingName() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6><l>King Name", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "eQJvD9YaY1wSp3+8UppiGXJfsYzTuyKaIi2kEMN3p+okeYZPgO93YlOFMQK77rtO/IHipI+nboWTOEhHfYqXDIbkdX3br3Rt9oiaDXVeENRvBux/6LkIf2UImM09blWd1bDiZXFbIG44MrTjVrU1OuSOvK5+YDg1dg9tjN3J1NqAs4BEZJzo+hyxF6PlOSE/qNHkCUxISo+INL9JCRs6n07o/EBaZfJCTC1lSe39c4cUVUstZsQUf92x15ZUr1sk/gOnYhtjkunqKjAOeY07DHy8oZrO7yt2Mjz5Cpzo+cBFx7U+BEYpIbaHdR5avY2LFuLrt7hokrV3ftbqI24+TWK5doSoLcZQJ8J+9mn1nF8mdXrE6mF6otd7S7q9Px7EPQp2ZMA0IZDrLvkPbT0QHS3/igS/dPyk9TfdAGzTjMCQUlsOShSN5aYYTFQTlvFZ6F+8LNHaXev515MiA1ZbQMeP1jyNUHIQ8/nrPCd4G28Vw2UH53doFpGBHIRJSXWSQbTkq3m8IKQGqqZ2Du3VMmiv7+kEczS9jHwRNxWl9n2uiQwXobgjj4icsO7Gy4LI6lvq1iCqpc/VEXwBag08jK4InHAe6XbS7aLkv0CZ9/x/Q4WCmten5PSj4TGz0CSGhJx04GCTbk8ZymTyFfpPbVi+W9HDh7T7NPfXHMq9swA=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwODMxNTA1OTM1OSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQxZjQxNjFhZjZkNGZkZmJhYzE3YWRjZjlkMGI2ZjFmZmI1MTdlN2I5MzZhYWQ5YzM3Yjc0N2NlNjRlYzRjNiIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(129.5, 196.000, 196.5, 180, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		new GUICommisions().open(event.player());
	}
}
