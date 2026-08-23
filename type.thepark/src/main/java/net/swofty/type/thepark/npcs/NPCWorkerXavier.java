package net.swofty.type.thepark.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.savanna.MissionCheckOnMelody;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCWorkerXavier extends HypixelNPC {

	public NPCWorkerXavier() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Worker Xavier", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "N9cojjCDNgktzBbFaxCNlNdB1/6cX+ECMUSROjXklRlawF4q0JlIZ/upmJTco3BaWUfHw7HQpOzE5wmLR3sVxzTbN+SffTjmwwbHAfiyEOSwsL4reJmCDHduZd2hwQib1Cmpi/nxfocU59zZ3JCpZH4yrNmTEe56nhIZnBGn+K+1rtzxIWPpBaGzdHQKZEfMRG/InSov40mgLqwJIqFAsOQnHocfkjuE4Pf/6dwxsfYALsQ2mWE1Ab1ZiVX6Tnc1BTSNTRY0756FwNegXgJfZPBO9zPfx33vTJ3+vbXfegVxeUdx7wKbCS24NRSVleiObX0a3fkSA/cyKtf4iLHzKxn+AQvD0lXYZPvUmUx9CiAmUhwfdF8R+O7ZgwPP401cF24cb4BtmKFzqQ4+0ZILiUcFTggcinjAMMraDfk/TJhHzjEXYUxG47D99+aRaAxI9rDFKU4tKDdxQSkGVDYAqiQvK56N6UIhMj11MN6jkIdHD3wDvGy+CowvKc0qFQVdfdq9EblPzhj2MrcmdiLlrSN/nhcO2iYTXjEPaD7XbhGYlF0DOPLhDFVhLhXkJMe01nSqzijziwRdrOWrGy4uwJH4m3NS0k9u/3ywN+C6zvUDYZXBmQYUiX9gG8V06GXi7POIRb3guP32xnJ2zLdcrCJ1X3uEgRfnIa9uS+7mmRE=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTU5NzMwNzUyNzg1MCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDIwOTEzZDg0YmJmMTRjN2M2YzcwMjA0ZmY2ODQ5ODAxNzhmNTk4NTlkNWRiZTY4MDRlNWM5MGRlNjRjNzY0MiIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-424.5, 110, -15.5, -60, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		if (isInDialogue(player)) return;

		MissionData data = player.getMissionData();
		if (data.isCurrentlyActive(MissionCheckOnMelody.class)) {
			sendNPCMessage(player, "Go and make sure <d>Melody <f>is okay! She's on a small plateau nearby!");
			return;
		}

		setDialogue(player, "idle");
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return List.of(
				DialogueSet.builder().key("intro").lines(
						"<c>WOAH WOAH! <f>Look out there!",
						"There's been a <3>huge storm<f>! It knocked down a bunch of trees - it's been a safety hazard!",
						"Oh my goodness! I forgot about <d>Melody<f>! She's still over on <d>Melody's Plateau<f>",
						"Can you go and make sure she's okay?"
				).build(),
				DialogueSet.builder().key("idle").lines(
						"Acacia trees are native to the <a>Savanna Woodland<f>.",
						"Don't feel bad about knocking them down - they regrow real fast!"
				).build()
		).toArray(DialogueSet[]::new);
	}
}
