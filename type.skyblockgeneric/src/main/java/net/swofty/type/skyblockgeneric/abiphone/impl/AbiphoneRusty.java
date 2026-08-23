package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.gui.inventories.rusty.GUIRusty;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class AbiphoneRusty extends AbiphoneNPC {

	public AbiphoneRusty() {
		super("rusty", "Rusty", "SkyBlock's Janitor");
	}

	@Override
	public void onAdd(SkyBlockPlayer player, int slot) {
		if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RUSTY_ABOUT_ABIPHONE)) {
			if (dialogue().isInDialogue(player)) return;
			dialogue().setDialogue(player, "abiphone").thenRun(() -> {
				player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RUSTY_ABOUT_ABIPHONE, true);
				super.onAdd(player, slot);
			});
			return;
		}
		super.onAdd(player, slot);
	}

	@Override
	public void onCall(HypixelPlayer player) {
		new GUIRusty().open(player);
	}

	@Override
	public ItemStack.Builder getIcon() {
		return ItemStack.builder(Material.VILLAGER_SPAWN_EGG);
	}

	@Override
	public DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder()
						.key("abiphone").lines(
								"<f>Did I find an Abiphone?",
								"<f>Yes, sometimes I do find one lying around.",
								"<f>What?",
								"<f>You?",
								"<f>You want my contact?",
								"<f>Me?",
								"<f>The janitor?",
								"<f>I...",
						"<f>I don't... don't know what to say...",
								"<f>Yes of course you can have it!"
						).build()
		};
	}
}
