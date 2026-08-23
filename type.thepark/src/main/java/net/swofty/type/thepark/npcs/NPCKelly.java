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
import net.swofty.type.skyblockgeneric.mission.missions.thepark.spruce.MissionCollectSpruceLogs;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.spruce.MissionFindKelly;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.spruce.MissionGiveKellySpruceLogs;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCKelly extends HypixelNPC {

	public NPCKelly() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Kelly", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "Pi7jmNymIAKdg4Z80WSEuhjifAL8q7GSXY131I/bb6Aec5NSUknOcSbCva6iXGn1Ke5AMDLBlyUsP45rgxwObRqjN04JxNvbp37dwXc9PeerOtbN+8pN4NXkMg0xFx1qcttxrSwdSmVgQ0W0wDqDvX5VxAhVNiC8zzv6Dj/8YjdkF6DKr7A+kH69CpgNrlPXChbyMXuHnx0AVFPfcMazb0K8nzRvuQy5RT5mBvgPee3nKvc8OvVHL6RQjXpAKnDv4Oo+yDE1ipAvl98eUmIczM2yeSqXX+JHGOJKEUAlbjED7SZSDbx2njzwTDYeiBRPnWki/wWGvsnYcLhiMrTQPb6ZMk7VNAqAmug3VfTsY+1tutJ6+C6oYnIvfoG1WHNbUHOLbfd2ijqwE/bihIE7PGL3W1PDuAqW8lyvACXHNCKy0Wp2iopixiDK2mXmMxaXIq7m4u6r/5GfP0AAd75BAx+mSk8pPb0rHlKqJuDVoxM4fBL2SBDU+mx0zDVLJE/pJsN97Bphs0XRs8W/Jf67notKp/iviiiJQA4owNe0G/ckpiFEFW4Qiob7AyOrY3ECjTgivZpM6hw0NOhYZ1Mh780Szgd7WrwxVAUrnPjKkkZZns/IiuRrzYLu49g22NhADNedMpaEW35V13tvvsDdEGkMehbA4UxszBIS9mMj7fA=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTcyOTAyNDE4ODkyMywKICAicHJvZmlsZUlkIiA6ICJjNmViMzdjNmE4YjM0MDI3OGJjN2FmZGE3ZjMxOWJmMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbFJleUNhbGFiYXphbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82ZWIzZjllMmFjMGRkNWY5NzRlMGFlNzEwZWU3NGYxZDhlOTZlODY2YTUzMGIwZjRiNDE3N2I5ZTYzNWYzNGQ3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-350.5, 94, 33.5, -180, 0);
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
		if (data.isCurrentlyActive(MissionFindKelly.class)) {
			setDialogue(player, "first-interaction").thenRun(() -> {
				NPCOption.sendOption(player, "kelly", true, List.of(
						new NPCOption.Option(
								"yes",
								NamedTextColor.GREEN,
								false,
								"Yeah",
								(p) -> {
									setDialogue(p, "option-yes").thenRun(() -> {
										data.endMission(MissionFindKelly.class);
									});
								}
						),
						new NPCOption.Option(
								"no",
								NamedTextColor.RED,
								false,
								"Never heard of him",
								(p) -> {
									setDialogue(p, "option-no").thenRun(() -> {
										data.endMission(MissionFindKelly.class);
									});
								}
						)
				));
			});
			return;
		}
		if (data.isCurrentlyActive(MissionCollectSpruceLogs.class)) {
			setDialogue(player, "during-collect");
			return;
		}
		if (data.isCurrentlyActive(MissionGiveKellySpruceLogs.class)) {
			if (!player.removeItemFromPlayer(ItemType.SPRUCE_LOG, 128)) {
				setDialogue(player, "during-collect");
				return;
			}
			setDialogue(player, "after-collecting").thenRun(() -> {
				player.openView(new ClaimRewardView(), new ClaimRewardView.State(ItemType.KELLY_TSHIRT, () -> {
					data.endMission(MissionGiveKellySpruceLogs.class);
				}));
			});
			return;
		}
		setDialogue(player, "idle-" + (1 + (int) (Math.random() * 3)));
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return List.of(
				DialogueSet.builder().key("first-interaction").lines(
						"Hey hey! Did <e>Charlie <f>send you?"
				).build(),
				DialogueSet.builder().key("option-yes").lines(
						"Awesome!",
						"Okay, so basically.",
						"I was breaking this tree right here, right.",
						"But then I broke my axe!",
						"And now I don't want to break any more trees, in case I break any more axes!",
						"Anyways, if you can bring me <a>128 Spruce Logs<f>, I'll give you a reward!"
				).build(),
				DialogueSet.builder().key("option-no").lines(
						"Strange... I could've sworn you'd met.",
						"Anyways, I was breaking this tree right here, right.",
						"But then I broke my axe!",
						"And now I don't want to break any more trees, in case I break any more axes!",
						"If you can bring me <a>128 Spruce Logs<f>, I'll give you a reward!"
				).build(),
				DialogueSet.builder().key("during-collect").lines(
						"Bring me <a>128 Spruce Logs<f>!",
						"That's two stacks of them, if you're struggling to do the math!"
				).build(),
				DialogueSet.builder().key("after-collecting").lines(
						"Oh, that's amazing! Thank you so much!",
						"Now I can take this all back to <e>Charlie<f>, and say <l>I did it!",
						"And then, with the coins he gives me, I can get a <3>Spruce Minion<f>!",
						// "Thank you, (player), please take this reward!", // seems to be missing from the server, even though docs say it exists.
						"You should check out the <a>Dark Thicket <f>next. Be careful though!",
						"I've heard some people are holding a <c>Cult Meeting <f>there <c><l>RIGHT NOW!"
				).build(),
				DialogueSet.builder().key("idle-1").lines(
						"I talk a lot, so I've learned to just tune myself out"
				).build(),
				DialogueSet.builder().key("idle-2").lines(
						"I can't control what I say to people. I spend the whole day talking."
				).build(),
				DialogueSet.builder().key("idle-3").lines(
						"Did you check out the <c>Cult over in the <a>Dark Thicket <f>yet."
				).build()
		).toArray(DialogueSet[]::new);
	}
}
