package net.swofty.type.thepark.npcs;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEventKeys;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class NPCMelancholicViking extends HypixelNPC {

	public NPCMelancholicViking() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<b>Melancholic Viking", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "LNUoJj3H66O7noZiY2VAq/XAuD03DCaLMtu7K8ieIvHszL3iWoD8GSb3wmx0p5JHyC1IaTuuJncHi1ncHACzG64s5QlMPdtQxY7Cwy1DgE8l7v0eVRWPrLOgp1hEeakvU4pxsZEym62T56Z0TVHv+L4DD5B96JEaxvQBK9vFqSea5+N9Pe/4DoZb3jxjHpVicKkPi496C+IG9Av/0F33Oz74PdlyHnNiOnRwCrQQIrpC//2ZNlHq3RNLOqQS9h+VFAGmoUISMb5TO2xXHLnI4Db1IcD1GvsuZl7hOuKY6OA3QwlYyXxf/T34MVWeCEPGjS/MUsgS28XcWVpSPc4hCz9JUgAEqhR3nYxSSUlG63bYJCZLObP0xWSVjNEgPKp02qIRiJdC/0s1vC4beaohfhlePPTbEC9M6117p5fXL/xCvhIcv6FpXo4Vp53mKjXSooI/R+zro66jAMddhcNnj7C26q2jdjm69QjdN5/dpfRXSJsX0MOxlpasTYN/nr9uIx01JYnN8TQyrjqn9TWZr/UckfOp8MIHmUaOb71I33nG6U9+JHNTGDKcgN1CAv2Yzbai5WjdJRSSMWaGSZt8vJT+Q4sUdseS4sVNATaw+MqkcDX1YXwYs13FpoO0enbJHe/yZ04oSgmRcTUGjPy2QJ7cvTyzOTElYhXNAALBnns=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzIzMTkwMDQ5OTQsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NkNTEwNDNkNmExOGZjMDE0ZjI2NGIxZTdkN2ZjMTgzM2FmYThjZjZjYTI5ZjJiOGY2OTFmZGY1NGRhZmVmZGEifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-335.5, 92.5, 73.5, -180, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}

			@Override
            public @NonNull String chatName(HypixelPlayer player) {
				return "<b>Viking";
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		if (isInDialogue(player)) return;

		ItemStack heldItemStack = player.getItemInMainHand();
		Material heldItem = heldItemStack.material();
		if (heldItem.name().endsWith("_boat")) {
			setDialogue(player, "holding-boat");
			return;
		}
		if (heldItem == Material.ICE || heldItem == Material.PACKED_ICE) {
			setDialogue(player, "holding-ice");
			return;
		}
		if (heldItem == Material.COD || heldItem == Material.SALMON) {
			setDialogue(player, "holding-raw-fish");
			return;
		}
		if (heldItem == Material.FISHING_ROD) {
			setDialogue(player, "holding-fishing-rod");
			return;
		}
		// TODO: finish this monstrosity
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		LocalDate pastDate = LocalDate.of(2009, 9, 1);
		LocalDate now = LocalDate.now();
		Period period = Period.between(pastDate, now);
		int years = period.getYears();
		int months = period.getMonths();

		return List.of(
				DialogueSet.builder().key("intro").line("I last saw the sea <b>{} years, {} months <f>ago.", years, months)
						.line("I wish I could remember what it felt like!")
						.line("Sadly, my memory is now my worst enemy.")
						.line("Please, help me remember the <b>sea<f>.").sound(Sound.sound().type(SoundEventKeys.ENTITY_VILLAGER_HURT.key()).pitch(0.5f).volume(0.9f).build()).build(),
				DialogueSet.builder().key("holding-boat").lines(
						"Wow!",
						"A boat!",
						"Throw them at people you hate"
				).build(),
				DialogueSet.builder().key("starting-to-splash").lines(
						"<a>YES! <f>This totally reminds me of the sea!",
						"Although... there were <a>more <f>fishes back then."
				).build(),
				DialogueSet.builder().key("enough-splashes").lines(
						"<a><l>WOW! </l><f>This <b>feels <f>just like on my Drakkar!",
						"I suddenly feel so great!",
						"Thanks for bringing <e>joy <e>joy <f>to an old viking!",
						"Take a look at my wares!"
				).build(),
				DialogueSet.builder().key("splashing-no-requirements").lines(
						"<e>Wow! Nice move! <f>There just isn't the ambience to fully appreciate it."
				).build(),
				DialogueSet.builder().key("holding-ice").lines(
						"Don't you have some liquid water?"
				).build(),
				DialogueSet.builder().key("holding-raw-fish").lines(
						"I prefer when the fishes are lively and go splish-splash in the water!"
				).build(),
				DialogueSet.builder().key("holding-fishing-rod").lines(
						"It's a nice thought, but I don't feel like fishing right now."
				).build(),
				DialogueSet.builder().key("holding-magical-water-bucket").lines(
						"There's as much water here as an ocean.",
						"If only you could pour it somewhere!"
				).build()
		).toArray(DialogueSet[]::new);
	}
}
