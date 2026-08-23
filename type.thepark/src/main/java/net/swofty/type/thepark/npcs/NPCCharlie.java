package net.swofty.type.thepark.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.birchpark.MissionCollectBirchLogs;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.birchpark.MissionGiveCharlieBirchLogs;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.birchpark.MissionTalkToCharlie;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.birchpark.MissionTalkToCharlieAgain;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCCharlie extends HypixelNPC {

	public NPCCharlie() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Charlie", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "K19XwXZoYOPTzVSDFP/MvfjqWQxjTEvpYba9cawDA1A3cZ5AbMDRhmuEnYTsje2Fht/sQa9KyO/woYgfOQ4lfIuTDYJYPtR4Nzr4KRH+FCgSY8bEWe/f4IhswiV1N8NV6+bd5tD/eqEl5TGgz84NtKNb/lRQN7UG6orAZ8KlndnzbgaM6jcA1rS0PUhavigLF42z0qu9E03jSVepIkqGAnUoo6tMKgYctV6d/WQv9U3yM3yXQX6wvCeP/M3vFbTJLkiGgXrlIfjSVBntDfbNXSGkb/0H/vR+9HuvQnmlJ2f68fLkWVZJ3D3Qaok8+j7xX5DcSVcTCut2Rm1Qa4Uvs7JlEKeEDCRs+ymy+qD7U/7x6yPbF+KFu/8NcXew/jLXPOpLjMywoNsvo4BT8t4qIhQDioJBC0YjY+NYLrk34loNBQ6q+EUW3QuX0HyixlI+A5Ug/vcrSlykhTql1+OyXiLTvIpoT3ezS/b0aT8mktilwBxmA46KVNIBaTIRiF5iZe4WMf+0kyndXM+WZ+AIADy/QCQZZVV0gr4gA/6tYRXWESeRJM5loqZgRcPRtQAGV8wUUE9qBfGPgmRxt9E+jxlJJ5Pfr17fzH2pmMZsVa7wfyJhW7C5AHmCze6qgCgs3l8XNDY4DJ3v1tRvurhpAWqC8Ejjd12eVZlytssS71E=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NjAxOTQxOTU3NzMsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU3NjZlYjA5N2JiNDg2YjNlYTcwN2FjMTMxM2ExYmZmZjg4NDZhMmY1NTQ2MDA4MmNlZjIxMmYxODkxMzBmMWMifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-277.5, 80, -17.5, -90, 0);
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
		if (isInDialogue(player)) return;

		MissionData data = player.getMissionData();
		if (data.isCurrentlyActive(MissionTalkToCharlie.class)) {
			setDialogue(player, "initial-hello").thenRun(() -> {
				data.endMission(MissionTalkToCharlie.class);
			});
			return;
		}

		if (data.isCurrentlyActive(MissionCollectBirchLogs.class)) {
			sendNPCMessage(player, "I don't see <a>64 Birch Logs<f>. Where are they?");
			return;
		}

		if (data.isCurrentlyActive(MissionGiveCharlieBirchLogs.class)) {
			sendNPCMessage(player, "Cheers! Here, take these <a>trousers <f>for payment!");
			data.endMission(MissionGiveCharlieBirchLogs.class);
			return;
		}

		if(data.isCurrentlyActive(MissionTalkToCharlieAgain.class)) {
			setDialogue(player, "talk-again").thenRun(() -> {
				data.endMission(MissionTalkToCharlieAgain.class);
			});
			return;
		}

		setDialogue(player, "idle-" + (1 + (int)(Math.random() * 3)));
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return List.of(
				DialogueSet.builder()
						.key("initial-hello").lines(
								"Howdy! Yer a friend of <a>Lumber Jack<f>, are ya?",
								"Well, works for me!",
								"I need <a>64 Birch Logs<f>. Could you grab 'em for me?",
								"Do that, and then we can talk!"
						).build(),
				DialogueSet.builder()
						.key("talk-again").lines(
								"<a>The Park <f>is home to many different kinds of wood. Each kind has its own <e>collection<f>.",
								"Me and my mates each gather a specific type of wood, and then split it all evenly so we can all increase our different collections!",
								"I wanted <e>Kelly <f>to get some <a>Spruce Logs <f>for us today, but I've not seen her in a while... Can you maybe look for her in the <a>Spruce Woods<f>?"
						).build(),
				DialogueSet.builder()
						.key("idle-1").lines(
								"So much wood to chop, such little time..."
						).build(),
				DialogueSet.builder()
						.key("idle-2").lines(
								"So you heard about that weird <c>Cult <f>over in the <a>Dark Thicket<f>? I'm considerin' joining them."
						).build(),
				DialogueSet.builder()
						.key("idle-3").lines(
								"What? <a>Lumber Jack <f>said I have a <5>Treecapitator<f>? In my dreams..."
						).build()
		).toArray(DialogueSet[]::new);
	}
}
