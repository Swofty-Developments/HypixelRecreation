package net.swofty.type.skyblockgeneric.abiphone;

import lombok.Builder;
import lombok.Getter;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.components.AbiphoneComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;

@Getter
public abstract class AbiphoneNPC {

	private final String id;
	private final Text name;
	private final Text description;
	private final AbiphoneDialogueController dialogueController = new AbiphoneDialogueController(this);

	protected AbiphoneNPC(String id, String name, String description) {
		this(id, Text.of(name), Text.of(description));
	}

	protected AbiphoneNPC(String id, Text name, Text description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public abstract void onCall(HypixelPlayer player);

	public void onAdd(SkyBlockPlayer player, int slot) {
		player.updateItemInSlot(slot, (i) -> {
			if (!i.hasComponent(AbiphoneComponent.class)) return;
			i.getAttributeHandler().addAbiphoneNPC(this);
			player.sendMessage("<b>✆ {} <f>has been added to your Abiphone's contacts!", getName());
		});
	}

	protected AbiphoneDialogueController dialogue() {
		return dialogueController;
	}

	public DialogueSet[] dialogues(HypixelPlayer player) {
		return DialogueSet.EMPTY;
	}

	public abstract ItemStack.Builder getIcon();

	public void sendNPCMessage(HypixelPlayer player, String message) {
		sendNPCMessage(player, Text.of(message));
	}

	public void sendNPCMessage(HypixelPlayer player, Text message) {
		player.sendMessage("<e>[NPC] {}<f>: <b>✆ <f>{}", getName(), message);
	}

	@Builder
	public record DialogueSet(String key, Text[] lines, boolean abiPhone) {
		public static final DialogueSet[] EMPTY = new DialogueSet[0];

		public static class DialogueSetBuilder {
			public DialogueSetBuilder lines(String... markup) {
				if (markup == null) {
					this.lines = null;
					return this;
				}

				Text[] texts = new Text[markup.length];
				for (int index = 0; index < markup.length; index++) {
					texts[index] = Text.read(markup[index]);
				}
				this.lines = texts;
				return this;
			}

			public DialogueSetBuilder line(String markup, Object... arguments) {
				Text[] existing = this.lines == null ? new Text[0] : this.lines;
				Text[] appended = Arrays.copyOf(existing, existing.length + 1);
				appended[existing.length] = Text.of(markup, arguments);
				this.lines = appended;
				return this;
			}
		}
	}

}
