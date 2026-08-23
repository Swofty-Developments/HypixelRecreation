package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.shop.GUIShopAlda;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCAlda extends HypixelNPC implements NPCAbiphoneTrait {

	public NPCAlda() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Alda", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "Hnuo54LzyUBtfOeOoRCBnA4+od399IK7QccXSkbc7V3TSi/ayJV9QeaMlIhbjzYHA1qQDPxpeYQ03PwpZMM6DT/yk8Xu0fz4orHFcSMdTRxlP6c6fugA2Behus7AKL+IkMmZe7Gjm4mxLJLXqWCCe/t24LNfe6hna9r9pzuvHl7kBcS44z+9Ue5LlkuK7nDLGYOnwic8og0GZNSfv8/k1Gir3U5DEdixrANq9AKXS0LSxwLfTpbXrW3UplMmYKs939GFeGA1t960FyjrEG4xX5oFRBxjZaLhaxKsRAIUxR1sP7nsejAqrrojxlZYs+5WIn1NFamvd1fo38CiiOo+IkETsoilO84PF/Ka1qRcsaOjjQnXQ9IAUfP9W9egv4yDwjaXuW0A8FPqPCH/u6j6o9CuUzhN7+uQsOMqyBOr6/qt0cNIkQs0Y9WhAC34hXCMfYctANhsz7gHhcIEV/5bDYPPFHnew8z3PUqAyLJCNTb8bgEQrCavCjBrnoKsQl02ZC2bSeDA5g8ASRQyGlNZOJFpd6rMcANdmKY6ZwTkCiaJirqpftJVK5qDJY0A8N0GHGieIHQohHQ5ffCMU5E1Jxkct5wLX0zPJOuzngQD2YZKLL8DtUKekrwKbyD2gCxuNN9NaFE+thcHdEpYl7DJzgWh0IoCXUGMB44cDypteeo=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTc1NTcwNTI3ODc3MywKICAicHJvZmlsZUlkIiA6ICJhZTg3MzEyNjBmMzY0ZWE2YjU3YTRkYjI5Mjk1YTA1OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJGdW50aW1lX0ZveHlfMTkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGI1NjQ3ZjkzZmQ4ZTFkYTljZGIxNTFkZDliZGY0ZjQ4YmI1OWExZDExNzQ4ZjE5MThjMTM2Yzg2ODA0YjIiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(71, 80, -59, 90, 0);
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
		player.openView(new GUIShopAlda());
	}

	@Override
	public String getAbiphoneKey() {
		return "alda";
	}
}
