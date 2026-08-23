package net.swofty.type.thepark.npcs;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.ClaimRewardView;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.darkthicket.*;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NPCRyan extends HypixelNPC {

	public static final Map<UUID, Boolean> hasInteractedMap = new HashMap<>();

	public NPCRyan() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<c>Ryan", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "cKGU6Ze2Y0I19H7/PrT/OdOjFADX9rg7dRgOMg95ugPfz4jSNmbIIan/i9C3+YaNN+QM3g2wAA/cFInXIK9CIisQ9C5ObDCT2h4ZhTueWSkygd7Vdm/34FyISNyu7maHRSJHg6HhcVaAVGZVyF9s9Tbmvgu3rX59kG1R+L65ArbPSDKlRLlyZYW0ut+F16WkjDjgT9qTiRvDGXWFBhFVaQ70Qr+68oWo3Q57+Ml2NGmY3AE9229uds17XuVD4O3ct09iwiXAtPUD9eHHdXW4oErtPCH/KoQ2mWG+JZwDf/bPvD2TZy0yKfVAJBxlmym4Ik5UOD88BpYATY+gPPm9k7xab3gVvVUY6YiRoev0NswOpYWwkowaY7sGlbDoFkNctTvUmKrO/o5RgT/N3M7tUX3PSaaupB/iqffV8b21d+V49YVQk1pOdNp6okIo/KNRSQImZZkGNzPEK6rJ6OQSgohHxcn/lDTW45ApKaD6gdkLsI/wtXeMR/9R3Nn3nJwMOvusit7m6zC2CRCOr4q4txin8sTOIgmZCCsrDhdAJOO4cYhYb/lREbjTe4FfYWMHq/WU0RFQzDk8eNVmYMEsVUQ7j7J977EfEy1vWJwbWTl++/IASPQiyrz9Cm1ys82H1NIPbguFKVcw61toiA0WdtwzMVqWYllj9O2TT9LXAck=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzE4ODIwNjYxNzUsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUxNzZjYjEzYTQ0MzYzMThhZTRjMmJjMzFkZjA5ZDU1NjE5MmRhZWJiNzBhM2IyODAyMGY3YWI5Y2ExNzA5YmIifX19";
			}

			@Override
            public String chatName(HypixelPlayer player) {
				return "<c>Ryan";
			}

			@Override
			public Pos position(HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				if (!player.getMissionData().hasCompleted(MissionSneakUpOnRyan.class)
						&& !hasInteractedMap.getOrDefault(player.getUuid(), false)) {
					long step = (System.currentTimeMillis() / 30000) % 4;
					int rotation = (int) (step * 90);
					return new Pos(-364.5, 102.5, -90.5, rotation, 0);
				}
				return new Pos(-364.5, 102.5, -90.5, -135, 0);
			}

			@Override
			public boolean looking(HypixelPlayer p) {
				SkyBlockPlayer player = (SkyBlockPlayer) p;
				return player.getMissionData().hasCompleted(MissionSneakUpOnRyan.class)
						|| hasInteractedMap.getOrDefault(player.getUuid(), false);
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		if (isInDialogue(player)) return;

		MissionData data = player.getMissionData();
		if (data.isCurrentlyActive(MissionSneakUpOnRyan.class)) {
			UUID playerUUID = player.getUuid();
			hasInteractedMap.put(playerUUID, true);
			updateForPlayer(player);
			setDialogue(player, "startled").thenRun(() -> {
				NPCOption.sendOption(player, "ryan", true, List.of(
						new NPCOption.Option(
								"accept",
								NamedTextColor.GREEN,
								false,
								"Sure, I guess?",
								p -> {
									setDialogue(p, "accept").thenRun(() -> {
										NPCOption.sendOption(p, "ryan", true, List.of(
												new NPCOption.Option(
														"accept_2",
														NamedTextColor.RED,
														false,
														"WHAT?",
														p2 -> {
															setDialogue(p2, "accept_2").thenRun(() -> {
																SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p2;
																sbPlayer.getMissionData().endMission(MissionSneakUpOnRyan.class);
															});
														})
										));
									});
								}
						)
				));
			});
			return;
		}

		if (data.isCurrentlyActive(MissionCompleteTrialOfFireOne.class)) {
			sendNPCMessage(player, "If you can last for <a>10 seconds <f>while standing in the campfire, I'll give you a reward!");
			return;
		}

		if (data.isCurrentlyActive(MissionTalkToRyan.class)) {
			setDialogue(player, "talk").thenRun(() -> {
				player.getMissionData().endMission(MissionTalkToRyan.class);
			});
			return;
		}

		if (data.isCurrentlyActive(MissionCollectDarkOakLogs.class)) {
			sendNPCMessage(player, "Grab me <a>256 Dark Oak Logs <f>and I'll make you a badge! You don't have enough yet!");
			return;
		}

		if (data.isCurrentlyActive(MissionGiveRyanDarkOakLogs.class)) {
			if (!player.removeItemFromPlayer(ItemType.DARK_OAK_LOG, 256)) {
				sendNPCMessage(player, "Grab me <a>256 Dark Oak Logs <f>and I'll make you a badge! You don't have enough yet!");
				return;
			}
			setDialogue(player, "give_logs").thenRun(() -> {
				player.openView(new ClaimRewardView(), new ClaimRewardView.State(ItemType.CAMPFIRE_TALISMAN_1, () -> {
					player.getMissionData().endMission(MissionGiveRyanDarkOakLogs.class);
				}));
			});
			return;
		}
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return List.of(
				DialogueSet.builder().key("startled").lines(
						"<c>AAAHHHHH! <f>Don't startle me like that bro!",
						"<f>Y'know, someone as stealthy as you could be a useful member of the <c>Campfire Cult<f>.",
						"<f>What do you say to that?"
				).build(),
				DialogueSet.builder().key("accept").lines(
						"Cool! Anyways, there's just one thing you have to do before you can join.",
						"Stand in the <6><l>CAMPFIRE</l><f>!"
				).build(),
				DialogueSet.builder().key("accept_2").lines(
						"Yeah don't worry, it's not as hot as it looks.",
						"If you can last for <a>10 seconds <f>while standing in the campfire, I'll give you a reward!"
				).build(),
				DialogueSet.builder().key("talk").lines(
						"Wow, you did it!",
						"Nice job, hotshot! I'd give you the Campfire Initiate Badge I right now, but I'm out of wood to make one with...",
						"Sorry about that...",
						"I need to keep watch and make sure nobody blows out thee <6>Campfire<f>, but if you can grab me <a>256 Dark Oak Logs<f>, I'll make one for ya!"
				).build(),
				DialogueSet.builder().key("give_logs").line("Thanks for the wood! Here's your badge, as promised!")
						.line("A Campfire Initiate Badge I, for our latest initiate, {}<f>!", player.getColouredName())
						.line("I can upgrade that badge for you as you survive more <c>Campfire Trials<f>, you know?")
						.line("<f>Simply stand in the <6>campfire <f>whenever you think you're ready!")
						.line("<f>But be careful, though, as each trial burns a little hotter than the last!").build()
		).toArray(DialogueSet[]::new);
	}
}
