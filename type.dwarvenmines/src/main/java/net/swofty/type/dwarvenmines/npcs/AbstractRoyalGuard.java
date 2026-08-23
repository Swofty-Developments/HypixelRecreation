package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;
import java.util.stream.Stream;

public class AbstractRoyalGuard extends HypixelNPC {

	public AbstractRoyalGuard(Function<HypixelPlayer, String> signature, Function<HypixelPlayer, String> texture, Function<HypixelPlayer, Pos> position) {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Royal Guard", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return signature.apply(player);
			}

			@Override
			public String texture(HypixelPlayer player) {
				return texture.apply(player);
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return position.apply(player);
			}

			@Override
			public @NonNull String chatName(HypixelPlayer player) {
				return "<6>Royal Guard";
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
		setDialogue(player, String.valueOf((int) (Math.random() * 5 + 1)));
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return Stream.of(
				DialogueSet.builder()
						.key("1")
						.lines(
								"<f>There are no miners like the dwarves."
						)
						.build(),
				DialogueSet.builder()
						.key("2")
						.lines(
								"<f>STAND BACK!"
						)
						.build(),
				DialogueSet.builder()
						.key("3")
						.lines(
								"<f>Great treasures lie ahead, but at what cost?"
						)
						.build(),
				DialogueSet.builder()
						.key("4")
						.lines(
								"<f>Behold a fellow holder of Heart of the Mountain!"
						)
						.build(),
				DialogueSet.builder()
						.key("5")
						.lines(
								"<f>Watch yourself, traveller. There are creatures about."
						)
						.build()
		).toArray(DialogueSet[]::new);
	}
}