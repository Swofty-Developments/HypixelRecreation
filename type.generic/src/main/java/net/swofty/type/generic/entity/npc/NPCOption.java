package net.swofty.type.generic.entity.npc;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.ScheduleUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NPCOption {
	public static final Map<HypixelPlayer, OptionData> options = new HashMap<>();
	private static final Component selectAnOption = Component.text("Select an option:").color(NamedTextColor.YELLOW);

	public static void sendOption(
			HypixelPlayer player,
			String id,
			boolean prefix,
			List<Option> message
	) {
		sendOption(player, id, prefix ? selectAnOption : Component.empty(), message);
	}

	public static void sendOption(
			HypixelPlayer player,
			String id,
			String prompt,
			List<Option> message
	) {
		sendOption(player, id, Component.text(prompt, NamedTextColor.YELLOW), message);
	}

	private static void sendOption(
			HypixelPlayer player,
			String id,
			Component prompt,
			List<Option> message
	) {
		Component optionMessage = prompt.equals(Component.empty())
				? Component.empty()
				: prompt.appendSpace();

		for (int i = 0; i < message.size(); i++) {
			Option option = message.get(i);

			Component optionComponent = Component.text("[" + option.name() + "]")
					.color(option.color())
					.decoration(TextDecoration.BOLD, option.bold())
					.hoverEvent(
							HoverEvent.showText(Component.text("Click to select", NamedTextColor.YELLOW))
					)
					.clickEvent(
							ClickEvent.runCommand("/selectnpcoption " + id + " " + option.key())
					);

			optionMessage = optionMessage.append(optionComponent);

			if (i != message.size() - 1) {
				optionMessage = optionMessage.appendSpace();
			}
		}

		player.sendMessage(optionMessage);
		options.put(player, new OptionData(id, message));

		ScheduleUtility.delay(
				() -> options.remove(player),
				20 * 60 * 5 // 5 minutes
		);
	}

	public record OptionData(String npcId, List<Option> options) {
	}

	@Builder
	public record Option(
			String key,
			NamedTextColor color,
			boolean bold,
			String name,
			Consumer<HypixelPlayer> action
	){}
}
