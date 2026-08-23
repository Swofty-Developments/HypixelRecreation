package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collections;
import java.util.List;

public class AbiphoneWalter extends AbiphoneNPC {

	public AbiphoneWalter() {
		super("walter", "Walter", "Sells Superboom TNT.");
	}

	@Override
	public void onCall(HypixelPlayer player) {

	}

	@Override
	public void onAdd(SkyBlockPlayer player, int slot) {
		if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_GIVEN_WALTER_CUBE)) {
			if (dialogue().isInDialogue(player)) return;
			dialogue().setDialogue(player, "abiphone").thenRun(() -> {
				NPCOption.sendOption(player, "walter", false, Collections.singletonList(new NPCOption.Option(
						"pay", // actual id from Hypixel
						NamedTextColor.GREEN,
						true,
						"DONATE CUBE",
						(p) -> {
							// TODO: check requirements
							dialogue().setDialogue(player, "donate_cube_no_requirements");
							// player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_GIVEN_WALTER_CUBE, true);
							// super.onAdd(player, slot);
						}
				)));
			});
			return;
		}
		super.onAdd(player, slot);
	}

	@Override
	public DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder()
						.key("abiphone").lines( // when clicking with an Abiphone
								"My abiphone is for Platinum-level donors of the Walter cause only.",
								"You know these superbooms don't craft themselves right?",
								"You just need <3>Sulphur Collection 7 <f>and to then donate an Enchanted Sulphur Cube!" // then show "donate cube" option
						).build(),
				DialogueSet.builder()
						.key("donate_cube").lines( // when donating the cube with requirements met
								"Welcome to the Platinum club, high roller!",
								"Call me anytime!",
								"And before you ask... yes, I do try to commercialize all of my friendships."
						).build(),
				DialogueSet.builder()
						.key("donate_cube_no_requirements").lines( // when donating the cube without requirements met
								"Mmh... you're missing something to become a Platinum-level donor..."
						).build()
		};
	}

	@Override
	public ItemStack.Builder getIcon() {
		return ItemStacks.head("43ac21b653a27632dbc8373c6e6fba5b8c97b7ecbfef2d793630d149b116ba81", Text.empty(), List.of());
	}
}
