package net.swofty.type.thepark.npcs;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.ClaimRewardView;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.jungle.*;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.thepark.TypeTheParkLoader;

import java.util.List;

public class NPCMolbert extends HypixelNPC {

	public NPCMolbert() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Molbert", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "W0e+/EVqkTyw3gQZgRQbswV8EA1K/YE557v6VpVu/a0B7XzcMMtS8aUyzsljpBiXM3pjPo36D4Wjq45AB/Ij8GaCV2M491lTk0OLzZnRYvGOZ0HvT8K0TdWkOZQtFfh45IX97yvrnfSx1P5XYvJxXSBCKFn6lG9CpYT4Oy5RQG2ARiBkwq3NNGbgJFEfgMQl5KyszgyWCvyXsEQJo9UVYvlGLgrMML+/bORdDSzlnm3R8/tQtCbCfKfRNKlNiv03c02vDGrWDdCB6JYXd+58V5uRwP4NrzSer6GTd59sHLNx1lWMvvag/5m+1A+XFaBTs6wK9m6PB5AVEm9ROhIHjKRpuDjUBHnKnc/docuV+JKlqu3z1BQ/nUccznZmOvKJ8eewtawtfd4/HalVsIPrE5f4nUoH7C1t50xO20fZ3Z7B7hurdSwJEuP1vV1AxhM21Y8ux42yQOqQ5l0pYeIPJTIs99MUaYJYqdWJll1Mp3AHuNelDbTVBvqzt0wtFvN5I5ULoeJrbV7t1PmCUJIP6pcyg9wGPRBW5ZauR2sp8TRNhrZOCRTiaWST+XbVnw7HkprqaoHqutxyascDQej38AknySBbMzcCZYS8CEwQYfMTbW7E/6SO5USapx0K29t+nh1ZOvfx2mI8hFW/qH5HFP2FhyZ1dSISfpWDCq7LWUg=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTU5MDQ1OTI1MTYwOCwKICAicHJvZmlsZUlkIiA6ICI3M2ZkNzU2NWJkZTY0MGQzYWE4MGUxMWUwMTMwMjc3OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJHYUJySWVMVnR6IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2YyMDc4M2UyNzQzYjg5NzZiMDBmODI3ZWUyODM2NzVkNzkxMTE0MGM5ODI4NDIzOWY2YmMxYzhkZDFkMjdjODEiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				MissionData data = player.getMissionData();
				if (!data.hasCompleted(MissionLeaveTheAreaAgain.class)) {
					return new Pos(-465.500, 120.000, -42.500, 10, 0);
				}
				if (data.isCurrentlyActive(MissionTalkToMolbertAgainAgainAgain.class) || data.isCurrentlyActive(MissionHelpMolbert.class)) {
					return new Pos(-448.500, 119.281, -64.125, 180, 0);
				}
				return new Pos(-447.5, 120, -63.5, 45, 0);
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
		if (data.isCurrentlyActive(MissionTalkToMolbert.class)) {
			String key = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SAID_MOLBERT_IS_MOLE)
					? "quick-intro" : "intro";
			setDialogue(player, key).thenRun(() -> {
				NPCOption.sendOption(player, "molbert", true, List.of(
						new NPCOption.Option(
								"yes",
								NamedTextColor.GREEN,
								false,
								"Sure...",
								(p) -> {
									setDialogue(player, "option-sure").thenRun(() -> {
										data.endMission(MissionTalkToMolbert.class);
									});
								}
						),
						new NPCOption.Option(
								"no",
								NamedTextColor.RED,
								false,
								"You look like a mole yourself",
								(p) -> {
									setDialogue(player, "option-mole").thenRun(() -> {
										player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SAID_MOLBERT_IS_MOLE, true);
									});
								}
						)
				));
			});
			return;
		}

		if (data.isCurrentlyActive(MissionCollectJungleLogs.class)) {
			sendNPCMessage(player, "If you can bring me <a>512 Jungle Logs<f>, we can make some <5>traps<f>!");
			return;
		}

		if (data.isCurrentlyActive(MissionGiveMolbertJungleLogs.class)) {
			if (!player.removeItemFromPlayer(ItemType.JUNGLE_LOG, 512)) {
				sendNPCMessage(player, "You don't have <a>512 Jungle Logs<f> on you.");
				return;
			}

			setDialogue(player, "after-resources").thenRun(() -> {
				data.endMission(MissionGiveMolbertJungleLogs.class);
			});
			return;
		}

		if (data.isCurrentlyActive(MissionLeaveTheArea.class)) {
			sendNPCMessage(player, "Come back later.");
			return;
		}

		if (data.isCurrentlyActive(MissionTalkToMolbertAgain.class)) {
			setDialogue(player, "after-coming-back").thenRun(() -> {
				data.endMission(MissionTalkToMolbertAgain.class);
			});
			TypeTheParkLoader.entities.forEach(Entity::updateViewableRule);
			return;
		}

		if (data.isCurrentlyActive(MissionPlaceTraps.class)) {
			setDialogue(player, "after-coming-back");
			TypeTheParkLoader.entities.forEach(Entity::updateViewableRule);
			return;
		}

		if (data.isCurrentlyActive(MissionTalkToMolbertAgainAgain.class)) {
			setDialogue(player, "after-placing-traps").thenRun(() -> {
				data.endMission(MissionTalkToMolbertAgainAgain.class);
			});
			return;
		}
		if (data.isCurrentlyActive(MissionLeaveTheAreaAgain.class)) {
			sendNPCMessage(player, "This might take some time, so you should come back later.");
			return;
		}

		if (data.isCurrentlyActive(MissionTalkToMolbertAgainAgainAgain.class)) {
			TypeTheParkLoader.entities.forEach(Entity::updateViewableRule);
			setDialogue(player, "stuck").thenRun(() -> {
				data.endMission(MissionTalkToMolbertAgainAgainAgain.class);
				NPCOption.sendOption(player, "molbert", false, List.of(
						new NPCOption.Option(
								"help",
								NamedTextColor.GREEN,
								true,
								"HELP MOLBERT",
								(p) -> {
									data.endMission(MissionHelpMolbert.class);
									TypeTheParkLoader.entities.forEach(Entity::updateViewableRule);
								}
						)
				));
			});
			return;
		}
		if (data.isCurrentlyActive(MissionTalkToMolbertFourth.class)) {
			setDialogue(player, "explain").thenRun(() -> {
				NPCOption.sendOption(player, "molbert", true, List.of(
						new NPCOption.Option(
								"iknow",
								NamedTextColor.GREEN,
								false,
								"I know that you are a mole",
								(p) -> {
									setDialogue(player, "option-iknow").thenRun(() -> {
										player.openView(new ClaimRewardView(), new ClaimRewardView.State(ItemType.MOLE_HAT, () -> {
											data.endMission(MissionTalkToMolbertFourth.class);
										}));
									});
								}
						)
				));
			});
			return;
		}
		setDialogue(player, "idle-" + (int) (1 + Math.random() * 2));
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return List.of(
				DialogueSet.builder().key("intro").lines(
						"<6>Moles<f>, you see. They have been burrowing everywhere, their tunnels are disrupting the landscape, they ruined the <5><o>beauty </o><f>of this place. It's gotten hard to ignore.",
						"You need an experienced person to handle the situation.",
						"Those troublesome creatures are ruining everything and must be dealt with. Do you understand?",
						"This park deserves better and I want see it flourish like it used to be.",
						"Let us keep this between us, and I will <a>generously reward <f>you for your efforts.",
						"The moles must be dealt with, and I am sure you will do so quickly and quietly."
				).build(),
				DialogueSet.builder().key("option-mole").lines(
						"<c><l>Ridiculous! </l><f>How could I be a mole if I am <b>wearing human clothes<f>, huh?",
						"Have you seen moles wearing human clothes before?",
						"I don't think so. Now get back to the issue!"
				).build(),
				DialogueSet.builder().key("quick-intro").lines(
						"The moles must be dealt with, and I am sure you will do so quickly and quietly."
				).build(),
				DialogueSet.builder().key("option-sure").lines(
						"Great. First thing we need is to build some <5>traps<f>.",
						"For that however, I need <a>512 Jungle Logs<f>, should be enough to get things going.",
						"Moles <6>loooove carrots<f>, they are just too good to resist.",
						"Don't worry, I've got a stash ready.",
						"Just thinking about them <o>makes me</o><f>... Uhm I mean <o>makes them </o><f>come out of their hiding space."
				).build(),
				DialogueSet.builder().key("after-resources").lines(
						"Fantastic, that's all I needed to build the <5>traps<f>.",
						"It will take some time to assemble them, so you should <a>come back later<f>."
				).build(),
				DialogueSet.builder().key("after-coming-back").lines(
						"The <5>traps <f>are ready for use; All that remains is to set them up in the <a>right place<f>. Once you find the ideal spots, go ahead and deploy them."
				).build(),
				DialogueSet.builder().key("after-placing-traps").lines(
						"Good job, partner. Now we only need to <6>wait <f>for the <a>right moment <f>for these pests to show up.",
						"This might take some time, so you should come back later."
				).build(),
				DialogueSet.builder().key("stuck").lines(
						"Hey.. <o>uhm </o><f>partner! I <l>swear </l><f>it's not what you think!",
						"Could you lend me a hand here, I can <a>explain everything<f>!"
				).build(),
				DialogueSet.builder().key("explain").lines(
						"<a>Thank you so much<f>, I was stuck there for <o>at least ??? minutes</o><f>, I was almost gone for good.",
						"<o>I.. I </o><f>must have <d>slipped by accident <f>and then fell right into the <5>trap<f>! ... <o>Yes</o><f>, <l>that's what happened</l><f>!",
						"...and then I ate the <6>carrot <f>because I was almost starving in there!"
				).build(),
				DialogueSet.builder().key("option-iknow").lines(
						"<f>Ok, fine you caught me. But <c>please don't tell the others<f>, they wouldn't want to be my friends anymore if they knew the truth.",
						"Here, take this <a>compensation <f>for all the trouble I made you go through.",
						"I hope you forgive me after this and we can still be <6>friends<f>."
				).build(),
				DialogueSet.builder().key("idle-1").lines(
						"I wish I could be in love just like <9>Romero <f>& <d>Juliette<f>."
				).build(),
				DialogueSet.builder().key("idle-2").lines(
						"I hope you forgive me after this and we can still be <6>friends<f>."
				).build()
		).toArray(DialogueSet[]::new);
	}
}
