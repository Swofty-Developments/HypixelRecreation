package net.swofty.type.goldmine.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.rusty.GUIRusty;
import net.swofty.type.skyblockgeneric.mission.missions.goldmine.lazyminer.MissionFindLazyMinerPickaxe;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class VillagerRusty extends HypixelNPC implements NPCAbiphoneTrait {
	public VillagerRusty() {
		super(new VillagerConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Rusty", "<e><l>CLICK"};
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-20.00, 78.00, -325.41, -50f, 0f);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}

			@Override
			public VillagerProfession profession() {
				return VillagerProfession.LIBRARIAN;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;

		boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RUSTY);
		boolean isAboveSkyBlockLevel6 = player.getSkyBlockExperience().getLevel().getLevel() >= 6;

		if (!hasSpokenBefore) {
			if (isAboveSkyBlockLevel6) {
				setDialogue(player, "first-interaction-over-sb-6").thenRun(() -> {
					player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RUSTY, true);
				});
			} else {
				setDialogue(player, "first-interaction-below-sb-6").thenRun(() -> {
					player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RUSTY, true);
				});
			}
			return;
		}

		boolean hasSpokenAboutPickaxe = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RUSTY_ABOUT_PICKAXE);
		boolean hasFoundPickaxe = player.getMissionData().hasCompleted(MissionFindLazyMinerPickaxe.class);
		if (!hasSpokenAboutPickaxe && hasFoundPickaxe) {
			setDialogue(player, "found-pickaxe").thenRun(() -> {
				player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RUSTY_ABOUT_PICKAXE, true);
			});
			return;
		}

		new GUIRusty().open(player);
	}

	@Override
	public DialogueSet[] dialogues(HypixelPlayer player) {
		return Stream.of(
				DialogueSet.builder()
						.key("found-pickaxe").lines(
								"You found the Lazy Miner's pickaxe!",
								"I'll have to put it in my stores.",
								"Click me again!"
						).build(),
				DialogueSet.builder()
						.key("first-interaction-over-sb-6").lines(
								"Hi, I’m the janitor of this mine.",
								"You would not believe how many people leave ingots and stones behind them!",
								"It drives me insane, but at least you unlocked <a>Auto-pickup<f>.",
								"It makes my job a lot easier, but despite that, I still find so many items on the ground.",
								"Maybe some of those items are yours? In which case I'll let you buy them back."
						).build(),
				DialogueSet.builder()
						.key("first-interaction-below-sb-6").lines(
								"Hi, I’m the janitor of this mine.",
								"You would not believe how many people leave ingots and stones behind them!",
								"It drives me insane, but at least you'll unlock <a>Auto-pickup <f>at <3>SkyBlock Level 6.",
								"It makes my job a lot easier, but despite that, I still find so many items on the ground.",
								"Maybe some of those items are yours? In which case I'll let you buy them back."
						).build()
		).toArray(DialogueSet[]::new);
	}

	@Override
	public String getAbiphoneKey() {
		return "rusty";
	}
}