package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.dwarvenmines.gui.GUICommisions;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class EmissaryBraum extends HypixelNPC {

	public EmissaryBraum() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Emissary Braum", "<e><l>CLICK"};
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYyNDU0NjIwMjAwMiwKICAicHJvZmlsZUlkIiA6ICJmNDY0NTcxNDNkMTU0ZmEwOTkxNjBlNGJmNzI3ZGNiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWxhcGFnbzA1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNiNmJiMDUyZmQ3NDM0NDliNTY0NTRlNDRjMTkxYTYwZTFkOWFmZTJmZmNlMGM3MTRlMWE1YjdiOGJjMzNmZTkiCiAgICB9CiAgfQp9";
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "Dq8FhgjZhwXIOlyBfL3uKysAbeI9j7Q5jNFFFPpDbQ5xoq7USXP6PI/E18gTZG/1G7VWaRZj2677c8R8ftNS9gq2iBSfgyCKEhjYoqRRAQiRFr1O4FsTuCEvoeOdti2p5PY/5nxs0fVYJpeBYm6UBJ5WJEci/pcuzhugYLPL5N1YNoeBaUCZzMj1FcTaTY42uSiHrrJ2BuvuvhyS48GFGcYOC87NKHv13jbQmiiS/pLoObmSvK4rkl8PqnpqM0BwOxLsdM0IKinC/Qq0SNS2mL+iOy99E7X4vLtVR4aHVycdbn3M1S8m0eWSkJA9stYSD0gWNcXQh6PwfguLovwMdy7otQWRHpubo5JNeBWoc8k12i4ry/uk0c8yTn34cmCLzu6igstnFM/Y7vYV2KYudpAWg/vTFNksLuIPo0tcxjOxygattSBej70HE9TVNFQJlfZtuwUbYA58f5KIwbJU7+hzcBJh1XGOQbYL/4I2/kIDR9WseYs98Bj47oKXIQbzA2baeHJcL+pmNsg1lU3SSfMXMh5Un856kjTetes40M0yI5YmChPukQv4OSESAzuAkFi3MjvJ/GjJS1bXoH2+X6YWRA7pxGnC1+UD26SnAGxjyyknAuo+H0we4K5jVfegCwMrySp4B/LzQtnQX2fJqeEKPiywP1U8kjEwzyX963w=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(89, 198, -92, 90, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		HypixelPlayer player = event.player();
		if (isInDialogue(player)) return;
		if (player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_EMISSARY_BRAUM)) {
			setDialogue(player, "first-interaction").thenRun(() -> {
				player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_EMISSARY_BRAUM, true);
			});
			return;
		}
		new GUICommisions().open(player);
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("first-interaction").lines(
						"I am the King's favorite Emissary, which is why I'm the furthest away from him!",
						"Commissions inside the Crystal Hollows are different from the ones in the Dwarven Mines.",
						"Click me again to receive your first set of Crystal Hollows commissions.",
						"Once you complete them, come back to me!"
				).build()
		};
	}
}
