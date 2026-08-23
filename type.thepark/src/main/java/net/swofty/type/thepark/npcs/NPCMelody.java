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
import net.swofty.type.skyblockgeneric.mission.missions.thepark.savanna.MissionCheckOnMelody;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.savanna.MissionCollectAcaciaLogs;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.savanna.MissionGiveMelodyAcaciaLogs;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCMelody extends HypixelNPC {

	public NPCMelody() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Melody <d>♫", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "l3+nGUvRiH//fkK8SwTwQFjXyqt+kNd5n4MCXSmtxoQTeUGZpa4dR5wwxpyYue5TacFcSVEXZaAq4Y2Uu8KMb9JYlVrL4W8O7Sl//jik9hd3DyOHhHd9oLyt1x1GvU1hiUmcXX74ma6YERznSPXepDMiEX0lsx3+JTHbpQoIqpuuqcl0wgRT9hrKPYGE32s3DCV7MIpepD2z/hmnXCCSRBjjPfL8qflCB3eAFUBw767vfbt1J8f290Hvb5CgL4AgZehplAXmqQM4j8Rd/8DAv+nCny5OrC2f2Xj3dxsYXcJzxUcBvPmPBrYrT8/uF7jBia4rW7bU6kGzy5IzR6pqOJmQnU3Frrwi/3dJnCJ3iFVllPOiWqCYC4AVOy0+0cAVlZMSWtLIg+ZCXNc3Ah5/eoBxIBOtH2pA1iYO2P3FqW4g2jn0JH+4ck1GvuFywj6XgHvkmnGo5pZsudnGdHt0a4RECWA+lrbPfoFmQND0Q4B2pqLcCXufF3k0Q8471XNJVzMPgnWpTJ5p3wS16xZZ0ETm9kEnETs6506hcig/JyVgZsPxMVctsu5LfNp8jhXVhlWRfuC+X6MNURmqVBGv14Erc29952dhf1LwYna5+Tq8LdIWMZ7KHgpg2L8P503HvsY34O/TOdGZ855ZEgrj5FTgBdJYY+oJXCMmtALPeJA=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzI0OTg3MzU2MjMsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzRkMjM4Y2NjODE4YWIwYzBmZGViZDA3ZDRhNTZjMzUzZmY1ZWI1NDFjODVkMzg2MzYyMTYxNzgxZDFkZDc0Y2MiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQ==";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-411.5, 109, 71.5, 169, 0);
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
			setDialogue(player, "intro").thenRun(() -> {
				NPCOption.sendOption(player, "melody", true, List.of(
						new NPCOption.Option(
								"okay",
								NamedTextColor.GREEN,
								false,
								"Are you okay?",
								(p) -> {
									setDialogue(player, "option").thenRun(() -> {
										data.endMission(MissionCheckOnMelody.class);
									});
								}
						)
				));
			});
			return;
		}
		if (data.isCurrentlyActive(MissionCollectAcaciaLogs.class)) {
			sendNPCMessage(player, "If you can bring me <a>512 Acacia Logs<f>, I can craft another <d>Harp<f>!");
			return;
		}
		if (data.isCurrentlyActive(MissionGiveMelodyAcaciaLogs.class)) {
			if (!player.removeItemFromPlayer(ItemType.ACACIA_LOG, 256)) {
				sendNPCMessage(player, "It seems like you don't have enough <a>Acacia Logs<f> with you. Please come back when you have collected enough!");
				return;
			}
			player.openView(new ClaimRewardView(), new ClaimRewardView.State(ItemType.MELODY_SHOES, () -> {
				player.getMissionData().endMission(MissionGiveMelodyAcaciaLogs.class);
			}));
		}
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return List.of(
				DialogueSet.builder().key("intro").lines(
						"Hello! <d>♫"
				).build(),
				DialogueSet.builder().key("option").lines(
						"Yes, I'm fine, though my beloved <d>Harp <f>was broken to pieces by the storm.",
						"If you would be so willing, could you bring me the materials so that I may make another?",
						"My brother was on his way to help, but you got here first.",
						"If you'd be so kind as to bring me <a>512 Acacia Logs<f>, I'll be able to do the rest"
				).build(),
				DialogueSet.builder().key("thank_you").lines(
						"Thank you so much! <d>❤",
						"I already have some string, so I can just use this wood to fashion the frame of the harp.",
						"Now, if I'm doing this right, this goes here and...",
						"It worked! This harp looks and sounds even more beautiful than the last! <d>♪",
						"Thank you for your help - please take this as a reward.",
						"Talk to me again if you ever want to givee my <d>Harp <f>a try!"
				).build()
		).toArray(DialogueSet[]::new);
	}

}
