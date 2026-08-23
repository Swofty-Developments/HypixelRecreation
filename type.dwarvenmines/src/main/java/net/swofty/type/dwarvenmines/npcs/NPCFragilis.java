package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.dwarvenmines.gui.fragilis.GUIHandyBlockGuide;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCFragilis extends HypixelNPC {

	public NPCFragilis() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<5>Fragilis", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "BcJCc3fjJe3eO7IxHJwWr+PiG1o71OUkqsxf9pdPiY/dOMb5eglmAfamNTVRJgp4ojRRmNdqArXdtT5/ZCiGI7LnmCa4LpPUVdHVU9xAcICE6R21NyG8VL8ARed8nKbUWNEhwBH3TwmoiMZYIA/SZitw8UXUJlvngq8qN5diunMzBJrxwy3KJ+irZCRaUMr4cGfqYBt0U0N0iq7+urCFcj2kEDX8oUIJkCUYXaqqe2Z83ivSMfm7hWthPJuWQEINnegJGEWhat74NwyRR+xr1NWbnfyxgImYBVgs7gL0IS1Fh4VzU/LGefzsYj9P3M7f2CYUiXKiKdtsd0ZQRDFFkJmY999qDcLRykxNLi0eatbEU88N5dqj0f10hwdf6e+h9JMlDQlsFTxbpk1Yb98VTBd+PzJUd6K3NlfXAhwGtPTdHlm/OZCuXqwMLrVVoW2m8V8nhaahc3R4ssBMVVBETXWB34LNC+8HKvCXYbK6sWNZfrtXkRegIXnZyj2i2res009GVJicJ6DCCSKzlMIk85piowdrlRhDYEnnwgUEFZUQg9uBueZ2UJfFDc7zPGfIhvTzpA8r3GbB0lY/qw4uPr3kIk+RF1jrg3mr8YY8/c9DNG3YNdewBTLSek8KSF2/i3wwX7J1d/TPw/VYE5rypBXX3bWro+t9e/++oVONSWg=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYxNTI0Mjg3Njg0NiwKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzMwNzIyYzAwMGFlZTBiYmEzMDU4YjQyNWZlNWVlYWM3NjZlMGEwYzNjM2E3ZWRmNDI5YWQxNGUxNTI5N2RkMzMiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(88, 199, -108, 0, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		new GUIHandyBlockGuide().open(event.player());
	}
}